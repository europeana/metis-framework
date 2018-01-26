package eu.europeana.metis.core.rest;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eu.europeana.metis.core.workflow.HasMongoObjectId;
import java.util.List;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-06-01
 */
public class ResponseListWrapper<T extends HasMongoObjectId> {

  @JacksonXmlElementWrapper(localName = "Results")
  @JacksonXmlProperty(localName = "Result")
  private List<T> results;
  private int listSize;
  private int nextPage;

  public void setResultsAndLastPage(
      List<T> results,
      int resultsPerRequestLimit, int nextPage) {
    if (results != null && results.size() != 0) {
      if (results.size() < resultsPerRequestLimit) {
        this.nextPage = -1;
      } else {
        this.nextPage = nextPage+1;
      }
      listSize = results.size();
    } else {
      this.nextPage = -1;
    }
    this.results = results;
  }

  public List<T> getResults() {
    return results;
  }

  public void setResults(List<T> results) {
    this.results = results;
  }

  public int getNextPage() {
    return nextPage;
  }

  public void setNextPage(int nextPage) {
    this.nextPage = nextPage;
  }

  public int getListSize() {
    return listSize;
  }

  public void setListSize(int listSize) {
    this.listSize = listSize;
  }

}
