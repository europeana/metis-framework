package eu.europeana.metis.core.workflow;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-26
 */
public enum WorkflowStatus {
  INQUEUE, RUNNING, FINISHED, CANCELLED, NULL;

  @JsonCreator
  public static WorkflowStatus getWorkflowStatusFromEnumName(String name){
    for (WorkflowStatus workflowStatus: WorkflowStatus.values()) {
      if(workflowStatus.name().equalsIgnoreCase(name)){
        return workflowStatus;
      }
    }
    return NULL;
  }
}
