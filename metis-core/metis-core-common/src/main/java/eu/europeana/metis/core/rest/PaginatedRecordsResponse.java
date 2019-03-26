package eu.europeana.metis.core.rest;

import java.util.List;

/**
 * Class that encapsulates a list of {@link Record} including a {@link #nextPage} field.
 */
public class PaginatedRecordsResponse extends RecordsResponse {

  private String nextPage;

  /**
   * Constructor with the required parameters.
   *
   * @param records the list of {@link Record}
   * @param nextPage the String representation of the nextPage which is retrieved from a previous
   * response
   */
  public PaginatedRecordsResponse(List<Record> records, String nextPage) {
    super(records);
    this.nextPage = nextPage;
  }

  public String getNextPage() {
    return nextPage;
  }

  public void setNextPage(String nextPage) {
    this.nextPage = nextPage;
  }
}
