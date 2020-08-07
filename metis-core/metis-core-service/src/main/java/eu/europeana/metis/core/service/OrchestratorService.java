package eu.europeana.metis.core.service;

import com.google.common.collect.Sets;
import eu.europeana.metis.authentication.user.AccountRole;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.core.common.DaoFieldNames;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.PluginWithExecutionId;
import eu.europeana.metis.core.dao.WorkflowDao;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.dao.WorkflowExecutionDao.ExecutionDatasetPair;
import eu.europeana.metis.core.dao.WorkflowExecutionDao.ResultList;
import eu.europeana.metis.core.dao.WorkflowUtils;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetExecutionInformation;
import eu.europeana.metis.core.dataset.DatasetExecutionInformation.PublicationStatus;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowFoundException;
import eu.europeana.metis.core.exceptions.PluginExecutionNotAllowed;
import eu.europeana.metis.core.exceptions.WorkflowAlreadyExistsException;
import eu.europeana.metis.core.exceptions.WorkflowExecutionAlreadyExistsException;
import eu.europeana.metis.core.execution.WorkflowExecutorManager;
import eu.europeana.metis.core.rest.ExecutionHistory;
import eu.europeana.metis.core.rest.ExecutionHistory.Execution;
import eu.europeana.metis.core.rest.PluginsWithDataAvailability;
import eu.europeana.metis.core.rest.PluginsWithDataAvailability.PluginWithDataAvailability;
import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.rest.VersionEvolution;
import eu.europeana.metis.core.rest.VersionEvolution.VersionEvolutionStep;
import eu.europeana.metis.core.rest.execution.overview.ExecutionAndDatasetView;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.DataStatus;
import eu.europeana.metis.core.workflow.plugins.ExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.ExecutionProgress;
import eu.europeana.metis.core.workflow.plugins.MetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.ExternalTaskException;
import eu.europeana.metis.exception.GenericMetisException;
import eu.europeana.metis.exception.UserUnauthorizedException;
import eu.europeana.metis.utils.DateUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * Service class that controls the communication between the different DAOs of the system.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-24
 */
