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
    @JsonSubTypes.Type(value=VoidOaipmhHarvestPluginMetadata.class, name="OAIPMH_HARVEST"),
    @JsonSubTypes.Type(value=VoidHTTPHarvestPluginMetadata.class, name="HTTP_HARVEST"),
    @JsonSubTypes.Type(value=VoidDereferencePluginMetadata.class, name="DEREFERENCE"),
    @JsonSubTypes.Type(value=VoidMetisPluginMetadata.class, name="VOID")
})
public interface AbstractMetisPluginMetadata {
  PluginType getPluginType();
  Map<String, List<String>> getParameters();
  void setParameters(Map<String, List<String>> parameters);
}
