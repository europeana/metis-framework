package eu.europeana.metis.core.dao;

import static eu.europeana.metis.mongo.MorphiaUtils.getListOfQueryRetryable;
import static eu.europeana.metis.utils.ExternalRequestUtil.retryableExternalRequestForNetworkExceptions;

import dev.morphia.DeleteOptions;
import dev.morphia.UpdateOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filters;
import dev.morphia.query.experimental.updates.UpdateOperator;
import dev.morphia.query.experimental.updates.UpdateOperators;
import eu.europeana.metis.core.dataset.DepublishRecordId;
import eu.europeana.metis.core.dataset.DepublishRecordId.DepublicationStatus;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.rest.DepublishRecordIdView;
import eu.europeana.metis.core.rest.RequestLimits;
import eu.europeana.metis.core.util.DepublishRecordIdSortField;
import eu.europeana.metis.core.util.SortDirection;
import eu.europeana.metis.exception.BadContentException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

/**
 * DAO for {@link DepublishRecordId} objects.
 */
@Repository
public class DepublishRecordIdDao {

  private final MorphiaDatastoreProvider morphiaDatastoreProvider;
  private final long maxDepublishRecordIdsPerDataset;
  private final int pageSize;

  /**
   * Constructor.
   *
   * @param morphiaDatastoreProvider The datastore provider.
   * @param maxDepublishRecordIdsPerDataset The maximum number of records we allow per dataset.
   */
  public DepublishRecordIdDao(MorphiaDatastoreProvider morphiaDatastoreProvider,
      long maxDepublishRecordIdsPerDataset) {
    this(morphiaDatastoreProvider, maxDepublishRecordIdsPerDataset,
        RequestLimits.DEPUBLISHED_RECORDS_PER_REQUEST.getLimit());
  }

  /**
   * Constructor allowing setting the page size
   *
   * @param morphiaDatastoreProvider The datastore provider.
   * @param maxDepublishRecordIdsPerDataset The maximum number of records we allow per dataset.
   * @param pageSize The page size for list requests.
   */
  DepublishRecordIdDao(MorphiaDatastoreProvider morphiaDatastoreProvider,
      long maxDepublishRecordIdsPerDataset, int pageSize) {
    this.morphiaDatastoreProvider = morphiaDatastoreProvider;
    this.maxDepublishRecordIdsPerDataset = maxDepublishRecordIdsPerDataset;
    this.pageSize = pageSize;
  }

  private Set<String> getNonExistingRecordIds(String datasetId, Set<String> recordIds) {
    return retryableExternalRequestForNetworkExceptions(() -> {

      // Create query for existing records in list. Only return record IDs.
      final Query<DepublishRecordId> query = morphiaDatastoreProvider.getDatastore()
          .find(DepublishRecordId.class);
      query.filter(Filters.eq(DepublishRecordId.DATASET_ID_FIELD, datasetId));
      query.filter(Filters.in(DepublishRecordId.RECORD_ID_FIELD, recordIds));

      // Execute query and find existing record IDs.
      final FindOptions findOptions = new FindOptions();
      findOptions.projection().include(DepublishRecordId.RECORD_ID_FIELD);
      findOptions.projection().exclude(DepublishRecordId.ID_FIELD);
      final Set<String> existing;
      existing = getListOfQueryRetryable(query, findOptions).stream()
          .map(DepublishRecordId::getRecordId)
          .collect(Collectors.toSet());

      // Return the other ones: the record IDs not found in the database.
      return recordIds.stream().filter(recordId -> !existing.contains(recordId))
          .collect(Collectors.toSet());
    });
  }

  /**
   * Add depublished records to persistence. This method checks whether the depublished record
   * already exists, and if so, doesn't add it again. All new records (but not the existing ones)
   * will have the default depublication status ({@link DepublicationStatus#PENDING_DEPUBLICATION})
   * and no depublication date.
   *
   * @param datasetId The dataset to which the records belong.
   * @param candidateRecordIds The IDs of the depublish record ids to add.
   * @return How many of the passed records were in fact added. This counter is not thread-safe: if
   * multiple threads try to add the same records, their combined counters may overrepresent the
   * number of records that were actually added.
   * @throws BadContentException In case adding the records would violate the maximum number of
   * depublished records that each dataset can have.
   */
  public int createRecordIdsToBeDepublished(String datasetId, Set<String> candidateRecordIds)
      throws BadContentException {

    // Check list size: if this is too large we can throw exception regardless of what's in the database.
    if (candidateRecordIds.size() > maxDepublishRecordIdsPerDataset) {
      throw new BadContentException(
          "Can't add these records: this would violate the maximum number of records per dataset.");
    }

    // Get the nonexisting records: those we actually add.
    final Set<String> recordIdsToAdd = getNonExistingRecordIds(datasetId, candidateRecordIds);

    // Count: determine whether we are not above our maximum.
    final long existingCount = countDepublishRecordIdsForDataset(datasetId);
    if (existingCount + recordIdsToAdd.size() > maxDepublishRecordIdsPerDataset) {
      throw new BadContentException(
          "Can't add these records: this would violate the maximum number of records per dataset.");
    }

    // Add the records and we're done.
    addRecords(recordIdsToAdd, datasetId, DepublicationStatus.PENDING_DEPUBLICATION, null);
    return recordIdsToAdd.size();
  }

