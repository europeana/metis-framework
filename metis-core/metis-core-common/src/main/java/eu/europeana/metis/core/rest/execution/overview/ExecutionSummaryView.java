package eu.europeana.metis.core.rest.execution.overview;

import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;

/**
 * This class represents the vital information on a workflow execution needed for the execution
 * overview.
 */
public class ExecutionSummaryView {

  private ObjectId id;
  private WorkflowStatus workflowStatus;
  private boolean cancelling;

  private Date createdDate;
  private Date startedDate;
  private Date updatedDate;
  private Date finishedDate;

  private List<PluginSummaryView> plugins;

  ExecutionSummaryView() {
  }

  ExecutionSummaryView(WorkflowExecution execution) {
    this.id = execution.getId();
    this.workflowStatus = execution.getWorkflowStatus();
    this.cancelling = execution.isCancelling();
    this.createdDate = execution.getCreatedDate();
    this.startedDate = execution.getStartedDate();
    this.updatedDate = execution.getUpdatedDate();
    this.finishedDate = execution.getFinishedDate();
    this.plugins = execution.getMetisPlugins().stream().map(PluginSummaryView::new).collect(
        Collectors.toList());
  }

  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }

  public WorkflowStatus getWorkflowStatus() {
    return workflowStatus;
  }

  public boolean isCancelling() {
    return cancelling;
  }

  public Date getCreatedDate() {
    return Optional.ofNullable(createdDate).map(Date::getTime).map(Date::new).orElse(null);
  }

  public Date getStartedDate() {
    return Optional.ofNullable(startedDate).map(Date::getTime).map(Date::new).orElse(null);
  }

  public Date getUpdatedDate() {
    return Optional.ofNullable(updatedDate).map(Date::getTime).map(Date::new).orElse(null);
  }

  public Date getFinishedDate() {
    return Optional.ofNullable(finishedDate).map(Date::getTime).map(Date::new).orElse(null);
  }

  public List<PluginSummaryView> getPlugins() {
    return Collections.unmodifiableList(plugins);
  }
}
