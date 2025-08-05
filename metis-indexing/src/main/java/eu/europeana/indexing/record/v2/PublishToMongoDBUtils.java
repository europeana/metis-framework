package eu.europeana.indexing.record.v2;

import com.mongodb.MongoClientException;
import com.mongodb.MongoConfigurationException;
import com.mongodb.MongoIncompatibleDriverException;
import com.mongodb.MongoInternalException;
import com.mongodb.MongoInterruptedException;
import com.mongodb.MongoSecurityException;
import com.mongodb.MongoSocketException;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.common.contract.IndexerForPersistence.ComputedDates;
import eu.europeana.indexing.common.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.common.exception.IndexingException;
import eu.europeana.indexing.common.exception.RecordRelatedIndexingException;
import eu.europeana.indexing.common.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.record.v2.entity.FullBeanUpdater;
import eu.europeana.metis.mongo.dao.RecordDao;
import java.util.Date;

/**
 * This class provides utilities for publishing
 * {@link eu.europeana.corelib.definitions.edm.beans.FullBean} objects to a Mongo DB.
 */
class PublishToMongoDBUtils {

  private static final String MONGO_SERVER_PUBLISH_ERROR = "Could not publish to Mongo server.";

  private PublishToMongoDBUtils() {
  }

  /**
   * This publishes a record to the provided Mongo DB connection.
   * @param recordDate The date that would represent the created/updated date of a record
   * @param createdDate The date that would represent the created date if the record already exists,
   * e.g. from a redirected record. Can be null (in which case <code>recordDate</code> would be used).
   * @param fullBean The record to publish.
   * @param recordDao The Mongo DB connection.
   * @param preserveUpdateAndCreateTimesFromRdf This regulates whether we should preserve (use) the
   * updated and created dates that are set in the input record or if they should be recomputed
   * using any equivalent record that is currently in the database.
   * @return The dates that were used for the record.
   * @throws IndexingException which can be one of:
   * <ul>
   * <li>{@link IndexerRelatedIndexingException} In case an error occurred during publication.</li>
   * <li>{@link SetupRelatedIndexingException} in case an error occurred during indexing setup</li>
   * <li>{@link RecordRelatedIndexingException} in case an error occurred related to record
   * contents</li>
   * </ul>
   */
  static ComputedDates publishRecord(Date recordDate, Date createdDate,
      FullBeanImpl fullBean, RecordDao recordDao, boolean preserveUpdateAndCreateTimesFromRdf)
      throws IndexingException {
    final ComputedDates computedDates;
    try {
      final FullBeanImpl savedFullBean = new FullBeanUpdater(preserveUpdateAndCreateTimesFromRdf)
          .update(fullBean, recordDate, createdDate, recordDao);
      computedDates = new ComputedDates(savedFullBean.getTimestampUpdated(),
          savedFullBean.getTimestampCreated());
    } catch (MongoIncompatibleDriverException | MongoConfigurationException |
             MongoSecurityException e) {
      throw new SetupRelatedIndexingException(MONGO_SERVER_PUBLISH_ERROR, e);
    } catch (MongoSocketException | MongoClientException | MongoInternalException |
             MongoInterruptedException e) {
      throw new IndexerRelatedIndexingException(MONGO_SERVER_PUBLISH_ERROR, e);
    } catch (RuntimeException e) {
      throw new RecordRelatedIndexingException(MONGO_SERVER_PUBLISH_ERROR, e);
    }
    return computedDates;
  }
}
