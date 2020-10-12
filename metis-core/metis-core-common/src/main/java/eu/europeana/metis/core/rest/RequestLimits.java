package eu.europeana.metis.core.rest;

/**
 * Contains default values for limits per specific endpoint requests.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-16
 */
public enum RequestLimits {

  DATASETS_PER_REQUEST(10),
  WORKFLOW_EXECUTIONS_PER_REQUEST(10),
  SCHEDULED_EXECUTIONS_PER_REQUEST(10),
  DEPUBLISHED_RECORDS_PER_REQUEST(20);

  private final int limit;

  RequestLimits(int limit) {
    this.limit = limit;
  }

  public int getLimit() {
    return limit;
  }
}
