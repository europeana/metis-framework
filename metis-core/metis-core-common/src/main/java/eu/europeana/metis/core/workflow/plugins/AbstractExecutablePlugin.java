package eu.europeana.metis.core.workflow.plugins;

import com.fasterxml.jackson.annotation.JsonFormat;
import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.common.model.Revision;
import eu.europeana.cloud.common.model.dps.TaskInfo;
import eu.europeana.cloud.common.model.dps.TaskState;
import eu.europeana.cloud.service.dps.DpsTask;
import eu.europeana.cloud.service.dps.InputDataType;
import eu.europeana.cloud.service.dps.exception.DpsException;
import eu.europeana.metis.CommonStringValues;
import eu.europeana.metis.core.workflow.CancelledSystemId;
import eu.europeana.metis.exception.ExternalTaskException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.annotations.Indexed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This abstract class represents plugins that are executable by Metis.
 *
 * @param <M> The type of the plugin metadata that this plugin represents.
 */
public abstract class AbstractExecutablePlugin<M extends AbstractExecutablePluginMetadata> extends
    AbstractMetisPlugin<M> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractExecutablePlugin.class);

  @Indexed
  @JsonFormat(pattern = CommonStringValues.DATE_FORMAT)
  private Date updatedDate;
  private String externalTaskId;
  private ExecutionProgress executionProgress = new ExecutionProgress();

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

  /**
   * @return updated {@link Date} of the execution of the plugin
   */
  public Date getUpdatedDate() {
    return updatedDate == null ? null : new Date(updatedDate.getTime());
  }

  /**
   * @param updatedDate {@link Date}
   */
  public void setUpdatedDate(Date updatedDate) {
    this.updatedDate = updatedDate == null ? null : new Date(updatedDate.getTime());
  }

  /**
   * @return String representation of the external task identifier of the execution
   */
  public String getExternalTaskId() {
    return this.externalTaskId;
  }

  /**
   * @param externalTaskId String representation of the external task identifier of the execution
   */
  public void setExternalTaskId(String externalTaskId) {
    this.externalTaskId = externalTaskId;
  }

  /**
   * Progress information of the execution of the plugin
   *
   * @return {@link ExecutionProgress}
   */
  public ExecutionProgress getExecutionProgress() {
    return this.executionProgress;
  }

  /**
   * @param executionProgress {@link ExecutionProgress} of the external execution
   */
  public void setExecutionProgress(ExecutionProgress executionProgress) {
    this.executionProgress = executionProgress;
  }

  /**
   * It is required as an abstract method to have proper serialization on the api level.
   *
   * @return the topologyName string coming from {@link Topology}
   */
  public abstract String getTopologyName();

  private Revision createOutputRevisionForExecution(String ecloudProvider, boolean published) {
    return new Revision(getPluginType().name(), ecloudProvider, getStartedDate(), false, published,
        false);
  }

  private DpsTask createDpsTaskForPluginWithExistingDataset(Map<String, String> parameters,
      EcloudBasePluginParameters ecloudBasePluginParameters, boolean publish) {
    DpsTask dpsTask = new DpsTask();

    Map<InputDataType, List<String>> dataEntries = new EnumMap<>(InputDataType.class);
    dataEntries.put(InputDataType.DATASET_URLS, Collections
        .singletonList(String.format(CommonStringValues.S_DATA_PROVIDERS_S_DATA_SETS_S_TEMPLATE,
            ecloudBasePluginParameters.getEcloudBaseUrl(),
            ecloudBasePluginParameters.getEcloudProvider(),
            ecloudBasePluginParameters.getEcloudDatasetId())));
    dpsTask.setInputData(dataEntries);

    dpsTask.setParameters(parameters);
    dpsTask.setOutputRevision(
        createOutputRevisionForExecution(ecloudBasePluginParameters.getEcloudProvider(), publish));
    return dpsTask;
  }

  DpsTask createDpsTaskForHarvestPlugin(EcloudBasePluginParameters ecloudBasePluginParameters,
      Map<String, String> extraParameters, String targetUrl) {
    DpsTask dpsTask = new DpsTask();

    Map<InputDataType, List<String>> dataEntries = new EnumMap<>(InputDataType.class);
    dataEntries.put(InputDataType.REPOSITORY_URLS, Collections.singletonList(targetUrl));
    dpsTask.setInputData(dataEntries);

    Map<String, String> parameters = new HashMap<>();
    if (extraParameters != null) {
      parameters.putAll(extraParameters);
    }
    parameters.put("PROVIDER_ID", ecloudBasePluginParameters.getEcloudProvider());
    parameters.put("OUTPUT_DATA_SETS",
        String.format(CommonStringValues.S_DATA_PROVIDERS_S_DATA_SETS_S_TEMPLATE,
            ecloudBasePluginParameters.getEcloudBaseUrl(),
            ecloudBasePluginParameters.getEcloudProvider(),
            ecloudBasePluginParameters.getEcloudDatasetId()));
    parameters.put("NEW_REPRESENTATION_NAME", getRepresentationName());
    dpsTask.setParameters(parameters);

    dpsTask.setOutputRevision(
        createOutputRevisionForExecution(ecloudBasePluginParameters.getEcloudProvider(), false));
    return dpsTask;
  }

  DpsTask createDpsTaskForProcessPlugin(EcloudBasePluginParameters ecloudBasePluginParameters,
      Map<String, String> extraParameters) {
    Map<String, String> parameters = new HashMap<>();
    if (extraParameters != null) {
      parameters.putAll(extraParameters);
    }
    parameters.put("REPRESENTATION_NAME", getRepresentationName());
    parameters.put("REVISION_NAME", getPluginMetadata().getRevisionNamePreviousPlugin());
    parameters.put("REVISION_PROVIDER", ecloudBasePluginParameters.getEcloudProvider());
    DateFormat dateFormat = new SimpleDateFormat(CommonStringValues.DATE_FORMAT, Locale.US);
    parameters.put("REVISION_TIMESTAMP",
        dateFormat.format(getPluginMetadata().getRevisionTimestampPreviousPlugin()));
    parameters.put("PREVIOUS_TASK_ID", ecloudBasePluginParameters.getPreviousExternalTaskId());
    parameters.put("NEW_REPRESENTATION_NAME", getRepresentationName());
    parameters.put("OUTPUT_DATA_SETS",
        String.format(CommonStringValues.S_DATA_PROVIDERS_S_DATA_SETS_S_TEMPLATE,
            ecloudBasePluginParameters.getEcloudBaseUrl(),
            ecloudBasePluginParameters.getEcloudProvider(),
            ecloudBasePluginParameters.getEcloudDatasetId()));
    return createDpsTaskForPluginWithExistingDataset(parameters, ecloudBasePluginParameters, false);
  }

  DpsTask createDpsTaskForIndexPlugin(EcloudBasePluginParameters ecloudBasePluginParameters,
      String datasetId,
      boolean useAlternativeIndexingEnvironment, boolean preserveTimestamps,
      String targetDatabase) {
    Map<String, String> extraParameters = new HashMap<>();
    extraParameters.put("METIS_DATASET_ID", datasetId);
    extraParameters.put("TARGET_INDEXING_DATABASE", targetDatabase);
    extraParameters.put("USE_ALT_INDEXING_ENV", String.valueOf(useAlternativeIndexingEnvironment));
    extraParameters.put("PRESERVE_TIMESTAMPS", String.valueOf(preserveTimestamps));
    return createDpsTaskForProcessPlugin(ecloudBasePluginParameters, extraParameters);
  }

  Map<String, String> createParametersForHostConnectionLimits(
      Map<String, Integer> connectionLimitToDomains) {
    Map<String, String> parameters = new HashMap<>();
    if (connectionLimitToDomains != null) {
      connectionLimitToDomains.entrySet().stream()
          .filter(entry -> !StringUtils.isBlank(entry.getKey()) && entry.getValue() != null)
          .forEach(entry -> parameters
              .put("host.limit." + entry.getKey(), Integer.toString(entry.getValue())));
    }
    return parameters;
  }

  Map<String, String> createParametersForValidation(String urlOfSchemasZip, String schemaRootPath,
      String schematronRootPath) {
    Map<String, String> extraParameters = new HashMap<>();
    extraParameters.put("SCHEMA_NAME", urlOfSchemasZip);
    extraParameters.put("ROOT_LOCATION", schemaRootPath);
    extraParameters.put("SCHEMATRON_LOCATION", schematronRootPath);
    return extraParameters;
  }

  /**
   * Prepare the {@link DpsTask} based on the specific implementation of the plugin.
   *
   * @param ecloudBasePluginParameters the basic parameter required for each execution
   * @return the {@link DpsTask} prepared with all the required parameters
   */
  abstract DpsTask prepareDpsTask(EcloudBasePluginParameters ecloudBasePluginParameters);

  /**
   * Starts the execution of the plugin at the external location.
   * <p>It is non blocking method and the {@link #monitor(DpsClient)} should be used to monitor the
   * external execution</p>
   *
   * @param dpsClient {@link DpsClient} used to submit the external execution
   * @param ecloudBasePluginParameters the basic parameter required for each execution
   * @throws ExternalTaskException exceptions that encapsulates the external occurred exception
   */
  public void execute(DpsClient dpsClient, EcloudBasePluginParameters ecloudBasePluginParameters)
      throws ExternalTaskException {
    String pluginTypeName = getPluginType().name();
    LOGGER.info("Starting execution of {} plugin for ecloudDatasetId {}", pluginTypeName,
        ecloudBasePluginParameters.getEcloudDatasetId());
    try {
      DpsTask dpsTask = prepareDpsTask(ecloudBasePluginParameters);
      setExternalTaskId(Long.toString(dpsClient.submitTask(dpsTask, getTopologyName())));
    } catch (DpsException | RuntimeException e) {
      throw new ExternalTaskException("Submitting task failed", e);
    }
    LOGGER.info("Submitted task with externalTaskId: {}", getExternalTaskId());
  }

  /**
   * Request a monitor call to the external execution. This method also updates the execution
   * progress statistics.
   *
   * @param dpsClient {@link DpsClient} used to request a monitor call the external execution
   * @return {@link MonitorResult} object containing the current state of the task.
   * @throws ExternalTaskException exceptions that encapsulates the external occurred exception
   */
  public MonitorResult monitor(DpsClient dpsClient) throws ExternalTaskException {
    LOGGER.info("Requesting progress information for externalTaskId: {}", getExternalTaskId());
    TaskInfo taskInfo;
    try {
      taskInfo = dpsClient.getTaskProgress(getTopologyName(), Long.parseLong(getExternalTaskId()));
    } catch (DpsException | RuntimeException e) {
      throw new ExternalTaskException("Requesting task progress failed", e);
    }
    LOGGER.info("Task information received for externalTaskId: {}", getExternalTaskId());
    getExecutionProgress().copyExternalTaskInformation(taskInfo);
    return new MonitorResult(taskInfo.getState(), taskInfo.getInfo());
  }

  /**
   * Request a cancel call to the external execution.
   *
   * @param dpsClient {@link DpsClient} used to request a monitor call the external execution
   * @param cancelledById the reason a task is being cancelled, is it a user identifier of a system
   * identifier
   * @throws ExternalTaskException exceptions that encapsulates the external occurred exception
   */
  public void cancel(DpsClient dpsClient, String cancelledById) throws ExternalTaskException {
    LOGGER.info("Cancel execution for externalTaskId: {}", getExternalTaskId());
    try {
      dpsClient.killTask(getTopologyName(), Long.parseLong(getExternalTaskId()),
          CancelledSystemId.SYSTEM_MINUTE_CAP_EXPIRE.name().equals(cancelledById)
              ? "Cancelled By System" : "Cancelled By User");
    } catch (DpsException | RuntimeException e) {
      throw new ExternalTaskException("Requesting task cancellation failed", e);
    }
  }

  /**
   * This object represents the result of a monitor call. It contains the information that
   * monitoring processes need.
   */
  public static class MonitorResult {

    private final TaskState taskState;
    private final String taskInfo;

    /**
     * Constructor.
     *
     * @param taskState The current state of the task.
     * @param taskInfo The info message. Can be null or empty.
     */
    public MonitorResult(TaskState taskState, String taskInfo) {
      this.taskState = taskState;
      this.taskInfo = taskInfo;
    }

    public TaskState getTaskState() {
      return taskState;
    }

    public String getTaskInfo() {
      return taskInfo;
    }
  }
}
