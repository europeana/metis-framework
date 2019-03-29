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
public class ExecutionAndDatasetView implements HasMongoObjectId {


  private ExecutionSummaryView execution;
  private DatasetSummaryView dataset;
  private ExecutionProgressView executionProgress;

  ExecutionAndDatasetView() {
  }

  /**
   * Constructor.
   *
   * @param execution The exection.
   * @param dataset The dataset that matches the execution (i.e. {@link Dataset#getId()} matches
   * {@link WorkflowExecution#getDatasetId()}).
   */
  public ExecutionAndDatasetView(WorkflowExecution execution, Dataset dataset) {
    this.execution = new ExecutionSummaryView(execution);
    this.dataset = new DatasetSummaryView(dataset);
    this.executionProgress = new ExecutionProgressView(execution);
  }

  public ExecutionSummaryView getExecution() {
    return execution;
  }

  public DatasetSummaryView getDataset() {
    return dataset;
  }

  public ExecutionProgressView getExecutionProgress() {
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
