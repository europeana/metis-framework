package eu.europeana.indexing;

import java.io.Closeable;
import java.io.IOException;
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
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.MongoClientOptions.Builder;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.mongo.server.impl.EdmMongoServerImpl;

class IndexingConnectionProvider implements Closeable {

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexingConnectionProvider.class);

  private final LBHttpSolrServer httpSolrServer;
  private final CloudSolrServer zookeeperServer;
  private final EdmMongoServer mongoServer;

  IndexingConnectionProvider(IndexingSettings settings) throws IndexerConfigurationException {

    // Create Solr and Zookeeper connections.
    this.httpSolrServer = setUpSolrConnection(settings);
    if (settings.establishZookeeperConnection()) {
      this.zookeeperServer = setUpZookeeperConnection(settings, httpSolrServer);
    } else {
      this.zookeeperServer = null;
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

  private static LBHttpSolrServer setUpSolrConnection(IndexingSettings settings)
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

  private static CloudSolrServer setUpZookeeperConnection(IndexingSettings settings,
      LBHttpSolrServer httpSolrServer) throws IndexerConfigurationException {

    // Compile Zookeeper-specific connection string
    final String zookeeperHostString = settings.getZookeeperHosts().stream()
        .map(IndexingConnectionProvider::toZookeeperAddressString).collect(Collectors.joining(","));
    final String zookeeperChrootString =
        settings.getZookeeperChroot() == null ? "" : settings.getZookeeperChroot();
    final String zookeeperConnectionString = zookeeperHostString + zookeeperChrootString;
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

  static String toZookeeperAddressString(InetSocketAddress address) {
    return address.getHostString() + ":" + address.getPort();
  }

  FullBeanDao getFullBeanDao() {
    return new FullBeanDao(mongoServer);
  }

  FullBeanPublisher getFullBeanPublisher() {
    final SolrServer solrServer = zookeeperServer == null ? httpSolrServer : zookeeperServer;
    return new FullBeanPublisher(getFullBeanDao(), solrServer);
  }

  @Override
  public void close() throws IOException {
    httpSolrServer.shutdown();
    if (zookeeperServer != null) {
      zookeeperServer.shutdown();
    }
    mongoServer.close();
  }
}
