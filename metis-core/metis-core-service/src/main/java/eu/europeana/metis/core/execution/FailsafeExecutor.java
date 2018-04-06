package eu.europeana.metis.core.execution;

import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.core.workflow.OrderField;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
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
  private static final String FAILSAFE_LOCK = "failsafeLock";
  private final RLock lock;

  /**
   * Constructor the executor
   *
   * @param orchestratorService {@link OrchestratorService}
   * @param redissonClient {@link RedissonClient}
   */
  public FailsafeExecutor(OrchestratorService orchestratorService, RedissonClient redissonClient) {
    this.orchestratorService = orchestratorService;
    this.lock = redissonClient.getFairLock(FAILSAFE_LOCK);
  }

  /**
   * Makes a run to check if there are executions hanging and if some are found it will re-send them in the distributed queue.
   * It is meant that this method is ran periodically.
   */
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
        LOGGER.warn("Cannot connect to unlock, failsafe thread continues", e);
      }
    }
  }

  private void addUserWorkflowExecutionsWithStatusInQueue(WorkflowStatus workflowStatus,
      List<WorkflowExecution> workflowExecutions) {
    int nextPage = 0;
    ResponseListWrapper<WorkflowExecution> userWorkflowExecutionResponseListWrapper =new ResponseListWrapper<>();
    do {
      userWorkflowExecutionResponseListWrapper.clear();
      userWorkflowExecutionResponseListWrapper.setResultsAndLastPage(orchestratorService
              .getAllWorkflowExecutions(-1, null, EnumSet.of(workflowStatus),
                  OrderField.ID, true, nextPage),
          orchestratorService.getWorkflowExecutionsPerRequest(), nextPage);
      workflowExecutions
          .addAll(userWorkflowExecutionResponseListWrapper.getResults());
      nextPage = userWorkflowExecutionResponseListWrapper.getNextPage();
    } while (nextPage != -1);
  }
}
