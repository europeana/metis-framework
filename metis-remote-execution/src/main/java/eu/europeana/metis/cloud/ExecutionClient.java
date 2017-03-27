package eu.europeana.metis.cloud;

import java.util.Map;

/**
 * Remote execution client
 * Created by gmamakis on 27-3-17.
 */
public interface ExecutionClient {
    /**
     * Create a task
     * @param executionId The executionId of the task (will be used as the task name
     * @param parameters The parameters for the task (needs to be handled per workflow)
     * @param datasetId The dataset identifier
     * @param topology The topology to be invoked. Available topologies {@link TopologyNames}
     * @return The task id in Europeana Cloud. This is required to perform a number of tasks within Cloud.
     *
     */
    long createDpsTask(String executionId, Map<String, String> parameters, String datasetId, String topology);

    /**
     * Retrieve the URL for the progress statistics of an execution
     * @param topology The name of the topology
     * @param taskId The task id
     * @return The URL for the statistics of the task
     */
    String getStatisticsForTask(String topology, long taskId);

    /**
     * Retrieve the URL for task notification
     * @param topology The name of the topology
     * @param taskId The task id
     * @return The URL for the notifications of the task
     */
    String getNotification(String topology,long taskId);
}
