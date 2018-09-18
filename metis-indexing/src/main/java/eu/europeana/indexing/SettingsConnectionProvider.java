package eu.europeana.indexing;

import eu.europeana.indexing.exception.IndexingException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
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

/**
 * This class is an implementation of {@link AbstractConnectionProvider} that sets up the connection
 * using an {@link IndexingSettings} object. Various methods are made public so that this class may
 * be constructed and used outside the scope of the indexing library.
 * 
 * @author jochen
 *
 */
public final class SettingsConnectionProvider extends AbstractConnectionProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(SettingsConnectionProvider.class);

  private final LBHttpSolrClient httpSolrClient;
  private final CloudSolrClient cloudSolrClient;
  private final MongoClient mongoClient;
  private final EdmMongoServer mongoServer;

  /**
   * Constructor. Sets up the required connections using the supplied settings.
   * 
   * @param settings The indexing settings (connection settings).
   * @throws IndexingException In case the connections could not be set up.
   */
  public SettingsConnectionProvider(IndexingSettings settings) throws IndexingException {

    // Create Solr and Zookeeper connections.
    this.httpSolrClient = setUpHttpSolrConnection(settings);
    if (settings.hasZookeeperConnection()) {
      this.cloudSolrClient = setUpCloudSolrConnection(settings, this.httpSolrClient);
    } else {
      this.cloudSolrClient = null;
    }

    // Create mongo connection.
    this.mongoClient = createMongoClient(settings);
    this.mongoServer = setUpMongoConnection(settings, this.mongoClient);
  }

  private static MongoClient createMongoClient(IndexingSettings settings) throws IndexingException {

    // Extract data from settings
    final List<ServerAddress> hosts = settings.getMongoHosts();
    final MongoCredential credentials = settings.getMongoCredentials();
    final boolean enableSsl = settings.mongoEnableSsl();

    // Perform logging
    final String databaseName = settings.getMongoDatabaseName();
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info(
          "Connecting to Mongo hosts: [{}], database [{}], with{} authentication, with{} SSL. ",
          hosts.stream().map(ServerAddress::toString).collect(Collectors.joining(", ")),
          databaseName,
          credentials == null ? "out" : "", enableSsl ? "" : "out");
    }

    // Create client
    final MongoClientOptions.Builder optionsBuilder = new Builder().sslEnabled(enableSsl);
    if (credentials == null) {
      return new MongoClient(hosts, optionsBuilder.build());
    } else {
      return new MongoClient(hosts, settings.getMongoCredentials(), optionsBuilder.build());
    }
  }

  private static EdmMongoServer setUpMongoConnection(IndexingSettings settings, MongoClient client)
      throws IndexingException {
    try {
      return new EdmMongoServerImpl(client, settings.getMongoDatabaseName(), true);
    } catch (MongoDBException e) {
      throw new IndexingException("Could not set up mongo server.", e);
    }
  }

  private static LBHttpSolrClient setUpHttpSolrConnection(IndexingSettings settings)
      throws IndexingException {
    final String[] solrHosts =
        settings.getSolrHosts().stream().map(URI::toString).toArray(String[]::new);
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Connecting to Solr hosts: [{}]", String.join(", ", solrHosts));
    }
    return new LBHttpSolrClient.Builder().withBaseSolrUrls(solrHosts).build();
  }

  private static CloudSolrClient setUpCloudSolrConnection(IndexingSettings settings,
      LBHttpSolrClient httpSolrClient) throws IndexingException {

    // Get information from settings
    final Set<String> hosts = settings.getZookeeperHosts().stream()
        .map(SettingsConnectionProvider::toCloudSolrClientAddressString)
        .collect(Collectors.toSet());
    final String chRoot = settings.getZookeeperChroot();
    final String defaultCollection = settings.getZookeeperDefaultCollection();
    final Integer connectionTimeoutInSecs = settings.getZookeeperTimeoutInSecs();

    // Configure connection builder
    final CloudSolrClient.Builder builder = new CloudSolrClient.Builder();
    builder.withZkHost(hosts);
    if (chRoot != null) {
      builder.withZkChroot(chRoot);
    }
    builder.withLBHttpSolrClient(httpSolrClient);

    // Set up Zookeeper connection
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info(
          "Connecting to Zookeeper hosts: [{}] with chRoot [{}] and default connection [{}]. Connection time-out: {}.",
          String.join(", ", hosts), chRoot, defaultCollection,
          connectionTimeoutInSecs == null ? "default" : (connectionTimeoutInSecs + " seconds"));
    }
    final CloudSolrClient cloudSolrClient = builder.build();
    cloudSolrClient.setDefaultCollection(defaultCollection);
    if (connectionTimeoutInSecs != null) {
      final int timeoutInMillis = (int) Duration.ofSeconds(connectionTimeoutInSecs).toMillis();
      cloudSolrClient.setZkConnectTimeout(timeoutInMillis);
      cloudSolrClient.setZkClientTimeout(timeoutInMillis);
    }
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

  @Override
  public SolrClient getSolrClient() {
    return cloudSolrClient == null ? httpSolrClient : cloudSolrClient;
  }

  @Override
  public EdmMongoServer getMongoClient() {
    return mongoServer;
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
