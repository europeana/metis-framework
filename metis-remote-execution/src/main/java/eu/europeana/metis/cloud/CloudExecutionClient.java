package eu.europeana.metis.cloud;

import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.service.dps.DpsTask;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Europeana Cloud DPS integration module
 * Created by gmamakis on 27-3-17.
 */
public class CloudExecutionClient implements ExecutionClient {


    @Autowired
    DpsClient dpsClient;

    @Override
    public long createDpsTask(String executionId, Map<String, String> parameters, String datasetId, String topology){
        DpsTask task = new DpsTask();
        task.setTaskName(executionId);
        task.setParameters(parameters);
        List<String> dataset = new ArrayList<>();
        dataset.add(datasetId);
        task.addDataEntry("cloud-datasets", dataset);
        dpsClient.submitTask(task,topology);
        return task.getTaskId();
    }

    @Override
    public String getStatisticsForTask(String topology, long taskId){
        return dpsClient.getTaskProgress(topology,taskId);
    }

    @Override
    public String getNotification(String topology, long taskId) {
        return dpsClient.getTaskNotification(topology,taskId);
    }


}
