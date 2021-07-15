package eu.europeana.metis.repository.dao;

import com.mongodb.client.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.DiscriminatorFunction;
import dev.morphia.mapping.MapperOptions;
import dev.morphia.mapping.NamingStrategy;
import dev.morphia.query.experimental.filters.Filters;
import eu.europeana.metis.network.ExternalRequestUtil;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database Access Object for {@link Record} instances.
 */
public class RecordDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(RecordDao.class);

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

    Optional<Record> recordFound = datastore.find(Record.class).stream()
        .filter(x -> x.getRecordId().equals(record.getRecordId())).findFirst();

    if (recordFound.isPresent()) {
      record.setId(recordFound.get().getId());
    } else {
      final ObjectId objectId = Optional.ofNullable(record.getId()).orElseGet(ObjectId::new);
      record.setId(objectId);
    }
    final Record recordSaved = ExternalRequestUtil.retryableExternalRequestForNetworkExceptions(
        () -> datastore.save(record));
    LOGGER.info("Record for datasetId '{}' created in Mongo", record.getDatasetId());

    return recordSaved;
  }

  public List<Record> getAllRecordsFromDataset(String datasetId) {
    return datastore.find(Record.class).stream().filter(x -> x.getDatasetId().equals(datasetId))
        .collect(Collectors.toList());
  }
}
