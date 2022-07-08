package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.common.model.Revision;
import eu.europeana.cloud.common.model.dps.TaskInfo;
import eu.europeana.cloud.service.dps.DpsTask;
import eu.europeana.cloud.service.dps.InputDataType;
import eu.europeana.cloud.service.dps.PluginParameterKeys;
import eu.europeana.cloud.service.dps.exception.DpsException;
import eu.europeana.metis.core.workflow.SystemId;
import eu.europeana.metis.exception.ExternalTaskException;
import eu.europeana.metis.utils.CommonStringValues;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This abstract class is the base implementation of {@link ExecutablePlugin} and all executable plugins should inherit from it.
 *
 * @param <M> The type of the plugin metadata that this plugin represents.
 */
public abstract class AbstractExecutablePlugin<M extends AbstractExecutablePluginMetadata> extends
    AbstractMetisPlugin<M> implements ExecutablePlugin {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractExecutablePlugin.class);

  private String externalTaskId;
  private ExecutionProgress executionProgress = new ExecutionProgress();

  /**
   * Required by (de)serialization in db.
   * <p>It is not to be used manually</p>
   */
  AbstractExecutablePlugin() {
    //Required by (de)serialization in db
  }

  /**
   * Constructor with provided pluginType
   *
   * @param pluginType {@link PluginType}
   */
  AbstractExecutablePlugin(PluginType pluginType) {
    super(pluginType);
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata required and the pluginType.
   *
   * @param pluginType a {@link PluginType} related to the implemented plugin
   * @param pluginMetadata the plugin metadata
   */
  AbstractExecutablePlugin(PluginType pluginType, M pluginMetadata) {
    super(pluginType, pluginMetadata);
  }

  @Override
  public String getExternalTaskId() {
    return this.externalTaskId;
  }

  /**
   * @param externalTaskId String representation of the external task identifier of the execution
   */
  public void setExternalTaskId(String externalTaskId) {
    this.externalTaskId = externalTaskId;
  }

  @Override
  public ExecutionProgress getExecutionProgress() {
    return this.executionProgress;
  }

  /**
   * @param executionProgress {@link ExecutionProgress} of the external execution
   */
  public void setExecutionProgress(ExecutionProgress executionProgress) {
    this.executionProgress = executionProgress;
  }

  private Revision createOutputRevisionForExecution(String ecloudProvider, boolean published) {
    return new Revision(getPluginType().name(), ecloudProvider, getStartedDate(), false, published, false);
  }

  private DpsTask createDpsTaskForPluginWithExistingDataset(Map<String, String> parameters,
      EcloudBasePluginParameters ecloudBasePluginParameters, boolean publish) {
    DpsTask dpsTask = new DpsTask();

    Map<InputDataType, List<String>> dataEntries = new EnumMap<>(InputDataType.class);
    dataEntries.put(InputDataType.DATASET_URLS, Collections.singletonList(String
        .format(CommonStringValues.S_DATA_PROVIDERS_S_DATA_SETS_S_TEMPLATE, ecloudBasePluginParameters.getEcloudBaseUrl(),
            ecloudBasePluginParameters.getEcloudProvider(), ecloudBasePluginParameters.getEcloudDatasetId())));
    dpsTask.setInputData(dataEntries);

    dpsTask.setParameters(parameters);
    dpsTask.setOutputRevision(createOutputRevisionForExecution(ecloudBasePluginParameters.getEcloudProvider(), publish));
    return dpsTask;
  }

  DpsTask createDpsTaskForHarvestPlugin(EcloudBasePluginParameters ecloudBasePluginParameters,
      Map<String, String> extraParameters, String targetUrl, boolean incrementalProcessing) {
    DpsTask dpsTask = new DpsTask();

    Map<InputDataType, List<String>> dataEntries = new EnumMap<>(InputDataType.class);
    dataEntries.put(InputDataType.REPOSITORY_URLS, Collections.singletonList(targetUrl));
    dpsTask.setInputData(dataEntries);

    Map<String, String> parameters = new HashMap<>();
    if (extraParameters != null) {
      parameters.putAll(extraParameters);
    }

    final DateFormat dateFormat = new SimpleDateFormat(CommonStringValues.DATE_FORMAT_Z, Locale.US);
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    parameters.put(PluginParameterKeys.INCREMENTAL_HARVEST, String.valueOf(incrementalProcessing));
    parameters.put(PluginParameterKeys.HARVEST_DATE, dateFormat.format(getStartedDate()));
    parameters.put(PluginParameterKeys.PROVIDER_ID, ecloudBasePluginParameters.getEcloudProvider());
    parameters.put(PluginParameterKeys.OUTPUT_DATA_SETS, String
        .format(CommonStringValues.S_DATA_PROVIDERS_S_DATA_SETS_S_TEMPLATE, ecloudBasePluginParameters.getEcloudBaseUrl(),
            ecloudBasePluginParameters.getEcloudProvider(), ecloudBasePluginParameters.getEcloudDatasetId()));
    parameters.put(PluginParameterKeys.NEW_REPRESENTATION_NAME, MetisPlugin.getRepresentationName());
    dpsTask.setParameters(parameters);

    dpsTask.setOutputRevision(createOutputRevisionForExecution(ecloudBasePluginParameters.getEcloudProvider(), false));
    return dpsTask;
  }

  DpsTask createDpsTaskForProcessPlugin(EcloudBasePluginParameters ecloudBasePluginParameters,
      Map<String, String> extraParameters) {
    Map<String, String> parameters = new HashMap<>();
    if (extraParameters != null) {
      parameters.putAll(extraParameters);
    }
    parameters.put(PluginParameterKeys.REPRESENTATION_NAME, MetisPlugin.getRepresentationName());
    parameters.put(PluginParameterKeys.REVISION_NAME, getPluginMetadata().getRevisionNamePreviousPlugin());
    parameters.put(PluginParameterKeys.REVISION_PROVIDER, ecloudBasePluginParameters.getEcloudProvider());
    DateFormat dateFormat = new SimpleDateFormat(CommonStringValues.DATE_FORMAT_Z, Locale.US);
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    parameters
        .put(PluginParameterKeys.REVISION_TIMESTAMP, dateFormat.format(getPluginMetadata().getRevisionTimestampPreviousPlugin()));
    parameters.put(PluginParameterKeys.PREVIOUS_TASK_ID, ecloudBasePluginParameters.getPreviousExternalTaskId());
    parameters.put(PluginParameterKeys.NEW_REPRESENTATION_NAME, MetisPlugin.getRepresentationName());
    parameters.put(PluginParameterKeys.OUTPUT_DATA_SETS, String
        .format(CommonStringValues.S_DATA_PROVIDERS_S_DATA_SETS_S_TEMPLATE, ecloudBasePluginParameters.getEcloudBaseUrl(),
            ecloudBasePluginParameters.getEcloudProvider(), ecloudBasePluginParameters.getEcloudDatasetId()));
    return createDpsTaskForPluginWithExistingDataset(parameters, ecloudBasePluginParameters, false);
  }

  DpsTask createDpsTaskForIndexPlugin(EcloudBasePluginParameters ecloudBasePluginParameters, String datasetId,
      boolean incrementalIndexing, Date harvestDate, boolean preserveTimestamps,
      List<String> datasetIdsToRedirectFrom, boolean performRedirects, String targetDatabase) {
    final DateFormat dateFormat = new SimpleDateFormat(CommonStringValues.DATE_FORMAT_Z, Locale.US);
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    final Map<String, String> extraParameters = new HashMap<>();
    extraParameters.put(PluginParameterKeys.METIS_DATASET_ID, datasetId);
    extraParameters.put(PluginParameterKeys.INCREMENTAL_INDEXING, String.valueOf(incrementalIndexing));
    extraParameters.put(PluginParameterKeys.HARVEST_DATE, dateFormat.format(harvestDate));
    extraParameters.put(PluginParameterKeys.METIS_TARGET_INDEXING_DATABASE, targetDatabase);
    extraParameters.put(PluginParameterKeys.METIS_RECORD_DATE, dateFormat.format(getStartedDate()));
    extraParameters.put(PluginParameterKeys.METIS_PRESERVE_TIMESTAMPS, String.valueOf(preserveTimestamps));
    extraParameters.put(PluginParameterKeys.DATASET_IDS_TO_REDIRECT_FROM, String.join(",", datasetIdsToRedirectFrom));
    extraParameters.put(PluginParameterKeys.PERFORM_REDIRECTS, String.valueOf(performRedirects));
    return createDpsTaskForProcessPlugin(ecloudBasePluginParameters, extraParameters);
  }

  Map<String, String> createParametersForValidationExternal(String urlOfSchemasZip, String schemaRootPath, String schematronRootPath) {
    final Map<String, String> parametersForValidation = createParametersForValidation(urlOfSchemasZip, schemaRootPath,
        schematronRootPath);
    parametersForValidation.put(PluginParameterKeys.GENERATE_STATS, Boolean.TRUE.toString());
    return parametersForValidation;
  }

  Map<String, String> createParametersForValidationInternal(String urlOfSchemasZip, String schemaRootPath, String schematronRootPath) {
    final Map<String, String> parametersForValidation = createParametersForValidation(urlOfSchemasZip, schemaRootPath,
        schematronRootPath);
    parametersForValidation.put(PluginParameterKeys.GENERATE_STATS, Boolean.FALSE.toString());
    return parametersForValidation;
  }

  private Map<String, String> createParametersForValidation(String urlOfSchemasZip, String schemaRootPath, String schematronRootPath) {
    Map<String, String> extraParameters = new HashMap<>();
    extraParameters.put(PluginParameterKeys.SCHEMA_NAME, urlOfSchemasZip);
    extraParameters.put(PluginParameterKeys.ROOT_LOCATION, schemaRootPath);
    extraParameters.put(PluginParameterKeys.SCHEMATRON_LOCATION, schematronRootPath);
    return extraParameters;
  }

  /**
   * Prepare the {@link DpsTask} based on the specific implementation of the plugin.
   *
   * @param ecloudBasePluginParameters the basic parameter required for each execution
   * @return the {@link DpsTask} prepared with all the required parameters
   */
  abstract DpsTask prepareDpsTask(String datasetId, EcloudBasePluginParameters ecloudBasePluginParameters);

  @Override
  public void execute(String datasetId, DpsClient dpsClient, EcloudBasePluginParameters ecloudBasePluginParameters)
      throws ExternalTaskException {
    String pluginTypeName = getPluginType().name();
    LOGGER.info("Starting execution of {} plugin for ecloudDatasetId {}", pluginTypeName,
        ecloudBasePluginParameters.getEcloudDatasetId());

    DpsTask dpsTask = prepareDpsTask(datasetId, ecloudBasePluginParameters);
    try {
      setExternalTaskId(Long.toString(dpsClient.submitTask(dpsTask, getTopologyName())));
      setDataStatus(DataStatus.VALID);
    } catch (DpsException | RuntimeException e) {
      throw new ExternalTaskException("Submitting task failed", e);
    }
    LOGGER.info("Submitted task with externalTaskId: {}", getExternalTaskId());
  }

  @Override
  public MonitorResult monitor(DpsClient dpsClient) throws ExternalTaskException {
    LOGGER.info("Requesting progress information for externalTaskId: {}", getExternalTaskId());
    TaskInfo taskInfo;
    try {
      taskInfo = dpsClient.getTaskProgress(getTopologyName(), Long.parseLong(getExternalTaskId()));
    } catch (DpsException | RuntimeException e) {
      throw new ExternalTaskException("Requesting task progress failed", e);
    }
    LOGGER.info("Task information received for externalTaskId: {}", getExternalTaskId());
    updateExecutionProgress(taskInfo);
    return new MonitorResult(taskInfo.getState(), taskInfo.getStateDescription());
  }

  /**
   * Update this object's {@link ExecutionProgress} based on the received {@link TaskInfo}.
   *
   * @param taskInfo {@link TaskInfo}
   */
  void updateExecutionProgress(TaskInfo taskInfo) {

    // Calculate the various counts.
    // The expectedRecordsNumber we get from ecloud is dynamic and can change during execution.
    int expectedRecordCount;
    int processedRecordCount;
    int deletedRecordCount;

    if (getPluginMetadata() instanceof AbstractHarvestPluginMetadata && ((AbstractHarvestPluginMetadata) this.getPluginMetadata())
        .isIncrementalHarvest()) {
      //Incremental Harvest
      //deletedRecordsCount never used
      //expectedPostProcessedRecordsNumber and postProcessedRecordsCount represent deleted records
      expectedRecordCount = taskInfo.getExpectedRecordsNumber();
      processedRecordCount = taskInfo.getProcessedRecordsCount() + taskInfo.getIgnoredRecordsCount();
      deletedRecordCount = taskInfo.getPostProcessedRecordsCount();
    } else if (getPluginMetadata() instanceof AbstractHarvestPluginMetadata) {
      //Full Harvest
      //expectedPostProcessedRecordsNumber, postProcessedRecordsCount and ignoredRecordsCount not used
      //deletedRecordsCount is always 0
      expectedRecordCount = taskInfo.getExpectedRecordsNumber();
      processedRecordCount = taskInfo.getProcessedRecordsCount();
      deletedRecordCount = taskInfo.getDeletedRecordsCount();
    } else if (getPluginMetadata() instanceof AbstractIndexPluginMetadata && !((AbstractIndexPluginMetadata) this
        .getPluginMetadata()).isIncrementalIndexing()) {
      //Full Indexing
      //ignoredRecordsCount never used
      //expectedPostProcessedRecordsNumber and postProcessedRecordsCount represent deleted records
      //The deletedRecordsCount is always 0
      expectedRecordCount = taskInfo.getExpectedRecordsNumber();
      processedRecordCount = taskInfo.getProcessedRecordsCount();
      deletedRecordCount = taskInfo.getPostProcessedRecordsCount();
    } else {
      //Other plugins including incremental indexing
      //expectedPostProcessedRecordsNumber, postProcessedRecordsCount and ignoredRecordsCount not used
      expectedRecordCount = taskInfo.getExpectedRecordsNumber() - taskInfo.getDeletedRecordsCount();
      processedRecordCount = taskInfo.getProcessedRecordsCount();
      deletedRecordCount = taskInfo.getDeletedRecordsCount();
    }

    int errorCount = taskInfo.getProcessedErrorsCount() + taskInfo.getDeletedErrorsCount();
    int ignoredCount = taskInfo.getIgnoredRecordsCount();

    // Update the execution progress.
    getExecutionProgress().setExpectedRecords(expectedRecordCount);
    getExecutionProgress().setProcessedRecords(processedRecordCount);
    getExecutionProgress().setDeletedRecords(deletedRecordCount);
    getExecutionProgress().setIgnoredRecords(ignoredCount);
    getExecutionProgress().setErrors(errorCount);
    getExecutionProgress().recalculateProgressPercentage();
    getExecutionProgress().setStatus(taskInfo.getState());
  }

  @Override
  public void cancel(DpsClient dpsClient, String cancelledById) throws ExternalTaskException {
    LOGGER.info("Cancel execution for externalTaskId: {}", getExternalTaskId());
    try {
      dpsClient.killTask(getTopologyName(), Long.parseLong(getExternalTaskId()),
          SystemId.SYSTEM_MINUTE_CAP_EXPIRE.name().equals(cancelledById) ? "Cancelled By System" : "Cancelled By User");
    } catch (DpsException | RuntimeException e) {
      throw new ExternalTaskException("Requesting task cancellation failed", e);
    }
  }
}
