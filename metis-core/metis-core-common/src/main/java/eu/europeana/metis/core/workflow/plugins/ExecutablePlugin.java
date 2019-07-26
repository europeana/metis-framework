package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.common.model.dps.TaskState;
import eu.europeana.metis.exception.ExternalTaskException;
import java.util.Date;
import java.util.Optional;

/**
 * This interface represents plugins that are executable by Metis.
 *
 * @param <M> The type of the plugin metadata that this plugin represents.
 */
public interface ExecutablePlugin<M extends ExecutablePluginMetadata> extends MetisPlugin<M> {

  /**
   * @return updated {@link Date} of the execution of the plugin
   */
  Date getUpdatedDate();

  /**
   * @param updatedDate {@link Date}
   */
  void setUpdatedDate(Date updatedDate);

  /**
   * @return String representation of the external task identifier of the execution
   */
  String getExternalTaskId();

  /**
   * @param externalTaskId String representation of the external task identifier of the execution
   */
  void setExternalTaskId(String externalTaskId);

  /**
   * Progress information of the execution of the plugin
   *
   * @return {@link ExecutionProgress}
   */
  ExecutionProgress getExecutionProgress();

  /**
   * @param executionProgress {@link ExecutionProgress} of the external execution
   */
  void setExecutionProgress(ExecutionProgress executionProgress);

  /**
   * @return The data status of this plugin. If null, this should be interpreted as being equal to
   * {@link DataStatus#VALID} (due to backwards-compatibility).
   */
  DataStatus getDataStatus();

  /**
   * Returns the data state for the plugin taking into account the default value.
   *
   * @param plugin The plugin.
   * @return The data status of the given plugin. Is not null.
   */
  static DataStatus getDataStatus(AbstractExecutablePlugin plugin) {
    return Optional.ofNullable(plugin.getDataStatus()).orElse(DataStatus.VALID);
  }

  void setDataStatus(DataStatus dataStatus);

  /**
   * It is required as an abstract method to have proper serialization on the api level.
   *
   * @return the topologyName string coming from {@link Topology}
   */
  String getTopologyName();

  /**
   * Starts the execution of the plugin at the external location.
   * <p>It is non blocking method and the {@link #monitor(DpsClient)} should be used to monitor the
   * external execution</p>
   *
   * @param datasetId the dataset id that is required for some of the plugins
   * @param dpsClient {@link DpsClient} used to submit the external execution
   * @param ecloudBasePluginParameters the basic parameter required for each execution
   * @throws ExternalTaskException exceptions that encapsulates the external occurred exception
   */
  void execute(String datasetId, DpsClient dpsClient, EcloudBasePluginParameters ecloudBasePluginParameters)
      throws ExternalTaskException;

  /**
   * Request a monitor call to the external execution. This method also updates the execution
   * progress statistics.
   *
   * @param dpsClient {@link DpsClient} used to request a monitor call the external execution
   * @return {@link AbstractExecutablePlugin.MonitorResult} object containing the current state of
   * the task.
   * @throws ExternalTaskException exceptions that encapsulates the external occurred exception
   */
  AbstractExecutablePlugin.MonitorResult monitor(DpsClient dpsClient) throws ExternalTaskException;

  /**
   * Request a cancel call to the external execution.
   *
   * @param dpsClient {@link DpsClient} used to request a monitor call the external execution
   * @param cancelledById the reason a task is being cancelled, is it a user identifier of a system
   * identifier
   * @throws ExternalTaskException exceptions that encapsulates the external occurred exception
   */
  void cancel(DpsClient dpsClient, String cancelledById) throws ExternalTaskException;

  /**
   * This object represents the result of a monitor call. It contains the information that
   * monitoring processes need.
   */
  class MonitorResult {

    private final TaskState taskState;
    private final String taskInfo;

    /**
     * Constructor.
     *
     * @param taskState The current state of the task.
     * @param taskInfo The info message. Can be null or empty.
     */
    public MonitorResult(TaskState taskState, String taskInfo) {
      this.taskState = taskState;
      this.taskInfo = taskInfo;
    }

    public TaskState getTaskState() {
      return taskState;
    }

    public String getTaskInfo() {
      return taskInfo;
    }
  }
}
