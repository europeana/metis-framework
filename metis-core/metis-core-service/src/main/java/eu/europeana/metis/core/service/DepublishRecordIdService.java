package eu.europeana.metis.core.service;

import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.core.dao.DepublishRecordIdDao;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.rest.DepublishRecordIdView;
import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.util.DepublishRecordIdSortField;
import eu.europeana.metis.core.util.SortDirection;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.DepublishPluginMetadata;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.GenericMetisException;
import eu.europeana.metis.exception.UserUnauthorizedException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service object for all operations concerning depublished records. The functionality in this class
 * is checked for user authentication.
 */
@Service
public class DepublishRecordIdService {

  private static final Pattern LINE_SEPARATION_PATTERN = Pattern.compile("\\R");
  private final Authorizer authorizer;
  private final OrchestratorService orchestratorService;
  private final DepublishRecordIdDao depublishRecordIdDao;

  private static final Pattern INVALID_CHAR_IN_RECORD_ID = Pattern.compile("[^a-zA-Z0-9_]");

  /**
   * Constructor.
   *
   * @param authorizer The authorizer for checking permissions.
   * @param orchestratorService The orchestrator service
   * @param depublishRecordIdDao The DAO for depublished records.
   */
  @Autowired
  public DepublishRecordIdService(Authorizer authorizer, OrchestratorService orchestratorService,
      DepublishRecordIdDao depublishRecordIdDao) {
    this.authorizer = authorizer;
    this.orchestratorService = orchestratorService;
    this.depublishRecordIdDao = depublishRecordIdDao;
  }

  /**
   * This method checks/validates and normalizes incoming depublished record IDs for persistence.
   *
   * @param datasetId The dataset ID to which the depublished record belongs.
   * @param recordId The unchecked and non-normalized record ID.
   * @return The checked and normalized record ID. Or empty Optional if the incoming ID is empty.
   * @throws BadContentException In case the incoming record ID does not validate.
   */
  Optional<String> checkAndNormalizeRecordId(String datasetId, String recordId)
      throws BadContentException {

    // Trim and check that string is not empty. We allow empty record IDs, we return null.
    final String recordIdTrimmed = recordId.trim();
    final Optional<String> result;
    if (recordIdTrimmed.isEmpty()) {
      result = Optional.empty();
    } else {
      result = validateNonEmptyRecordId(datasetId, recordIdTrimmed);
    }
    return result;
  }

  private Optional<String> validateNonEmptyRecordId(String datasetId, String recordIdTrimmed)
      throws BadContentException {
    Optional<String> result;// Check if it is a valid URL. This also checks for spaces.
    try {
      new URI(recordIdTrimmed);
    } catch (URISyntaxException e) {
      throw new BadContentException("Invalid record ID (is not a valid URI): " + recordIdTrimmed,
          e);
    }

    // Split in segments based on the slash - don't discard empty segments at the end.
    final String[] segments = recordIdTrimmed.split("/", -1);
    final String lastSegment = segments[segments.length - 1];
    final String penultimateSegment = segments.length > 1 ? segments[segments.length - 2] : "";

    // Check last segment: cannot be empty.
    if (lastSegment.isEmpty()) {
      throw new BadContentException("Invalid record ID (ends with '/'): " + recordIdTrimmed);
    }

    // Check last segment: cannot contain invalid characters
    if (INVALID_CHAR_IN_RECORD_ID.matcher(lastSegment).find()) {
      throw new BadContentException(
          "Invalid record ID (contains invalid characters): " + lastSegment);
    }

    // Check penultimate segment: if it is empty, it must be because it is the start of the ID.
    if (penultimateSegment.isEmpty() && segments.length > 2) {
      throw new BadContentException(
          "Invalid record ID (dataset ID seems to be missing): " + recordIdTrimmed);
    }

    // Check penultimate segment: if it is not empty, it must be equal to the dataset ID.
    if (!penultimateSegment.isEmpty() && !penultimateSegment.equals(datasetId)) {
      throw new BadContentException(
          "Invalid record ID (doesn't seem to belong to the correct dataset): "
              + recordIdTrimmed);
    }

    // Return the last segment (the record ID without the dataset ID).
    result = Optional.of(lastSegment);
    return result;
  }

