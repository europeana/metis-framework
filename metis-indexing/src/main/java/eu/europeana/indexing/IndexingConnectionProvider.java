package eu.europeana.indexing;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.LBHttpSolrClient;
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

  private final LBHttpSolrClient httpSolrClient;
  private final CloudSolrClient cloudSolrClient;
  private final MongoClient mongoClient;
  private final EdmMongoServer mongoServer;

  /**
   * Constructor. Sets up the required connections using the supplied settings.
   * 
   * @param settings The indexing settings (connection settings).
   * @throws IndexerConfigurationException In case the connections could not be set up.
   */
  IndexingConnectionProvider(IndexingSettings settings) throws IndexerConfigurationException {

    // Create Solr and Zookeeper connections.
    this.httpSolrClient = setUpHttpSolrConnection(settings);
    if (settings.establishZookeeperConnection()) {
      this.cloudSolrClient = setUpCloudSolrConnection(settings, this.httpSolrClient);
    } else {
      this.cloudSolrClient = null;
    }

    // Create mongo connection.
    this.mongoClient = createMongoClient(settings);
    this.mongoServer = setUpMongoConnection(settings, this.mongoClient);
  }

  private static MongoClient createMongoClient(IndexingSettings settings)
      throws IndexerConfigurationException {

    // Extract data from settings
    final List<ServerAddress> hosts = settings.getMongoHosts();
    final MongoCredential credentials = settings.getMongoCredentials();
    final boolean enableSsl = settings.mongoEnableSsl();

    // Perform logging
    final String databaseName = settings.getMongoDatabaseName();
    LOGGER.info(
        "Connecting to Mongo hosts: [{}], database [{}], with{} authentication, with{} SSL. ",
        hosts.stream().map(ServerAddress::toString).collect(Collectors.joining(", ")), databaseName,
        credentials == null ? "out" : "", enableSsl ? "" : "out");

    // Create client
    final MongoClientOptions.Builder optionsBuilder = new Builder().sslEnabled(enableSsl);
    if (credentials == null) {
      return new MongoClient(hosts, optionsBuilder.build());
    } else {
      return new MongoClient(hosts, settings.getMongoCredentials(), optionsBuilder.build());
    }
  }

  private static EdmMongoServer setUpMongoConnection(IndexingSettings settings, MongoClient client)
      throws IndexerConfigurationException {
    try {
      return new EdmMongoServerImpl(client, settings.getMongoDatabaseName());
    } catch (MongoDBException e) {
      throw new IndexerConfigurationException("Could not set up mongo server.", e);
    }
  }

  private static LBHttpSolrClient setUpHttpSolrConnection(IndexingSettings settings)
      throws IndexerConfigurationException {
    final String[] solrHosts =
        settings.getSolrHosts().stream().map(URI::toString).toArray(String[]::new);
    LOGGER.info("Connecting to Solr hosts: [{}]",
        Arrays.stream(solrHosts).collect(Collectors.joining(", ")));
    return new LBHttpSolrClient.Builder().withBaseSolrUrls(solrHosts).build();
  }

  private static CloudSolrClient setUpCloudSolrConnection(IndexingSettings settings,
      LBHttpSolrClient httpSolrClient) throws IndexerConfigurationException {

    // Get information from settings
    final Set<String> hosts = settings.getZookeeperHosts().stream()
        .map(IndexingConnectionProvider::toCloudSolrClientAddressString)
        .collect(Collectors.toSet());
    final String chRoot = settings.getZookeeperChroot();
    final String defaultCollection = settings.getZookeeperDefaultCollection();

    // Configure connection builder
    final CloudSolrClient.Builder builder = new CloudSolrClient.Builder();
    builder.withZkHost(hosts);
    if (chRoot != null) {
      builder.withZkChroot(chRoot);
    }
    builder.withLBHttpSolrClient(httpSolrClient);

    // Set up Zookeeper connection
    LOGGER.info("Connecting to Zookeeper hosts: [{}] with chRoot [{}] and default connection [{}].",
        hosts.stream().collect(Collectors.joining(", ")), chRoot, defaultCollection);
    final CloudSolrClient cloudSolrClient = builder.build();
    cloudSolrClient.setDefaultCollection(defaultCollection);
    cloudSolrClient.connect();

    // Done
    return cloudSolrClient;
  }

  /**
   * This utility method converts an address (host plus port) to a string that is accepted by
   * {@link CloudSolrClient}.
   * 
   * @param address The address to convert.
   * @return The compliant string.
   */
  static String toCloudSolrClientAddressString(InetSocketAddress address) {
    return address.getHostString() + ":" + address.getPort();
  }

  /**
   * Provides a Publisher object for publishing Full Beans so that they may be found by users.
   * 
   * @return A publisher.
   */
  FullBeanPublisher getFullBeanPublisher() {
    final SolrClient solrServer = cloudSolrClient == null ? httpSolrClient : cloudSolrClient;
    return new FullBeanPublisher(new FullBeanDao(mongoServer), solrServer);
  }

  @Override
  public void close() throws IOException {
    mongoServer.close();
    mongoClient.close();
    httpSolrClient.close();
    if (cloudSolrClient != null) {
      cloudSolrClient.close();
    }
  }
}
