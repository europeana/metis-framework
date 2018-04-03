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
    @JsonSubTypes.Type(value = EnrichmentPluginMetadata.class, name = "ENRICHMENT")
})
@Embedded
public abstract class AbstractMetisPluginMetadata {

  private boolean mocked = true;
  private boolean enabled = false;
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

  public void setRevisionNamePreviousPlugin(String revisionNamePreviousPlugin) {
    this.revisionNamePreviousPlugin = revisionNamePreviousPlugin;
  }

  public Date getRevisionTimestampPreviousPlugin() {
    return revisionTimestampPreviousPlugin == null?null:new Date(revisionTimestampPreviousPlugin.getTime());
  }

  public void setRevisionTimestampPreviousPlugin(Date revisionTimestampPreviousPlugin) {
    this.revisionTimestampPreviousPlugin = revisionTimestampPreviousPlugin == null?null:new Date(revisionTimestampPreviousPlugin.getTime());
  }
}