  private void addRecords(Set<String> recordIdsToAdd, String datasetId,
      DepublicationStatus depublicationStatus, Instant depublicationDate) {
    final List<DepublishRecordId> objectsToAdd = recordIdsToAdd.stream().map(recordId -> {
      final DepublishRecordId depublishRecordId = new DepublishRecordId();
      depublishRecordId.setId(new ObjectId());
      depublishRecordId.setDatasetId(datasetId);
      depublishRecordId.setRecordId(recordId);
      depublishRecordId.setDepublicationStatus(depublicationStatus);
      depublishRecordId.setDepublicationDate(depublicationDate);
      return depublishRecordId;
    }).collect(Collectors.toList());
    retryableExternalRequestForNetworkExceptions(() -> {
      morphiaDatastoreProvider.getDatastore().save(objectsToAdd);
      return Optional.empty();
    });
  }

  /**
   * Deletes a list of record ids from the database. Only record ids that are in a {@link
   * DepublicationStatus#PENDING_DEPUBLICATION} state will be removed.
   *
   * @param datasetId The dataset to which the depublish record ids belong.
   * @param recordIds The depublish record ids to be removed
   * @return The number or record ids that were removed.
   * @throws BadContentException In case adding the records would violate the maximum number of
   * depublished records that each dataset can have.
   */
  public Long deletePendingRecordIds(String datasetId, Set<String> recordIds)
      throws BadContentException {

    // Check list size: if this is too large we can throw exception regardless of what's in the database.
    if (recordIds.size() > maxDepublishRecordIdsPerDataset) {
      throw new BadContentException(
          "Can't remove these records: this would violate the maximum number of records per dataset.");
    }

    final Query<DepublishRecordId> query = morphiaDatastoreProvider.getDatastore()
        .find(DepublishRecordId.class);
    query.filter(Filters.eq(DepublishRecordId.DATASET_ID_FIELD, datasetId));
    query.filter(Filters.in(DepublishRecordId.RECORD_ID_FIELD, recordIds));
    query.filter(Filters.eq(DepublishRecordId.DEPUBLICATION_STATUS_FIELD,
        DepublicationStatus.PENDING_DEPUBLICATION));

    return retryableExternalRequestForNetworkExceptions(
        () -> query.delete(new DeleteOptions().multi(true)).getDeletedCount());
  }

  /**
   * Counts how many records we have for a given dataset.
   *
   * @param datasetId The ID of the dataset to count for.
   * @return The number of records for the given dataset.
   */
  private long countDepublishRecordIdsForDataset(String datasetId) {
    return retryableExternalRequestForNetworkExceptions(
        () -> morphiaDatastoreProvider.getDatastore().find(DepublishRecordId.class)
            .filter(Filters.eq(DepublishRecordId.DATASET_ID_FIELD, datasetId)).count());
  }

  /**
   * Counts how many records we have for a given dataset that have the status {@link
   * DepublicationStatus#DEPUBLISHED}.
   *
   * @param datasetId The ID of the dataset to count for.
   * @return The number of records.
   */
  public long countSuccessfullyDepublishedRecordIdsForDataset(String datasetId) {
    return retryableExternalRequestForNetworkExceptions(
        () -> morphiaDatastoreProvider.getDatastore().find(DepublishRecordId.class)
            .filter(Filters.eq(DepublishRecordId.DATASET_ID_FIELD, datasetId)).filter(Filters
                .eq(DepublishRecordId.DEPUBLICATION_STATUS_FIELD, DepublicationStatus.DEPUBLISHED))
            .count());
  }

