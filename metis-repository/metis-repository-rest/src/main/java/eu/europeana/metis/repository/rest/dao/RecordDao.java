package eu.europeana.metis.repository.rest.dao;

import static eu.europeana.metis.utils.CommonStringValues.CRLF_PATTERN;

import com.mongodb.client.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.DiscriminatorFunction;
import dev.morphia.mapping.MapperOptions;
import dev.morphia.mapping.NamingStrategy;
import dev.morphia.query.experimental.filters.Filters;
import eu.europeana.metis.network.ExternalRequestUtil;
import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database Access Object for {@link Record} instances.
 */
public class RecordDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(RecordDao.class);
  private static final String RECORD_ID_FIELD = "recordId";
  private static final String DATASET_ID_FIELD = "datasetId";

  private final Datastore datastore;

  /**
   * Constructor.
   *
   * @param mongo Client to the mongo database.
   * @param databaseName The name of the database.
   */
  public RecordDao(MongoClient mongo, String databaseName) {
    final MapperOptions mapperOptions = MapperOptions.builder().discriminatorKey("className")
                                                     .discriminator(DiscriminatorFunction.className())
                                                     .collectionNaming(NamingStrategy.identity()).build();
    this.datastore = Morphia.createDatastore(mongo, databaseName, mapperOptions);
    this.datastore.getMapper().map(Record.class);
  }

  /**
   * Create record in the database.
   *
   * @param providedRecord - The record to be saved in the database
   * @return Whether the record was inserted (true) or updated (false).
   */
  public boolean createRecord(Record providedRecord) {

    Optional<Record> recordFound = datastore.find(Record.class)
                                            .filter(Filters.eq(RECORD_ID_FIELD, providedRecord.getRecordId())).stream()
                                            .findFirst();

    recordFound.ifPresent(value -> providedRecord.setId(value.getId()));

    ExternalRequestUtil.retryableExternalRequestForNetworkExceptions(
        () -> datastore.save(providedRecord));
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Record for datasetId '{}' created in Mongo",
          CRLF_PATTERN.matcher(providedRecord.getDatasetId()).replaceAll(""));
    }

    return recordFound.isEmpty();
  }

  /**
   * Creates list of records that belong to the given dataset ID.
   *
   * @param datasetId - The id of the dataset the records belong to
   * @return A stream of records that are part of the dataset with datasetId
   */
  public Stream<Record> getAllRecordsFromDataset(String datasetId) {
    return datastore.find(Record.class).filter(Filters.eq(DATASET_ID_FIELD, datasetId)).stream();
  }

  /**
   * Returns a record with the given record ID.
   *
   * @param recordId - The unique ID of the record
   * @return The record found
   */
  public Record getRecord(String recordId) {
    Optional<Record> recordFound = datastore.find(Record.class)
                                            .filter(Filters.eq(RECORD_ID_FIELD, recordId))
                                            .stream()
                                            .findFirst();

    if (recordFound.isPresent()) {
      return recordFound.get();
    } else {
      if (LOGGER.isWarnEnabled()) {
        LOGGER.warn("There is no such record with id {}.", CRLF_PATTERN.matcher(recordId).replaceAll(""));
      }
      return null;
    }
  }

  /**
   * Deletes a record with the given record ID.
   *
   * @param recordId The unique ID of the record to delete.
   * @return whether the record was deleted (i.e. whether the record existed).
   */
  public boolean deleteRecord(String recordId) {
    final boolean isDeleted = datastore.find(Record.class)
                                       .filter(Filters.eq(RECORD_ID_FIELD, recordId)).delete().getDeletedCount() > 0;
    if (!isDeleted) {
      LOGGER.warn("There is no such record with id {}.", recordId);
    }
    return isDeleted;
  }
}
