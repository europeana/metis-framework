package eu.europeana.metis.core.workflow.plugins;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.mongodb.morphia.annotations.Embedded;

/**
 * Interface that gathers required methods for a class that contains plugin metadata
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-06-01
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "pluginType")
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

  public AbstractMetisPluginMetadata() {
  }

  public abstract PluginType getPluginType();

  public boolean isMocked() {
    return mocked;
  }

  public void setMocked(boolean mocked) {
    this.mocked = mocked;
  }
}
