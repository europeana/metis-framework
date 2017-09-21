package eu.europeana.metis.core.execution;

import eu.europeana.metis.core.dao.UserWorkflowExecutionDao;
import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import java.util.ArrayList;
import java.util.List;
import org.apache.solr.common.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-09-21
 */
public class FailsafeExecutor implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(FailsafeExecutor.class);

  private final int periodicFailsafeCheckInSecs = 60;
  private final UserWorkflowExecutionDao userWorkflowExecutionDao;
  private final UserWorkflowExecutorManager userWorkflowExecutorManager;

  public FailsafeExecutor(UserWorkflowExecutionDao userWorkflowExecutionDao,
      UserWorkflowExecutorManager userWorkflowExecutorManager) {
    this.userWorkflowExecutionDao = userWorkflowExecutionDao;
    this.userWorkflowExecutorManager = userWorkflowExecutorManager;
  }

  @Override
  public void run() {
    while (true) {
      try {
        LOGGER.info("Failsafe thread woke up.");
        List<UserWorkflowExecution> allInQueueAndRunningUserWorkflowExecutions = new ArrayList<>();
        addQueueExecutions(WorkflowStatus.RUNNING, allInQueueAndRunningUserWorkflowExecutions);
        addQueueExecutions(WorkflowStatus.INQUEUE, allInQueueAndRunningUserWorkflowExecutions);

        if (allInQueueAndRunningUserWorkflowExecutions.size() != 0) {
          userWorkflowExecutionDao
              .removeActiveExecutionsFromList(allInQueueAndRunningUserWorkflowExecutions,
                  userWorkflowExecutorManager.getMonitorCheckInSecs());

          for (UserWorkflowExecution userWorkflowExecution : allInQueueAndRunningUserWorkflowExecutions) {
            userWorkflowExecutorManager
                .addUserWorkflowExecutionToQueue(userWorkflowExecution.getId().toString(),
                    userWorkflowExecution.getWorkflowPriority());
          }
        }

        LOGGER.info("Failsafe thread sleeping for {} seconds.", periodicFailsafeCheckInSecs);
        Thread.sleep(periodicFailsafeCheckInSecs * 1000);
      } catch (Exception e) {
        LOGGER.warn("Thread was interruped", e);
      }
    }
  }

  private void addQueueExecutions(WorkflowStatus workflowStatus,
      List<UserWorkflowExecution> userWorkflowExecutions) {
    String nextPage = null;
    do {
      ResponseListWrapper<UserWorkflowExecution> userWorkflowExecutionResponseListWrapper = new ResponseListWrapper<>();
      userWorkflowExecutionResponseListWrapper.setResultsAndLastPage(userWorkflowExecutionDao
              .getAllUserWorkflowExecutions(workflowStatus, nextPage),
          userWorkflowExecutionDao.getUserWorkflowExecutionsPerRequest());
      userWorkflowExecutions
          .addAll(userWorkflowExecutionResponseListWrapper.getResults());
      nextPage = userWorkflowExecutionResponseListWrapper.getNextPage();
    } while (!StringUtils.isEmpty(nextPage));
  }
}
