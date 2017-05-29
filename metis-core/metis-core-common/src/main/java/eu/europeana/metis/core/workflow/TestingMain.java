package eu.europeana.metis.core.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import org.bson.types.ObjectId;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-26
 */
public class TestingMain {
  private static UserWorkflowExecution userWorkflowExecutionRead;

  public static void main(String[] args) throws IOException {
    VoidMetisPlugin voidMetisPlugin = new VoidMetisPlugin();
    voidMetisPlugin.setPluginStatus(PluginStatus.INQUEUE);
    VoidDereferencePlugin voidDereferencePlugin = new VoidDereferencePlugin();
    voidDereferencePlugin.setPluginStatus(PluginStatus.INQUEUE);

    UserWorkflowExecution userWorkflowExecution = new UserWorkflowExecution();
    userWorkflowExecution.setId(new ObjectId());
    userWorkflowExecution.harvest = true;
    userWorkflowExecution.incremental = false;
    userWorkflowExecution.setOwner("owner1");
    userWorkflowExecution.setWorkflowName("workflow11");
    userWorkflowExecution.setVoidDereferencePlugin(voidDereferencePlugin);
    userWorkflowExecution.setVoidMetisPlugin(voidMetisPlugin);

    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);

    //Store in UserWorkflows in Mongo
    String jsonInString = mapper.writeValueAsString(userWorkflowExecution);
    System.out.println(jsonInString);

    mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    userWorkflowExecutionRead = mapper.readValue(jsonInString, UserWorkflowExecution.class);
//    PluginType pluginType = mapper.readValue(jsonInString, PluginType.class);
    jsonInString = mapper.writeValueAsString(userWorkflowExecutionRead);
    System.out.println(jsonInString);

//    exeuteWorkflowByOwnerAndNameForDataset("owner1", "workflow11", "dataset1");

  }

//  private static void exeuteWorkflowByOwnerAndNameForDataset(String owner, String workflowName, String datasetName)
//      throws JsonProcessingException {
//    //DatasetName should exist and initialize the harvesting plugin
//    HarvestingMetadata harvestingMetadata = new HarvestingMetadata();
//    harvestingMetadata.setHarvestType(HarvestType.OAIPMH);
//    harvestingMetadata.setMetadataSchema("oaidc");
//    //Find workflow with owner and workflowname
//    userWorkflowRead.setId(new ObjectId());
//    List<AbstractMetisPlugin> abstractMetisPlugins = userWorkflowRead.getAbstractMetisPlugins();
//    //Find correct plugin for harvesting from first group
//    VoidOaipmhHarvestPlugin voidOaipmhHarvestPlugin = new VoidOaipmhHarvestPlugin();
//    voidOaipmhHarvestPlugin.setPluginStatus(PluginStatus.RUNNING);
//    abstractMetisPlugins.add(0, voidOaipmhHarvestPlugin);
//    userWorkflowRead.setAbstractMetisPlugins(abstractMetisPlugins);
//    userWorkflowRead.setDatasetName(datasetName);
//    userWorkflowRead.setWorkflowStatus(WorkflowStatus.RUNNING);
//    //Store in workflowExecutions this time in Mongo
//    ObjectMapper mapper = new ObjectMapper();
//    mapper.enable(SerializationFeature.INDENT_OUTPUT);
//    String jsonInString = mapper.writeValueAsString(userWorkflowRead);
//
////    System.out.println(jsonInString);
//  }

}
