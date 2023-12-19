package eu.europeana.metis.core.rest.execution.overview;

import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This object contains all information regarding the workflow execution's progress.
 */
public class ExecutionProgressView {

  private static final Set<PluginStatus> EXECUTING_STATUS_SET = Stream
      .of(PluginStatus.RUNNING, PluginStatus.CLEANING, PluginStatus.PENDING,
          PluginStatus.IDENTIFYING_DELETED_RECORDS).collect(Collectors.toSet());
  private static final Set<PluginStatus> FINISHED_STATUS_SET = Stream
      .of(PluginStatus.FINISHED, PluginStatus.FAILED, PluginStatus.CANCELLED)
      .collect(Collectors.toSet());

  private int stepsDone;
  private int stepsTotal;
  private PluginProgressView currentPluginProgress;

  ExecutionProgressView() {
  }

  ExecutionProgressView(WorkflowExecution execution) {
    List<AbstractMetisPlugin> metisPlugins = execution.getMetisPlugins();
    this.stepsDone = (int) metisPlugins.stream()
        .map(AbstractMetisPlugin::getPluginStatus).filter(FINISHED_STATUS_SET::contains).count();
    final AbstractMetisPlugin currentPlugin = metisPlugins.stream()
        .filter(plugin -> EXECUTING_STATUS_SET.contains(plugin.getPluginStatus())).findFirst()
        .orElse(null);
    this.stepsTotal = metisPlugins.size();
    if (currentPlugin instanceof AbstractExecutablePlugin abstractExecutablePlugin) {
      this.currentPluginProgress = new PluginProgressView(abstractExecutablePlugin.getExecutionProgress());
    }
  }

  public int getStepsDone() {
    return stepsDone;
  }

  public int getStepsTotal() {
    return stepsTotal;
  }

  public PluginProgressView getCurrentPluginProgress() {
    return currentPluginProgress;
  }
}
