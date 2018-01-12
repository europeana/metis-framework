package eu.europeana.metis.core.execution;

import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-09-21
 */
public class FailsafeExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(FailsafeExecutor.class);

  private final OrchestratorService orchestratorService;
  private final RedissonClient redissonClient;
  private static final String FAILSAFE_LOCK = "failsafeLock";
  private final RLock lock;

  public FailsafeExecutor(OrchestratorService orchestratorService, RedissonClient redissonClient) {
    this.orchestratorService = orchestratorService;
    this.redissonClient = redissonClient;
    this.lock = redissonClient.getFairLock(FAILSAFE_LOCK);
  }

  public void performFailsafe() {
    try {
      lock.lock();
      List<WorkflowExecution> allInQueueAndRunningWorkflowExecutions = new ArrayList<>();
      addUserWorkflowExecutionsWithStatusInQueue(WorkflowStatus.RUNNING,
          allInQueueAndRunningWorkflowExecutions);
      addUserWorkflowExecutionsWithStatusInQueue(WorkflowStatus.INQUEUE,
          allInQueueAndRunningWorkflowExecutions);

      if (!allInQueueAndRunningWorkflowExecutions.isEmpty()) {
        orchestratorService
            .removeActiveWorkflowExecutionsFromList(allInQueueAndRunningWorkflowExecutions);

        for (WorkflowExecution workflowExecution : allInQueueAndRunningWorkflowExecutions) {
          orchestratorService.addWorkflowExecutionToQueue(workflowExecution.getId().toString(),
              workflowExecution.getWorkflowPriority());
        }
      }
    } catch (RuntimeException e) {
      LOGGER.warn(
          "Exception thrown from rabbitmq channel or Redis disconnection, failsafe thread continues",
          e);
    } finally {
      try {
        lock.unlock();
      } catch (RedisConnectionException e) {
        LOGGER.warn("Cannot connect to unlock, failsafe thread continues");
      }
    }
  }

  private void addUserWorkflowExecutionsWithStatusInQueue(WorkflowStatus workflowStatus,
      List<WorkflowExecution> workflowExecutions) {
    String nextPage = null;
    ResponseListWrapper<WorkflowExecution> userWorkflowExecutionResponseListWrapper;
    do {
      userWorkflowExecutionResponseListWrapper = new ResponseListWrapper<>();
      userWorkflowExecutionResponseListWrapper.setResultsAndLastPage(orchestratorService
              .getAllWorkflowExecutions(workflowStatus, nextPage),
          orchestratorService.getWorkflowExecutionsPerRequest());
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
