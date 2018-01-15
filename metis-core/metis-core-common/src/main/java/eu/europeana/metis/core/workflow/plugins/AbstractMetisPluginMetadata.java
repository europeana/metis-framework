package eu.europeana.metis.core.workflow.plugins;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;
import java.util.Map;

/**
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
    @JsonSubTypes.Type(value=ValidationPluginMetadata.class, name="VALIDATION")
})
public interface AbstractMetisPluginMetadata {
  PluginType getPluginType();
  boolean isMocked();
  void setMocked(boolean mocked);
  Map<String, List<String>> getParameters();
  void setParameters(Map<String, List<String>> parameters);
}
