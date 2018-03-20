package eu.europeana.metis.core.rest;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that encapsulates a list of {@link Record} including a {@link #nextPage} field.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-02-26
 */
public class RecordsResponse {

  private List<Record> records;
  private String nextPage;

  /**
   * Constructor with the required parameters.
   * @param records the list of {@link Record}
   * @param nextPage the String representation of the nextPage which is retrieved from a previous response
   */
  public RecordsResponse(List<Record> records, String nextPage) {
    this.records = new ArrayList<>(records);
    this.nextPage = nextPage;
  }

  public List<Record> getRecords() {
    return new ArrayList<>(records);
  }

  public void setRecords(List<Record> records) {
    this.records = new ArrayList<>(records);
  }

  public String getNextPage() {
    return nextPage;
  }

  public void setNextPage(String nextPage) {
    this.nextPage = nextPage;
  }
}
