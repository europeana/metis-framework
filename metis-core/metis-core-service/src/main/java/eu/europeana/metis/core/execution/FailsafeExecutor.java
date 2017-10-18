package eu.europeana.metis.core.execution;

import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PreDestroy;
import org.apache.solr.common.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-09-21
 */
public class FailsafeExecutor implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(FailsafeExecutor.class);

  private final int periodicFailsafeCheckInSecs;
  private final OrchestratorService orchestratorService;
  private final RedissonClient redissonClient;
  private static final String FAILSAFE_LOCK = "failsafeLock";
  private final boolean infiniteLoop; //True for infinite loop which is the normal scenario, false for testing

  public FailsafeExecutor(OrchestratorService orchestratorService, RedissonClient redissonClient, int periodicFailsafeCheckInSecs, boolean infiniteLoop) {
    this.orchestratorService = orchestratorService;
    this.redissonClient = redissonClient;
    this.periodicFailsafeCheckInSecs = periodicFailsafeCheckInSecs;
    this.infiniteLoop = infiniteLoop;
  }

  @SuppressWarnings("InfiniteLoopStatement")
  @Override
  public void run() {
    RLock lock = redissonClient.getFairLock(FAILSAFE_LOCK);
    do {
      try {
        LOGGER.info("Failsafe thread sleeping for {} seconds.", periodicFailsafeCheckInSecs);
        Thread.sleep(periodicFailsafeCheckInSecs * 1000L);

        lock.lock();
        LOGGER.info("Failsafe thread woke up.");
        List<UserWorkflowExecution> allInQueueAndRunningUserWorkflowExecutions = new ArrayList<>();
        addUserWorkflowExecutionsWithStatusInQueue(WorkflowStatus.RUNNING, allInQueueAndRunningUserWorkflowExecutions);
        addUserWorkflowExecutionsWithStatusInQueue(WorkflowStatus.INQUEUE, allInQueueAndRunningUserWorkflowExecutions);

        if (!allInQueueAndRunningUserWorkflowExecutions.isEmpty()) {
          orchestratorService
              .removeActiveUserWorkflowExecutionsFromList(allInQueueAndRunningUserWorkflowExecutions);

          for (UserWorkflowExecution userWorkflowExecution : allInQueueAndRunningUserWorkflowExecutions) {
            orchestratorService
                .addUserWorkflowExecutionToQueue(userWorkflowExecution.getId().toString(),
                    userWorkflowExecution.getWorkflowPriority());
          }
        }
      } catch (Exception e) {
        LOGGER.warn(
            "Thread was interruped or exception thrown from rabbitmq channel disconnection, failsafe thread continues",
            e);
      }
      finally {
        lock.unlock();
      }
    } while (infiniteLoop);
  }

  private void addUserWorkflowExecutionsWithStatusInQueue(WorkflowStatus workflowStatus,
      List<UserWorkflowExecution> userWorkflowExecutions) {
    String nextPage = null;
    ResponseListWrapper<UserWorkflowExecution> userWorkflowExecutionResponseListWrapper;
    do {
      userWorkflowExecutionResponseListWrapper = new ResponseListWrapper<>();
      userWorkflowExecutionResponseListWrapper.setResultsAndLastPage(orchestratorService
              .getAllUserWorkflowExecutions(workflowStatus, nextPage),
          orchestratorService.getUserWorkflowExecutionsPerRequest());
      userWorkflowExecutions
          .addAll(userWorkflowExecutionResponseListWrapper.getResults());
      nextPage = userWorkflowExecutionResponseListWrapper.getNextPage();
    } while (!StringUtils.isEmpty(nextPage));
  }

  @PreDestroy
  public void close() {
    if (!redissonClient.isShutdown()) {
      this.redissonClient.shutdown();
    }
  }
}
