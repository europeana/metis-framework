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

  public void createWorkflow(int datasetId, Workflow workflow)
      throws WorkflowAlreadyExistsException, NoDatasetFoundException {
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

  public void updateWorkflow(int datasetId, Workflow workflow)
      throws NoWorkflowFoundException, NoDatasetFoundException {
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

  public void deleteWorkflow(int datasetId) {
    workflowDao.deleteWorkflow(datasetId);
  }

  public Workflow getWorkflow(int datasetId) {
    return workflowDao.getWorkflow(datasetId);
  }

  public List<Workflow> getAllWorkflows(String workflowOwner, int nextPage) {
    return workflowDao.getAllWorkflows(workflowOwner, nextPage);
  }

  public WorkflowExecution getWorkflowExecutionByExecutionId(String executionId) {
    return workflowExecutionDao.getById(executionId);
  }

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

  //Used for direct, on the fly provided, execution of a Workflow
  public WorkflowExecution addWorkflowInQueueOfWorkflowExecutions(int datasetId,
      Workflow workflow, PluginType enforcedPluginType,
      int priority)
      throws GenericMetisException {
    Dataset dataset = checkDatasetExistence(datasetId);
    //Generate uuid workflowName and check if by any chance it exists.
    checkRestrictionsOnWorkflowCreate(workflow);
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
          String.format(
              "Workflow execution for datasetId: %s, already exists with id: %s, and is not completed",
              datasetId, storedWorkflowExecutionId));
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

  public void cancelWorkflowExecution(String executionId)
      throws NoWorkflowExecutionFoundException {

    WorkflowExecution workflowExecution = workflowExecutionDao
        .getById(executionId);
    if (workflowExecution != null && (
        workflowExecution.getWorkflowStatus() == WorkflowStatus.RUNNING
            || workflowExecution.getWorkflowStatus() == WorkflowStatus.INQUEUE)) {
      workflowExecutorManager.cancelWorkflowExecution(workflowExecution);
    } else {
      throw new NoWorkflowExecutionFoundException(String.format(
          "Running workflowExecution with executionId: %s, does not exist or not active",
          executionId));
    }
  }

  public void removeActiveWorkflowExecutionsFromList(
      List<WorkflowExecution> workflowExecutions) {
    workflowExecutionDao
        .removeActiveExecutionsFromList(workflowExecutions,
            workflowExecutorManager.getMonitorCheckIntervalInSecs());
  }

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

  public int getWorkflowExecutionsPerRequest() {
    return workflowExecutionDao.getWorkflowExecutionsPerRequest();
  }

  public int getWorkflowsPerRequest() {
    return workflowDao.getWorkflowsPerRequest();
  }

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

  public List<WorkflowExecution> getAllWorkflowExecutions(int datasetId,
      String workflowOwner,
      Set<WorkflowStatus> workflowStatuses, OrderField orderField, boolean ascending,
      int nextPage) {
    return workflowExecutionDao
        .getAllWorkflowExecutions(datasetId, workflowOwner,
            workflowStatuses,
            orderField, ascending, nextPage);
  }

  /**
   * Retrieve dataset level information of past executions {@link DatasetExecutionInformation}
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
    datasetExecutionInformation.setFirstPublishedDate(firstPublishPlugin != null ?
        firstPublishPlugin.getFinishedDate() : null);
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
    this.metisCoreUrl = metisCoreUrl;
  }
}
