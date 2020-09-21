package eu.europeana.metis.core.rest.execution.details;

import com.fasterxml.jackson.annotation.JsonFormat;
import eu.europeana.metis.CommonStringValues;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ExecutionView {

  private final String id;
  private final String datasetId;
  private final WorkflowStatus workflowStatus;
  private final String ecloudDatasetId;
  private final String cancelledBy;
  private final String startedBy;
  private final int workflowPriority;
  private final boolean cancelling;
  @JsonFormat(pattern = CommonStringValues.DATE_FORMAT)
  private final Date createdDate;
  @JsonFormat(pattern = CommonStringValues.DATE_FORMAT)
  private final Date startedDate;
  @JsonFormat(pattern = CommonStringValues.DATE_FORMAT)
  private final Date updatedDate;
  @JsonFormat(pattern = CommonStringValues.DATE_FORMAT)
  private final Date finishedDate;
  private final List<PluginView> metisPlugins;

  public ExecutionView(WorkflowExecution execution) {
    this.id = execution.getId().toString();
    this.datasetId = execution.getDatasetId();
    this.workflowStatus = execution.getWorkflowStatus();
    this.ecloudDatasetId = execution.getEcloudDatasetId();
    this.cancelledBy = execution.getCancelledBy();
    this.startedBy = execution.getStartedBy();
    this.workflowPriority = execution.getWorkflowPriority();
    this.cancelling = execution.isCancelling();
    this.createdDate = execution.getCreatedDate();
    this.startedDate = execution.getStartedDate();
    this.updatedDate = execution.getUpdatedDate();
    this.finishedDate = execution.getFinishedDate();
    this.metisPlugins = execution.getMetisPlugins().stream().map(PluginView::new)
            .collect(Collectors.toList());
  }

  public String getId() {
    return id;
  }

  public String getDatasetId() {
    return datasetId;
  }

  public WorkflowStatus getWorkflowStatus() {
    return workflowStatus;
  }

  public String getEcloudDatasetId() {
    return ecloudDatasetId;
  }

  public String getCancelledBy() {
    return cancelledBy;
  }

  public String getStartedBy() {
    return startedBy;
  }

  public int getWorkflowPriority() {
    return workflowPriority;
  }

  public boolean isCancelling() {
    return cancelling;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public Date getStartedDate() {
    return startedDate;
  }

  public Date getUpdatedDate() {
    return updatedDate;
  }

  public Date getFinishedDate() {
    return finishedDate;
  }

  public List<PluginView> getMetisPlugins() {
    return metisPlugins;
  }
}
