package eu.europeana.metis.core.rest;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eu.europeana.metis.core.workflow.HasMongoObjectId;
import java.util.ArrayList;
import java.util.List;


/**
 * Class used to wrap a list of result object to be given back as a response on a REST API
 * endpoint.
 *
 * @param <T> should extend {@link HasMongoObjectId}
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-06-01
 */

public class ResponseListWrapper<T extends HasMongoObjectId> {

  @JacksonXmlElementWrapper(localName = "Results")
  @JacksonXmlProperty(localName = "Result")
  private List<T> results;
  private int listSize;
  private int nextPage;

  /**
   * Accepts a list of results objects, and based on the resultsPerRequestLimit it will determine if
   * there would be another nextPage. This method assumes a page count of 1.
   *
   * @param results the {@link List} of objects
   * @param resultsPerRequestLimit the result limit per request
   * @param nextPage the positive next page number or -1 if there shouldn't be another page
   */
  public void setResultsAndLastPage(List<T> results, int resultsPerRequestLimit, int nextPage) {
    setResultsAndLastPage(results, resultsPerRequestLimit, nextPage, 1);
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
    if (results == null || results.isEmpty()) {
      this.nextPage = -1;
    } else {
      if (results.size() < resultsPerRequestLimit * pageCount) {
        this.nextPage = -1;
      } else {
        this.nextPage = nextPage + pageCount;
      }
      listSize = results.size();
    }
    this.results = results == null ? null : new ArrayList<>(results);
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
    return results == null ? null : new ArrayList<>(results);
  }

  public void setResults(List<T> results) {
    this.results = results == null ? null : new ArrayList<>(results);
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