  /**
   * Get a list of depublish records for a given dataset.
   * <p>Ids are retrieved regardless of their status</p>
   *
   * @param datasetId The dataset for which to retrieve the records. Cannot be null.
   * @param page The page (batch) number, starting at 0. Cannot be null.
   * @param sortField The sorting field. Cannot be null.
   * @param sortDirection The sorting direction. Cannot be null.
   * @param searchQuery Search query for the record ID. Can be null.
   * @return A (possibly empty) list of depublish record ids.
   */
  public List<DepublishRecordIdView> getDepublishRecordIds(String datasetId, int page,
      DepublishRecordIdSortField sortField, SortDirection sortDirection,
      String searchQuery) {
    final Query<DepublishRecordId> query = prepareQueryForDepublishRecordIds(datasetId, null,
        searchQuery);

    // Compute pagination
    final FindOptions findOptions = new FindOptions()
        .sort(sortDirection.createSort(sortField.getDatabaseField())).skip(page * pageSize)
        .limit(pageSize);

    // Execute query with correct pagination
    final List<DepublishRecordId> result = getListOfQueryRetryable(query, findOptions);

    // Convert result to right object.
    return result.stream().map(DepublishRecordIdView::new).collect(Collectors.toList());
  }

  /**
   * Get all depublished records for a given dataset.
   * <p>This method is to be used with caution since it doesn't have a limit on the returned items.
   * It is mainly used to minimize, internal to the application, database requests. Ids are returned
   * based on the provided status filter parameter</p>
   *
   * @param datasetId The dataset for which to retrieve the records. Cannot be null.
   * @param sortField The sorting field. Cannot be null.
   * @param sortDirection The sorting direction. Cannot be null.
   * @param depublicationStatus The depublication status of the records. Can be null.
   * @return A (possibly empty) list of depublish record ids.
   * @throws BadContentException In case the records would violate the maximum number of depublished
   * records that each dataset can have.
   */
  public Set<String> getAllDepublishRecordIdsWithStatus(String datasetId,
      DepublishRecordIdSortField sortField, SortDirection sortDirection,
      DepublicationStatus depublicationStatus) throws BadContentException {
    return getAllDepublishRecordIdsWithStatus(datasetId, sortField, sortDirection,
        depublicationStatus,
        Collections.emptySet());
  }


  /**
   * Get all depublished records for a given dataset.
   * <p>This method is to be used with caution since it doesn't have a limit on the returned items.
   * It is mainly used to minimize, internal to the application, database requests. Ids are returned
   * based on the provided status filter parameter</p>
   *
   * @param datasetId The dataset for which to retrieve the records. Cannot be null.
   * @param sortField The sorting field. Cannot be null.
   * @param sortDirection The sorting direction. Cannot be null.
   * @param depublicationStatus The depublication status of the records. Can be null.
   * @param recordIds The record ids provided, that are to be checked upon. Can be null/empty
   * @return A (possibly empty) list of depublish record ids.
   * @throws BadContentException In case the records would violate the maximum number of depublished
   * records that each dataset can have.
   */
  public Set<String> getAllDepublishRecordIdsWithStatus(String datasetId,
      DepublishRecordIdSortField sortField, SortDirection sortDirection,
      DepublicationStatus depublicationStatus, Set<String> recordIds) throws BadContentException {
    // Check list size: if this is too large we can throw exception regardless of what's in the database.
    if (!CollectionUtils.isEmpty(recordIds) && (recordIds.size()
        > maxDepublishRecordIdsPerDataset)) {
      throw new BadContentException(
          "Can't remove these records: this would violate the maximum number of records per dataset.");
    }

    final Query<DepublishRecordId> query = prepareQueryForDepublishRecordIds(datasetId,
        depublicationStatus, null);
    final FindOptions findOptions = new FindOptions();
    findOptions.projection().include(DepublishRecordId.RECORD_ID_FIELD);
    findOptions.projection().exclude(DepublishRecordId.ID_FIELD);
    findOptions.sort(sortDirection.createSort(sortField.getDatabaseField()));

    if (!CollectionUtils.isEmpty(recordIds)) {
      query.filter(Filters.in(DepublishRecordId.RECORD_ID_FIELD, recordIds));
    }

    // Execute query with correct pagination
    final List<DepublishRecordId> result = getListOfQueryRetryable(query, findOptions);

    // Convert result to right object.
    return result.stream().map(DepublishRecordId::getRecordId).collect(Collectors.toSet());
  }

