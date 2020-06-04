package eu.europeana.metis.core.dao;

import dev.morphia.query.Query;
import eu.europeana.metis.core.dataset.DepublishedRecord;
import eu.europeana.metis.core.dataset.DepublishedRecord.DepublicationState;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.utils.ExternalRequestUtil;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * DAO for {@link DepublishedRecord} objects.
 */
@Repository
public class DepublishedRecordDao {

  private final MorphiaDatastoreProvider morphiaDatastoreProvider;

  private final long maximumRecordsPerDataset;

  /**
   * Constructor.
   *
   * @param morphiaDatastoreProvider The datastore provider.
   * @param maximumRecordsPerDataset The maximum number of records we allow per dataset.
   */
  public DepublishedRecordDao(MorphiaDatastoreProvider morphiaDatastoreProvider,
          long maximumRecordsPerDataset) {
    this.morphiaDatastoreProvider = morphiaDatastoreProvider;
    this.maximumRecordsPerDataset = maximumRecordsPerDataset;
  }

  private Set<String> getNonExistingRecords(String datasetId, Set<String> recordIds) {
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(() -> {

      // Create query for existing records in list. Only return record IDs.
      final Query<DepublishedRecord> query = morphiaDatastoreProvider.getDatastore()
              .createQuery(DepublishedRecord.class);
      query.field(DepublishedRecord.DATASET_ID_FIELD).equal(datasetId);
      query.field(DepublishedRecord.RECORD_ID_FIELD).in(recordIds);
      query.project(DepublishedRecord.RECORD_ID_FIELD, true);
      query.project(DepublishedRecord.ID_FIELD, false);

      // Execute query and find existing record IDs.
      final Set<String> existing = query.find().toList().stream()
              .map(DepublishedRecord::getRecordId).collect(Collectors.toSet());

      // Return the other ones: the record IDs not found in the database.
      return recordIds.stream().filter(recordId -> !existing.contains(recordId))
              .collect(Collectors.toSet());
    });
  }

  /**
   * Add depublished records to persistence. This method checks whether the depublished record
   * already exists, and if so, doesn't add it again. All new records (but not the existing ones)
   * will have the default depublication state ({@link DepublicationState#NOT_DEPUBLISHED}) and no
   * depublication date.
   *
   * @param datasetId The dataset to which the records belong.
   * @param candidateRecordIds The IDs of the records to add.
   * @return How many of the passed records were in fact added. This counter is not thread-safe: if
   * multiple threads try to add the same records, their combined counters may overrepresent the
   * number of records that were actually added.
   * @throws BadContentException In case adding the records would violate the maximum number of
   * depublished records that each dataset can have.
   */
  public int createRecordsToBeDepublished(String datasetId, Set<String> candidateRecordIds)
          throws BadContentException {

    // Check list size: if this is too large we can throw exception regardless of what's in the database.
    if (candidateRecordIds.size() > maximumRecordsPerDataset) {
      throw new BadContentException(
              "Can't add these records: this would violate the maximum number of records per dataset.");
    }

    // Get the nonexisting records: those we actually add.
    final Set<String> recordsToAdd = getNonExistingRecords(datasetId, candidateRecordIds);

    // Count: determine whether we are not above our maximum.
    final long existingCount = ExternalRequestUtil.retryableExternalRequestConnectionReset(
            () -> morphiaDatastoreProvider.getDatastore().createQuery(DepublishedRecord.class)
                    .field(DepublishedRecord.DATASET_ID_FIELD).equal(datasetId).count());
    if (existingCount + recordsToAdd.size() > maximumRecordsPerDataset) {
      throw new BadContentException(
              "Can't add these records: this would violate the maximum number of records per dataset.");
    }

    // Add the records
    final List<DepublishedRecord> objectsToAdd = recordsToAdd.stream().map(recordId -> {
      final DepublishedRecord record = new DepublishedRecord();
      record.setDatasetId(datasetId);
      record.setRecordId(recordId);
      record.setDepublicationState(DepublicationState.NOT_DEPUBLISHED);
      return record;
    }).collect(Collectors.toList());
    ExternalRequestUtil.retryableExternalRequestConnectionReset(() -> {
      morphiaDatastoreProvider.getDatastore().save(objectsToAdd);
      return null;
    });

    // Done
    return recordsToAdd.size();
  }
}
