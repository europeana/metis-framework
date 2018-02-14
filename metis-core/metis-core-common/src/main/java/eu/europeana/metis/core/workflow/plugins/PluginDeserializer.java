package eu.europeana.metis.core.workflow.plugins;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-26
 */
public class PluginDeserializer extends JsonDeserializer<AbstractMetisPlugin> {

  @Override
  public AbstractMetisPlugin deserialize(JsonParser jp, DeserializationContext ctxt)
      throws IOException {
    ObjectMapper mapper = (ObjectMapper) jp.getCodec();
    JsonNode node = mapper.readTree(jp);
    String pluginName = node.get("pluginType").textValue();
    AbstractMetisPlugin abstractMetisPlugin = null;
    PluginType pluginType = PluginType.getPluginTypeFromEnumName(pluginName);
    if (pluginType != null) {
      switch (pluginType) {
        case OAIPMH_HARVEST:
          abstractMetisPlugin = mapper.readValue(node.toString(), OaipmhHarvestPlugin.class);
          break;
        case HTTP_HARVEST:
          abstractMetisPlugin = mapper.readValue(node.toString(), HTTPHarvestPlugin.class);
          break;
        case VALIDATION_INTERNAL:
          abstractMetisPlugin = mapper.readValue(node.toString(), ValidationInternalPlugin.class);
          break;
        case TRANSFORMATION:
          abstractMetisPlugin = mapper.readValue(node.toString(), TransformationPlugin.class);
          break;
        case VALIDATION_EXTERNAL:
          abstractMetisPlugin = mapper.readValue(node.toString(), ValidationExternalPlugin.class);
          break;
        case ENRICHMENT:
          abstractMetisPlugin = mapper.readValue(node.toString(), EnrichmentPlugin.class);
          break;
        default:
          abstractMetisPlugin = null;
          break;
      }
    }
    return abstractMetisPlugin;
  }
}
