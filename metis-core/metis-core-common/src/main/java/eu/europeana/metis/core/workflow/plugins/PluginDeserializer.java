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
    switch (pluginType) {
      case HTTP_HARVEST:
        abstractMetisPlugin = mapper.readValue(node.toString(), VoidHTTPHarvestPlugin.class);
        break;
      case OAIPMH_HARVEST:
        abstractMetisPlugin = mapper.readValue(node.toString(), VoidOaipmhHarvestPlugin.class);
        break;
      case DEREFERENCE:
        abstractMetisPlugin = mapper.readValue(node.toString(), VoidDereferencePlugin.class);
        break;
      case VOID:
        abstractMetisPlugin = mapper.readValue(node.toString(), VoidMetisPlugin.class);
        break;
      case QA:
        abstractMetisPlugin = null;
        break;
      case NULL:
        abstractMetisPlugin = null;
        break;
      default:
        break;
    }
    return abstractMetisPlugin;
  }
}
