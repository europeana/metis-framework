package eu.europeana.metis.core.workflow.plugins;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import eu.europeana.metis.CommonStringValues;
import java.util.Date;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Indexed;

/**
 * This abstract class specifies the minimum a plugin should support so that it can be plugged in
 * the Metis workflow registry and can be accessible via the REST API of Metis.
 *
 * @param <M> The type of the plugin metadata that this plugin represents.
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
public abstract class AbstractMetisPlugin<M extends AbstractMetisPluginMetadata> {

  // TODO JV We should remove the pluginType. The pluginMetadata already has this information.
  protected final PluginType pluginType;
  private static final String REPRESENTATION_NAME = "metadataRecord";

  @Indexed
  private String id;

  private PluginStatus pluginStatus = PluginStatus.INQUEUE;
  private String failMessage;
  @Indexed
  @JsonFormat(pattern = CommonStringValues.DATE_FORMAT)
  private Date startedDate;
  @Indexed
  @JsonFormat(pattern = CommonStringValues.DATE_FORMAT)
  private Date finishedDate;
  private M pluginMetadata;

  /**
   * Constructor with provided pluginType
   *
   * @param pluginType {@link PluginType}
   */
  AbstractMetisPlugin(PluginType pluginType) {
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
  public M getPluginMetadata() {
    return pluginMetadata;
  }

  /**
   * @param pluginMetadata {@link AbstractMetisPluginMetadata} to add for the plugin
   */
  public void setPluginMetadata(M pluginMetadata) {
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
   * This method sets the plugin status and also clears the fail message.
   *
   * @param pluginStatus {@link PluginStatus}
   */
  public void setPluginStatusAndResetFailMessage(PluginStatus pluginStatus) {
    setPluginStatus(pluginStatus);
    setFailMessage(null);
  }

  public String getFailMessage() {
    return failMessage;
  }

  public void setFailMessage(String failMessage) {
    this.failMessage = failMessage;
  }
}
