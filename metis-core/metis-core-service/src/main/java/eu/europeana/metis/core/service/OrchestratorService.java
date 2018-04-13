package eu.europeana.metis.core.service;

import eu.europeana.metis.CommonStringValues;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.DatasetXsltDao;
import eu.europeana.metis.core.dao.WorkflowDao;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetExecutionInformation;
import eu.europeana.metis.core.dataset.DatasetXslt;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowFoundException;
import eu.europeana.metis.core.exceptions.PluginExecutionNotAllowed;
import eu.europeana.metis.core.exceptions.WorkflowAlreadyExistsException;
import eu.europeana.metis.core.exceptions.WorkflowExecutionAlreadyExistsException;
import eu.europeana.metis.core.execution.ExecutionRules;
import eu.europeana.metis.core.execution.WorkflowExecutorManager;
import eu.europeana.metis.core.workflow.OrderField;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.EnrichmentPlugin;
import eu.europeana.metis.core.workflow.plugins.HTTPHarvestPlugin;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.core.workflow.plugins.TransformationPlugin;
import eu.europeana.metis.core.workflow.plugins.TransformationPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ValidationExternalPlugin;
import eu.europeana.metis.core.workflow.plugins.ValidationInternalPlugin;
import eu.europeana.metis.exception.ExternalTaskException;
import eu.europeana.metis.exception.GenericMetisException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

  private final WorkflowExecutionDao workflowExecutionDao;
  private final WorkflowDao workflowDao;
  private final DatasetDao datasetDao;
  private final DatasetXsltDao datasetXsltDao;
  private final WorkflowExecutorManager workflowExecutorManager;
  private final RedissonClient redissonClient;
  private String metisCoreUrl; //Initialize with setter

  /**
   * Constructor with all the required parameters
   *
   * @param workflowDao the Dao instance to access the Workflow database
   * @param workflowExecutionDao the Dao instance to access the WorkflowExecution database
   * @param datasetDao the Dao instance to access the Dataset database
   * @param datasetXsltDao the Dao instance to access the DatasetXslt database
   * @param workflowExecutorManager the instance that handles the production and consumption of workflowExecutions
   * @param redissonClient the instance of Redisson library that handles distributed locks
   * @throws IOException that can be thrown when initializing the {@link WorkflowExecutorManager}
   */
  @Autowired
  public OrchestratorService(WorkflowDao workflowDao,
      WorkflowExecutionDao workflowExecutionDao, DatasetDao datasetDao,
      DatasetXsltDao datasetXsltDao, WorkflowExecutorManager workflowExecutorManager,
      RedissonClient redissonClient) throws IOException {
    this.workflowDao = workflowDao;
    this.workflowExecutionDao = workflowExecutionDao;
    this.datasetDao = datasetDao;
    this.datasetXsltDao = datasetXsltDao;
    this.workflowExecutorManager = workflowExecutorManager;
    this.redissonClient = redissonClient;

    this.workflowExecutorManager.initiateConsumer();
  }

  /**
   * Create a workflow using a datasetId and the {@link Workflow} that contains the requested plugins.
   * When creating a new workflow all the plugins specified will be automatically enabled.
   *
   * @param datasetId the identifier of the dataset for which the workflow should be created
   * @param workflow the workflow with the plugins requested
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link WorkflowAlreadyExistsException} if a workflow for the dataset identifier provided already exists</li>
   * <li>{@link NoDatasetFoundException} if the dataset identifier provided does not exist</li>
   * </ul>
   */
  public void createWorkflow(int datasetId, Workflow workflow)
      throws GenericMetisException {
    if (datasetDao.getDatasetByDatasetId(datasetId) == null) {
      throw new NoDatasetFoundException(
          String.format("Dataset with datasetId: %s does NOT exist", datasetId));
    }
    workflow.setDatasetId(datasetId);
    checkRestrictionsOnWorkflowCreate(workflow);
    workflow.getMetisPluginsMetadata()
        .forEach(abstractMetisPluginMetadata -> abstractMetisPluginMetadata.setEnabled(true));
    workflowDao.create(workflow);
  }

  /**
   * Update an already existent workflow using a datasetId and the {@link Workflow} that contains the requested plugins.
   * When updating an existent workflow all specified plugins will be enabled and all plugins that were existent in the system
   * beforehand will be kept with their configuration but will be disabled.
   *
   * @param datasetId the identifier of the dataset for which the workflow should be updated
   * @param workflow the workflow with the plugins requested
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoWorkflowFoundException} if a workflow for the dataset identifier provided does not exist</li>
   * <li>{@link NoDatasetFoundException} if the dataset identifier provided does not exist</li>
   * </ul>
   */
  public void updateWorkflow(int datasetId, Workflow workflow)
      throws GenericMetisException {
    if (datasetDao.getDatasetByDatasetId(datasetId) == null) {
      throw new NoDatasetFoundException(
          String.format("Dataset with datasetId: %s does NOT exist", datasetId));
    }
    workflow.setDatasetId(datasetId);
    Workflow storedWorkflow = checkRestrictionsOnWorkflowUpdate(workflow);
    workflow.setId(storedWorkflow.getId());
    overwriteNewPluginMetadataOnWorkflowAndDisableOtherPluginMetadata(workflow, storedWorkflow);

    workflowDao.update(workflow);
  }

  private void overwriteNewPluginMetadataOnWorkflowAndDisableOtherPluginMetadata(Workflow workflow,
      Workflow storedWorkflow) {
    //Overwrite only ones provided and disable the rest, already stored, plugins
    workflow.getMetisPluginsMetadata()
        .forEach(abstractMetisPluginMetadata -> abstractMetisPluginMetadata.setEnabled(true));
    List<AbstractMetisPluginMetadata> storedPluginsExcludingNewPlugins = storedWorkflow
        .getMetisPluginsMetadata()
        .stream().filter(abstractMetisPluginMetadata ->
            workflow.getPluginMetadata(abstractMetisPluginMetadata.getPluginType()) == null)
        .peek(abstractMetisPluginMetadata -> abstractMetisPluginMetadata.setEnabled(false))
        .collect(Collectors.toList());
    workflow.setMetisPluginsMetadata(Stream.concat(storedPluginsExcludingNewPlugins.stream(),
        workflow.getMetisPluginsMetadata().stream()).collect(Collectors.toList()));
  }

  /**
   * Deletes a workflow.
   *
   * @param datasetId the dataset identifier that corresponds to the workflow to be deleted
   */
  public void deleteWorkflow(int datasetId) {
    workflowDao.deleteWorkflow(datasetId);
  }

  /**
   * Get a workflow for a dataset identifier.
   *
   * @param datasetId the dataset identifier
   * @return the Workflow object
   */
  public Workflow getWorkflow(int datasetId) {
    return workflowDao.getWorkflow(datasetId);
  }

  /**
   * Get all workflows for a workflow owner paged.
   *
   * @param workflowOwner the workflow owner used as a fielter
   * @param nextPage the nextPage token or -1
   * @return a list of the Workflow objects
   */
  public List<Workflow> getAllWorkflows(String workflowOwner, int nextPage) {
    return workflowDao.getAllWorkflows(workflowOwner, nextPage);
  }

  /**
   * Get a WorkflowExecution using an execution identifier.
   *
   * @param executionId the execution identifier
   * @return the WorkflowExecution object
   */
  public WorkflowExecution getWorkflowExecutionByExecutionId(String executionId) {
    return workflowExecutionDao.getById(executionId);
  }

  /**
   * Does checking, prepares and adds a WorkflowExecution in the queue.
   * That means it updates the status of the WorkflowExecution to {@link WorkflowStatus#INQUEUE}, adds it to the database
   * and also it's identifier goes into the distributed queue of WorkflowExecutions.
   * The source data for the first plugin in the workflow can be controlled, if required, from the {@code enforcedPluginType},
   * which means that the last valid plugin that is provided with that parameter, will be used as the source data.
   *
   * @param datasetId the dataset identifier for which the execution will take place
   * @param enforcedPluginType optional, the plugin type to be used as source data
   * @param priority the priority of the execution in case the system gets overloaded, 0 lowest, 10 highest
   * @return the WorkflowExecution object that was generated
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoWorkflowFoundException} if a workflow for the dataset identifier provided does not exist</li>
   * <li>{@link NoDatasetFoundException} if the dataset identifier provided does not exist</li>
   * <li>{@link ExternalTaskException} if there was an exception when contacting the external resource(ECloud)</li>
   * <li>{@link PluginExecutionNotAllowed} if the execution of the first plugin was not allowed, because a valid source plugin could not be found</li>
   * <li>{@link WorkflowExecutionAlreadyExistsException} if a workflow execution for the generated execution identifier already exists, almost impossible to happen since ids are UUIDs</li>
   * </ul>
   */
  public WorkflowExecution addWorkflowInQueueOfWorkflowExecutions(int datasetId,
      PluginType enforcedPluginType, int priority)
      throws GenericMetisException {

    Dataset dataset = checkDatasetExistence(datasetId);
    Workflow workflow = checkWorkflowExistence(datasetId);
    datasetDao.checkAndCreateDatasetInEcloud(dataset);

    WorkflowExecution workflowExecution = new WorkflowExecution(dataset, workflow,
        createMetisPluginsList(dataset, workflow, enforcedPluginType), priority);
    workflowExecution.setWorkflowStatus(WorkflowStatus.INQUEUE);
    RLock executionDatasetIdLock = redissonClient
        .getFairLock(String.format(EXECUTION_FOR_DATASETID_SUBMITION_LOCK, dataset.getDatasetId()));
    executionDatasetIdLock.lock();
    String storedWorkflowExecutionId = workflowExecutionDao.existsAndNotCompleted(datasetId);
    if (storedWorkflowExecutionId != null) {
      executionDatasetIdLock.unlock();
      throw new WorkflowExecutionAlreadyExistsException(
          String.format("Workflow execution already exists with id %s and is not completed",
              storedWorkflowExecutionId));
    }
    workflowExecution.setCreatedDate(new Date());
    String objectId = workflowExecutionDao.create(workflowExecution);
    executionDatasetIdLock.unlock();
    workflowExecutorManager.addWorkflowExecutionToQueue(objectId, priority);
    LOGGER.info("WorkflowExecution with id: {}, added to execution queue", objectId);
    return workflowExecutionDao.getById(objectId);
  }

  private List<AbstractMetisPlugin> createMetisPluginsList(Dataset dataset, Workflow workflow,
      PluginType enforcedPluginType)
      throws PluginExecutionNotAllowed {
    List<AbstractMetisPlugin> metisPlugins = new ArrayList<>();

    boolean firstPluginDefined = addHarvestingPlugin(workflow, metisPlugins);
    addProcessPlugins(dataset, workflow, enforcedPluginType, metisPlugins, firstPluginDefined);
    return metisPlugins;
  }

  private boolean addHarvestingPlugin(Workflow workflow,
      List<AbstractMetisPlugin> metisPlugins) {
    AbstractMetisPluginMetadata pluginMetadata = workflow
        .getPluginMetadata(PluginType.OAIPMH_HARVEST);
    if (pluginMetadata != null && pluginMetadata.isEnabled()) {
      OaipmhHarvestPlugin oaipmhHarvestPlugin = new OaipmhHarvestPlugin(pluginMetadata);
      oaipmhHarvestPlugin
          .setId(new ObjectId().toString() + "-" + oaipmhHarvestPlugin.getPluginType().name());
      metisPlugins.add(oaipmhHarvestPlugin);
      return true;
    }
    pluginMetadata = workflow.getPluginMetadata(PluginType.HTTP_HARVEST);
    if (pluginMetadata != null && pluginMetadata.isEnabled()) {
      HTTPHarvestPlugin httpHarvestPlugin = new HTTPHarvestPlugin(pluginMetadata);
      httpHarvestPlugin
          .setId(new ObjectId().toString() + "-" + httpHarvestPlugin.getPluginType().name());
      metisPlugins.add(httpHarvestPlugin);
      return true;
    }
    return false;
  }

  private boolean addProcessPlugins(Dataset dataset, Workflow workflow,
      PluginType enforcedPluginType,
      List<AbstractMetisPlugin> metisPlugins,
      boolean firstPluginDefined) throws PluginExecutionNotAllowed {
    firstPluginDefined = addProcessPlugin(dataset, workflow, enforcedPluginType, metisPlugins,
        firstPluginDefined, PluginType.VALIDATION_EXTERNAL);
    firstPluginDefined = addProcessPlugin(dataset, workflow, enforcedPluginType, metisPlugins,
        firstPluginDefined, PluginType.TRANSFORMATION);
    firstPluginDefined = addProcessPlugin(dataset, workflow, enforcedPluginType, metisPlugins,
        firstPluginDefined, PluginType.VALIDATION_INTERNAL);
    firstPluginDefined = addProcessPlugin(dataset, workflow, enforcedPluginType, metisPlugins,
        firstPluginDefined, PluginType.ENRICHMENT);
    return firstPluginDefined;
  }

  private boolean addProcessPlugin(Dataset dataset, Workflow workflow,
      PluginType enforcedPluginType, List<AbstractMetisPlugin> metisPlugins,
      boolean firstPluginDefined, PluginType pluginType) throws PluginExecutionNotAllowed {
    AbstractMetisPluginMetadata pluginMetadata = workflow.getPluginMetadata(pluginType);
    if (pluginMetadata != null && pluginMetadata.isEnabled()) {
      if (!firstPluginDefined) {
        AbstractMetisPlugin previousPlugin = getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(
            dataset.getDatasetId(), pluginMetadata.getPluginType(), enforcedPluginType);
        pluginMetadata.setRevisionNamePreviousPlugin(previousPlugin.getPluginType().name());
        pluginMetadata.setRevisionTimestampPreviousPlugin(previousPlugin.getStartedDate());
      }
      AbstractMetisPlugin abstractMetisPlugin;
      if (pluginType == PluginType.VALIDATION_EXTERNAL) {
        abstractMetisPlugin = new ValidationExternalPlugin(pluginMetadata);
      } else if (pluginType == PluginType.TRANSFORMATION) {
        setupXsltUrlForPluginMetadata(dataset, pluginMetadata);
        abstractMetisPlugin = new TransformationPlugin(pluginMetadata);
      } else if (pluginType == PluginType.VALIDATION_INTERNAL) {
        abstractMetisPlugin = new ValidationInternalPlugin(pluginMetadata);
      } else if (pluginType == PluginType.ENRICHMENT) {
        abstractMetisPlugin = new EnrichmentPlugin(pluginMetadata);
      } else {
        //This is practically impossible to happen since the pluginMetadata has to be valid in the Workflow using a pluginType, before reaching this state.
        throw new PluginExecutionNotAllowed(CommonStringValues.PLUGIN_EXECUTION_NOT_ALLOWED);
      }
      abstractMetisPlugin
          .setId(new ObjectId().toString() + "-" + abstractMetisPlugin.getPluginType().name());
      metisPlugins.add(abstractMetisPlugin);
      firstPluginDefined = true;
    }
    return firstPluginDefined;
  }

  private void setupXsltUrlForPluginMetadata(Dataset dataset,
      AbstractMetisPluginMetadata abstractMetisPluginMetadata) {
    DatasetXslt xsltObject;
    if (((TransformationPluginMetadata) abstractMetisPluginMetadata).isCustomXslt()) {
      xsltObject = datasetXsltDao.getById(dataset.getXsltId().toString());
    } else {
      xsltObject = datasetXsltDao.getLatestXsltForDatasetId(DatasetXsltDao.DEFAULT_DATASET_ID);
    }
    if (xsltObject != null && StringUtils.isNotEmpty(xsltObject.getXslt())) {
      ((TransformationPluginMetadata) abstractMetisPluginMetadata)
          .setXsltUrl(metisCoreUrl + RestEndpoints
              .resolve(RestEndpoints.DATASETS_XSLT_XSLTID, xsltObject.getId().toString()));
    }
  }

  /**
   * Request to cancel a workflow execution.
   * The execution will go into a cancelling state until it's properly {@link WorkflowStatus#CANCELLED} from the system
   *
   * @param executionId the execution identifier of the execution to cancel
   * @throws NoWorkflowExecutionFoundException if no worklfowExecution could be found
   */
  public void cancelWorkflowExecution(String executionId)
      throws NoWorkflowExecutionFoundException {

    WorkflowExecution workflowExecution = workflowExecutionDao
        .getById(executionId);
    if (workflowExecution != null && (
        workflowExecution.getWorkflowStatus() == WorkflowStatus.RUNNING
            || workflowExecution.getWorkflowStatus() == WorkflowStatus.INQUEUE)) {
      workflowExecutionDao.setCancellingState(workflowExecution);
      LOGGER.info(
          "Cancelling user workflow execution with id: {}", workflowExecution.getId());
    } else {
      throw new NoWorkflowExecutionFoundException(String.format(
          "Running workflowExecution with executionId: %s, does not exist or not active",
          executionId));
    }
  }

  /**
   * Cleans a workflowExecutions list and removes active executions.
   *
   * @param workflowExecutions the list of workflowExecutions to clean
   */
  public void removeActiveWorkflowExecutionsFromList(
      List<WorkflowExecution> workflowExecutions) {
    workflowExecutionDao
        .removeActiveExecutionsFromList(workflowExecutions,
            workflowExecutorManager.getMonitorCheckIntervalInSecs());
  }

  /**
   * Adds the workflowExecution identifier to the distributed queue.
   *
   * @param workflowExecutionObjectId the workflowExecution identifier
   * @param priority the priority of the execution in the queue, 0 lowest, 10 highest
   */
  public void addWorkflowExecutionToQueue(String workflowExecutionObjectId,
      int priority) {
    workflowExecutorManager
        .addWorkflowExecutionToQueue(workflowExecutionObjectId, priority);
  }

  private void checkRestrictionsOnWorkflowCreate(Workflow workflow)
      throws WorkflowAlreadyExistsException {

    if (StringUtils.isNotEmpty(workflowExists(workflow))) {
      throw new WorkflowAlreadyExistsException(String.format(
          "Workflow with workflowOwner: %s, and datasetId: %s, already exists",
          workflow.getWorkflowOwner(), workflow.getDatasetId()));
    }
  }

  private Workflow checkRestrictionsOnWorkflowUpdate(Workflow workflow)
      throws NoWorkflowFoundException {

    Workflow storedWorkflow = getWorkflow(workflow.getDatasetId());
    if (storedWorkflow == null) {
      throw new NoWorkflowFoundException(String.format(
          "Workflow with datasetId: %s, not found", workflow.getDatasetId()));
    }

    return storedWorkflow;
  }

  private String workflowExists(Workflow workflow) {
    return workflowDao.exists(workflow);
  }

  /**
   * The number of WorkflowExecutions that would be returned if a get all request would be performed.
   *
   * @return the number representing the size during a get all request
   */
  public int getWorkflowExecutionsPerRequest() {
    return workflowExecutionDao.getWorkflowExecutionsPerRequest();
  }

  /**
   * The number of Workflows that would be returned if a get all request would be performed.
   *
   * @return the number representing the size during a get all request
   */
  public int getWorkflowsPerRequest() {
    return workflowDao.getWorkflowsPerRequest();
  }

  /**
   * Check if a specified {@code pluginType} is allowed for execution.
   * This is checked based on, if there was a previous successful finished plugin that follows a specific order unless the {@code enforcedPluginType} is used.
   *
   * @param datasetId the dataset identifier of which the executions are based on
   * @param pluginType the pluginType to be checked for allowance of execution
   * @param enforcedPluginType optional, the plugin type to be used as source data
   * @return the abstractMetisPlugin that the execution on {@code pluginType} will be based on. Can be null if the
   * {@code pluginType} is the first one in the total order of executions e.g. One of the harvesting plugins.
   * @throws PluginExecutionNotAllowed if the no plugin was found so the {@code pluginType} will be based upon.
   */
  public AbstractMetisPlugin getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(
      int datasetId, PluginType pluginType,
      PluginType enforcedPluginType) throws PluginExecutionNotAllowed {
    AbstractMetisPlugin latestFinishedPluginIfRequestedPluginAllowedForExecution = ExecutionRules
        .getLatestFinishedPluginIfRequestedPluginAllowedForExecution(pluginType, enforcedPluginType,
            datasetId, workflowExecutionDao);
    if ((latestFinishedPluginIfRequestedPluginAllowedForExecution == null
        && !ExecutionRules.getHarvestPluginGroup().contains(pluginType))
        || doesPluginHaveAllErrorRecords(
        latestFinishedPluginIfRequestedPluginAllowedForExecution)) {
      throw new PluginExecutionNotAllowed(CommonStringValues.PLUGIN_EXECUTION_NOT_ALLOWED);
    }
    return latestFinishedPluginIfRequestedPluginAllowedForExecution;
  }

  private boolean doesPluginHaveAllErrorRecords(AbstractMetisPlugin abstractMetisPlugin) {
    return abstractMetisPlugin != null && abstractMetisPlugin.getExecutionProgress() != null
        && abstractMetisPlugin.getExecutionProgress().getProcessedRecords() == abstractMetisPlugin
        .getExecutionProgress().getErrors();
  }

  /**
   * Get all WorkflowExecutions paged.
   *
   * @param datasetId the dataset identifier filter, can be -1 to get all datasets
   * @param workflowOwner the workflow owner, can be null
   * @param workflowStatuses a set of workflow statuses to filter, can be empty or null
   * @param orderField the field to be used to sort the results
   * @param ascending a boolean value to request the ordering to ascending or descending
   * @param nextPage the nextPage token
   * @return a list of all the WorkflowExecutions found
   */
  public List<WorkflowExecution> getAllWorkflowExecutions(int datasetId, String workflowOwner,
      Set<WorkflowStatus> workflowStatuses, OrderField orderField, boolean ascending,
      int nextPage) {
    return workflowExecutionDao
        .getAllWorkflowExecutions(datasetId, workflowOwner,
            workflowStatuses,
            orderField, ascending, nextPage);
  }

  /**
   * Retrieve dataset level information of past executions {@link DatasetExecutionInformation}
   *
   * @param datasetId the dataset identifier to generate the information for
   * @return the structured class containing all the execution information
   */
  public DatasetExecutionInformation getDatasetExecutionInformation(int datasetId) {
    AbstractMetisPlugin lastHarvestPlugin = workflowExecutionDao
        .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(datasetId, EnumSet
            .of(PluginType.HTTP_HARVEST, PluginType.OAIPMH_HARVEST));
    AbstractMetisPlugin firstPublishPlugin = workflowExecutionDao
        .getFirstFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(datasetId, EnumSet
            .of(PluginType.PUBLISH));
    AbstractMetisPlugin lastPublishPlugin = workflowExecutionDao
        .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(datasetId, EnumSet
            .of(PluginType.PUBLISH));

    DatasetExecutionInformation datasetExecutionInformation = new DatasetExecutionInformation();
    if (lastHarvestPlugin != null) {
      datasetExecutionInformation.setLastHarvestedDate(lastHarvestPlugin.getFinishedDate());
      datasetExecutionInformation.setLastHarvestedRecords(
          lastHarvestPlugin.getExecutionProgress().getProcessedRecords() - lastHarvestPlugin
              .getExecutionProgress().getErrors());
    }
    datasetExecutionInformation.setFirstPublishedDate(firstPublishPlugin == null ? null :
        firstPublishPlugin.getFinishedDate());
    if (lastPublishPlugin != null) {
      datasetExecutionInformation.setLastPublishedDate(lastPublishPlugin.getFinishedDate());
      datasetExecutionInformation.setLastPublishedRecords(
          lastPublishPlugin.getExecutionProgress().getProcessedRecords() - lastPublishPlugin
              .getExecutionProgress().getErrors());
    }

    return datasetExecutionInformation;
  }

  private Dataset checkDatasetExistence(int datasetId) throws NoDatasetFoundException {
    Dataset dataset = datasetDao.getDatasetByDatasetId(datasetId);
    if (dataset == null) {
      throw new NoDatasetFoundException(
          String.format("No dataset found with datasetId: %s, in METIS", datasetId));
    }
    return dataset;
  }

  private Workflow checkWorkflowExistence(int datasetId)
      throws NoWorkflowFoundException {
    Workflow workflow = workflowDao
        .getWorkflow(datasetId);
    if (workflow == null) {
      throw new NoWorkflowFoundException(
          String.format("No workflow found with datasetId: %s, in METIS", datasetId));
    }
    return workflow;
  }

  public void setMetisCoreUrl(String metisCoreUrl) {
    synchronized (this) {
      this.metisCoreUrl = metisCoreUrl;
    }
  }
}
