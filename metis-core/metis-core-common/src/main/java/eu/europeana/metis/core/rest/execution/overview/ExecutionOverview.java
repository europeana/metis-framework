package eu.europeana.metis.core.rest.execution.overview;

import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.workflow.HasMongoObjectId;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import org.bson.types.ObjectId;

/**
 * This object contains an instance of {@link WorkflowExecution}, but paired to it the execution's
 * {@link Dataset}. It implements {@link HasMongoObjectId} so that it can be a type for {@link
 * ResponseListWrapper}.
 */
public class ExecutionOverview implements HasMongoObjectId {


  private ExecutionSummary execution;
  private DatasetSummary dataset;
  private ExecutionProgress executionProgress;

  ExecutionOverview() {
  }

  /**
   * Constructor.
   *
   * @param execution The exection.
   * @param dataset The dataset that matches the execution (i.e. {@link Dataset#getId()} matches
   * {@link WorkflowExecution#getDatasetId()}).
   */
  public ExecutionOverview(WorkflowExecution execution, Dataset dataset) {
    this.execution = new ExecutionSummary(execution);
    this.dataset = new DatasetSummary(dataset);
    this.executionProgress = new ExecutionProgress(execution);
  }

  public ExecutionSummary getExecution() {
    return execution;
  }

  public DatasetSummary getDataset() {
    return dataset;
  }

  public ExecutionProgress getExecutionProgress() {
    return executionProgress;
  }

  @Override
  public ObjectId getId() {
    return execution.getId();
  }

  @Override
  public void setId(ObjectId id) {
    execution.setId(id);
  }

}
