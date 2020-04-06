package eu.europeana.metis.core.rest;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Class used to wrap a list of result object to be given back as a response on a REST API
 * endpoint.
 *
 * @param <T> the type of objects to be wrapped
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-06-01
 */

public class ResponseListWrapper<T> {

  @JacksonXmlElementWrapper(localName = "Results")
  @JacksonXmlProperty(localName = "Result")
  private List<T> results;
  private int listSize;
  private int nextPage;
  private Boolean maxResultCountReached;

  /**
   * Accepts a list of results objects, and based on the resultsPerRequestLimit it will determine if
   * there would be another nextPage. This method assumes a page count of 1.
   *
   * @param results the {@link List} of objects
   * @param resultsPerRequestLimit the result limit per request
   * @param nextPage the positive next page number or -1 if there shouldn't be another page
   */
  public void setResultsAndLastPage(List<T> results, int resultsPerRequestLimit, int nextPage) {
    this.setResultsAndLastPage(results, resultsPerRequestLimit, nextPage, null);
  }
  
  /**
   * Accepts a list of results objects, and based on the resultsPerRequestLimit it will determine if
   * there would be another nextPage. This method assumes a page count of 1.
   *
   * @param results the {@link List} of objects
   * @param resultsPerRequestLimit the result limit per request
   * @param nextPage the positive next page number or -1 if there shouldn't be another page
   * @param maxResultCountReached whether the maximum result count is reached (the number of
   *        results, regardless of pagination, the server is willing to serve). Can be null if this
   *        is not applicable.
   */
  public void setResultsAndLastPage(List<T> results, int resultsPerRequestLimit, int nextPage,
      Boolean maxResultCountReached) {
    setResultsAndLastPage(results, resultsPerRequestLimit, nextPage, 1, maxResultCountReached);
  }

  /**
   * Accepts a list of results objects, and based on the resultsPerRequestLimit it will determine if
   * there would be another nextPage.
   *
   * @param results the {@link List} of objects
   * @param resultsPerRequestLimit the result limit per request
   * @param nextPage the positive next page number or -1 if there shouldn't be another page
   * @param pageCount the number of pages that were requested.
   */
  public void setResultsAndLastPage(List<T> results, int resultsPerRequestLimit, int nextPage,
      int pageCount) {
    this.setResultsAndLastPage(results, resultsPerRequestLimit, nextPage, pageCount, null);
  }
  
  /**
   * Accepts a list of results objects, and based on the resultsPerRequestLimit it will determine if
   * there would be another nextPage.
   *
   * @param results the {@link List} of objects
   * @param resultsPerRequestLimit the result limit per request
   * @param nextPage the positive next page number or -1 if there shouldn't be another page
   * @param pageCount the number of pages that were requested.
   * @param maxResultCountReached whether the maximum result count is reached (the number of
   *        results, regardless of pagination, the server is willing to serve). Can be null if this
   *        is not applicable.
   */
  public void setResultsAndLastPage(List<T> results, int resultsPerRequestLimit, int nextPage,
      int pageCount, Boolean maxResultCountReached) {
    if (results == null || results.isEmpty() || Boolean.TRUE.equals(maxResultCountReached)) {
      this.nextPage = -1;
    } else if (results.size() < resultsPerRequestLimit * pageCount) {
      this.nextPage = -1;
    } else {
      this.nextPage = nextPage + pageCount;
    }
    this.listSize = results == null ? 0 : results.size();
    this.maxResultCountReached = maxResultCountReached;
    setResults(results);
  }

  /**
   * Clear the internal data. Used to not recreate the same structure all the time if it's used in a
   * loop.
   */
  public void clear() {
    if (results != null) {
      results.clear();
    }
    listSize = 0;
    nextPage = 0;
  }

  public List<T> getResults() {
    return Optional.ofNullable(results).map(ArrayList::new).orElse(null);
  }

  public void setResults(List<T> results) {
    this.results = Optional.ofNullable(results).map(ArrayList::new).orElse(null);
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

  public Boolean getMaxResultCountReached() {
    return maxResultCountReached;
  }

  public void setMaxResultCountReached(Boolean maxResultCountReached) {
    this.maxResultCountReached = maxResultCountReached;
  }
}
