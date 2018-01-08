package eu.europeana.metis.core.rest;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-16
 */
public enum RequestLimits {
  DATASETS_PER_REQUEST(5), USER_WORKFLOW_EXECUTIONS_PER_REQUEST(
      5), USER_WORKFLOWS_PER_REQUEST(
      5),  SUGGEST_TERMS_PER_REQUEST(5);

  private int limit;

  RequestLimits(int limit) {
    this.limit = limit;
  }

  public int getLimit() {
    return limit;
  }
}
