package eu.europeana.indexing.mongo;

import static eu.europeana.indexing.utils.IndexingSettingsUtils.nonNullFieldName;
import static eu.europeana.indexing.utils.IndexingSettingsUtils.nonNullMessage;

import com.mongodb.MongoConfigurationException;
import com.mongodb.MongoIncompatibleDriverException;
import com.mongodb.MongoSecurityException;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import eu.europeana.indexing.AbstractConnectionProvider;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.metis.mongo.connection.MongoClientProvider;
import eu.europeana.metis.mongo.connection.MongoProperties;
import eu.europeana.metis.mongo.dao.RecordDao;
import eu.europeana.metis.mongo.dao.RecordRedirectDao;
import java.lang.invoke.MethodHandles;
import java.util.stream.Collectors;
import org.apache.solr.client.solrj.SolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The type Mongo connection provider.
 */
public final class MongoConnectionProvider implements AbstractConnectionProvider {

  private static final String MONGO_SERVER_SETUP_ERROR = "Could not set up connection to Mongo server.";

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final MongoClient mongoClient;
  private final RecordDao recordDao;
  private final RecordRedirectDao recordRedirectDao;

  /**
   * Instantiates a new Mongo connection provider.
   *
   * @param settings the mongo settings
   * @throws SetupRelatedIndexingException the setup related indexing exception
   */
  public MongoConnectionProvider(MongoIndexingSettings settings) throws SetupRelatedIndexingException {

    // Sanity check
    settings = nonNullMessage(settings, "The provided mongo settings object is null.");

    try {
      this.mongoClient = createMongoClient(settings);
      this.recordDao = new RecordDao(this.mongoClient, nonNullFieldName(settings.getMongoDatabaseName(), "mongoDatabaseName"));
      this.recordRedirectDao = new RecordRedirectDao(this.mongoClient,
          nonNullFieldName(settings.getRecordRedirectDatabaseName(), "recordRedirectDatabaseName"));
    } catch (MongoIncompatibleDriverException | MongoConfigurationException | MongoSecurityException e) {
      throw new SetupRelatedIndexingException(MONGO_SERVER_SETUP_ERROR, e);
    }
  }

  private static MongoClient createMongoClient(MongoIndexingSettings settings) throws SetupRelatedIndexingException {
    MongoProperties<SetupRelatedIndexingException> properties = (MongoProperties<SetupRelatedIndexingException>) settings.getDatabaseProperties();
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Connecting to Mongo hosts: [{}], database [{}], with{} authentication, with{} SSL. ",
          properties.getMongoHosts().stream().map(ServerAddress::toString)
                    .collect(Collectors.joining(", ")),
          settings.getMongoDatabaseName(),
          properties.getMongoCredentials() == null ? "out" : "",
          properties.mongoEnableSsl() ? "" : "out");
    }

    return new MongoClientProvider<>(properties).createMongoClient();
  }

  @Override
  public SolrClient getSolrClient() {
    return null;
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
  public void close() {
    mongoClient.close();
  }
}
