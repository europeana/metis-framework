package eu.europeana.metis.core.rest;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eu.europeana.metis.core.workflow.HasMongoObjectId;
import java.util.List;


/**
 * Class used to wrap a list of result object to be given back as a response on a REST API endpoint.
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-06-01
 * @param <T> should extend {@link HasMongoObjectId}
 */

public class ResponseListWrapper<T extends HasMongoObjectId> {

  @JacksonXmlElementWrapper(localName = "Results")
  @JacksonXmlProperty(localName = "Result")
  private List<T> results;
  private int listSize;
  private int nextPage;

  /**
   * Accepts a list of results objects, and based on the resultsPerRequestLimit it will determine if
   * there would be another nextPage.
   * @param results the {@link List} of objects
   * @param resultsPerRequestLimit the result limit per request
   * @param nextPage the positive next page number or -1 if there shouldn't be another page
   */
  public void setResultsAndLastPage(
      List<T> results,
      int resultsPerRequestLimit, int nextPage) {
    if (results != null && !results.isEmpty()) {
      if (results.size() < resultsPerRequestLimit) {
        this.nextPage = -1;
      } else {
        this.nextPage = nextPage + 1;
      }
      listSize = results.size();
    } else {
      this.nextPage = -1;
    }
    this.results = results;
  }

  /**
   * Clear the internal data.
   * Used to not recreate the same structure all the time if it's used in a loop.
   */
  public void clear() {
    if (results != null) {
      results.clear();
    }
    listSize = 0;
    nextPage = 0;
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
