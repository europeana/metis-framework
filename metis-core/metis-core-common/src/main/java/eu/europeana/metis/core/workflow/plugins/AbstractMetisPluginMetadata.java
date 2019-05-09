package eu.europeana.metis.core.workflow.plugins;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import eu.europeana.metis.CommonStringValues;
import java.util.Date;
import org.mongodb.morphia.annotations.Embedded;

/**
 * Abstract super class for all plugin metadata
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

  private String revisionNamePreviousPlugin;
  @JsonFormat(pattern = CommonStringValues.DATE_FORMAT)
  private Date revisionTimestampPreviousPlugin;

  public AbstractMetisPluginMetadata() {
  }

  public abstract PluginType getPluginType();

  public String getRevisionNamePreviousPlugin() {
    return revisionNamePreviousPlugin;
  }

  void setRevisionNamePreviousPlugin(String revisionNamePreviousPlugin) {
    this.revisionNamePreviousPlugin = revisionNamePreviousPlugin;
  }

  public Date getRevisionTimestampPreviousPlugin() {
    return revisionTimestampPreviousPlugin == null ? null
        : new Date(revisionTimestampPreviousPlugin.getTime());
  }

  void setRevisionTimestampPreviousPlugin(Date revisionTimestampPreviousPlugin) {
    this.revisionTimestampPreviousPlugin = revisionTimestampPreviousPlugin == null ? null
        : new Date(revisionTimestampPreviousPlugin.getTime());
  }
}
