package eu.europeana.metis.core.workflow.plugins;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import eu.europeana.metis.CommonStringValues;
import java.util.Date;
import org.mongodb.morphia.annotations.Embedded;

/**
 * Interface that gathers required methods for a class that contains plugin metadata
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-06-01
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "pluginType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = OaipmhHarvestPluginMetadata.class, name = "OAIPMH_HARVEST"),
    @JsonSubTypes.Type(value = HTTPHarvestPluginMetadata.class, name = "HTTP_HARVEST"),
    @JsonSubTypes.Type(value = ValidationExternalPluginMetadata.class, name = "VALIDATION_EXTERNAL"),
    @JsonSubTypes.Type(value = TransformationPluginMetadata.class, name = "TRANSFORMATION"),
    @JsonSubTypes.Type(value = ValidationInternalPluginMetadata.class, name = "VALIDATION_INTERNAL"),
    @JsonSubTypes.Type(value = NormalizationPluginMetadata.class, name = "NORMALIZATION"),
    @JsonSubTypes.Type(value = EnrichmentPluginMetadata.class, name = "ENRICHMENT"),
    @JsonSubTypes.Type(value = MediaProcessPluginMetadata.class, name = "MEDIA_PROCESS"),
    @JsonSubTypes.Type(value = LinkCheckingPluginMetadata.class, name = "LINK_CHECKING"),
    @JsonSubTypes.Type(value = IndexToPreviewPluginMetadata.class, name = "PREVIEW"),
    @JsonSubTypes.Type(value = IndexToPublishPluginMetadata.class, name = "PUBLISH")
})
@Embedded
public abstract class AbstractMetisPluginMetadata {

  private boolean mocked = true;
  private boolean enabled;
  private String revisionNamePreviousPlugin;
  @JsonFormat(pattern = CommonStringValues.DATE_FORMAT)
  private Date revisionTimestampPreviousPlugin;

  public AbstractMetisPluginMetadata() {
  }

  public abstract PluginType getPluginType();

  public boolean isMocked() {
    return mocked;
  }

  public void setMocked(boolean mocked) {
    this.mocked = mocked;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getRevisionNamePreviousPlugin() {
    return revisionNamePreviousPlugin;
  }

  private void setRevisionNamePreviousPlugin(String revisionNamePreviousPlugin) {
    this.revisionNamePreviousPlugin = revisionNamePreviousPlugin;
  }

  public Date getRevisionTimestampPreviousPlugin() {
    return revisionTimestampPreviousPlugin == null ? null
        : new Date(revisionTimestampPreviousPlugin.getTime());
  }

  private void setRevisionTimestampPreviousPlugin(Date revisionTimestampPreviousPlugin) {
    this.revisionTimestampPreviousPlugin = revisionTimestampPreviousPlugin == null ? null
        : new Date(revisionTimestampPreviousPlugin.getTime());
  }

  /**
   * For the current plugin, setup the source/previous revision information.
   * <p>
   * The source revision information that this plugin will be based on, is coming from the {@code previousAbstractMetisPlugin} plugin metadata.
   * The {@code previousAbstractMetisPlugin} can be a RevisionLess plugin, in which case the revision information for the current plugin
   * will take the source revision information that the {@code previousAbstractMetisPlugin} was based on.
   * </p>
   *
   * @param previousAbstractMetisPlugin the source/previous plugin that is used to base the current plugin on
   */
  public void setPreviousRevisionInformation(AbstractMetisPlugin previousAbstractMetisPlugin) {
    if (previousAbstractMetisPlugin.getPluginType()
        .isRevisionLess()) { //If previous plugin is revisionLess use the previous plugin of that instead
      this.setRevisionNamePreviousPlugin(
          previousAbstractMetisPlugin.getPluginMetadata().getRevisionNamePreviousPlugin());
      this.setRevisionTimestampPreviousPlugin(
          previousAbstractMetisPlugin.getPluginMetadata().getRevisionTimestampPreviousPlugin());
    } else {
      this.setRevisionNamePreviousPlugin(previousAbstractMetisPlugin.getPluginType().name());
      this.setRevisionTimestampPreviousPlugin(previousAbstractMetisPlugin.getStartedDate());
    }
  }
}
