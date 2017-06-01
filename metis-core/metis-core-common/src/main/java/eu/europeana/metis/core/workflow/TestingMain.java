package eu.europeana.metis.core.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginDeserializer;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.core.workflow.plugins.VoidDereferencePlugin;
import eu.europeana.metis.core.workflow.plugins.VoidOaipmhHarvestPlugin;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-26
 */
public class TestingMain {

  public static void main(String[] args) throws IOException, InterruptedException {

    List<AbstractMetisPlugin> abstractMetisPlugins = new ArrayList<>();
    VoidOaipmhHarvestPlugin voidOaipmhHarvestPlugin = new VoidOaipmhHarvestPlugin();
    voidOaipmhHarvestPlugin.setId("1");
    voidOaipmhHarvestPlugin.setMetadataSchema("schema");
    abstractMetisPlugins.add(voidOaipmhHarvestPlugin);

    VoidDereferencePlugin voidDereferencePlugin = new VoidDereferencePlugin();
    voidDereferencePlugin.setId("2");

    Map<String, List<String>> stringListMap = new HashMap<>();
    List<String> testlist = new ArrayList<>();
    testlist.add("testlist");
    stringListMap.put("test", testlist);
    voidDereferencePlugin.setParameters(stringListMap);
    abstractMetisPlugins.add(voidDereferencePlugin);

    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(abstractMetisPlugins);
//    json = "{\"id\":\"1\",\"pluginStatus\":\"INQUEUE\",\"pluginType\":\"OAIPMH_HARVEST\",\"metadataSchema\":\"schema\",\"startedDate\":null,\"updatedDate\":null,\"finishedDate\":null,\"recordsProcessed\":0,\"recordsFailed\":0,\"recordsCreated\":0,\"recordsUpdated\":0,\"recordsDeleted\":0,\"parameters\":null}";
//    System.out.println(json);

    SimpleModule module = new SimpleModule();
    module.addDeserializer(AbstractMetisPlugin.class, new PluginDeserializer());
    mapper.registerModule(module);
//    VoidOaipmhHarvestPlugin voidOaipmhHarvestPlugin1 = (VoidOaipmhHarvestPlugin) mapper
//        .readValue(json, AbstractMetisPlugin.class);
    List<AbstractMetisPlugin> list = Arrays
        .asList(mapper.readValue(json, AbstractMetisPlugin[].class));

    for (AbstractMetisPlugin plugin :
        list) {
      if(plugin.getPluginType() == PluginType.OAIPMH_HARVEST)
        System.out.println(((VoidOaipmhHarvestPlugin)plugin).getMetadataSchema());
      if(plugin.getPluginType() == PluginType.DEREFERENCE) {
        Map<String, List<String>> parameters = ((VoidDereferencePlugin) plugin).getParameters();
        System.out.println(parameters.get("test").get(0));
      }
    }
  }
}
