package eu.europeana.metis.core.rest;

import com.fasterxml.jackson.annotation.JsonFormat;
import eu.europeana.metis.CommonStringValues;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * This class represents the entire execution history for a dataset.
 */
public class ExecutionHistory {

  private List<Execution> executions;

  public List<Execution> getExecutions() {
    return Collections.unmodifiableList(executions);
  }

  public void setExecutions(Collection<Execution> executions) {
    this.executions = new ArrayList<>(executions);
  }

  /**
   * This class represents one workflow execution.
   */
  public static class Execution {

    private String workflowExecutionId;

    @JsonFormat(pattern = CommonStringValues.DATE_FORMAT)
    private Date startedDate;

    public String getWorkflowExecutionId() {
      return workflowExecutionId;
    }

    public Date getStartedDate() {
      return new Date(startedDate.getTime());
    }

    public void setWorkflowExecutionId(String workflowExecutionId) {
      this.workflowExecutionId = workflowExecutionId;
    }

    public void setStartedDate(Date startedDate) {
      this.startedDate = new Date(startedDate.getTime());
    }
  }
}
