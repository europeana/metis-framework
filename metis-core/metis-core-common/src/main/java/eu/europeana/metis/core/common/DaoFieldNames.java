package eu.europeana.metis.core.common;

/**
 * Enumeration that contains field names for dao queries.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2019-07-04
 */
public enum DaoFieldNames {
  ID("_id"),
  DATASET_ID("datasetId"),
  DATASET_NAME("datasetName"),
  WORKFLOW_NAME("workflowName"),
  WORKFLOW_STATUS("workflowStatus"),
  PLUGIN_STATUS("pluginStatus"),
  PLUGIN_TYPE("pluginType"),
  METIS_PLUGINS("metisPlugins"),
  CREATED_DATE("createdDate"),
  STARTED_DATE("startedDate"),
  UPDATED_DATE("updatedDate"),
  FINISHED_DATE("finishedDate");

  private final String fieldName;

  DaoFieldNames(String fieldName) {
    this.fieldName = fieldName;
  }

  public String getFieldName() {
    return fieldName;
  }

  @Override
  public String toString() {
    return fieldName;
  }
}
