package eu.europeana.metis.core.service;

import eu.europeana.metis.CommonStringValues;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.authentication.user.AccountRole;
import eu.europeana.metis.authentication.user.MetisUser;
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
import eu.europeana.metis.core.workflow.ValidationProperties;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.HTTPHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPreviewPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPublishPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.core.workflow.plugins.TransformationPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ValidationExternalPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ValidationInternalPluginMetadata;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.ExternalTaskException;
import eu.europeana.metis.exception.GenericMetisException;
import eu.europeana.metis.exception.UserUnauthorizedException;
import eu.europeana.metis.utils.DateUtils;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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
  private final Authorizer authorizer;

  private ValidationProperties validationExternalProperties; // Use getter and setter!
  private ValidationProperties validationInternalProperties; // Use getter and setter!
  private String metisCoreUrl; // Use getter and setter for this field!
  private boolean metisUseAlternativeIndexingEnvironment; // Use getter and setter for this field!
  private int solrCommitPeriodInMins; // Use getter and setter for this field!

  /**
   * Constructor with all the required parameters
   *
   * @param workflowDao the Dao instance to access the Workflow database
   * @param workflowExecutionDao the Dao instance to access the WorkflowExecution database
   * @param datasetDao the Dao instance to access the Dataset database
   * @param datasetXsltDao the Dao instance to access the DatasetXslt database
   * @param workflowExecutorManager the instance that handles the production and consumption of
   * workflowExecutions
   * @param redissonClient the instance of Redisson library that handles distributed locks
   * @param authorizer the authorizer
   * @throws IOException that can be thrown when initializing the {@link WorkflowExecutorManager}
   */
  @Autowired
  public OrchestratorService(WorkflowDao workflowDao, WorkflowExecutionDao workflowExecutionDao,
      DatasetDao datasetDao, DatasetXsltDao datasetXsltDao,
      WorkflowExecutorManager workflowExecutorManager, RedissonClient redissonClient,
      Authorizer authorizer) {
    this.workflowDao = workflowDao;
    this.workflowExecutionDao = workflowExecutionDao;
    this.datasetDao = datasetDao;
    this.datasetXsltDao = datasetXsltDao;
    this.workflowExecutorManager = workflowExecutorManager;
    this.redissonClient = redissonClient;
    this.authorizer = authorizer;
  }

  /**
   * Create a workflow using a datasetId and the {@link Workflow} that contains the requested
   * plugins. When creating a new workflow all the plugins specified will be automatically enabled.
   *
   * @param metisUser the user wishing to perform this operation
   * @param datasetId the identifier of the dataset for which the workflow should be created
   * @param workflow the workflow with the plugins requested
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link WorkflowAlreadyExistsException} if a workflow for the dataset identifier provided
   * already exists</li>
   * <li>{@link NoDatasetFoundException} if the dataset identifier provided does not exist</li>
   * <li>{@link UserUnauthorizedException} if the user is not authorized to perform this task</li>
   * </ul>
   */
  public void createWorkflow(MetisUser metisUser, String datasetId, Workflow workflow)
      throws GenericMetisException {
    authorizer.authorizeWriteExistingDatasetById(metisUser, datasetId);
    try {
      validateAndTrimHarvestParameters(workflow);
    } catch (MalformedURLException | URISyntaxException e) {
      throw new BadContentException("Harvesting parameters are invalid", e);
    }
    if (datasetDao.getDatasetByDatasetId(datasetId) == null) {
      throw new NoDatasetFoundException(
          String.format("Dataset with datasetId: %s does NOT exist", datasetId));
    }
    workflow.setDatasetId(datasetId);
    checkRestrictionsOnWorkflowCreate(datasetId, workflow);
    workflow.getMetisPluginsMetadata()
        .forEach(abstractMetisPluginMetadata -> abstractMetisPluginMetadata.setEnabled(true));
    workflowDao.create(workflow);
  }

  /**
   * Update an already existent workflow using a datasetId and the {@link Workflow} that contains
   * the requested plugins. When updating an existent workflow all specified plugins will be enabled
   * and all plugins that were existent in the system beforehand will be kept with their
   * configuration but will be disabled.
   *
   * @param metisUser the user wishing to perform this operation
   * @param datasetId the identifier of the dataset for which the workflow should be updated
   * @param workflow the workflow with the plugins requested
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoWorkflowFoundException} if a workflow for the dataset identifier provided does
   * not exist</li>
   * <li>{@link NoDatasetFoundException} if the dataset identifier provided does not exist</li>
   * <li>{@link UserUnauthorizedException} if the user is not authorized to perform this task</li>
   * </ul>
   */
  public void updateWorkflow(MetisUser metisUser, String datasetId, Workflow workflow)
      throws GenericMetisException {
    authorizer.authorizeWriteExistingDatasetById(metisUser, datasetId);
    try {
      validateAndTrimHarvestParameters(workflow);
    } catch (MalformedURLException | URISyntaxException e) {
      throw new BadContentException("Harvesting parameters are invalid", e);
    }
    if (datasetDao.getDatasetByDatasetId(datasetId) == null) {
      throw new NoDatasetFoundException(
          String.format("Dataset with datasetId: %s does NOT exist", datasetId));
    }
    workflow.setDatasetId(datasetId);
    Workflow storedWorkflow = checkRestrictionsOnWorkflowUpdate(datasetId, workflow);
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
    return getWorkflow(datasetId);
  }

  private Workflow getWorkflow(String datasetId) {
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
   * enforcedPluginType}, which means that the last valid plugin that is provided with that
   * parameter, will be used as the source data. </p>
   * <p> <b>Please note:</b> this method is not checked for authorization: it is only meant to be
   * called from a scheduled task. </p>
   *
   * @param datasetId the dataset identifier for which the execution will take place
   * @param enforcedPluginType optional, the plugin type to be used as source data
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
      String datasetId, PluginType enforcedPluginType, int priority) throws GenericMetisException {
    final Dataset dataset = datasetDao.getDatasetByDatasetId(datasetId);
    if (dataset == null) {
      throw new NoDatasetFoundException(
          String.format("No dataset found with datasetId: %s, in METIS", datasetId));
    }
    return addWorkflowInQueueOfWorkflowExecutions(dataset, enforcedPluginType, priority);
  }

  /**
   * Does checking, prepares and adds a WorkflowExecution in the queue. That means it updates the
   * status of the WorkflowExecution to {@link WorkflowStatus#INQUEUE}, adds it to the database and
   * also it's identifier goes into the distributed queue of WorkflowExecutions. The source data for
   * the first plugin in the workflow can be controlled, if required, from the {@code
   * enforcedPluginType}, which means that the last valid plugin that is provided with that
   * parameter, will be used as the source data.
   *
   * @param metisUser the user wishing to perform this operation
   * @param datasetId the dataset identifier for which the execution will take place
   * @param enforcedPluginType optional, the plugin type to be used as source data
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
      String datasetId, PluginType enforcedPluginType, int priority) throws GenericMetisException {
    final Dataset dataset = authorizer.authorizeWriteExistingDatasetById(metisUser, datasetId);
    return addWorkflowInQueueOfWorkflowExecutions(dataset, enforcedPluginType, priority);
  }

  private WorkflowExecution addWorkflowInQueueOfWorkflowExecutions(Dataset dataset,
      PluginType enforcedPluginType, int priority) throws GenericMetisException {
    Workflow workflow = checkWorkflowExistence(dataset.getDatasetId());

    List<AbstractMetisPlugin> metisPlugins = createMetisPluginsList(dataset, workflow,
        enforcedPluginType);
    //metisPlugins will be empty if the workflow was empty or all the plugins inside the workflow were disabled.
    if (metisPlugins.isEmpty()) {
      throw new BadContentException("Workflow has either no plugins or are all disabled");
    }

    datasetDao.checkAndCreateDatasetInEcloud(dataset);
    WorkflowExecution workflowExecution = new WorkflowExecution(dataset, workflow, metisPlugins,
        priority);
    workflowExecution.setWorkflowStatus(WorkflowStatus.INQUEUE);
    RLock executionDatasetIdLock = redissonClient
        .getFairLock(String.format(EXECUTION_FOR_DATASETID_SUBMITION_LOCK, dataset.getDatasetId()));
    executionDatasetIdLock.lock();
    String storedWorkflowExecutionId = workflowExecutionDao
        .existsAndNotCompleted(dataset.getDatasetId());
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
      PluginType enforcedPluginType) throws PluginExecutionNotAllowed {
    List<AbstractMetisPlugin> metisPlugins = new ArrayList<>();

    boolean firstPluginDefined = addHarvestingPlugin(dataset, workflow, metisPlugins);
    addNonHarvestPlugins(dataset, workflow, enforcedPluginType, metisPlugins, firstPluginDefined);
    return metisPlugins;
  }

  private boolean addHarvestingPlugin(Dataset dataset, Workflow workflow,
      List<AbstractMetisPlugin> metisPlugins) {
    OaipmhHarvestPluginMetadata oaipmhMetadata =
        (OaipmhHarvestPluginMetadata) workflow.getPluginMetadata(PluginType.OAIPMH_HARVEST);
    HTTPHarvestPluginMetadata httpMetadata = (HTTPHarvestPluginMetadata) workflow
        .getPluginMetadata(PluginType.HTTP_HARVEST);
    final AbstractMetisPlugin plugin;
    if (oaipmhMetadata != null && oaipmhMetadata.isEnabled()) {
      plugin = PluginType.OAIPMH_HARVEST.getNewPlugin(oaipmhMetadata);
      oaipmhMetadata.setDatasetId(dataset.getDatasetId());
    } else if (httpMetadata != null && httpMetadata.isEnabled()) {
      plugin = PluginType.HTTP_HARVEST.getNewPlugin(httpMetadata);
      httpMetadata.setDatasetId(dataset.getDatasetId());
    } else {
      plugin = null;
    }
    if (plugin != null) {
      plugin.setId(new ObjectId().toString() + "-" + plugin.getPluginType().name());
      metisPlugins.add(plugin);
      return true;
    }
    return false;
  }

  private boolean addNonHarvestPlugins(Dataset dataset, Workflow workflow,
      PluginType enforcedPluginType, List<AbstractMetisPlugin> metisPlugins,
      boolean firstPluginDefined) throws PluginExecutionNotAllowed {
    firstPluginDefined = addNonHarvestPlugin(dataset, workflow, enforcedPluginType, metisPlugins,
        firstPluginDefined, PluginType.VALIDATION_EXTERNAL);
    firstPluginDefined = addNonHarvestPlugin(dataset, workflow, enforcedPluginType, metisPlugins,
        firstPluginDefined, PluginType.TRANSFORMATION);
    firstPluginDefined = addNonHarvestPlugin(dataset, workflow, enforcedPluginType, metisPlugins,
        firstPluginDefined, PluginType.VALIDATION_INTERNAL);
    firstPluginDefined = addNonHarvestPlugin(dataset, workflow, enforcedPluginType, metisPlugins,
        firstPluginDefined, PluginType.NORMALIZATION);
    firstPluginDefined = addNonHarvestPlugin(dataset, workflow, enforcedPluginType, metisPlugins,
        firstPluginDefined, PluginType.ENRICHMENT);
    firstPluginDefined = addNonHarvestPlugin(dataset, workflow, enforcedPluginType, metisPlugins,
        firstPluginDefined, PluginType.MEDIA_PROCESS);
    firstPluginDefined = addNonHarvestPlugin(dataset, workflow, enforcedPluginType, metisPlugins,
        firstPluginDefined, PluginType.LINK_CHECKING);
    firstPluginDefined = addNonHarvestPlugin(dataset, workflow, enforcedPluginType, metisPlugins,
        firstPluginDefined, PluginType.PREVIEW);
    firstPluginDefined = addNonHarvestPlugin(dataset, workflow, enforcedPluginType, metisPlugins,
        firstPluginDefined, PluginType.PUBLISH);
    return firstPluginDefined;
  }

  private boolean addNonHarvestPlugin(Dataset dataset, Workflow workflow,
      PluginType enforcedPluginType, List<AbstractMetisPlugin> metisPlugins,
      boolean firstPluginDefined, PluginType pluginType) throws PluginExecutionNotAllowed {
    AbstractMetisPluginMetadata pluginMetadata = workflow.getPluginMetadata(pluginType);
    if (pluginMetadata != null && pluginMetadata.isEnabled()) {
      if (!firstPluginDefined) {
        AbstractMetisPlugin previousPlugin = getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(
            dataset.getDatasetId(), pluginMetadata.getPluginType(), enforcedPluginType);
        pluginMetadata
            .setPreviousRevisionInformation(previousPlugin); //Set all previous revision information
      }

      // Sanity check
      if (ExecutionRules.getHarvestPluginGroup().contains(pluginType)) {
        //This is practically impossible to happen since the pluginMetadata has to be valid in the Workflow using a pluginType, before reaching this state.
        throw new PluginExecutionNotAllowed(CommonStringValues.PLUGIN_EXECUTION_NOT_ALLOWED);
      }

      // Add some extra configuration to the plugin metadata
      switch (pluginType) {
        case TRANSFORMATION:
          setupXsltUrlForPluginMetadata(dataset, pluginMetadata);
          break;
        case VALIDATION_EXTERNAL:
          setupValidationForPluginMetadata(pluginMetadata, getValidationExternalProperties());
          break;
        case VALIDATION_INTERNAL:
          setupValidationForPluginMetadata(pluginMetadata, getValidationInternalProperties());
          break;
        case PREVIEW:
          ((IndexToPreviewPluginMetadata) pluginMetadata).setUseAlternativeIndexingEnvironment(
              getMetisUseAlternativeIndexingEnvironment());
          break;
        case PUBLISH:
          ((IndexToPublishPluginMetadata) pluginMetadata).setUseAlternativeIndexingEnvironment(
              getMetisUseAlternativeIndexingEnvironment());
          break;
        default:
          break;
      }

      // Create plugin
      AbstractMetisPlugin abstractMetisPlugin = pluginType.getNewPlugin(pluginMetadata);
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
          .setXsltUrl(getMetisCoreUrl() + RestEndpoints
              .resolve(RestEndpoints.DATASETS_XSLT_XSLTID, xsltObject.getId().toString()));
    }
    ((TransformationPluginMetadata) abstractMetisPluginMetadata)
        .setDatasetId(dataset.getDatasetId());
    //DatasetName in Transformation should be a concatenation datasetId_datasetName
    ((TransformationPluginMetadata) abstractMetisPluginMetadata)
        .setDatasetName(dataset.getDatasetId() + "_" + dataset.getDatasetName());
    ((TransformationPluginMetadata) abstractMetisPluginMetadata)
        .setCountry(dataset.getCountry().getName());
    ((TransformationPluginMetadata) abstractMetisPluginMetadata)
        .setLanguage(dataset.getLanguage().name().toLowerCase(Locale.US));
  }

  private static void setupValidationForPluginMetadata(AbstractMetisPluginMetadata metadata,
      ValidationProperties validationProperties) {
    if (metadata instanceof ValidationExternalPluginMetadata) {
      final ValidationExternalPluginMetadata castMetadata =
          (ValidationExternalPluginMetadata) metadata;
      castMetadata.setUrlOfSchemasZip(validationProperties.getUrlOfSchemasZip());
      castMetadata.setSchemaRootPath(validationProperties.getSchemaRootPath());
      castMetadata.setSchematronRootPath(validationProperties.getSchematronRootPath());
    } else if (metadata instanceof ValidationInternalPluginMetadata) {
      final ValidationInternalPluginMetadata castMetadata =
          (ValidationInternalPluginMetadata) metadata;
      castMetadata.setUrlOfSchemasZip(validationProperties.getUrlOfSchemasZip());
      castMetadata.setSchemaRootPath(validationProperties.getSchemaRootPath());
      castMetadata.setSchematronRootPath(validationProperties.getSchematronRootPath());
    } else {
      throw new IllegalArgumentException("The provided metadata does not have the right type. "
          + "Expecting metadata for a validation plugin, but instead received metadata of type "
          + metadata.getClass().getName() + ".");
    }
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
      workflowExecutionDao.setCancellingState(workflowExecution);
      LOGGER.info(
          "Cancelling user workflow execution with id: {}", workflowExecution.getId());
    } else {
      throw new NoWorkflowExecutionFoundException(String.format(
          "Running workflowExecution with executionId: %s, does not exist or not active",
          executionId));
    }
  }

  private void checkRestrictionsOnWorkflowCreate(String datasetId, Workflow workflow)
      throws WorkflowAlreadyExistsException, PluginExecutionNotAllowed {

    if (StringUtils.isNotEmpty(workflowExists(workflow))) {
      throw new WorkflowAlreadyExistsException(
          String.format("Workflow with datasetId: %s, already exists", workflow.getDatasetId()));
    }
    workflowOrderValidator(datasetId, workflow);
  }

  private Workflow checkRestrictionsOnWorkflowUpdate(String datasetId, Workflow workflow)
      throws NoWorkflowFoundException, PluginExecutionNotAllowed {

    Workflow storedWorkflow = getWorkflow(workflow.getDatasetId());
    if (storedWorkflow == null) {
      throw new NoWorkflowFoundException(String.format(
          "Workflow with datasetId: %s, not found", workflow.getDatasetId()));
    }
    workflowOrderValidator(datasetId, workflow);

    return storedWorkflow;
  }

  private void workflowOrderValidator(String datasetId, Workflow workflow)
      throws PluginExecutionNotAllowed {
    //Workflow should not have duplicated plugins.
    if (listContainsDuplicates(workflow.getMetisPluginsMetadata())) {
      throw new PluginExecutionNotAllowed(CommonStringValues.PLUGIN_EXECUTION_NOT_ALLOWED);
    }
    // Sanity check, for the first plugin, that will throw exception if there is NO pluginType to be
    // based on in the database.
    getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(datasetId,
        workflow.getMetisPluginsMetadata().get(0).getPluginType(), null);
    // If ok then check the order of all subsequent plugins. Start from index 1.
    final boolean valid = workflow.getMetisPluginsMetadata().stream().skip(1)
        .map(AbstractMetisPluginMetadata::getPluginType)
        .filter(pluginType -> !ExecutionRules.getHarvestPluginGroup().contains(pluginType))
        .allMatch(pluginType -> checkWorkflowForPluginType(workflow, pluginType));
    if (!valid) {
      throw new PluginExecutionNotAllowed(CommonStringValues.PLUGIN_EXECUTION_NOT_ALLOWED);
    }
  }

  private static <T> boolean listContainsDuplicates(List<T> list) {
    return !list.stream().allMatch(new HashSet<>()::add);
  }

  private static boolean checkWorkflowForPluginType(Workflow workflow, PluginType pluginType) {
    final Set<PluginType> pluginTypesSetThatPluginTypeCanBeBasedOn =
        ExecutionRules.getPluginTypesSetThatPluginTypeCanBeBasedOn(pluginType);
    return workflow.pluginTypeOccursOnlyAfter(pluginType, pluginTypesSetThatPluginTypeCanBeBasedOn);
  }

  private String workflowExists(Workflow workflow) {
    return workflowDao.exists(workflow);
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
   * The number of Workflows that would be returned if a get all request would be performed.
   *
   * @return the number representing the size during a get all request
   */
  public int getWorkflowsPerRequest() {
    return workflowDao.getWorkflowsPerRequest();
  }

  /**
   * Check if a specified {@code pluginType} is allowed for execution. This is checked based on, if
   * there was a previous successful finished plugin that follows a specific order unless the {@code
   * enforcedPluginType} is used.
   *
   * @param metisUser the user wishing to perform this operation
   * @param datasetId the dataset identifier of which the executions are based on
   * @param pluginType the pluginType to be checked for allowance of execution
   * @param enforcedPluginType optional, the plugin type to be used as source data
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
  public AbstractMetisPlugin getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(
      MetisUser metisUser, String datasetId, PluginType pluginType, PluginType enforcedPluginType)
      throws GenericMetisException {
    authorizer.authorizeReadExistingDatasetById(metisUser, datasetId);
    return getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(datasetId, pluginType,
        enforcedPluginType);
  }

  private AbstractMetisPlugin getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(
      String datasetId, PluginType pluginType, PluginType enforcedPluginType)
      throws PluginExecutionNotAllowed {
    AbstractMetisPlugin latestFinishedPluginIfRequestedPluginAllowedForExecution =
        ExecutionRules.getLatestFinishedPluginIfRequestedPluginAllowedForExecution(pluginType,
            enforcedPluginType, datasetId, workflowExecutionDao);
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
  public List<WorkflowExecution> getAllWorkflowExecutions(MetisUser metisUser, String datasetId,
      Set<WorkflowStatus> workflowStatuses, OrderField orderField, boolean ascending, int nextPage)
      throws GenericMetisException {

    // Authorize
    if (datasetId == null) {
      authorizer.authorizeReadAllDatasets(metisUser);
    } else {
      authorizer.authorizeReadExistingDatasetById(metisUser, datasetId);
    }

    // Determine the dataset IDs to filter on.
    final Set<String> datasetIds;
    if (datasetId != null) {
      datasetIds = Collections.singleton(datasetId);
    } else if (metisUser.getAccountRole() == AccountRole.METIS_ADMIN) {
      datasetIds = null;
    } else {
      datasetIds = datasetDao.getAllDatasetsByOrganizationId(metisUser.getOrganizationId()).stream()
          .map(Dataset::getDatasetId).collect(Collectors.toSet());
    }

    // Find the executions.
    return workflowExecutionDao.getAllWorkflowExecutions(datasetIds, workflowStatuses, orderField,
        ascending, nextPage);
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

    AbstractMetisPlugin lastHarvestPlugin = workflowExecutionDao
        .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(datasetId, EnumSet
            .of(PluginType.HTTP_HARVEST, PluginType.OAIPMH_HARVEST));
    AbstractMetisPlugin firstPublishPlugin = workflowExecutionDao
        .getFirstFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(datasetId, EnumSet
            .of(PluginType.PUBLISH));
    AbstractMetisPlugin lastPublishPlugin = workflowExecutionDao
        .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(datasetId, EnumSet
            .of(PluginType.PUBLISH));
    AbstractMetisPlugin lastPreviewPlugin = workflowExecutionDao
        .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(datasetId, EnumSet
            .of(PluginType.PREVIEW));

    DatasetExecutionInformation datasetExecutionInformation = new DatasetExecutionInformation();
    if (lastHarvestPlugin != null) {
      datasetExecutionInformation.setLastHarvestedDate(lastHarvestPlugin.getFinishedDate());
      datasetExecutionInformation.setLastHarvestedRecords(
          lastHarvestPlugin.getExecutionProgress().getProcessedRecords() - lastHarvestPlugin
              .getExecutionProgress().getErrors());
    }
    datasetExecutionInformation.setFirstPublishedDate(firstPublishPlugin == null ? null :
        firstPublishPlugin.getFinishedDate());
    Date currentDate = new Date();
    if (lastPublishPlugin != null) {
      datasetExecutionInformation.setLastPublishedDate(lastPublishPlugin.getFinishedDate());
      datasetExecutionInformation.setLastPublishedRecords(
          lastPublishPlugin.getExecutionProgress().getProcessedRecords() - lastPublishPlugin
              .getExecutionProgress().getErrors());
      datasetExecutionInformation.setLastPublishedRecordsReadyForViewing(
          DateUtils.calculateDateDifference(lastPublishPlugin.getFinishedDate(), currentDate,
              TimeUnit.MINUTES) > getSolrCommitPeriodInMins());
    }
    if (lastPreviewPlugin != null) {
      datasetExecutionInformation.setLastPreviewDate(lastPreviewPlugin.getFinishedDate());
      datasetExecutionInformation.setLastPreviewRecords(
          lastPreviewPlugin.getExecutionProgress().getProcessedRecords() - lastPreviewPlugin
              .getExecutionProgress().getErrors());
      datasetExecutionInformation.setLastPreviewRecordsReadyForViewing(
          DateUtils.calculateDateDifference(lastPreviewPlugin.getFinishedDate(), currentDate,
              TimeUnit.MINUTES) > getSolrCommitPeriodInMins());
    }

    return datasetExecutionInformation;
  }

  private void validateAndTrimHarvestParameters(Workflow workflow)
      throws MalformedURLException, URISyntaxException {
    OaipmhHarvestPluginMetadata oaipmhPluginMetadata = (OaipmhHarvestPluginMetadata) workflow
        .getPluginMetadata(PluginType.OAIPMH_HARVEST);
    if (oaipmhPluginMetadata != null) {
      URL url = new URL(oaipmhPluginMetadata.getUrl().trim()); // this would check for the protocol
      URI validatedUri = url.toURI();// does the extra checking required for validation of URI

      //Remove all the query parameters
      String urlWithoutQueryParameters = new URI(validatedUri.getScheme(),
          validatedUri.getAuthority(), validatedUri.getPath(), null, null).toString();
      oaipmhPluginMetadata.setUrl(urlWithoutQueryParameters);
      oaipmhPluginMetadata.setMetadataFormat(oaipmhPluginMetadata.getMetadataFormat() == null ? null
          : oaipmhPluginMetadata.getMetadataFormat().trim());
      oaipmhPluginMetadata.setSetSpec(oaipmhPluginMetadata.getSetSpec() == null ? null
          : oaipmhPluginMetadata.getSetSpec().trim());
    }

    HTTPHarvestPluginMetadata httpHarvestPluginMetadata = (HTTPHarvestPluginMetadata) workflow
        .getPluginMetadata(PluginType.HTTP_HARVEST);
    if (httpHarvestPluginMetadata != null) {
      URL u = new URL(
          httpHarvestPluginMetadata.getUrl().trim()); // this would check for the protocol
      u.toURI(); // does the extra checking required for validation of URI
      httpHarvestPluginMetadata.setUrl(httpHarvestPluginMetadata.getUrl().trim());
    }
  }

  private Workflow checkWorkflowExistence(String datasetId)
      throws NoWorkflowFoundException {
    Workflow workflow = workflowDao.getWorkflow(datasetId);
    if (workflow == null) {
      throw new NoWorkflowFoundException(
          String.format("No workflow found with datasetId: %s, in METIS", datasetId));
    }
    return workflow;
  }

  public ValidationProperties getValidationExternalProperties() {
    synchronized (this) {
      return validationExternalProperties;
    }
  }

  public void setValidationExternalProperties(ValidationProperties validationExternalProperties) {
    synchronized (this) {
      this.validationExternalProperties = validationExternalProperties;
    }
  }

  public ValidationProperties getValidationInternalProperties() {
    synchronized (this) {
      return validationInternalProperties;
    }
  }

  public void setValidationInternalProperties(ValidationProperties validationInternalProperties) {
    synchronized (this) {
      this.validationInternalProperties = validationInternalProperties;
    }
  }

  public void setMetisCoreUrl(String metisCoreUrl) {
    synchronized (this) {
      this.metisCoreUrl = metisCoreUrl;
    }
  }

  private String getMetisCoreUrl() {
    synchronized (this) {
      return this.metisCoreUrl;
    }
  }

  public boolean getMetisUseAlternativeIndexingEnvironment() {
    synchronized (this) {
      return metisUseAlternativeIndexingEnvironment;
    }
  }

  public void setMetisUseAlternativeIndexingEnvironment(
      boolean metisUseAlternativeIndexingEnvironment) {
    synchronized (this) {
      this.metisUseAlternativeIndexingEnvironment = metisUseAlternativeIndexingEnvironment;
    }
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
