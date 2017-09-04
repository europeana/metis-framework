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
  private String nextPage;
  private int listSize;

  public void setResultsAndLastPage(
      List<T> results,
      int resultsPerRequestLimit) {
    if (results != null && results.size() != 0) {
      if (results.size() < resultsPerRequestLimit) {
        nextPage = null;
      } else {
        nextPage = results.get(results.size() - 1).getId().toString();
      }
      listSize = results.size();
    } else {
      nextPage = null;
    }
    this.results = results;
  }

  public List<T> getResults() {
    return results;
  }

  public void setResults(List<T> results) {
    this.results = results;
  }

  public String getNextPage() {
    return nextPage;
  }

  public void setNextPage(String nextPage) {
    this.nextPage = nextPage;
  }

  public int getListSize() {
    return listSize;
  }

  public void setListSize(int listSize) {
    this.listSize = listSize;
  }

}