  /**
   * Adds a list of record ids to be depublished for the dataset.
   *
   * @param metisUser The user performing this operation.
   * @param datasetId The ID of the dataset to which the depublished records belong.
   * @param recordIdsInSeparateLines The string containing the record IDs in separate lines.
   * @return How many of the passed records were in fact added. This counter is not thread-safe: if
   * multiple threads try to add the same records, their combined counters may overrepresent the
   * number of records that were actually added.
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoDatasetFoundException} if the dataset for datasetId was not found.</li>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized</li>
   * <li>{@link BadContentException} if some content or the operation were invalid</li>
   * </ul>
   */
  public int addRecordIdsToBeDepublished(MetisUser metisUser, String datasetId,
      String recordIdsInSeparateLines) throws GenericMetisException {

    // Authorize.
    authorizer.authorizeWriteExistingDatasetById(metisUser, datasetId);
    // Check and normalize the record IDs.
    final Set<String> normalizedRecordIds = normalizeRecordIds(datasetId, recordIdsInSeparateLines);

    // Add the records.
    return depublishRecordIdDao.createRecordIdsToBeDepublished(datasetId, normalizedRecordIds);
  }

  /**
   * Deletes a list of record ids from the database. Only record ids that are in a {@link
   * eu.europeana.metis.core.dataset.DepublishRecordId.DepublicationStatus#PENDING_DEPUBLICATION}
   * state will be removed.
   *
   * @param metisUser The user performing this operation.
   * @param datasetId The ID of the dataset to which the depublish record ids belong.
   * @param recordIdsInSeparateLines The string containing the record IDs in separate lines.
   * @return The number or record ids that were removed.
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoDatasetFoundException} if the dataset for datasetId was not found.</li>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized</li>
   * <li>{@link BadContentException} if some content or the operation were invalid</li>
   * </ul>
   */
  public Integer deletePendingRecordIds(MetisUser metisUser, String datasetId,
      String recordIdsInSeparateLines) throws GenericMetisException {

    // Authorize.
    authorizer.authorizeWriteExistingDatasetById(metisUser, datasetId);
    // Check and normalize the record IDs.(Just in case)
    final Set<String> normalizedRecordIds = normalizeRecordIds(datasetId, recordIdsInSeparateLines);

    return depublishRecordIdDao.deletePendingRecordIds(datasetId, normalizedRecordIds);
  }

  private Set<String> normalizeRecordIds(String datasetId, String recordIdsInSeparateLines)
      throws BadContentException {
    final String[] recordIds = LINE_SEPARATION_PATTERN.split(recordIdsInSeparateLines);
    final Set<String> normalizedRecordIds = new HashSet<>(recordIds.length);
    for (String recordId : recordIds) {
      checkAndNormalizeRecordId(datasetId, recordId).ifPresent(normalizedRecordIds::add);
    }
    return normalizedRecordIds;
  }

  /**
   * Retrieve the list of depublish record ids for a specific dataset.
   * <p>Ids are retrieved regardless of their status</p>
   *
   * @param metisUser The user performing this operation. Cannot be null.
   * @param datasetId The ID of the dataset for which to retrieve the records. Cannot be null.
   * @param page The page to retrieve. Cannot be null.
   * @param sortField The field on which to sort. Cannot be null.
   * @param sortDirection The direction in which to sort. Cannot be null.
   * @param searchQuery Search query for the record ID. Can be null.
   * @return A list of records.
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoDatasetFoundException} if the dataset for datasetId was not found.</li>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized</li>
   * </ul>
   */
  public ResponseListWrapper<DepublishRecordIdView> getDepublishRecordIds(MetisUser metisUser,
      String datasetId, int page, DepublishRecordIdSortField sortField,
      SortDirection sortDirection, String searchQuery) throws GenericMetisException {

    // Authorize.
    authorizer.authorizeReadExistingDatasetById(metisUser, datasetId);

    // Get the page of records
    final List<DepublishRecordIdView> records = depublishRecordIdDao
        .getDepublishRecordIds(datasetId, page, sortField, sortDirection, searchQuery);

    // Compile the result
    final ResponseListWrapper<DepublishRecordIdView> result = new ResponseListWrapper<>();
    result.setResultsAndLastPage(records, depublishRecordIdDao.getPageSize(), page);
    return result;
  }

  public WorkflowExecution createAndAddInQueueDepublishWorkflowExecution(MetisUser metisUser,
      String datasetId,
      boolean datasetDepublish, int priority) throws GenericMetisException {
    // Authorize.
    authorizer.authorizeReadExistingDatasetById(metisUser, datasetId);

    //Prepare depublish workflow, do not save in the database. Only create workflow execution
    final Workflow workflow = new Workflow();
    workflow.setDatasetId(datasetId);
    final DepublishPluginMetadata depublishPluginMetadata = new DepublishPluginMetadata();
    depublishPluginMetadata.setEnabled(true);
    depublishPluginMetadata.setDatasetDepublish(datasetDepublish);
    workflow.setMetisPluginsMetadata(Collections.singletonList(depublishPluginMetadata));

    return orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(metisUser, datasetId, workflow, null, priority);
  }
}
