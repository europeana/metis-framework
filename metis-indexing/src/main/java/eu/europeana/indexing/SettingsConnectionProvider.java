package eu.europeana.indexing;

import com.mongodb.MongoConfigurationException;
import com.mongodb.MongoIncompatibleDriverException;
import com.mongodb.MongoSecurityException;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import eu.europeana.indexing.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.metis.mongo.RecordDao;
import eu.europeana.metis.mongo.MongoClientProvider;
import eu.europeana.metis.mongo.RecordRedirectDao;
import eu.europeana.metis.solr.CompoundSolrClient;
import eu.europeana.metis.solr.SolrClientProvider;
import java.io.IOException;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is an implementation of {@link AbstractConnectionProvider} that sets up the connection
 * using an {@link IndexingSettings} object. Various methods are made public so that this class may
 * be constructed and used outside the scope of the indexing library.
 *
 * @author jochen
 */
public final class SettingsConnectionProvider implements AbstractConnectionProvider {

  private static final String MONGO_SERVER_SETUP_ERROR = "Could not set up connection to Mongo server.";

  private static final Logger LOGGER = LoggerFactory.getLogger(SettingsConnectionProvider.class);

  private final CompoundSolrClient solrClient;
  private final MongoClient mongoClient;
  private final RecordDao recordDao;
  private final RecordRedirectDao recordRedirectDao;

  /**
   * Constructor. Sets up the required connections using the supplied settings.
   *
   * @param settings The indexing settings (connection settings).
   * @throws SetupRelatedIndexingException In case the connections could not be set up.
   * @throws IndexerRelatedIndexingException In case the connection could not be established.
   */
  public SettingsConnectionProvider(IndexingSettings settings)
      throws SetupRelatedIndexingException, IndexerRelatedIndexingException {

    // Sanity check
    if (settings == null) {
      throw new SetupRelatedIndexingException("The provided settings object is null.");
    }

    // Create Solr and Zookeeper connections.
    this.solrClient = new SolrClientProvider<>(settings.getSolrProperties()).createSolrClient();

    // Create mongo connection.
    try {
      this.mongoClient = createMongoClient(settings);
      this.recordDao = setUpEdmMongoConnection(settings, this.mongoClient);
      this.recordRedirectDao = setUpRecordRedirectDaoConnection(settings, this.mongoClient);
    } catch (MongoIncompatibleDriverException | MongoConfigurationException | MongoSecurityException e) {
      throw new SetupRelatedIndexingException(MONGO_SERVER_SETUP_ERROR, e);
    } catch (RuntimeException e) {
      throw new IndexerRelatedIndexingException(MONGO_SERVER_SETUP_ERROR, e);
    }
  }

  private static MongoClient createMongoClient(IndexingSettings settings)
      throws SetupRelatedIndexingException {

    // Perform logging
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info(
          "Connecting to Mongo hosts: [{}], database [{}], with{} authentication, with{} SSL. ",
          settings.getMongoProperties().getMongoHosts().stream().map(ServerAddress::toString)
              .collect(Collectors.joining(", ")),
          settings.getMongoDatabaseName(),
          settings.getMongoProperties().getMongoCredentials() == null ? "out" : "",
          settings.getMongoProperties().mongoEnableSsl() ? "" : "out");
    }

    // Create client
    return new MongoClientProvider<>(settings.getMongoProperties()).createMongoClient();
  }

  private static RecordDao setUpEdmMongoConnection(IndexingSettings settings,
      MongoClient client)
      throws SetupRelatedIndexingException {
    try {
      return new RecordDao(client, settings.getMongoDatabaseName());
    } catch (RuntimeException e) {
      throw new SetupRelatedIndexingException("Could not set up mongo server.", e);
    }
  }

  private static RecordRedirectDao setUpRecordRedirectDaoConnection(IndexingSettings settings,
      MongoClient client)
      throws SetupRelatedIndexingException {
    try {
      RecordRedirectDao recordRedirectDao = null;
      if (StringUtils.isNotBlank(settings.getRecordRedirectDatabaseName())) {
        recordRedirectDao = new RecordRedirectDao(client, settings.getRecordRedirectDatabaseName());
      }
      return recordRedirectDao;
    } catch (RuntimeException e) {
      throw new SetupRelatedIndexingException("Could not set up mongo server.", e);
    }
  }

  @Override
  public SolrClient getSolrClient() {
    return this.solrClient.getSolrClient();
  }

  @Override
  public RecordDao getRecordDao() {
    return recordDao;
  }

  @Override
  public RecordRedirectDao getRecordRedirectDao() {
    return recordRedirectDao;
  }

  @Override
  public void close() throws IOException {
    recordDao.close();
    mongoClient.close();
    this.solrClient.close();
  }
}
