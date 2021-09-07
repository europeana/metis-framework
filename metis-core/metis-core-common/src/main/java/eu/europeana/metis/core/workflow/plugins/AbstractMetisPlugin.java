package eu.europeana.metis.core.workflow.plugins;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import dev.morphia.annotations.Entity;
import eu.europeana.metis.utils.CommonStringValues;
import java.util.Date;

/**
 * This abstract class is the base implementation of {@link MetisPlugin} and all other plugins
 * should inherit from it.
 *
 * @param <M> The type of the plugin metadata that this plugin represents.
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-06-01
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.EXISTING_PROPERTY, property = "pluginType")
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
@Entity
public abstract class AbstractMetisPlugin<M extends AbstractMetisPluginMetadata> implements
    MetisPlugin {

  protected PluginType pluginType;
  private String id;

  private PluginStatus pluginStatus = PluginStatus.INQUEUE;
  private DataStatus dataStatus;
  private String failMessage;
  @JsonFormat(pattern = CommonStringValues.DATE_FORMAT)
  private Date startedDate;
  @JsonFormat(pattern = CommonStringValues.DATE_FORMAT)
  private Date updatedDate;
  @JsonFormat(pattern = CommonStringValues.DATE_FORMAT)
  private Date finishedDate;
  private M pluginMetadata;


  /**
   * Required by (de)serialization in db.
   * <p>It is not to be used manually</p>
   */
  protected AbstractMetisPlugin() {
    //Required by (de)serialization in db
  }

  /**
   * Constructor with provided pluginType
   *
   * @param pluginType {@link PluginType}
   */
  protected AbstractMetisPlugin(PluginType pluginType) {
    this.pluginType = pluginType;
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata required and the pluginType.
   *
   * @param pluginType a {@link PluginType} related to the implemented plugin
   * @param pluginMetadata The plugin metadata.
   */
  AbstractMetisPlugin(PluginType pluginType, M pluginMetadata) {
    this.pluginType = pluginType;
    this.pluginMetadata = pluginMetadata;
  }

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public PluginType getPluginType() {
    return pluginType;
  }

  @Override
  public DataStatus getDataStatus() {
    return dataStatus;
  }

  public void setDataStatus(DataStatus dataStatus) {
    this.dataStatus = dataStatus;
  }

  @Override
  public M getPluginMetadata() {
    return pluginMetadata;
  }

  public void setPluginMetadata(M pluginMetadata) {
    this.pluginMetadata = pluginMetadata;
  }

  @Override
  public Date getStartedDate() {
    return startedDate == null ? null : new Date(startedDate.getTime());
  }

  /**
   * @param startedDate {@link Date}
   */
  public void setStartedDate(Date startedDate) {
    this.startedDate = startedDate == null ? null : new Date(startedDate.getTime());
  }

  @Override
  public Date getUpdatedDate() {
    return updatedDate == null ? null : new Date(updatedDate.getTime());
  }

  /**
   * @param updatedDate {@link Date}
   */
  public void setUpdatedDate(Date updatedDate) {
    this.updatedDate = updatedDate == null ? null : new Date(updatedDate.getTime());
  }

  @Override
  public Date getFinishedDate() {
    return finishedDate == null ? null : new Date(finishedDate.getTime());
  }

  /**
   * @param finishedDate {@link Date}
   */
  public void setFinishedDate(Date finishedDate) {
    this.finishedDate = finishedDate == null ? null : new Date(finishedDate.getTime());
  }

  @Override
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
   * This method sets the plugin status and also clears the fail message.
   *
   * @param pluginStatus {@link PluginStatus}
   */
  public void setPluginStatusAndResetFailMessage(PluginStatus pluginStatus) {
    setPluginStatus(pluginStatus);
    setFailMessage(null);
  }

  @Override
  public String getFailMessage() {
    return failMessage;
  }

  public void setFailMessage(String failMessage) {
    this.failMessage = failMessage;
  }
}
