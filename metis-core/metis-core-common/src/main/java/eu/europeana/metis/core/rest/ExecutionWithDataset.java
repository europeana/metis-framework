package eu.europeana.metis.core.rest;

import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.workflow.HasMongoObjectId;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bson.types.ObjectId;

/**
 * This object contains an instance of {@link WorkflowExecution}, but paired to it the execution's
 * {@link Dataset}. It implements {@link HasMongoObjectId} so that it can be a type for {@link
 * ResponseListWrapper}.
 */
public class ExecutionWithDataset implements HasMongoObjectId {

  private static final Set<PluginStatus> EXECUTING_STATUS_SET = Stream
      .of(PluginStatus.RUNNING, PluginStatus.CLEANING, PluginStatus.PENDING)
      .collect(Collectors.toSet());
  private static final Set<PluginStatus> FINISHED_STATUS_SET = Stream
      .of(PluginStatus.FINISHED, PluginStatus.FAILED, PluginStatus.CANCELLED)
      .collect(Collectors.toSet());

  private WorkflowExecution execution;
  private Dataset dataset;
  private Progress progress;

  ExecutionWithDataset() {
  }

  /**
   * Constructor.
   *
   * @param execution The exection.
   * @param dataset The dataset that matches the execution (i.e. {@link Dataset#getId()} matches
   * {@link WorkflowExecution#getDatasetId()}).
   */
  public ExecutionWithDataset(WorkflowExecution execution, Dataset dataset) {
    this.execution = execution;
    this.dataset = dataset;

    final AbstractMetisPlugin currentPlugin = execution.getMetisPlugins().stream()
        .filter(plugin -> EXECUTING_STATUS_SET.contains(plugin.getPluginStatus())).findFirst()
        .orElse(null);
    final int finishedCount = (int) execution.getMetisPlugins().stream()
        .map(AbstractMetisPlugin::getPluginStatus).filter(FINISHED_STATUS_SET::contains).count();
    this.progress = new Progress(currentPlugin, finishedCount, execution.getMetisPlugins().size());
  }

  public WorkflowExecution getExecution() {
    return execution;
  }

  public Dataset getDataset() {
    return dataset;
  }

  public Progress getProgress() {
    return progress;
  }

  @Override
  public ObjectId getId() {
    return execution.getId();
  }

  @Override
  public void setId(ObjectId id) {
    execution.setId(id);
  }

  /**
   * This class contains progress information on this workflow execution.
   */
  public static class Progress {

    private Integer expectedRecords;
    private Integer processedRecords;
    private Integer errors;
    private int stepsDone;
    private int stepsTotal;

    Progress() {
    }

    Progress(AbstractMetisPlugin currentPlugin, int stepsDone, int stepsTotal) {
      this.expectedRecords =
          currentPlugin != null ? currentPlugin.getExecutionProgress().getExpectedRecords() : null;
      this.processedRecords =
          currentPlugin != null ? currentPlugin.getExecutionProgress().getProcessedRecords() : null;
      this.errors = currentPlugin != null ? currentPlugin.getExecutionProgress().getErrors() : null;
      this.stepsDone = stepsDone;
      this.stepsTotal = stepsTotal;
    }

    public Integer getExpectedRecords() {
      return expectedRecords;
    }

    public Integer getProcessedRecords() {
      return processedRecords;
    }

    public Integer getErrors() {
      return errors;
    }

    public int getStepsDone() {
      return stepsDone;
    }

    public int getStepsTotal() {
      return stepsTotal;
    }
  }
}
