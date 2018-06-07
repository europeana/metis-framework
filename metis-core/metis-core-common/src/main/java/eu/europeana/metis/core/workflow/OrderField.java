package eu.europeana.metis.core.workflow;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-23
 */
public enum OrderField {
  ID("_id"), FINISHED_DATE("finishedDate"), UPDATED_DATE("updatedDate"), STARTED_DATE(
      "startedDate"), CREATED_DATE("createdDate"), WORKFLOW_STATUS("workflowStatus"), WORKFLOW_NAME(
      "workflowName");

  private String orderFieldName;

  OrderField(String orderFieldName) {
    this.orderFieldName = orderFieldName;
  }

  public String getOrderFieldName() {
    return orderFieldName;
  }
}
