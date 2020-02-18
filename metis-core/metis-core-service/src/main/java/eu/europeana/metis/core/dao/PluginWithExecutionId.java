package eu.europeana.metis.core.dao;

import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.MetisPlugin;

/**
 * This object contains a pair consisting of a workflow execution ID and a plugin.
 *
 * @param <T> The plugin type.
 */
public class PluginWithExecutionId<T extends MetisPlugin> {

  private final String executionId;
  private final T plugin;

  /**
   * Constructor.
   *
   * @param execution The execution.
   * @param plugin The plugin.
   */
  public PluginWithExecutionId(WorkflowExecution execution, T plugin) {
    this(execution.getId().toString(), plugin);
  }

  /**
   * Constructor.
   *
   * @param executionId The execution ID.
   * @param plugin The plugin.
   */
  public PluginWithExecutionId(String executionId, T plugin) {
    this.executionId = executionId;
    this.plugin = plugin;
  }

  public String getExecutionId() {
    return executionId;
  }

  public T getPlugin() {
    return plugin;
  }

  @Override
  public boolean equals(Object otherObject) {
    if (!(otherObject instanceof PluginWithExecutionId)) {
      return false;
    }
    final PluginWithExecutionId other = (PluginWithExecutionId) otherObject;
    return this.getPlugin().getId().equals(other.getPlugin().getId());
  }

  @Override
  public int hashCode() {
    return this.getPlugin().getId().hashCode();
  }
}
