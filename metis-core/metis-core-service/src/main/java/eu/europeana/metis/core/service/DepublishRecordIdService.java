package eu.europeana.metis.core.service;

import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.core.dao.DepublishRecordIdDao;
import eu.europeana.metis.core.dataset.DatasetExecutionInformation;
import eu.europeana.metis.core.dataset.DatasetExecutionInformation.PublicationStatus;
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
import org.apache.commons.lang3.StringUtils;
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
      result = Optional.of(validateNonEmptyRecordId(datasetId, recordIdTrimmed));
    }
    return result;
  }

  private String validateNonEmptyRecordId(String datasetId, String recordIdTrimmed)
          throws BadContentException {

    // Check if it is a valid URL. This also checks for spaces.
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
    return lastSegment;
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

  /**
   * Creates a workflow with one plugin {@link eu.europeana.metis.core.workflow.plugins.DepublishPlugin}.
   * <p>The plugin will contain {@link DepublishPluginMetadata} that contain information of whether
   * the depublication
   * is for an entire dataset or for individual records ids. Those ids are either provided or all of
   * the ids, previously populated, from the database will be used. Only ids in {@link
   * eu.europeana.metis.core.dataset.DepublishRecordId.DepublicationStatus#PENDING_DEPUBLICATION}
   * will be attempted for depublication.</p>
   *
   * @param metisUser The user performing this operation. Cannot be null.
   * @param datasetId The ID of the dataset for which to retrieve the records. Cannot be null.
   * @param datasetDepublish true for dataset depublication, false for record depublication
   * @param priority the priority of the execution in case the system gets overloaded, 0 lowest, 10
   * highest
   * @param recordIdsInSeparateLines the specific pending record ids to depublish. Only record ids
   * that are marked as {@link eu.europeana.metis.core.dataset.DepublishRecordId.DepublicationStatus#PENDING_DEPUBLICATION}
   * in the database will be attempted for depublication.
   * @return the WorkflowExecution object that was generated
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link BadContentException} if the workflow is empty or no plugin enabled</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoDatasetFoundException} if the dataset
   * identifier provided does not exist</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authenticated or authorized to perform this operation</li>
   * <li>{@link eu.europeana.metis.exception.ExternalTaskException} if there was an exception when
   * contacting the external resource(ECloud)</li>
   * <li>{@link eu.europeana.metis.core.exceptions.PluginExecutionNotAllowed} if the execution of
   * the first plugin was not allowed, because a valid source plugin could not be found</li>
   * <li>{@link eu.europeana.metis.core.exceptions.WorkflowExecutionAlreadyExistsException} if a
   * workflow execution for the generated execution identifier already exists, almost impossible to
   * happen since ids are UUIDs</li>
   * </ul>
   */
  public WorkflowExecution createAndAddInQueueDepublishWorkflowExecution(MetisUser metisUser,
      String datasetId, boolean datasetDepublish, int priority, String recordIdsInSeparateLines)
      throws GenericMetisException {
    // Authorize.
    authorizer.authorizeReadExistingDatasetById(metisUser, datasetId);

    //Prepare depublish workflow, do not save in the database. Only create workflow execution
    final Workflow workflow = new Workflow();
    workflow.setDatasetId(datasetId);
    final DepublishPluginMetadata depublishPluginMetadata = new DepublishPluginMetadata();
    depublishPluginMetadata.setEnabled(true);
    depublishPluginMetadata.setDatasetDepublish(datasetDepublish);
    if (StringUtils.isNotBlank(recordIdsInSeparateLines)) {
      // Check and normalize the record IDs.(Just in case)
      final Set<String> normalizedRecordIds = normalizeRecordIds(datasetId,
          recordIdsInSeparateLines);
      depublishPluginMetadata.setRecordIdsToDepublish(normalizedRecordIds);
    }
    workflow.setMetisPluginsMetadata(Collections.singletonList(depublishPluginMetadata));

    return orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(metisUser, datasetId, workflow, null, priority);
  }

  /**
   * Determines whether a depublication can be triggered at this point. This is the case lf:
   * <ul>
   *   <li>
   *     No workflow is currently in progress, and
   *   </li>
   *   <li>
   *     The dataset has the status 'published' (as opposed to 'depublished' or 'neither'), and
   *   </li>
   *   <li>
   *     The records in the dataset are ready for viewing.
   *   </li>
   * </ul>
   * @param metisUser The user performing this operation. Cannot be null.
   * @param datasetId The ID of the dataset for which to retrieve the records. Cannot be null.
   * @return Whether a depublication can be triggered.
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link eu.europeana.metis.core.exceptions.NoDatasetFoundException} if the dataset
   * identifier provided does not exist</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authenticated or authorized to perform this operation</li>
   * </ul>
   */
  public boolean canTriggerDepublication(MetisUser metisUser, String datasetId)
          throws GenericMetisException {

    // Authorize.
    authorizer.authorizeReadExistingDatasetById(metisUser, datasetId);

    // If a workflow execution is currently in progress, we can't depublish.
    if (orchestratorService.getRunningOrInQueueExecution(datasetId) != null) {
      return false;
    }

    // If a (re-)index took place recently, or the status is not published, we can't depublish.
    final DatasetExecutionInformation executionInformation = orchestratorService
            .getDatasetExecutionInformation(datasetId);
    return executionInformation.getPublicationStatus() == PublicationStatus.PUBLISHED &&
            executionInformation.isLastPublishedRecordsReadyForViewing();
  }
}
