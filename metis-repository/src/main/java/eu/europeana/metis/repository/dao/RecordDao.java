package eu.europeana.metis.repository.dao;

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
   * Create record in the database
   *
   * @param record - The record to be saved in the database
   * @return The record just saved in the database
   */
  public Record createRecord(Record record) {

    Optional<Record> recordFound = datastore.find(Record.class)
        .filter(Filters.eq(RECORD_ID_FIELD, record.getRecordId())).stream().findFirst();

    recordFound.ifPresent(value -> record.setId(value.getId()));

    final Record recordSaved = ExternalRequestUtil.retryableExternalRequestForNetworkExceptions(
        () -> datastore.save(record));
    LOGGER.info("Record for datasetId '{}' created in Mongo", record.getDatasetId());

    return recordSaved;
  }

  /**
   * Creates  list of records that belong to the given dataset id
   *
   * @param datasetId - The id of the dataset the records belong to
   * @return A stream of records that are part of the dataset with datasetId
   */
  public Stream<Record> getAllRecordsFromDataset(String datasetId) {
    return datastore.find(Record.class).filter(Filters.eq(DATASET_ID_FIELD, datasetId)).stream();
  }
}
