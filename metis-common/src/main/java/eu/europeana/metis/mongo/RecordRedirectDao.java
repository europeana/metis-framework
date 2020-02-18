package eu.europeana.metis.mongo;

import com.mongodb.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Key;
import dev.morphia.Morphia;
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

  protected MongoClient mongoClient;
  protected Datastore datastore;

  /**
   * Constructs the DAO
   * <p>Initializes the datastore using the provided mongo client</p>
   *
   * @param mongoClient the mongo client connection
   * @param databaseName the database name of the record redirect database
   * @param createIndexes flag that enables/disables the creation of the indexes in the database
   */
  public RecordRedirectDao(MongoClient mongoClient, String databaseName, boolean createIndexes) {
    this.mongoClient = mongoClient;

    Morphia morphia = new Morphia();
    morphia.map(RecordRedirect.class);
    datastore = morphia.createDatastore(mongoClient, databaseName);
    if (createIndexes) {
      datastore.ensureIndexes();
    }
    LOGGER.info("RecordRedirect datastore is created");
  }

  /**
   * Create/Update a record redirect in the database
   *
   * @param recordRedirect the record redirect to be created/updated
   * @return the {@link ObjectId} as String
   */
  public String createUpdate(RecordRedirect recordRedirect) {
    Key<RecordRedirect> recordRedirectKey = ExternalRequestUtil
        .retryableExternalRequestConnectionReset(
            () -> datastore.save(recordRedirect));
    LOGGER.debug(
        "RecordRedirect with oldId: '{}', newId: '{}' and timestamp: '{}' created in Mongo",
        recordRedirect.getOldId(), recordRedirect.getNewId(), recordRedirect.getTimestamp());
    return recordRedirectKey == null ? null : recordRedirectKey.getId().toString();
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
            .filter("_id", new ObjectId(id)).first());
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
            () -> datastore.find(RecordRedirect.class).field(fieldName).equal(identifier).find()
                    .toList());
  }

  /**
   * Close internal database connections if applicable
   */
  public void close() {
    if (this.mongoClient != null) {
      LOGGER.info("Closing MongoClient for RecordRedirectDao");
      this.mongoClient.close();
    }
  }
}
