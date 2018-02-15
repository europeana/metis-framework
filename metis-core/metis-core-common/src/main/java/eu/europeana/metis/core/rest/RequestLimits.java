package eu.europeana.metis.core.rest;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-16
 */
public enum RequestLimits {
  DATASETS_PER_REQUEST(5), WORKFLOW_EXECUTIONS_PER_REQUEST(5), WORKFLOWS_PER_REQUEST(
      5), SCHEDULED_EXECUTIONS_PER_REQUEST(5);

  private int limit;

  RequestLimits(int limit) {
    this.limit = limit;
  }

  public int getLimit() {
    return limit;
  }
}
