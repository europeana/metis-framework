package eu.europeana.metis.core.workflow;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-06-01
 */
public class UserWorkflowExecutionWrapper {

  @JacksonXmlElementWrapper(localName = "UserWorkflowExecutions")
  @JacksonXmlProperty(localName = "UserWorkflowExecution")
  private List<UserWorkflowExecution> userWorkflowExecutions;
  private String nextPage;
  private int listSize;

  public void setUserWorkflowExecutionsAndLastPage(
      List<UserWorkflowExecution> userWorkflowExecutions,
      int userWorkflowExecutionsPerRequestLimit) {
    if (userWorkflowExecutions != null && userWorkflowExecutions.size() != 0) {
      if (userWorkflowExecutions.size() < userWorkflowExecutionsPerRequestLimit) {
        nextPage = null;
      } else {
        nextPage = userWorkflowExecutions.get(userWorkflowExecutions.size() - 1).getId().toString();
      }
      listSize = userWorkflowExecutions.size();
    } else {
      nextPage = null;
    }
    this.userWorkflowExecutions = userWorkflowExecutions;
  }

  public List<UserWorkflowExecution> getUserWorkflowExecutions() {
    return userWorkflowExecutions;
  }

  public void setUserWorkflowExecutions(
      List<UserWorkflowExecution> userWorkflowExecutions) {
    this.userWorkflowExecutions = userWorkflowExecutions;
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
