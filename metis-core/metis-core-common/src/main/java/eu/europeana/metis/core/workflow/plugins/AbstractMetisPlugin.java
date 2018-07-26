package eu.europeana.metis.core.workflow.plugins;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.common.model.Revision;
import eu.europeana.cloud.common.model.dps.TaskInfo;
import eu.europeana.cloud.service.dps.DpsTask;
import eu.europeana.cloud.service.dps.InputDataType;
import eu.europeana.cloud.service.dps.exception.DpsException;
import eu.europeana.metis.CommonStringValues;
import eu.europeana.metis.exception.ExternalTaskException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Indexed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This abstract class specifies the minimum o plugin should support so that it can be plugged in the
 * Metis workflow registry and can be accessible via the REST API of Metis.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-06-01
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "pluginType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = OaipmhHarvestPlugin.class, name = "OAIPMH_HARVEST"),
    @JsonSubTypes.Type(value = HTTPHarvestPlugin.class, name = "HTTP_HARVEST"),
    @JsonSubTypes.Type(value = ValidationInternalPlugin.class, name = "VALIDATION_INTERNAL"),
    @JsonSubTypes.Type(value = TransformationPlugin.class, name = "TRANSFORMATION"),
    @JsonSubTypes.Type(value = ValidationExternalPlugin.class, name = "VALIDATION_EXTERNAL"),
    @JsonSubTypes.Type(value = NormalizationPlugin.class, name = "NORMALIZATION"),
    @JsonSubTypes.Type(value = EnrichmentPlugin.class, name = "ENRICHMENT"),
    @JsonSubTypes.Type(value = MediaProcessPlugin.class, name = "MEDIA_PROCESS"),
    @JsonSubTypes.Type(value = LinkCheckingPlugin.class, name = "LINK_CHECKING"),
    @JsonSubTypes.Type(value = IndexToPreviewPlugin.class, name = "PREVIEW"),
    @JsonSubTypes.Type(value = IndexToPublishPlugin.class, name = "PUBLISH")
})
@Embedded
public abstract class AbstractMetisPlugin {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMetisPlugin.class);
  protected final PluginType pluginType;
  private static final String REPRESENTATION_NAME = "metadataRecord";

  @Indexed
  private String id;

  private PluginStatus pluginStatus = PluginStatus.INQUEUE;
  @Indexed
  @JsonFormat(pattern = CommonStringValues.DATE_FORMAT)
  private Date startedDate;
  @Indexed
  @JsonFormat(pattern = CommonStringValues.DATE_FORMAT)
  private Date updatedDate;
  @Indexed
  @JsonFormat(pattern = CommonStringValues.DATE_FORMAT)
  private Date finishedDate;
  private String externalTaskId;
  private ExecutionProgress executionProgress = new ExecutionProgress();
  private AbstractMetisPluginMetadata pluginMetadata;

  /**
   * Constructor with provided pluginType
   *
   * @param pluginType {@link PluginType}
   */
  AbstractMetisPlugin(PluginType pluginType) {
    //Required for json serialization
    this.pluginType = pluginType;
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata required and the pluginType.
   *
   * @param pluginMetadata one of the implemented {@link AbstractMetisPluginMetadata}
   * @param pluginType a {@link PluginType} related to the implemented plugin
   */
  AbstractMetisPlugin(PluginType pluginType, AbstractMetisPluginMetadata pluginMetadata) {
    this.pluginType = pluginType;
    this.pluginMetadata = pluginMetadata;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return {@link PluginType}
   */
  public PluginType getPluginType() {
    return pluginType;
  }

  public static String getRepresentationName() {
    return REPRESENTATION_NAME;
  }

  /**
   * The metadata corresponding to this plugin.
   *
   * @return {@link AbstractMetisPluginMetadata}
   */
  public AbstractMetisPluginMetadata getPluginMetadata() {
    return pluginMetadata;
  }

  /**
   * @param pluginMetadata {@link AbstractMetisPluginMetadata} to add for the plugin
   */
  public void setPluginMetadata(AbstractMetisPluginMetadata pluginMetadata) {
    this.pluginMetadata = pluginMetadata;
  }

  /**
   * @return started {@link Date} of the execution of the plugin
   */
  public Date getStartedDate() {
    return startedDate == null ? null : new Date(startedDate.getTime());
  }

  /**
   * @param startedDate {@link Date}
   */
  public void setStartedDate(Date startedDate) {
    this.startedDate = startedDate == null ? null : new Date(startedDate.getTime());
  }

  /**
   * @return finished {@link Date} of the execution of the plugin
   */
  public Date getFinishedDate() {
    return finishedDate == null ? null : new Date(finishedDate.getTime());
  }

  /**
   * @param finishedDate {@link Date}
   */
  public void setFinishedDate(Date finishedDate) {
    this.finishedDate = finishedDate == null ? null : new Date(finishedDate.getTime());
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
   * @return status {@link PluginStatus} of the execution of the plugin
   */
  public PluginStatus getPluginStatus() {
    return pluginStatus;
  }

  /**
   * @param pluginStatus {@link PluginStatus}
   */
  public void setPluginStatus(PluginStatus pluginStatus) {
    this.pluginStatus = pluginStatus;
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
  public void setExecutionProgress(
      ExecutionProgress executionProgress) {
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
      String ecloudBaseUrl, String ecloudProvider, String ecloudDataset, boolean publish) {
    DpsTask dpsTask = new DpsTask();

    Map<InputDataType, List<String>> dataEntries = new EnumMap<>(InputDataType.class);
    dataEntries.put(InputDataType.DATASET_URLS,
        Collections
            .singletonList(String.format(CommonStringValues.S_DATA_PROVIDERS_S_DATA_SETS_S_TEMPLATE,
                ecloudBaseUrl, ecloudProvider, ecloudDataset)));
    dpsTask.setInputData(dataEntries);

    dpsTask.setParameters(parameters);
    dpsTask.setOutputRevision(createOutputRevisionForExecution(ecloudProvider, publish));
    return dpsTask;
  }

  DpsTask createDpsTaskForHarvestPlugin(Map<String, String> extraParameters, String targetUrl,
      String ecloudBaseUrl, String ecloudProvider, String ecloudDataset) {
    DpsTask dpsTask = new DpsTask();

    Map<InputDataType, List<String>> dataEntries = new EnumMap<>(InputDataType.class);
    dataEntries.put(InputDataType.REPOSITORY_URLS, Collections.singletonList(targetUrl));
    dpsTask.setInputData(dataEntries);

    Map<String, String> parameters = new HashMap<>();
    if (extraParameters != null) {
      parameters.putAll(extraParameters);
    }
    parameters.put("PROVIDER_ID", ecloudProvider);
    parameters.put("OUTPUT_DATA_SETS",
        String.format(CommonStringValues.S_DATA_PROVIDERS_S_DATA_SETS_S_TEMPLATE, ecloudBaseUrl,
            ecloudProvider, ecloudDataset));
    parameters.put("NEW_REPRESENTATION_NAME", getRepresentationName());
    dpsTask.setParameters(parameters);

    dpsTask.setOutputRevision(createOutputRevisionForExecution(ecloudProvider, false));
    return dpsTask;
  }

  DpsTask createDpsTaskForProcessPlugin(Map<String, String> extraParameters, String ecloudBaseUrl,
      String ecloudProvider, String ecloudDataset) {
    Map<String, String> parameters = new HashMap<>();
    if (extraParameters != null) {
      parameters.putAll(extraParameters);
    }
    parameters.put("REPRESENTATION_NAME", getRepresentationName());
    parameters.put("REVISION_NAME", getPluginMetadata().getRevisionNamePreviousPlugin());
    parameters.put("REVISION_PROVIDER", ecloudProvider);
    DateFormat dateFormat = new SimpleDateFormat(CommonStringValues.DATE_FORMAT);
    parameters.put("REVISION_TIMESTAMP",
        dateFormat.format(getPluginMetadata().getRevisionTimestampPreviousPlugin()));
    parameters.put("NEW_REPRESENTATION_NAME", getRepresentationName());
    parameters.put("OUTPUT_DATA_SETS",
        String.format(CommonStringValues.S_DATA_PROVIDERS_S_DATA_SETS_S_TEMPLATE,
            ecloudBaseUrl, ecloudProvider, ecloudDataset));
    return createDpsTaskForPluginWithExistingDataset(parameters, ecloudBaseUrl, ecloudProvider,
        ecloudDataset, false);
  }

  DpsTask createDpsTaskForIndexPlugin(boolean useAlternativeIndexingEnvironment, boolean preserveTimestamps,
      String targetDatabase, String ecloudBaseUrl, String ecloudProvider, String ecloudDataset) {
    Map<String, String> extraParameters = new HashMap<>();
    extraParameters.put("TARGET_INDEXING_DATABASE", targetDatabase);
    extraParameters.put("USE_ALT_INDEXING_ENV", String.valueOf(useAlternativeIndexingEnvironment));
    extraParameters.put("PRESERVE_TIMESTAMPS", String.valueOf(preserveTimestamps));
    return createDpsTaskForProcessPlugin(extraParameters, ecloudBaseUrl, ecloudProvider,
        ecloudDataset);
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

  /**
   * Prepare the {@link DpsTask} based on the specific implementation of the plugin.
   *
   * @param ecloudBaseUrl the base url of the ecloud apis
   * @param ecloudProvider the ecloud provider to be used for the external task
   * @param ecloudDataset the ecloud dataset identifier to be used for the external task
   * @return the {@link DpsTask} prepared with all the required parameters
   */
  abstract DpsTask prepareDpsTask(String ecloudBaseUrl, String ecloudProvider,
      String ecloudDataset);

  /**
   * Starts the execution of the plugin at the external location.
   * <p>It is non blocking method and the {@link #monitor(DpsClient)} should be used to monitor the external execution</p>
   *
   * @param dpsClient {@link DpsClient} used to submit the external execution
   * @param ecloudBaseUrl the base url of the ecloud apis
   * @param ecloudProvider the ecloud provider to be used for the external task
   * @param ecloudDataset the ecloud dataset identifier to be used for the external task
   * @throws ExternalTaskException exceptions that encapsulates the external occurred exception
   */
  public void execute(DpsClient dpsClient, String ecloudBaseUrl, String ecloudProvider,
      String ecloudDataset) throws ExternalTaskException {
    if (!getPluginMetadata().isMocked()) {
      String pluginTypeName = getPluginType().name();
      LOGGER.info("Starting real execution of {} plugin for ecloudDatasetId {}", pluginTypeName,
          ecloudDataset);
      try {
        DpsTask dpsTask = prepareDpsTask(ecloudBaseUrl, ecloudProvider, ecloudDataset);
        setExternalTaskId(Long.toString(dpsClient.submitTask(dpsTask, getTopologyName())));
      } catch (DpsException | RuntimeException e) {
        throw new ExternalTaskException("Submitting task failed", e);
      }
      LOGGER.info("Submitted task with externalTaskId: {}", getExternalTaskId());
    }
  }

  /**
   * Request a monitor call to the external execution.
   *
   * @param dpsClient {@link DpsClient} used to request a monitor call the external execution
   * @return {@link ExecutionProgress} of the plugin.
   * @throws ExternalTaskException exceptions that encapsulates the external occurred exception
   */
  public ExecutionProgress monitor(DpsClient dpsClient) throws ExternalTaskException {
    LOGGER.info("Requesting progress information for externalTaskId: {}", getExternalTaskId());
    TaskInfo taskInfo;
    try {
      taskInfo = dpsClient.getTaskProgress(getTopologyName(), Long.parseLong(getExternalTaskId()));
    } catch (DpsException | RuntimeException e) {
      throw new ExternalTaskException("Requesting task progress failed", e);
    }
    LOGGER.info("Task information received for externalTaskId: {}", getExternalTaskId());
    return getExecutionProgress().copyExternalTaskInformation(taskInfo);
  }

  /**
   * Request a cancel call to the external execution.
   *
   * @param dpsClient {@link DpsClient} used to request a monitor call the external execution
   * @throws ExternalTaskException exceptions that encapsulates the external occurred exception
   */
  public void cancel(DpsClient dpsClient) throws ExternalTaskException {
    LOGGER.info("Cancel execution for externalTaskId: {}", getExternalTaskId());
    try {
      dpsClient.killTask(getTopologyName(), Long.parseLong(getExternalTaskId()));
    } catch (DpsException | RuntimeException e) {
      throw new ExternalTaskException("Requesting task cancellation failed", e);
    }
  }
}
