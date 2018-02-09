package eu.europeana.metis.core.workflow.plugins;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.metis.exception.ExternalTaskException;
import java.util.Date;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Indexed;

/**
 * This abstract class specifies the minimum o plugin should support so that it can be plugged in the
 * Metis workflow registry and can be accessible via the REST API of Metis.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-06-01
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "pluginType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = OaipmhHarvestPlugin.class, name = "OAIPMH_HARVEST"),
    @JsonSubTypes.Type(value = HTTPHarvestPlugin.class, name = "HTTP_HARVEST"),
    @JsonSubTypes.Type(value = ValidationInternalPlugin.class, name = "VALIDATION_INTENRAL"),
    @JsonSubTypes.Type(value = TransformationPlugin.class, name = "TRANSFORMATION"),
    @JsonSubTypes.Type(value = ValidationExternalPlugin.class, name = "VALIDATION_EXTERNAL"),
    @JsonSubTypes.Type(value = EnrichmentPlugin.class, name = "ENRICHMENT")
})
@Embedded
public abstract class AbstractMetisPlugin {

  private PluginType pluginType;
  private static final String representationName = "metadataRecord";

  @Indexed
  private String id;

  private PluginStatus pluginStatus = PluginStatus.INQUEUE;
  @Indexed
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date startedDate;
  @Indexed
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date updatedDate;
  @Indexed
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date finishedDate;
  private String externalTaskId;
  private ExecutionProgress executionProgress = new ExecutionProgress();
  private AbstractMetisPluginMetadata pluginMetadata;

  public AbstractMetisPlugin() {
    //Required for json serialization
  }

  public AbstractMetisPlugin(PluginType pluginType, AbstractMetisPluginMetadata pluginMetadata) {
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

  /**
   * @return {@link PluginType}
   */
  public void setPluginType(PluginType pluginType) {
    this.pluginType = pluginType;
  }

  public static String getRepresentationName() {
    return representationName;
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
    return this.startedDate;
  }

  /**
   * @param startedDate {@link Date}
   */
  public void setStartedDate(Date startedDate) {
    this.startedDate = startedDate;
  }

  /**
   * @return finished {@link Date} of the execution of the plugin
   */
  public Date getFinishedDate() {
    return this.finishedDate;
  }

  /**
   * @param finishedDate {@link Date}
   */
  public void setFinishedDate(Date finishedDate) {
    this.finishedDate = finishedDate;
  }

  /**
   * @return updated {@link Date} of the execution of the plugin
   */
  public Date getUpdatedDate() {
    return this.updatedDate;
  }

  /**
   * @param updatedDate {@link Date}
   */
  public void setUpdatedDate(Date updatedDate) {
    this.updatedDate = updatedDate;
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
   * Starts the execution of the plugin at the external location.
   * <p>It is non blocking method and the {@link #monitor(DpsClient)} should be used to monitor the external execution</p>
   *
   * @param dpsClient {@link DpsClient} used to submit the external execution
   * @param ecloudBaseUrl the base url of the ecloud apis
   * @param ecloudProvider the ecloud provider to be used for the external task
   * @param ecloudDataset the ecloud dataset identifier to be used for the external task
   */
  public abstract void execute(DpsClient dpsClient, String ecloudBaseUrl, String ecloudProvider,
      String ecloudDataset) throws ExternalTaskException;

  /**
   * Request a monitor call to the external execution.
   *
   * @param dpsClient {@link DpsClient} used to request a monitor call the external execution
   * @return {@link ExecutionProgress} of the plugin.
   */
  public abstract ExecutionProgress monitor(DpsClient dpsClient) throws ExternalTaskException;
}
