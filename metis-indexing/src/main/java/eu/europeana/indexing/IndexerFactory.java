package eu.europeana.indexing;

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

/**
 * This class creates instances of {@link Indexer}.
 */
public class IndexerFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexerFactory.class);

  private final IndexingSettings settings;

  /**
   * Constructor.
   * 
   * @param settings The settings to be applied to the indexer.
   */
  public IndexerFactory(IndexingSettings settings) {
    this.settings = settings;
  }

  /**
   * This method creates an indexer using the settings provided at construction.
   * 
   * @return An indexer.
   * @throws IndexerConfigurationException
   */
  public Indexer getIndexer() throws IndexerConfigurationException {

    // TODO JOCHEN Connect here? See sonarqube warning. Better to make Indexer closeable? Where do
    // we disconnect?

    final SolrServer solrServer = setUpSolrConnection();
    final EdmMongoServer mongoServer = setUpMongoConnection();
    final FullBeanDao fullBeanDao = new FullBeanDao(mongoServer);
    final FullBeanPublisher publisher = new FullBeanPublisher(fullBeanDao, solrServer);
    return new IndexerImpl(publisher);
  }

  private EdmMongoServer setUpMongoConnection() throws IndexerConfigurationException {

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

  private SolrServer setUpSolrConnection() throws IndexerConfigurationException {
    final SolrServer solrServer;
    try {

      // Set up Solr connection
      final String[] solrHosts =
          settings.getSolrHosts().stream().map(URI::toString).toArray(String[]::new);
      LOGGER.info("Connecting to Solr hosts: [{}]",
          Arrays.stream(solrHosts).collect(Collectors.joining(",")));
      final LBHttpSolrServer httpSolrServer = new LBHttpSolrServer(solrHosts);
      if (settings.getZookeeperHosts().isEmpty()) {

        // The Solr connection is the one to talk to.
        solrServer = httpSolrServer;
      } else {

        // Compile Zookeeper-specific connection string
        final String zookeeperHostString = settings.getZookeeperHosts().stream()
            .map(this::toZookeeperAddressString).collect(Collectors.joining(","));
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

        // The Zookeeper connection is the one to talk to.
        solrServer = cloudSolrServer;
      }
    } catch (MalformedURLException e) {
      throw new IndexerConfigurationException("Malformed URL provided in indexer settings.", e);
    }

    // Done
    return solrServer;
  }

  String toZookeeperAddressString(InetSocketAddress address) {
    return address.getHostString() + ":" + address.getPort();
  }
}
