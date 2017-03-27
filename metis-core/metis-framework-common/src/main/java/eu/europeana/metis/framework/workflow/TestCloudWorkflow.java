package eu.europeana.metis.framework.workflow;

import eu.europeana.metis.cloud.ExecutionClient;
import eu.europeana.metis.cloud.TopologyNames;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test showcasing integration with Europeana Cloud
 * Created by gmamakis on 27-3-17.
 */
public class TestCloudWorkflow implements MetisWorkflow {
    private String name;
    private Map<String,List<String>> parameters;
    @Autowired
    private ExecutionClient executionClient;
    public TestCloudWorkflow(){
        this.name="test-cloud";
    }
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setParameters(Map<String, List<String>> parameters) {
        this.parameters = parameters;
    }

    @Override
    public Map<String, List<String>> getParameters() {
        return parameters;
    }

    @Override
    public String execute() {
       long taskId= executionClient.createDpsTask(parameters.get((WorkflowParameters.EXECUTION_ID)).get(0),
               new HashMap<String, String>(),parameters.get(WorkflowParameters.DATASET).get(0),
               TopologyNames.TRANSFORMATION);
       return executionClient.getStatisticsForTask(TopologyNames.TRANSFORMATION,taskId);
    }

    @Override
    public CloudStatistics monitor(String dataseId) {
        CloudStatistics stats = convertToCloudStatistics(dataseId);
        return stats;
    }

    private CloudStatistics convertToCloudStatistics(String dataseId) {
        //TODO: Implement a cache that has the active ones (Key datasetID, Value statisticsUrl)
        //From there get the statisticsUrl invoke it and return a CloudStatistics implementation with everything required
        return new CloudStatistics() {
            @Override
            public Long getDeleted() {
                return null;
            }

            @Override
            public Long getProcessed() {
                return null;
            }

            @Override
            public Long getUpdated() {
                return null;
            }

            @Override
            public Long getCreated() {
                return null;
            }

            @Override
            public Long getFailed() {
                return null;
            }

            @Override
            public List<String> getFailedRecords() {
                return null;
            }
        };
    }

    @Override
    public boolean supports(String s) {
        return s.equalsIgnoreCase(name);
    }
}
