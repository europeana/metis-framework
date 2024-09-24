package eu.europeana.metis.core.service;

import eu.europeana.metis.authentication.user.MetisUserView;
import eu.europeana.metis.core.dao.DepublishRecordIdDao;
import eu.europeana.metis.core.dataset.DatasetExecutionInformation;
import eu.europeana.metis.core.dataset.DatasetExecutionInformation.PublicationStatus;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.rest.DepublishRecordIdView;
import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.util.DepublishRecordIdSortField;
import eu.europeana.metis.core.common.RecordIdUtils;
import eu.europeana.metis.core.util.SortDirection;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.utils.DepublicationReason;
import eu.europeana.metis.core.workflow.plugins.DepublishPluginMetadata;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.GenericMetisException;
import eu.europeana.metis.exception.UserUnauthorizedException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service object for all operations concerning depublished records. The functionality in this class
 * is checked for user authentication.
 */
@Service
public class DepublishRecordIdService {

  private final Authorizer authorizer;
  private final OrchestratorService orchestratorService;
  private final DepublishRecordIdDao depublishRecordIdDao;

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
   * Adds a list of record ids to be depublished for the dataset.
   *
   * @param metisUserView The user performing this operation.
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
  public int addRecordIdsToBeDepublished(MetisUserView metisUserView, String datasetId,
      String recordIdsInSeparateLines) throws GenericMetisException {

    // Authorize.
    authorizer.authorizeWriteExistingDatasetById(metisUserView, datasetId);

    // Check and normalize the record IDs.
    final Set<String> normalizedRecordIds = checkAndNormalizeRecordIds(datasetId,
        recordIdsInSeparateLines);

    // Add the records.
    return depublishRecordIdDao.createRecordIdsToBeDepublished(datasetId, normalizedRecordIds);
  }

  /**
   * Deletes a list of record ids from the database. Only record ids that are in a {@link
   * eu.europeana.metis.core.dataset.DepublishRecordId.DepublicationStatus#PENDING_DEPUBLICATION}
   * state will be removed.
   *
   * @param metisUserView The user performing this operation.
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
  public Long deletePendingRecordIds(MetisUserView metisUserView, String datasetId,
      String recordIdsInSeparateLines) throws GenericMetisException {

    // Authorize.
    authorizer.authorizeWriteExistingDatasetById(metisUserView, datasetId);

    // Check and normalize the record IDs (Just in case).
    final Set<String> normalizedRecordIds = checkAndNormalizeRecordIds(datasetId,
        recordIdsInSeparateLines);

    // Delete the records.
    return depublishRecordIdDao.deletePendingRecordIds(datasetId, normalizedRecordIds);
  }

  /**
   * Retrieve the list of depublish record ids for a specific dataset.
   * <p>Ids are retrieved regardless of their status</p>
   *
   * @param metisUserView The user performing this operation. Cannot be null.
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
  public ResponseListWrapper<DepublishRecordIdView> getDepublishRecordIds(
      MetisUserView metisUserView,
      String datasetId, int page, DepublishRecordIdSortField sortField,
      SortDirection sortDirection, String searchQuery) throws GenericMetisException {

    // Authorize.
    authorizer.authorizeReadExistingDatasetById(metisUserView, datasetId);

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
   * the depublication is for an entire dataset or for individual records ids. Those ids are either
   * provided or all of the ids, previously populated, from the database will be used. Only ids in
   * {@link eu.europeana.metis.core.dataset.DepublishRecordId.DepublicationStatus#PENDING_DEPUBLICATION}
   * will be attempted for depublication.</p>
   *
   * @param metisUserView The user performing this operation. Cannot be null.
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
  public WorkflowExecution createAndAddInQueueDepublishWorkflowExecution(
      MetisUserView metisUserView,
      String datasetId, boolean datasetDepublish, int priority, String recordIdsInSeparateLines,
      DepublicationReason depublicationReason)
      throws GenericMetisException {
    // Authorize.
    authorizer.authorizeReadExistingDatasetById(metisUserView, datasetId);

    //Prepare depublish workflow, do not save in the database. Only create workflow execution
    final Workflow workflow = new Workflow();
    workflow.setDatasetId(datasetId);
    final DepublishPluginMetadata depublishPluginMetadata = new DepublishPluginMetadata();
    depublishPluginMetadata.setEnabled(true);
    depublishPluginMetadata.setDatasetDepublish(datasetDepublish);
    depublishPluginMetadata.setDepublicationReason(depublicationReason);
    if (StringUtils.isNotBlank(recordIdsInSeparateLines)) {
      // Check and normalize the record IDs (Just in case).
      final Set<String> normalizedRecordIds = checkAndNormalizeRecordIds(datasetId,
          recordIdsInSeparateLines);
      depublishPluginMetadata.setRecordIdsToDepublish(normalizedRecordIds);
    }
    workflow.setMetisPluginsMetadata(Collections.singletonList(depublishPluginMetadata));

    return orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(metisUserView, datasetId, workflow, null, priority);
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
   *
   * @param metisUserView The user performing this operation. Cannot be null.
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
  public boolean canTriggerDepublication(MetisUserView metisUserView, String datasetId)
      throws GenericMetisException {

    // Authorize.
    authorizer.authorizeReadExistingDatasetById(metisUserView, datasetId);

    // Compute the result.
    final boolean result;
    if (orchestratorService.getRunningOrInQueueExecution(datasetId) != null) {
      // If a workflow execution is currently in progress, we can't depublish.
      result = false;
    } else {
      // If a (re-)index took place recently, or the status is not published, we can't depublish.
      final DatasetExecutionInformation executionInformation = orchestratorService
          .getDatasetExecutionInformation(datasetId);
      result = executionInformation.getPublicationStatus() == PublicationStatus.PUBLISHED &&
          executionInformation.isLastPublishedRecordsReadyForViewing();
    }

    // Done
    return result;
  }

  Set<String> checkAndNormalizeRecordIds(String datasetId,
      String recordIdsInSeparateLines) throws BadContentException {
    return RecordIdUtils.checkAndNormalizeRecordIds(datasetId, recordIdsInSeparateLines);
  }

}
