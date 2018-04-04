package eu.europeana.indexing;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.mongo.server.impl.EdmMongoServerImpl;
import eu.europeana.indexing.exception.IndexerConfigurationException;

/**
 * <p>
 * This class is maintainable for providing an instance of {@link Indexer} with the required
 * connections to persistence storage. It is responsible for maintaining the connections and
 * providing access to the following functionality:
 * <ol>
 * <li>A DAO object (instance of {@link FullBeanDao}) for storing Full Beans.</li>
 * <li>A Publisher object (instance of {@link FullBeanPublisher}) for publishing Full Beans to be
 * accessed by external agents.</li>
 * </ol>
 * </p>
 * <p>
 * Please note that this class is {@link Closeable} and must be closed to release it's resources.
 * </p>
 * 
 * @author jochen
 *
 */
class IndexingConnectionProvider implements Closeable {

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexingConnectionProvider.class);

  private final LBHttpSolrServer httpSolrServer;
  private final CloudSolrServer cloudSolrServer;
  private final EdmMongoServer mongoServer;

  /**
   * Constructor. Sets up the required connections using the supplied settings.
   * 
   * @param settings The indexing settings (connection settings).
   * @throws IndexerConfigurationException In case the connections could not be set up.
   */
  IndexingConnectionProvider(IndexingSettings settings) throws IndexerConfigurationException {

    // Create Solr and Zookeeper connections.
    this.httpSolrServer = setUpHttpSolrConnection(settings);
    if (settings.establishZookeeperConnection()) {
      this.cloudSolrServer = setUpCloudSolrConnection(settings, httpSolrServer);
    } else {
      this.cloudSolrServer = null;
    }

    // Create mongo connection.
    this.mongoServer = setUpMongoConnection(settings);
  }

  private static EdmMongoServer setUpMongoConnection(IndexingSettings settings)
      throws IndexerConfigurationException {

    // Create credentials
    final List<MongoCredential> credentials;
    if (settings.getMongoCredentials() == null) {
      credentials = Collections.emptyList();
    } else {
      credentials = Collections.singletonList(settings.getMongoCredentials());
    }

    // Create options
    final boolean enableSsl = settings.mongoEnableSsl();
    final MongoClientOptions.Builder optionsBuilder = new Builder().sslEnabled(enableSsl);

    // Create client
    final List<ServerAddress> hosts = settings.getMongoHosts();
    final MongoClient client = new MongoClient(hosts, credentials, optionsBuilder.build());

    // Perform logging
    final String databaseName = settings.getMongoDatabaseName();
    LOGGER.info(
        "Connecting to Mongo hosts: [{}], database [{}], with{} authentication, with{} SSL. ",
        hosts.stream().map(ServerAddress::toString).collect(Collectors.joining(", ")), databaseName,
        credentials.isEmpty() ? "out" : "", enableSsl ? "" : "out");

    // Create server
    try {
      return new EdmMongoServerImpl(client, databaseName);
    } catch (MongoDBException e) {
      throw new IndexerConfigurationException("Could not set up mongo server.", e);
    }
  }

  private static LBHttpSolrServer setUpHttpSolrConnection(IndexingSettings settings)
      throws IndexerConfigurationException {
    try {
      final String[] solrHosts =
          settings.getSolrHosts().stream().map(URI::toString).toArray(String[]::new);
      LOGGER.info("Connecting to Solr hosts: [{}]",
          Arrays.stream(solrHosts).collect(Collectors.joining(",")));
      return new LBHttpSolrServer(solrHosts);
    } catch (MalformedURLException e) {
      throw new IndexerConfigurationException("Malformed URL provided in indexer settings.", e);
    }
  }

  private static CloudSolrServer setUpCloudSolrConnection(IndexingSettings settings,
      LBHttpSolrServer httpSolrServer) throws IndexerConfigurationException {

    // Compile Zookeeper-specific connection string
    final String zookeeperConnectionString =
        toZookeeperAddressString(settings.getZookeeperHosts(), settings.getZookeeperChroot());
    final String zookeeperDefaultCollection = settings.getZookeeperDefaultCollection();

    // Set up Zookeeper connection.
    LOGGER.info("Connecting to Zookeeper: [{}] with default collection [{}]",
        zookeeperConnectionString, zookeeperDefaultCollection);
    final CloudSolrServer cloudSolrServer =
        new CloudSolrServer(zookeeperConnectionString, httpSolrServer);
    cloudSolrServer.setDefaultCollection(zookeeperDefaultCollection);
    cloudSolrServer.connect();

    // Done
    return cloudSolrServer;
  }

  /**
   * This utility method converts a list of addresses (host plus port) and a chroot to a string that
   * is accepted by Zookeeper, and hence by {@link CloudSolrServer}. See the documentation of
   * {@link org.apache.zookeeper.ZooKeeper} constructors, for instance
   * {@link org.apache.zookeeper.ZooKeeper#ZooKeeper(String, int, org.apache.zookeeper.Watcher)}.
   * 
   * @param zookeeperHosts The hosts.
   * @param zookeeperChroot The chroot.
   * @return The Zookeeper-compliant string.
   */
  static String toZookeeperAddressString(List<InetSocketAddress> zookeeperHosts,
      String zookeeperChroot) {
    final String zookeeperHostString = zookeeperHosts.stream()
        .map(IndexingConnectionProvider::toZookeeperAddressString).collect(Collectors.joining(","));
    return zookeeperHostString + (zookeeperChroot == null ? "" : zookeeperChroot);
  }

  /**
   * This utility method converts an address (host plus port) to a string that is accepted by
   * Zookeeper, and hence by {@link CloudSolrServer}. See the documentation of
   * {@link org.apache.zookeeper.ZooKeeper} constructors, for instance
   * {@link org.apache.zookeeper.ZooKeeper#ZooKeeper(String, int, org.apache.zookeeper.Watcher)}.
   * 
   * @param address The address to convert.
   * @return The Zookeeper-compliant string.
   */
  static String toZookeeperAddressString(InetSocketAddress address) {
    return address.getHostString() + ":" + address.getPort();
  }

  /**
   * Provides a DAO object for storing (saving or updating) Full Beans.
   * 
   * @return A DAO.
   */
  FullBeanDao getFullBeanDao() {
    return new FullBeanDao(mongoServer);
  }

  /**
   * Provides a Publisher object for publishing Full Beans so that they may be found by users.
   * 
   * @return A publisher.
   */
  FullBeanPublisher getFullBeanPublisher() {
    final SolrServer solrServer = cloudSolrServer == null ? httpSolrServer : cloudSolrServer;
    return new FullBeanPublisher(getFullBeanDao(), solrServer);
  }

  @Override
  public void close() {
    httpSolrServer.shutdown();
    if (cloudSolrServer != null) {
      cloudSolrServer.shutdown();
    }
    mongoServer.close();
  }
}
