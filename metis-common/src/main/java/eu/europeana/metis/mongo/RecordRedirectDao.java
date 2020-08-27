package eu.europeana.metis.mongo;

import com.mongodb.client.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.query.experimental.filters.Filters;
import eu.europeana.metis.utils.ExternalRequestUtil;
import java.util.List;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dataset Access Object for record redirects using Mongo.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2020-01-14
 */
public class RecordRedirectDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(RecordRedirectDao.class);
  private static final String OLD_ID = "oldId";
  private static final String NEW_ID = "newId";
  private final Datastore datastore;

  /**
   * Constructor to initialize the mongo mappings/collections and the {@link Datastore} connection.
   * This constructor is meant to be used when the database is already available.
   *
   * @param mongoClient the mongo client connection
   * @param databaseName the database name of the record redirect database
   */
  public RecordRedirectDao(MongoClient mongoClient, String databaseName) {
    this(mongoClient, databaseName, false);
  }

  /**
   * Constructor to initialize the mongo mappings/collections and the {@link Datastore} connection.
   * This constructor is meant to be used mostly for when the creation of the database is required.
   *
   * @param mongoClient the mongo client connection
   * @param databaseName the database name of the record redirect database
   * @param createIndexes flag that initiates the database/indices
   */
  public RecordRedirectDao(MongoClient mongoClient, String databaseName, boolean createIndexes) {
    datastore = createDatastore(mongoClient, databaseName);
    if (createIndexes) {
      LOGGER.info("Initializing database indices");
      datastore.ensureIndexes();
    }
  }

  private static Datastore createDatastore(MongoClient mongoClient, String databaseName) {
    final Datastore datastore = Morphia
        .createDatastore((com.mongodb.client.MongoClient) mongoClient, databaseName);
    datastore.getMapper().map(RecordRedirect.class);
    LOGGER.info("Datastore initialized");
    return datastore;
  }

  /**
   * Create/Update a record redirect in the database
   *
   * @param recordRedirect the record redirect to be created/updated
   * @return the {@link ObjectId} as String
   */
  public String createUpdate(RecordRedirect recordRedirect) {
    RecordRedirect recordRedirectSaved = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(
            () -> datastore.save(recordRedirect));
    LOGGER.debug(
        "RecordRedirect with oldId: '{}', newId: '{}' and timestamp: '{}' created in Mongo",
        recordRedirect.getOldId(), recordRedirect.getNewId(), recordRedirect.getTimestamp());
    return recordRedirectSaved == null ? null : recordRedirectSaved.getId().toString();
  }

  /**
   * Delete a record redirect in the database.
   *
   * @param recordRedirect The record to delete.
   */
  public void delete(RecordRedirect recordRedirect) {
    datastore.delete(recordRedirect);
  }

  /**
   * Get a record redirect by {@link ObjectId} String.
   *
   * @param id the {@link ObjectId} String to search with
   * @return the record redirect object
   */
  public RecordRedirect getById(String id) {
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(
        () -> datastore.find(RecordRedirect.class)
            .filter(Filters.eq("_id", new ObjectId(id))).first());
  }

  /**
   * Get record redirects using an {@link RecordRedirect#getOldId()}
   *
   * @param oldId the oldId of a record redirect
   * @return the record redirect objects
   */
  public List<RecordRedirect> getRecordRedirectsByOldId(String oldId) {
    return getRecordRedirects(OLD_ID, oldId);
  }

  /**
   * Get record redirects using an {@link RecordRedirect#getNewId()} ()}
   *
   * @param newId the newId of a record redirect
   * @return the record redirect objects
   */
  public List<RecordRedirect> getRecordRedirectsByNewId(String newId) {
    return getRecordRedirects(NEW_ID, newId);
  }

  private List<RecordRedirect> getRecordRedirects(String fieldName, String identifier) {
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(
        () -> datastore.find(RecordRedirect.class).filter(Filters.eq(fieldName, identifier))
            .iterator().toList());
  }

  public Datastore getDatastore() {
    return datastore;
  }
}
