//package eu.europeana.metis.core.workflow;
//
//import com.fasterxml.jackson.core.JsonParser;
//import com.fasterxml.jackson.databind.DeserializationContext;
//import com.fasterxml.jackson.databind.JsonDeserializer;
//import com.fasterxml.jackson.databind.JsonNode;
//import java.io.IOException;
//
///**
// * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
// * @since 2017-05-26
// */
//public class PluginDeserializer extends JsonDeserializer<AbstractMetisPlugin> {
//
//  @Override
//  public AbstractMetisPlugin deserialize(JsonParser jp, DeserializationContext ctxt)
//      throws IOException {
//    JsonNode node = jp.getCodec().readTree(jp);
//    String pluginName = node.path("pluginType").path("pluginName").textValue();
//    AbstractMetisPlugin abstractMetisPlugin = null;
//    PluginType pluginType = PluginType.getPluginTypeFromEnumName(pluginName);
//    switch (pluginType) {
//      case HTTP_HARVEST:
//        abstractMetisPlugin = ctxt.readValue(jp, VoidHTTPHarvestPlugin.class);
//        break;
//      case OAIPMH_HARVEST:
//        abstractMetisPlugin = ctxt.readValue(jp, VoidOaipmhHarvestPlugin.class);
//        break;
//      case DEREFERENCE:
//        abstractMetisPlugin = ctxt.readValue(jp, VoidDereferencePlugin.class);
//        break;
//      case VOID:
//        VoidMetisPlugin voidMetisPlugin = ctxt.readValue(jp, VoidMetisPlugin.class);
//        abstractMetisPlugin = voidMetisPlugin;
//        break;
//      case QA:
//        abstractMetisPlugin = null;
//        break;
//      case NULL:
//        abstractMetisPlugin = null;
//        break;
//      default:
//        break;
//    }
//    return abstractMetisPlugin;
//  }
//}
