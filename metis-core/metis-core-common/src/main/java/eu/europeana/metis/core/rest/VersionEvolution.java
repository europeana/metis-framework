package eu.europeana.metis.core.rest;

import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * This class represents a history of operations that are applied to a dataset.
 */
public class VersionEvolution {

  private List<VersionEvolutionStep> evolutionSteps;

  public List<VersionEvolutionStep> getEvolutionSteps() {
    return Collections.unmodifiableList(evolutionSteps);
  }

  public void setEvolutionSteps(Collection<VersionEvolutionStep> versions) {
    this.evolutionSteps = new ArrayList<>(versions);
  }

  /**
   * This class represents one operation applied to a dataset.
   */
  public static class VersionEvolutionStep {

    private String workflowExecutionId;
    private ExecutablePluginType pluginType;
    private Date finishedTime;

    public String getWorkflowExecutionId() {
      return workflowExecutionId;
    }

    public void setWorkflowExecutionId(String workflowExecutionId) {
      this.workflowExecutionId = workflowExecutionId;
    }

    public ExecutablePluginType getPluginType() {
      return pluginType;
    }

    public void setPluginType(ExecutablePluginType pluginType) {
      this.pluginType = pluginType;
    }

    public Date getFinishedTime() {
      return new Date(finishedTime.getTime());
    }

    public void setFinishedTime(Date finishedTime) {
      this.finishedTime = new Date(finishedTime.getTime());
    }
  }
}
