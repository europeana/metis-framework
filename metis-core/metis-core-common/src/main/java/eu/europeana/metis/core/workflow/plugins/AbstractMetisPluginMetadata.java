package eu.europeana.metis.core.workflow.plugins;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;
import java.util.Map;

/**
 * Interface that gathers required methods for a class that contains plugin metadata
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-06-01
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME,
    include=JsonTypeInfo.As.PROPERTY,
    property="pluginType")
@JsonSubTypes({
    @JsonSubTypes.Type(value=OaipmhHarvestPluginMetadata.class, name="OAIPMH_HARVEST"),
    @JsonSubTypes.Type(value=HTTPHarvestPluginMetadata.class, name="HTTP_HARVEST"),
    @JsonSubTypes.Type(value=EnrichmentPluginMetadata.class, name="ENRICHMENT"),
    @JsonSubTypes.Type(value=ValidationExternalPluginMetadata.class, name="VALIDATION_EXTERNAL")
})
public interface AbstractMetisPluginMetadata {
  PluginType getPluginType();
  boolean isMocked();
  void setMocked(boolean mocked);
  Map<String, List<String>> getParameters();
  void setParameters(Map<String, List<String>> parameters);
}
