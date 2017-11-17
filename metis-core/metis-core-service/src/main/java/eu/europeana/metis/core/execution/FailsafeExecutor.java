package eu.europeana.metis.core.execution;

import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PreDestroy;
import org.apache.solr.common.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisConnectionException;
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

  public FailsafeExecutor(OrchestratorService orchestratorService, RedissonClient redissonClient,
      int periodicFailsafeCheckInSecs, boolean infiniteLoop) {
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
        List<WorkflowExecution> allInQueueAndRunningWorkflowExecutions = new ArrayList<>();
        addUserWorkflowExecutionsWithStatusInQueue(WorkflowStatus.RUNNING,
            allInQueueAndRunningWorkflowExecutions);
        addUserWorkflowExecutionsWithStatusInQueue(WorkflowStatus.INQUEUE,
            allInQueueAndRunningWorkflowExecutions);

        if (!allInQueueAndRunningWorkflowExecutions.isEmpty()) {
          orchestratorService
              .removeActiveUserWorkflowExecutionsFromList(
                  allInQueueAndRunningWorkflowExecutions);

          for (WorkflowExecution workflowExecution : allInQueueAndRunningWorkflowExecutions) {
            orchestratorService
                .addUserWorkflowExecutionToQueue(workflowExecution.getId().toString(),
                    workflowExecution.getWorkflowPriority());
          }
        }
      } catch (Exception e) {
        LOGGER.warn(
            "Thread was interruped or exception thrown from rabbitmq channel or Redis disconnection, failsafe thread continues",
            e);
      } finally {
        try {
          lock.unlock();
        } catch (RedisConnectionException e) {
          LOGGER.warn("Cannot connect to unlock, failsafe thread continues");
        }
      }
    } while (infiniteLoop);
  }

  private void addUserWorkflowExecutionsWithStatusInQueue(WorkflowStatus workflowStatus,
      List<WorkflowExecution> workflowExecutions) {
    String nextPage = null;
    ResponseListWrapper<WorkflowExecution> userWorkflowExecutionResponseListWrapper;
    do {
      userWorkflowExecutionResponseListWrapper = new ResponseListWrapper<>();
      userWorkflowExecutionResponseListWrapper.setResultsAndLastPage(orchestratorService
              .getAllUserWorkflowExecutions(workflowStatus, nextPage),
          orchestratorService.getUserWorkflowExecutionsPerRequest());
      workflowExecutions
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
