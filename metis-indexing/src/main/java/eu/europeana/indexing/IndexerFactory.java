package eu.europeana.indexing;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoCredential;
import eu.europeana.indexing.service.PublishingService;
import eu.europeana.indexing.service.dao.FullBeanDao;
import eu.europeana.indexing.service.mongo.MorphiaDatastoreProvider;

/**
 * This class creates instances of {@link Indexer}.
 */
public class IndexerFactory {

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

    // TODO JOCHEN Connect here? See sonarqube warning. Better to make Indexer closeable?

    // Set up the connection with the Solr server.
    final CloudSolrServer cloudSolrServer;
    try {
      final LBHttpSolrServer solrServer = new LBHttpSolrServer(
          settings.getSolrHosts().stream().map(URI::toString).toArray(String[]::new));
      cloudSolrServer = new CloudSolrServer(settings.getZookeeperHost().toString(), solrServer);
      cloudSolrServer.setDefaultCollection(settings.getSolrCollectionName());
      cloudSolrServer.connect();
    } catch (MalformedURLException e) {
      throw new IndexerConfigurationException("Malformed URL provided in indexer settings.", e);
    }

    // Set up the connection with the Mongo server.
    final List<MongoCredential> mongoCredentials;
    if (settings.getMongoCredentials() == null) {
      mongoCredentials = Collections.emptyList();
    } else {
      mongoCredentials = Collections.singletonList(settings.getMongoCredentials());
    }
    final MongoClientOptions.Builder optionsBuilder =
        new Builder().sslEnabled(settings.mongoEnableSsl());
    final MorphiaDatastoreProvider morphiaDatastoreProvider = new MorphiaDatastoreProvider(
        new MongoClient(settings.getMongoHosts(), mongoCredentials, optionsBuilder.build()),
        settings.getMongoDatabaseName());

    // Set up the indexer.
    final FullBeanDao fullBeanDao = new FullBeanDao(morphiaDatastoreProvider);
    final PublishingService publishingService = new PublishingService(fullBeanDao, cloudSolrServer);
    return new IndexerImpl(publishingService);
  }
}