  private Query<DepublishRecordId> prepareQueryForDepublishRecordIds(String datasetId,
      DepublicationStatus depublicationStatus, String searchQuery) {
    // Create query.
    final Query<DepublishRecordId> query = morphiaDatastoreProvider.getDatastore()
        .find(DepublishRecordId.class);
    query.filter(Filters.eq(DepublishRecordId.DATASET_ID_FIELD, datasetId));
    if (Objects.nonNull(depublicationStatus)) {
      query.filter(Filters.eq(DepublishRecordId.DEPUBLICATION_STATUS_FIELD, depublicationStatus));
    }
    if (StringUtils.isNotBlank(searchQuery)) {
      query.filter(
          Filters.regex(DepublishRecordId.RECORD_ID_FIELD).pattern(Pattern.compile(searchQuery)));
    }

    return query;
  }

  /**
   * This method marks record ids with the provided {@link DepublicationStatus} and {@link Date}
   * where appropriate.
   * <p>A {@link DepublicationStatus#PENDING_DEPUBLICATION} unsets the depublication date</p>
   * <p>A {@link DepublicationStatus#DEPUBLISHED} sets the depublication date with the one
   * provided</p>
   *
   * @param datasetId the dataset for which to do this. Cannot be null
   * @param recordIds the records for which to set this. Can be null or empty, in which case the
   * operation will be performed on all records. If it is not empty, a new record will be created if
   * a record with the given record ID is not already present.
   * @param depublicationStatus the depublication status. Cannot be null
   * @param depublicationDate the depublication date. Can be null only if depublicationStatus is
   * {@link DepublicationStatus#PENDING_DEPUBLICATION}
   */
  public void markRecordIdsWithDepublicationStatus(String datasetId, Set<String> recordIds,
      DepublicationStatus depublicationStatus, @Nullable Date depublicationDate) {

    // Check correctness of parameters
    if (Objects.isNull(depublicationStatus) || StringUtils.isBlank(datasetId)) {
      throw new IllegalArgumentException(
          "DepublicationStatus cannot be null and datasetId cannot be empty");
    } else if (depublicationStatus == DepublicationStatus.DEPUBLISHED && Objects
        .isNull(depublicationDate)) {
      throw new IllegalArgumentException(String.format(
          "DepublicationDate cannot be null if depublicationStatus == %s ",
          DepublicationStatus.DEPUBLISHED.name()));
    }

    // If we have a specific record list, make sure that missing records are added.
    final Set<String> recordIdsToUpdate; // null if and only if we need to update all records
    if (CollectionUtils.isEmpty(recordIds)) {
      recordIdsToUpdate = null;
    } else {

      // Add the records that are missing.
      final Set<String> recordIdsToAdd = getNonExistingRecordIds(datasetId, recordIds);
      final Instant depublicationInstant = Optional.ofNullable(depublicationDate)
          .filter(date -> depublicationStatus != DepublicationStatus.PENDING_DEPUBLICATION)
          .map(Date::toInstant).orElse(null);
      addRecords(recordIdsToAdd, datasetId, depublicationStatus, depublicationInstant);

      // Compute the records to update - if there are none, we're done.
      recordIdsToUpdate = new HashSet<>(recordIds);
      recordIdsToUpdate.removeAll(recordIdsToAdd);
      if (recordIdsToUpdate.isEmpty()) {
        return;
      }
    }

    // Create query.
    final Query<DepublishRecordId> query = morphiaDatastoreProvider.getDatastore()
        .find(DepublishRecordId.class);
    query.filter(Filters.eq(DepublishRecordId.DATASET_ID_FIELD, datasetId));
    if (recordIdsToUpdate != null) {
      query.filter(Filters.in(DepublishRecordId.RECORD_ID_FIELD, recordIdsToUpdate));
    }

    // Define the update operations.
    final UpdateOperator firstUpdateOperator = UpdateOperators
        .set(DepublishRecordId.DEPUBLICATION_STATUS_FIELD, depublicationStatus);
    final ArrayList<UpdateOperator> extraUpdateOperators = new ArrayList<>();
    if (depublicationStatus == DepublicationStatus.PENDING_DEPUBLICATION) {
      extraUpdateOperators.add(UpdateOperators.unset(DepublishRecordId.DEPUBLICATION_DATE_FIELD));
    } else {
      extraUpdateOperators
          .add(UpdateOperators.set(DepublishRecordId.DEPUBLICATION_DATE_FIELD, depublicationDate));
    }

    // Apply the operations.
    retryableExternalRequestForNetworkExceptions(
        () -> query.update(firstUpdateOperator, extraUpdateOperators.toArray(UpdateOperator[]::new))
            .execute(new UpdateOptions().multi(true)));
  }

  /**
   * Returns the page size imposed by this DAO.
   *
   * @return The page size.
   */
  public int getPageSize() {
    return pageSize;
  }
}