@Service
public class OrchestratorService {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrchestratorService.class);
  //Use with String.format to suffix the datasetId
  private static final String EXECUTION_FOR_DATASETID_SUBMITION_LOCK = "EXECUTION_FOR_DATASETID_SUBMITION_LOCK_%s";

  public static final Set<ExecutablePluginType> HARVEST_TYPES = Sets.immutableEnumSet(
      ExecutablePluginType.HTTP_HARVEST, ExecutablePluginType.OAIPMH_HARVEST);
  public static final Set<ExecutablePluginType> EXECUTABLE_PREVIEW_TYPES = Sets.immutableEnumSet(
      ExecutablePluginType.PREVIEW);
  public static final Set<ExecutablePluginType> EXECUTABLE_PUBLISH_TYPES = Sets.immutableEnumSet(
      ExecutablePluginType.PUBLISH);
  public static final Set<ExecutablePluginType> EXECUTABLE_DEPUBLISH_TYPES = Sets.immutableEnumSet(
      ExecutablePluginType.DEPUBLISH);
  public static final Set<PluginType> PREVIEW_TYPES = Sets.immutableEnumSet(
      PluginType.PREVIEW, PluginType.REINDEX_TO_PREVIEW);
  public static final Set<PluginType> PUBLISH_TYPES = Sets.immutableEnumSet(
      PluginType.PUBLISH, PluginType.REINDEX_TO_PUBLISH);

  private final WorkflowExecutionDao workflowExecutionDao;
  private final WorkflowUtils workflowUtils;
  private final WorkflowDao workflowDao;
  private final DatasetDao datasetDao;
  private final WorkflowExecutorManager workflowExecutorManager;
  private final RedissonClient redissonClient;
  private final Authorizer authorizer;
  private final WorkflowExecutionFactory workflowExecutionFactory;
  private int solrCommitPeriodInMins; // Use getter and setter for this field!

  /**
   * Constructor with all the required parameters
   *
   * @param workflowDao the Dao instance to access the Workflow database
   * @param workflowExecutionDao the Dao instance to access the WorkflowExecution database
   * @param workflowUtils The utilities class providing more functionality on top of DAOs.
   * @param datasetDao the Dao instance to access the Dataset database
   * @param workflowExecutionFactory the orchestratorHelper instance
   * @param workflowExecutorManager the instance that handles the production and consumption of
   * workflowExecutions
   * @param redissonClient the instance of Redisson library that handles distributed locks
   * @param authorizer the authorizer
   */
  @Autowired
  public OrchestratorService(WorkflowExecutionFactory workflowExecutionFactory,
      WorkflowDao workflowDao, WorkflowExecutionDao workflowExecutionDao,
      WorkflowUtils workflowUtils, DatasetDao datasetDao,
      WorkflowExecutorManager workflowExecutorManager, RedissonClient redissonClient,
      Authorizer authorizer) {
    this.workflowExecutionFactory = workflowExecutionFactory;
    this.workflowDao = workflowDao;
    this.workflowExecutionDao = workflowExecutionDao;
    this.workflowUtils = workflowUtils;
    this.datasetDao = datasetDao;
    this.workflowExecutorManager = workflowExecutorManager;
    this.redissonClient = redissonClient;
    this.authorizer = authorizer;
  }

  /**
   * Create a workflow using a datasetId and the {@link Workflow} that contains the requested
   * plugins. If plugins are disabled, they (their settings) are still saved.
   *
   * @param metisUser the user wishing to perform this operation
   * @param datasetId the identifier of the dataset for which the workflow should be created
   * @param workflow the workflow with the plugins requested
   * @param enforcedPredecessorType optional, the plugin type to be used as source data
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link WorkflowAlreadyExistsException} if a workflow for the dataset identifier provided
   * already exists</li>
   * <li>{@link NoDatasetFoundException} if the dataset identifier provided does not exist</li>
   * <li>{@link UserUnauthorizedException} if the user is not authorized to perform this task</li>
   * <li>{@link BadContentException} if the workflow parameters have unexpected values</li>
   * </ul>
   */
  public void createWorkflow(MetisUser metisUser, String datasetId, Workflow workflow,
      ExecutablePluginType enforcedPredecessorType) throws GenericMetisException {

    // Authorize (check dataset existence) and set dataset ID to avoid discrepancy.
    authorizer.authorizeWriteExistingDatasetById(metisUser, datasetId);
    workflow.setDatasetId(datasetId);

    // Check that the workflow does not yet exist.
    if (workflowDao.workflowExistsForDataset(workflow.getDatasetId())) {
      throw new WorkflowAlreadyExistsException(
          String.format("Workflow with datasetId: %s, already exists", workflow.getDatasetId()));
    }

    // Validate the new workflow.
    workflowUtils.validateWorkflowPlugins(workflow, enforcedPredecessorType);

    // Save the workflow.
    workflowDao.create(workflow);
  }

  /**
   * Update an already existent workflow using a datasetId and the {@link Workflow} that contains
   * the requested plugins. If plugins are disabled, they (their settings) are still saved. Any
   * settings in plugins that are not sent in the request are removed.
   *
   * @param metisUser the user wishing to perform this operation
   * @param datasetId the identifier of the dataset for which the workflow should be updated
   * @param workflow the workflow with the plugins requested
   * @param enforcedPredecessorType optional, the plugin type to be used as source data
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoWorkflowFoundException} if a workflow for the dataset identifier provided does
   * not exist</li>
   * <li>{@link NoDatasetFoundException} if the dataset identifier provided does not exist</li>
   * <li>{@link UserUnauthorizedException} if the user is not authorized to perform this task</li>
   * <li>{@link BadContentException} if the workflow parameters have unexpected values</li>
   * </ul>
   */
  public void updateWorkflow(MetisUser metisUser, String datasetId, Workflow workflow,
      ExecutablePluginType enforcedPredecessorType) throws GenericMetisException {

    // Authorize (check dataset existence) and set dataset ID to avoid discrepancy.
    authorizer.authorizeWriteExistingDatasetById(metisUser, datasetId);
    workflow.setDatasetId(datasetId);

    // Get the current workflow in the database. If it doesn't exist, throw exception.
    final Workflow storedWorkflow = workflowDao.getWorkflow(workflow.getDatasetId());
    if (storedWorkflow == null) {
      throw new NoWorkflowFoundException(String.format(
          "Workflow with datasetId: %s, not found", workflow.getDatasetId()));
    }

    // Validate the new workflow.
    workflowUtils.validateWorkflowPlugins(workflow, enforcedPredecessorType);

    // Overwrite the workflow.
    workflow.setId(storedWorkflow.getId());
    workflowDao.update(workflow);
  }

  /**
   * Deletes a workflow.
   *
   * @param metisUser the user wishing to perform this operation
   * @param datasetId the dataset identifier that corresponds to the workflow to be deleted
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoDatasetFoundException} if the dataset identifier provided does not exist</li>
   * <li>{@link UserUnauthorizedException} if the user is not authorized to perform this task</li>
   * </ul>
   */
  public void deleteWorkflow(MetisUser metisUser, String datasetId) throws GenericMetisException {
    authorizer.authorizeWriteExistingDatasetById(metisUser, datasetId);
    workflowDao.deleteWorkflow(datasetId);
  }

  /**
   * Get a workflow for a dataset identifier.
   *
   * @param metisUser the user wishing to perform this operation
   * @param datasetId the dataset identifier
   * @return the Workflow object
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoDatasetFoundException} if the dataset identifier provided does not exist</li>
   * <li>{@link UserUnauthorizedException} if the user is not authorized to perform this task</li>
   * </ul>
   */
  public Workflow getWorkflow(MetisUser metisUser, String datasetId) throws GenericMetisException {
    authorizer.authorizeReadExistingDatasetById(metisUser, datasetId);
    return workflowDao.getWorkflow(datasetId);
  }

  /**
   * Get a WorkflowExecution using an execution identifier.
   *
   * @param metisUser the user wishing to perform this operation
   * @param executionId the execution identifier
   * @return the WorkflowExecution object
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoDatasetFoundException} if the dataset identifier provided does not exist</li>
   * <li>{@link UserUnauthorizedException} if the user is not authorized to perform this task</li>
   * </ul>
   */
  public WorkflowExecution getWorkflowExecutionByExecutionId(MetisUser metisUser,
      String executionId) throws GenericMetisException {
    final WorkflowExecution result = workflowExecutionDao.getById(executionId);
    if (result != null) {
      authorizer.authorizeReadExistingDatasetById(metisUser, result.getDatasetId());
    }
    return result;
  }

  /**
   * <p> Does checking, prepares and adds a WorkflowExecution in the queue. That means it updates
   * the status of the WorkflowExecution to {@link WorkflowStatus#INQUEUE}, adds it to the database
   * and also it's identifier goes into the distributed queue of WorkflowExecutions. The source data
   * for the first plugin in the workflow can be controlled, if required, from the {@code
   * enforcedPredecessorType}, which means that the last valid plugin that is provided with that
   * parameter, will be used as the source data. </p>
   * <p> <b>Please note:</b> this method is not checked for authorization: it is only meant to be
   * called from a scheduled task. </p>
   *
   * @param datasetId the dataset identifier for which the execution will take place
   * @param workflowProvided optional, the workflow to use instead of retrieving the saved one from
   * the db
   * @param enforcedPredecessorType optional, the plugin type to be used as source data
   * @param priority the priority of the execution in case the system gets overloaded, 0 lowest, 10
   * highest
   * @return the WorkflowExecution object that was generated
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoWorkflowFoundException} if a workflow for the dataset identifier provided does
   * not exist</li>
   * <li>{@link BadContentException} if the workflow is empty or no plugin enabled</li>
   * <li>{@link NoDatasetFoundException} if the dataset identifier provided does not exist</li>
   * <li>{@link ExternalTaskException} if there was an exception when contacting the external
   * resource(ECloud)</li>
   * <li>{@link PluginExecutionNotAllowed} if the execution of the first plugin was not allowed,
   * because a valid source plugin could not be found</li>
   * <li>{@link WorkflowExecutionAlreadyExistsException} if a workflow execution for the generated
   * execution identifier already exists, almost impossible to happen since ids are UUIDs</li>
   * </ul>
   */
  public WorkflowExecution addWorkflowInQueueOfWorkflowExecutionsWithoutAuthorization(
      String datasetId, @Nullable Workflow workflowProvided,
      @Nullable ExecutablePluginType enforcedPredecessorType, int priority)
      throws GenericMetisException {
    final Dataset dataset = datasetDao.getDatasetByDatasetId(datasetId);
    if (dataset == null) {
      throw new NoDatasetFoundException(
          String.format("No dataset found with datasetId: %s, in METIS", datasetId));
    }
    return addWorkflowInQueueOfWorkflowExecutions(dataset, workflowProvided,
        enforcedPredecessorType, priority);
  }

  /**
   * Does checking, prepares and adds a WorkflowExecution in the queue. That means it updates the
   * status of the WorkflowExecution to {@link WorkflowStatus#INQUEUE}, adds it to the database and
   * also it's identifier goes into the distributed queue of WorkflowExecutions. The source data for
   * the first plugin in the workflow can be controlled, if required, from the {@code
   * enforcedPredecessorType}, which means that the last valid plugin that is provided with that
   * parameter, will be used as the source data.
   *
   * @param metisUser the user wishing to perform this operation
   * @param datasetId the dataset identifier for which the execution will take place
   * @param workflowProvided optional, the workflow to use instead of retrieving the saved one from
   * the db
   * @param enforcedPredecessorType optional, the plugin type to be used as source data
   * @param priority the priority of the execution in case the system gets overloaded, 0 lowest, 10
   * highest
   * @return the WorkflowExecution object that was generated
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoWorkflowFoundException} if a workflow for the dataset identifier provided does
   * not exist</li>
   * <li>{@link BadContentException} if the workflow is empty or no plugin enabled</li>
   * <li>{@link NoDatasetFoundException} if the dataset identifier provided does not exist</li>
   * <li>{@link UserUnauthorizedException} if the user is not authorized to perform this task</li>
   * <li>{@link ExternalTaskException} if there was an exception when contacting the external
   * resource(ECloud)</li>
   * <li>{@link PluginExecutionNotAllowed} if the execution of the first plugin was not allowed,
   * because a valid source plugin could not be found</li>
   * <li>{@link WorkflowExecutionAlreadyExistsException} if a workflow execution for the generated
   * execution identifier already exists, almost impossible to happen since ids are UUIDs</li>
   * </ul>
   */
  public WorkflowExecution addWorkflowInQueueOfWorkflowExecutions(MetisUser metisUser,
      String datasetId, @Nullable Workflow workflowProvided,
      @Nullable ExecutablePluginType enforcedPredecessorType, int priority)
      throws GenericMetisException {
    final Dataset dataset = authorizer.authorizeWriteExistingDatasetById(metisUser, datasetId);
    return addWorkflowInQueueOfWorkflowExecutions(dataset, workflowProvided,
        enforcedPredecessorType, priority);
  }

  private WorkflowExecution addWorkflowInQueueOfWorkflowExecutions(Dataset dataset,
      @Nullable Workflow workflowProvided, @Nullable ExecutablePluginType enforcedPredecessorType,
      int priority)
      throws GenericMetisException {

    // Get the workflow or use the one provided.
    final Workflow workflow;
    if (Objects.isNull(workflowProvided)) {
      workflow = workflowDao.getWorkflow(dataset.getDatasetId());
    } else {
      workflow = workflowProvided;
    }
    if (workflow == null) {
      throw new NoWorkflowFoundException(
          String.format("No workflow found with datasetId: %s, in METIS", dataset.getDatasetId()));
    }

    // Validate the workflow and obtain the predecessor.
    final PluginWithExecutionId<ExecutablePlugin> predecessor = workflowUtils
        .validateWorkflowPlugins(workflow, enforcedPredecessorType);

    // Make sure that eCloud knows this dataset (needs to happen before we create the workflow).
    datasetDao.checkAndCreateDatasetInEcloud(dataset);

    // Create the workflow execution (without adding it to the database).
    final WorkflowExecution workflowExecution = workflowExecutionFactory
        .createWorkflowExecution(workflow, dataset, predecessor, priority);

    // Obtain the lock.
    RLock executionDatasetIdLock = redissonClient
        .getFairLock(String.format(EXECUTION_FOR_DATASETID_SUBMITION_LOCK, dataset.getDatasetId()));
    executionDatasetIdLock.lock();

    // Add the workflow execution to the database. Then release the lock.
    final String objectId;
    try {
      String storedWorkflowExecutionId =
          workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId());
      if (storedWorkflowExecutionId != null) {
        throw new WorkflowExecutionAlreadyExistsException(
            String.format("Workflow execution already exists with id %s and is not completed",
                storedWorkflowExecutionId));
      }
      workflowExecution.setWorkflowStatus(WorkflowStatus.INQUEUE);
      workflowExecution.setCreatedDate(new Date());
      objectId = workflowExecutionDao.create(workflowExecution);
    } finally {
      executionDatasetIdLock.unlock();
    }

    // Add the workflow execution to the queue.
    workflowExecutorManager.addWorkflowExecutionToQueue(objectId, priority);
    LOGGER.info("WorkflowExecution with id: {}, added to execution queue", objectId);

    // Done. Get a fresh copy of the workflow execution to return.
    return workflowExecutionDao.getById(objectId);
  }

  /**
   * Request to cancel a workflow execution. The execution will go into a cancelling state until
   * it's properly {@link WorkflowStatus#CANCELLED} from the system
   *
   * @param metisUser the user wishing to perform this operation
   * @param executionId the execution identifier of the execution to cancel
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoWorkflowExecutionFoundException} if no worklfowExecution could be found</li>
   * <li>{@link NoDatasetFoundException} if the dataset identifier provided does not exist</li>
   * <li>{@link UserUnauthorizedException} if the user is not authorized to perform this task</li>
   * </ul>
   */
  public void cancelWorkflowExecution(MetisUser metisUser, String executionId)
      throws GenericMetisException {

    WorkflowExecution workflowExecution = workflowExecutionDao.getById(executionId);
    if (workflowExecution != null) {
      authorizer.authorizeWriteExistingDatasetById(metisUser, workflowExecution.getDatasetId());
    }
    if (workflowExecution != null && (
        workflowExecution.getWorkflowStatus() == WorkflowStatus.RUNNING
            || workflowExecution.getWorkflowStatus() == WorkflowStatus.INQUEUE)) {
      workflowExecutionDao.setCancellingState(workflowExecution, metisUser);
      LOGGER.info(
          "Cancelling user workflow execution with id: {}", workflowExecution.getId());
    } else {
      throw new NoWorkflowExecutionFoundException(String.format(
          "Running workflowExecution with executionId: %s, does not exist or not active",
          executionId));
    }
  }

  /**
   * The number of WorkflowExecutions that would be returned if a get all request would be
   * performed.
   *
   * @return the number representing the size during a get all request
   */
  public int getWorkflowExecutionsPerRequest() {
    return workflowExecutionDao.getWorkflowExecutionsPerRequest();
  }

  /**
   * Check if a specified {@code pluginType} is allowed for execution. This is checked based on, if
   * there was a previous successful finished plugin that follows a specific order (unless the
   * {@code enforcedPredecessorType} is used) and that has the latest successful harvest plugin as
   * an ancestor.
   *
   * @param metisUser the user wishing to perform this operation
   * @param datasetId the dataset identifier of which the executions are based on
   * @param pluginType the pluginType to be checked for allowance of execution
   * @param enforcedPredecessorType optional, the plugin type to be used as source data
   * @return the abstractMetisPlugin that the execution on {@code pluginType} will be based on. Can
   * be null if the {@code pluginType} is the first one in the total order of executions e.g. One of
   * the harvesting plugins.
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link PluginExecutionNotAllowed} if the no plugin was found so the {@code pluginType}
   * will be based upon</li>
   * <li>{@link NoDatasetFoundException} if the dataset identifier provided does not exist</li>
   * <li>{@link UserUnauthorizedException} if the user is not authorized to perform this task</li>
   * </ul>
   */
  public ExecutablePlugin getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(
      MetisUser metisUser, String datasetId, ExecutablePluginType pluginType,
      ExecutablePluginType enforcedPredecessorType) throws GenericMetisException {
    authorizer.authorizeReadExistingDatasetById(metisUser, datasetId);
    return Optional.ofNullable(
        workflowUtils.computePredecessorPlugin(pluginType, enforcedPredecessorType, datasetId))
        .map(PluginWithExecutionId::getPlugin).orElse(null);
  }

  /**
   * Get all WorkflowExecutions paged.
   *
   * @param metisUser the user wishing to perform this operation
   * @param datasetId the dataset identifier filter, can be null to get all datasets
   * @param workflowStatuses a set of workflow statuses to filter, can be empty or null
   * @param orderField the field to be used to sort the results
   * @param ascending a boolean value to request the ordering to ascending or descending
   * @param nextPage the nextPage token
   * @return A list of all the WorkflowExecutions found. If the user is not admin, the list is
   * filtered to only show those executions that are in the user's organization.
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoDatasetFoundException} if the dataset identifier provided does not exist</li>
   * <li>{@link UserUnauthorizedException} if the user is not authorized to perform this task</li>
   * </ul>
   */
  public ResponseListWrapper<WorkflowExecution> getAllWorkflowExecutions(MetisUser metisUser,
      String datasetId, Set<WorkflowStatus> workflowStatuses, DaoFieldNames orderField,
      boolean ascending, int nextPage) throws GenericMetisException {

    // Authorize
    if (datasetId == null) {
      authorizer.authorizeReadAllDatasets(metisUser);
    } else {
      authorizer.authorizeReadExistingDatasetById(metisUser, datasetId);
    }

    // Determine the dataset IDs to filter on.
    final Set<String> datasetIds;
    if (datasetId == null) {
      datasetIds = getDatasetIdsToFilterOn(metisUser);
    } else {
      datasetIds = Collections.singleton(datasetId);
    }

    // Find the executions.
    final ResultList<WorkflowExecution> data = workflowExecutionDao.getAllWorkflowExecutions(
        datasetIds, workflowStatuses, orderField, ascending, nextPage, false);

    // Compile and return the result.
    final ResponseListWrapper<WorkflowExecution> result = new ResponseListWrapper<>();
    result.setResultsAndLastPage(data.getResults(), getWorkflowExecutionsPerRequest(), nextPage,
        data.isMaxResultCountReached());
    return result;
  }

  /**
   * Get the overview of WorkflowExecutions. This returns a list of executions ordered to display an
   * overview. First the ones in queue, then those in progress and then those that are finalized.
   * They will be sorted by creation date. This method does support pagination.
   *
   * @param metisUser the user wishing to perform this operation
   * @param pluginStatuses the plugin statuses to filter. Can be null.
   * @param pluginTypes the plugin types to filter. Can be null.
   * @param fromDate the date from where the results should start. Can be null.
   * @param toDate the date to where the results should end. Can be null.
   * @param nextPage the nextPage token, the end of the list is marked with -1 on the response
   * @param pageCount the number of pages that are requested
   * @return a list of all the WorkflowExecutions together with the datasets that they belong to.
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authenticated or authorized to perform this operation</li>
   * </ul>
   */
  public ResponseListWrapper<ExecutionAndDatasetView> getWorkflowExecutionsOverview(
      MetisUser metisUser, Set<PluginStatus> pluginStatuses, Set<PluginType> pluginTypes,
      Date fromDate, Date toDate, int nextPage, int pageCount) throws GenericMetisException {
    authorizer.authorizeReadAllDatasets(metisUser);
    final Set<String> datasetIds = getDatasetIdsToFilterOn(metisUser);
    final ResultList<ExecutionDatasetPair> data =
        workflowExecutionDao.getWorkflowExecutionsOverview(datasetIds, pluginStatuses, pluginTypes,
            fromDate, toDate, nextPage, pageCount);
    final List<ExecutionAndDatasetView> views = data.getResults().stream()
        .map(result -> new ExecutionAndDatasetView(result.getExecution(), result.getDataset()))
        .collect(Collectors.toList());
    final ResponseListWrapper<ExecutionAndDatasetView> result = new ResponseListWrapper<>();
    result.setResultsAndLastPage(views, getWorkflowExecutionsPerRequest(), nextPage, pageCount,
        data.isMaxResultCountReached());
    return result;
  }

  private Set<String> getDatasetIdsToFilterOn(MetisUser metisUser) {
    final Set<String> datasetIds;
    if (metisUser.getAccountRole() == AccountRole.METIS_ADMIN) {
      datasetIds = null;
    } else {
      datasetIds = datasetDao.getAllDatasetsByOrganizationId(metisUser.getOrganizationId()).stream()
          .map(Dataset::getDatasetId).collect(Collectors.toSet());
    }
    return datasetIds;
  }

  /**
   * Retrieve dataset level information of past executions {@link DatasetExecutionInformation}
   *
   * @param metisUser the user wishing to perform this operation
   * @param datasetId the dataset identifier to generate the information for
   * @return the structured class containing all the execution information
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoDatasetFoundException} if the dataset identifier provided does not exist</li>
   * <li>{@link UserUnauthorizedException} if the user is not authorized to perform this task</li>
   * </ul>
   */
  public DatasetExecutionInformation getDatasetExecutionInformation(MetisUser metisUser,
      String datasetId) throws GenericMetisException {
    authorizer.authorizeReadExistingDatasetById(metisUser, datasetId);
    return getDatasetExecutionInformation(datasetId);
  }

  DatasetExecutionInformation getDatasetExecutionInformation(String datasetId) {

    // Obtain the relevant parts of the execution history
    final ExecutablePlugin lastHarvestPlugin = Optional.ofNullable(workflowExecutionDao
        .getLatestSuccessfulExecutablePlugin(datasetId, HARVEST_TYPES, false))
        .map(PluginWithExecutionId::getPlugin).orElse(null);
    final PluginWithExecutionId<MetisPlugin> firstPublishPluginWithExecutionId = workflowExecutionDao
        .getFirstSuccessfulPlugin(datasetId, PUBLISH_TYPES);
    final MetisPlugin firstPublishPlugin = firstPublishPluginWithExecutionId == null ? null
        : firstPublishPluginWithExecutionId.getPlugin();
    final ExecutablePlugin lastExecutablePreviewPlugin = Optional.ofNullable(workflowExecutionDao
        .getLatestSuccessfulExecutablePlugin(datasetId, EXECUTABLE_PREVIEW_TYPES, false)).map(
        PluginWithExecutionId::getPlugin).orElse(null);
    final ExecutablePlugin lastExecutablePublishPlugin = Optional.ofNullable(workflowExecutionDao
        .getLatestSuccessfulExecutablePlugin(datasetId, EXECUTABLE_PUBLISH_TYPES, false))
        .map(PluginWithExecutionId::getPlugin).orElse(null);
    final PluginWithExecutionId<MetisPlugin> latestPreviewPluginWithExecutionId = workflowExecutionDao
        .getLatestSuccessfulPlugin(datasetId, PREVIEW_TYPES);
    final PluginWithExecutionId<MetisPlugin> latestPublishPluginWithExecutionId = workflowExecutionDao
        .getLatestSuccessfulPlugin(datasetId, PUBLISH_TYPES);
    final MetisPlugin lastPreviewPlugin = latestPreviewPluginWithExecutionId == null ? null
        : latestPreviewPluginWithExecutionId.getPlugin();
    final MetisPlugin lastPublishPlugin = latestPublishPluginWithExecutionId == null ? null
        : latestPublishPluginWithExecutionId.getPlugin();
    final ExecutablePlugin lastExecutableDepublishPlugin = Optional.ofNullable(workflowExecutionDao
        .getLatestSuccessfulExecutablePlugin(datasetId, EXECUTABLE_DEPUBLISH_TYPES, false))
        .map(PluginWithExecutionId::getPlugin).orElse(null);

    // Obtain the relevant current executions
    final WorkflowExecution runningOrInQueueExecution = getRunningOrInQueueExecution(datasetId);
    final boolean isPreviewCleaningOrRunning = isPluginInWorkflowCleaningOrRunning(
        runningOrInQueueExecution, PREVIEW_TYPES);
    final boolean isPublishCleaningOrRunning = isPluginInWorkflowCleaningOrRunning(
        runningOrInQueueExecution, PUBLISH_TYPES);

    final DatasetExecutionInformation executionInfo = new DatasetExecutionInformation();
    // Set the last harvest information
    if (Objects.nonNull(lastHarvestPlugin)) {
      executionInfo.setLastHarvestedDate(lastHarvestPlugin.getFinishedDate());
      executionInfo.setLastHarvestedRecords(
          lastHarvestPlugin.getExecutionProgress().getProcessedRecords() - lastHarvestPlugin
              .getExecutionProgress().getErrors());
    }
    final Date now = new Date();
    setPreviewInformation(executionInfo, lastExecutablePreviewPlugin, lastPreviewPlugin,
        isPreviewCleaningOrRunning, now);
    setPublishInformation(executionInfo, firstPublishPlugin, lastExecutablePublishPlugin,
        lastPublishPlugin, lastExecutableDepublishPlugin, isPublishCleaningOrRunning, now);

    return executionInfo;
  }

  WorkflowExecution getRunningOrInQueueExecution(String datasetId) {
    return workflowExecutionDao.getRunningOrInQueueExecution(datasetId);
  }

  private void setPreviewInformation(DatasetExecutionInformation executionInfo,
      ExecutablePlugin lastExecutablePreviewPlugin, MetisPlugin lastPreviewPlugin,
      boolean isPreviewCleaningOrRunning, Date date) {
    // Set the last preview information
    if (Objects.nonNull(lastPreviewPlugin)) {
      executionInfo.setLastPreviewDate(lastPreviewPlugin.getFinishedDate());
      executionInfo.setLastPreviewRecordsReadyForViewing(
          !isPreviewCleaningOrRunning && isPreviewOrPublishReadyForViewing(lastPreviewPlugin,
              date));
    }
    if (Objects.nonNull(lastExecutablePreviewPlugin)) {
      executionInfo.setLastPreviewRecords(
          lastExecutablePreviewPlugin.getExecutionProgress().getProcessedRecords()
              - lastExecutablePreviewPlugin.getExecutionProgress().getErrors());
    }
  }

  private void setPublishInformation(DatasetExecutionInformation executionInfo,
      MetisPlugin firstPublishPlugin, ExecutablePlugin lastExecutablePublishPlugin,
      MetisPlugin lastPublishPlugin, ExecutablePlugin lastExecutableDepublishPlugin,
      boolean isPublishCleaningOrRunning,
      Date date) {
    // Set the first publication information
    executionInfo.setFirstPublishedDate(firstPublishPlugin == null ? null :
        firstPublishPlugin.getFinishedDate());

    // Set the last publish information
    if (Objects.nonNull(lastPublishPlugin)) {
      executionInfo.setLastPublishedDate(lastPublishPlugin.getFinishedDate());
      executionInfo.setLastPublishedRecordsReadyForViewing(
          !isPublishCleaningOrRunning && isPreviewOrPublishReadyForViewing(lastPublishPlugin,
              date));
    }
    if (Objects.nonNull(lastExecutablePublishPlugin)) {
      executionInfo.setLastPublishedRecords(
          lastExecutablePublishPlugin.getExecutionProgress().getProcessedRecords()
              - lastExecutablePublishPlugin.getExecutionProgress().getErrors());
    }

    // Set the last depublish information
    if (Objects.nonNull(lastExecutableDepublishPlugin)) {
      executionInfo.setLastDepublishedDate(lastExecutableDepublishPlugin.getFinishedDate());
      executionInfo.setLastDepublishedRecords(
          lastExecutableDepublishPlugin.getExecutionProgress().getProcessedRecords()
              - lastExecutableDepublishPlugin.getExecutionProgress().getErrors());
    }

    if (Objects.nonNull(lastExecutablePublishPlugin) && Objects
        .nonNull(lastExecutableDepublishPlugin)) {
      if (lastExecutablePublishPlugin.getFinishedDate()
          .compareTo(lastExecutableDepublishPlugin.getFinishedDate()) > 0) {
        executionInfo.setPublicationStatus(PublicationStatus.PUBLISHED);
      } else {
        executionInfo.setPublicationStatus(PublicationStatus.DEPUBLISHED);
      }
    }
  }

  private boolean isPreviewOrPublishReadyForViewing(MetisPlugin plugin, Date now) {
    final boolean dataIsValid = !(plugin instanceof ExecutablePlugin)
        || MetisPlugin.getDataStatus((ExecutablePlugin) plugin) == DataStatus.VALID;
    final boolean enoughTimeHasPassed = getSolrCommitPeriodInMins() <
        DateUtils.calculateDateDifference(plugin.getFinishedDate(), now, TimeUnit.MINUTES);
    return dataIsValid && enoughTimeHasPassed;
  }

  private boolean isPluginInWorkflowCleaningOrRunning(WorkflowExecution runningOrInQueueExecution,
      Set<PluginType> pluginTypes) {
    return runningOrInQueueExecution != null && runningOrInQueueExecution.getMetisPlugins().stream()
        .filter(metisPlugin -> pluginTypes.contains(metisPlugin.getPluginType()))
        .map(AbstractMetisPlugin::getPluginStatus)
        .anyMatch(pluginStatus -> pluginStatus == PluginStatus.CLEANING
            || pluginStatus == PluginStatus.RUNNING);
  }

  /**
   * Retrieve dataset level history of past executions {@link DatasetExecutionInformation}
   *
   * @param metisUser the user wishing to perform this operation
   * @param datasetId the dataset identifier to generate the history for
   * @return the structured class containing all the execution history, ordered by date descending.
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoDatasetFoundException} if the dataset identifier provided does not exist</li>
   * <li>{@link UserUnauthorizedException} if the user is not authorized to perform this task</li>
   * </ul>
   */
  public ExecutionHistory getDatasetExecutionHistory(MetisUser metisUser, String datasetId)
      throws GenericMetisException {

    // Check that the user is authorized
    authorizer.authorizeReadExistingDatasetById(metisUser, datasetId);

    // Get the information from the database
    final List<Execution> executions = workflowExecutionDao.getAllExecutionStartDates(datasetId)
        .getResults().stream().map(entry -> {
          final Execution execution = new Execution();
          execution.setWorkflowExecutionId(entry.getExecutionIdAsString());
          execution.setStartedDate(entry.getStartedDate());
          return execution;
        }).collect(Collectors.toList());

    // Done
    final ExecutionHistory result = new ExecutionHistory();
    result.setExecutions(executions);
    return result;
  }

  /**
   * Retrieve a list of plugins with data availability {@link PluginsWithDataAvailability} for a
   * given workflow execution.
   *
   * @param metisUser the user wishing to perform this operation
   * @param executionId the identifier of the execution for which to get the plugins
   * @return the structured class containing all the execution history, ordered by date descending.
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if an
   * non-existing execution ID or version is provided.</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authenticated or authorized to perform this operation</li>
   * </ul>
   */
  public PluginsWithDataAvailability getExecutablePluginsWithDataAvailability(MetisUser metisUser,
      String executionId) throws GenericMetisException {

    // Get the execution and do the authorization check.
    final WorkflowExecution execution = getWorkflowExecutionByExecutionId(metisUser, executionId);
    if (execution == null) {
      throw new NoWorkflowExecutionFoundException(
          String.format("No workflow execution found for workflowExecutionId: %s", executionId));
    }

    // Compile the result.
    final List<PluginWithDataAvailability> plugins = execution.getMetisPlugins().stream()
        .filter(plugin -> plugin instanceof ExecutablePlugin)
        .map(plugin -> (ExecutablePlugin) plugin).map(OrchestratorService::convert)
        .collect(Collectors.toList());
    final PluginsWithDataAvailability result = new PluginsWithDataAvailability();
    result.setPlugins(plugins);

    // Done.
    return result;
  }

  private static PluginWithDataAvailability convert(ExecutablePlugin plugin) {

    // Decide on whether the plugin has successful data available.
    final ExecutionProgress progress = plugin.getExecutionProgress();
    final boolean hasSuccessfulData =
        progress != null && progress.getProcessedRecords() > progress.getErrors();

    // Create the result
    final PluginWithDataAvailability result = new PluginWithDataAvailability();
    result.setHasSuccessfulData(hasSuccessfulData);
    result.setPluginType(plugin.getPluginType());
    return result;
  }

  /**
   * Get the evolution of the records from when they were first imported until (and excluding) the
   * specified version.
   *
   * @param metisUser the user wishing to perform this operation
   * @param executionId The ID of the workflow exection in which the version is created.
   * @param pluginType The step within the workflow execution that created the version.
   * @return The record evolution.
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if an
   * non-existing execution ID or version is provided.</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authenticated or authorized to perform this operation</li>
   * </ul>
   */
  public VersionEvolution getRecordEvolutionForVersion(MetisUser metisUser,
      String executionId, PluginType pluginType) throws GenericMetisException {

    // Get the execution and do the authorization check.
    final WorkflowExecution execution = getWorkflowExecutionByExecutionId(metisUser, executionId);
    if (execution == null) {
      throw new NoWorkflowExecutionFoundException(
          String.format("No workflow execution found for workflowExecutionId: %s", executionId));
    }

    // Find the plugin (workflow step) in question.
    final AbstractMetisPlugin targetPlugin = execution.getMetisPluginWithType(pluginType)
        .orElseThrow(() -> new NoWorkflowExecutionFoundException(
            String.format("No plugin of type %s found for workflowExecution with id: %s",
                pluginType.name(), execution)));

    // Compile the version evolution.
    final Collection<Pair<ExecutablePlugin, WorkflowExecution>> evolutionSteps = workflowUtils
        .compileVersionEvolution(targetPlugin, execution);
    final VersionEvolution versionEvolution = new VersionEvolution();
    versionEvolution.setEvolutionSteps(evolutionSteps.stream().map(step -> {
      final VersionEvolutionStep evolutionStep = new VersionEvolutionStep();
      final ExecutablePlugin<?> plugin = step.getLeft();
      evolutionStep.setWorkflowExecutionId(step.getRight().getId().toString());
      evolutionStep.setPluginType(plugin.getPluginMetadata().getExecutablePluginType());
      evolutionStep.setFinishedTime(plugin.getFinishedDate());
      return evolutionStep;
    }).collect(Collectors.toList()));
    return versionEvolution;
  }

  public int getSolrCommitPeriodInMins() {
    synchronized (this) {
      return solrCommitPeriodInMins;
    }
  }

  public void setSolrCommitPeriodInMins(int solrCommitPeriodInMins) {
    synchronized (this) {
      this.solrCommitPeriodInMins = solrCommitPeriodInMins;
    }
  }
}
