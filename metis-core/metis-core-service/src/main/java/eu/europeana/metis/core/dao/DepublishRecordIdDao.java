package eu.europeana.metis.core.dao;

import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;
import dev.morphia.query.internal.MorphiaCursor;
import eu.europeana.metis.core.dataset.DepublishRecordId;
import eu.europeana.metis.core.dataset.DepublishRecordId.DepublicationStatus;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.rest.DepublishRecordIdView;
import eu.europeana.metis.core.rest.RequestLimits;
import eu.europeana.metis.core.util.DepublishRecordIdSortField;
import eu.europeana.metis.core.util.SortDirection;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.utils.ExternalRequestUtil;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

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
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(() -> {

      // Create query for existing records in list. Only return record IDs.
      final Query<DepublishRecordId> query = morphiaDatastoreProvider.getDatastore()
          .createQuery(DepublishRecordId.class);
      query.field(DepublishRecordId.DATASET_ID_FIELD).equal(datasetId);
      query.field(DepublishRecordId.RECORD_ID_FIELD).in(recordIds);
      query.project(DepublishRecordId.RECORD_ID_FIELD, true);
      query.project(DepublishRecordId.ID_FIELD, false);

      // Execute query and find existing record IDs.
      final Set<String> existing = query.find().toList().stream()
          .map(DepublishRecordId::getRecordId).collect(Collectors.toSet());

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

    // Add the records
    final List<DepublishRecordId> objectsToAdd = recordIdsToAdd.stream().map(recordId -> {
      final DepublishRecordId depublishRecordId = new DepublishRecordId();
      depublishRecordId.setDatasetId(datasetId);
      depublishRecordId.setRecordId(recordId);
      depublishRecordId.setDepublicationStatus(DepublicationStatus.PENDING_DEPUBLICATION);
      return depublishRecordId;
    }).collect(Collectors.toList());
    ExternalRequestUtil.retryableExternalRequestConnectionReset(() -> {
      morphiaDatastoreProvider.getDatastore().save(objectsToAdd);
      return Optional.empty();
    });

    // Done
    return recordIdsToAdd.size();
  }

  /**
   * Deletes a list of record ids from the database. Only record ids that are in a {@link
   * DepublicationStatus#PENDING_DEPUBLICATION} state will be removed.
   *
   * @param datasetId The dataset to which the depublish record ids belong.
   * @param recordIds The depublish record ids to be removed
   * @throws BadContentException In case adding the records would violate the maximum number of
   * depublished records that each dataset can have.
   * @return The number or record ids that were removed.
   */
  public Integer deletePendingRecordIds(String datasetId, Set<String> recordIds)
      throws BadContentException {

    // Check list size: if this is too large we can throw exception regardless of what's in the database.
    if (recordIds.size() > maxDepublishRecordIdsPerDataset) {
      throw new BadContentException(
          "Can't remove these records: this would violate the maximum number of records per dataset.");
    }

    final Query<DepublishRecordId> query = morphiaDatastoreProvider.getDatastore()
        .createQuery(DepublishRecordId.class);
    query.field(DepublishRecordId.DATASET_ID_FIELD).equal(datasetId);
    query.field(DepublishRecordId.RECORD_ID_FIELD).in(recordIds);
    query.field(DepublishRecordId.DEPUBLICATION_STATUS_FIELD)
        .equal(DepublicationStatus.PENDING_DEPUBLICATION);

    return ExternalRequestUtil.retryableExternalRequestConnectionReset(() -> {
      return morphiaDatastoreProvider.getDatastore().delete(query).getN();
    });
  }

  /**
   * Counts how many records we have for a given dataset.
   *
   * @param datasetId The ID of the dataset to count for.
   * @return The number of records for the given dataset.
   */
  private long countDepublishRecordIdsForDataset(String datasetId) {
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(
        () -> morphiaDatastoreProvider.getDatastore().createQuery(DepublishRecordId.class)
            .field(DepublishRecordId.DATASET_ID_FIELD).equal(datasetId).count());
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
    final Query<DepublishRecordId> query = prepareQueryForDepublishRecordIds(datasetId, sortField,
        sortDirection, null, searchQuery);

    // Compute pagination
    final int skip = page * pageSize;
    final int limit = skip + pageSize;
    final FindOptions findOptions = new FindOptions().skip(skip).limit(limit);

    // Execute query with correct pagination
    final List<DepublishRecordId> result = getListOfQuery(query, findOptions);

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
   */
  public Set<String> getAllDepublishRecordIdsWithStatus(String datasetId,
      DepublishRecordIdSortField sortField, SortDirection sortDirection,
      DepublicationStatus depublicationStatus) {
    final Query<DepublishRecordId> query = prepareQueryForDepublishRecordIds(datasetId, sortField,
        sortDirection, depublicationStatus, null);
    query.project(DepublishRecordId.RECORD_ID_FIELD, true);
    query.project(DepublishRecordId.ID_FIELD, false);

    // Execute query with correct pagination
    final List<DepublishRecordId> result = getListOfQuery(query, null);

    // Convert result to right object.
    return result.stream().map(DepublishRecordId::getRecordId).collect(Collectors.toSet());
  }

  private Query<DepublishRecordId> prepareQueryForDepublishRecordIds(String datasetId,
      DepublishRecordIdSortField sortField, SortDirection sortDirection,
      DepublicationStatus depublicationStatus, String searchQuery) {
    // Create query.
    final Query<DepublishRecordId> query = morphiaDatastoreProvider.getDatastore()
        .createQuery(DepublishRecordId.class);
    query.field(DepublishRecordId.DATASET_ID_FIELD).equal(datasetId);
    if (Objects.nonNull(depublicationStatus)) {
      query.field(DepublishRecordId.DEPUBLICATION_STATUS_FIELD).equal(depublicationStatus);
    }
    if (StringUtils.isNotBlank(searchQuery)) {
      query.field(DepublishRecordId.RECORD_ID_FIELD).contains(searchQuery);
    }

    // Set ordering
    query.order(sortDirection.createSort(sortField.getDatabaseField()));
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
   * @param depublicationStatus the depublication status. Cannot be null
   * @param depublicationDate the depublication date. Can be null only if depublicationStatus is
   * {@link DepublicationStatus#PENDING_DEPUBLICATION}
   */
  public void markRecordIdsWithDepublicationStatus(String datasetId,
      DepublicationStatus depublicationStatus, @Nullable Date depublicationDate) {

    //Check correctness of parameters
    if (Objects.isNull(depublicationStatus) || StringUtils.isBlank(datasetId)) {
      throw new IllegalArgumentException(
          "DepublicationStatus cannot be null and datasetId cannot be empty");
    } else if (depublicationStatus == DepublicationStatus.DEPUBLISHED && Objects
        .isNull(depublicationDate)) {
      throw new IllegalArgumentException(String.format(
          "DepublicationDate cannot be null if depublicationStatus == %s ",
          DepublicationStatus.DEPUBLISHED.name()));
    }

    // Create query.
    final Query<DepublishRecordId> query = morphiaDatastoreProvider.getDatastore()
        .createQuery(DepublishRecordId.class);
    query.field(DepublishRecordId.DATASET_ID_FIELD).equal(datasetId);

    // Define the update operations.
    final UpdateOperations<DepublishRecordId> updateOperations = morphiaDatastoreProvider
        .getDatastore().createUpdateOperations(DepublishRecordId.class);
    updateOperations.set(DepublishRecordId.DEPUBLICATION_STATUS_FIELD, depublicationStatus);
    if (depublicationStatus == DepublicationStatus.PENDING_DEPUBLICATION) {
      updateOperations.unset(DepublishRecordId.DEPUBLICATION_DATE_FIELD);
    } else {
      updateOperations.set(DepublishRecordId.DEPUBLICATION_DATE_FIELD, depublicationDate);
    }

    // Apply the operations.
    ExternalRequestUtil.retryableExternalRequestConnectionReset(
        () -> morphiaDatastoreProvider.getDatastore().update(query, updateOperations));
  }

  /**
   * Returns the page size imposed by this DAO.
   *
   * @return The page size.
   */
  public int getPageSize() {
    return pageSize;
  }

  private <T> List<T> getListOfQuery(Query<T> query, FindOptions findOptions) {
    return ExternalRequestUtil.retryableExternalRequestConnectionReset(() -> {
      final BiFunction<Query<T>, FindOptions, MorphiaCursor<T>> queryFunction = (querySupplied, findOptionsSupplied) -> {
        final MorphiaCursor<T> morphiaCursor;
        if (findOptionsSupplied == null) {
          morphiaCursor = querySupplied.find();
        } else {
          morphiaCursor = querySupplied.find(findOptionsSupplied);
        }
        return morphiaCursor;
      };
      try (MorphiaCursor<T> cursor = queryFunction.apply(query, findOptions)) {
        return cursor.toList();
      }
    });
  }
}
