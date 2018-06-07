package eu.europeana.metis.core.execution;

import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.core.service.ScheduleWorkflowService;
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledWorkflow;
import eu.europeana.metis.exception.GenericMetisException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-09-27
 */
public class SchedulerExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerExecutor.class);

  private final RLock lock;
  private final OrchestratorService orchestratorService;
  private final ScheduleWorkflowService scheduleWorkflowService;
  private static final String SCHEDULER_LOCK = "schedulerLock";
  private LocalDateTime lastExecutionTime = LocalDateTime.now();

  /**
   * Constructs the executor
   *
   * @param orchestratorService {@link OrchestratorService}
   * @param scheduleWorkflowService {@link ScheduleWorkflowService}
   * @param redissonClient {@link RedissonClient}
   */
  public SchedulerExecutor(OrchestratorService orchestratorService, ScheduleWorkflowService scheduleWorkflowService, RedissonClient redissonClient) {
    this.orchestratorService = orchestratorService;
    this.scheduleWorkflowService = scheduleWorkflowService;
    this.lock = redissonClient.getFairLock(SCHEDULER_LOCK);
  }

  /**
   * Makes a run to check if there are executions scheduled in a range of dates and if some are found it will send them in the distributed queue.
   * It is meant that this method is ran periodically.
   */
  public void performScheduling() {
    try {
      lock.lock();
      final LocalDateTime thisExecutionTime = LocalDateTime.now();
      LOGGER.info("Date range checking lowerbound: {}, upperBound:{}", this.lastExecutionTime,
          thisExecutionTime);
      List<ScheduledWorkflow> allCleanedScheduledWorkflows =
          getCleanedScheduledUserWorkflows(lastExecutionTime, thisExecutionTime);

      for (ScheduledWorkflow scheduledWorkflow : allCleanedScheduledWorkflows) {
        LOGGER.info("Adding ScheduledWorkflow with DatasetId: {},pointerDate: {}, frequence: {}",
            scheduledWorkflow.getDatasetId(), scheduledWorkflow.getPointerDate(),
            scheduledWorkflow.getScheduleFrequence());
        tryAddUserWorkflowInQueueOfUserWorkflowExecutions(scheduledWorkflow);
      }
      lastExecutionTime = thisExecutionTime;
    } catch (RuntimeException e) {
      LOGGER.warn(
          "Exception thrown from rabbitmq channel or Redis disconnection, scheduler thread continues",
          e);
    } finally {
      try {
        lock.unlock();
      } catch (RedisConnectionException e) {
        LOGGER.warn("Cannot connect to unlock, scheduler thread continues", e);
      }
    }
  }

  private List<ScheduledWorkflow> getCleanedScheduledUserWorkflows(LocalDateTime lowerBound,
      LocalDateTime upperBound) {
    List<ScheduledWorkflow> scheduledWorkflows = new ArrayList<>();
    scheduledWorkflows.addAll(getScheduledUserWorkflowsFrequenceOnce(lowerBound, upperBound));
    scheduledWorkflows.addAll(getScheduledUserWorkflowsFrequenceDaily(lowerBound, upperBound));
    scheduledWorkflows.addAll(getScheduledUserWorkflowsFrequenceWeekly(lowerBound, upperBound));
    scheduledWorkflows
        .addAll(getScheduledUserWorkflowsFrequenceMonthly(lowerBound, upperBound));
    return scheduledWorkflows;
  }

  private List<ScheduledWorkflow> getScheduledUserWorkflowsFrequenceOnce(
      LocalDateTime lowerBound,
      LocalDateTime upperBound) {
    int nextPage = 0;
    List<ScheduledWorkflow> scheduledWorkflows = new ArrayList<>();
    ResponseListWrapper<ScheduledWorkflow> scheduledUserWorkflowResponseListWrapper = new ResponseListWrapper<>();
    do {
      scheduledUserWorkflowResponseListWrapper.clear();
      scheduledUserWorkflowResponseListWrapper
          .setResultsAndLastPage(scheduleWorkflowService
                  .getAllScheduledWorkflowsByDateRangeONCE(lowerBound, upperBound, nextPage),
              scheduleWorkflowService.getScheduledWorkflowsPerRequest(), nextPage);
      scheduledWorkflows
          .addAll(scheduledUserWorkflowResponseListWrapper.getResults());
      nextPage = scheduledUserWorkflowResponseListWrapper.getNextPage();
    } while (nextPage != -1);
    return scheduledWorkflows;
  }

  private List<ScheduledWorkflow> getScheduledUserWorkflowsFrequenceDaily(
      LocalDateTime lowerBound,
      LocalDateTime upperBound) {
    List<ScheduledWorkflow> scheduledWorkflows = getScheduledUserWorkflows(
        ScheduleFrequence.DAILY);
    for (Iterator<ScheduledWorkflow> iterator = scheduledWorkflows.iterator();
        iterator.hasNext(); ) {
      ScheduledWorkflow scheduledWorkflow = iterator.next();

      LocalDateTime pointerDate = LocalDateTime
          .ofInstant(scheduledWorkflow.getPointerDate().toInstant(), ZoneId.systemDefault());
      LocalDateTime localDateToCheck = lowerBound.withYear(lowerBound.getYear())
          .withMonth(lowerBound.getMonthValue()).withHour(pointerDate.getHour())
          .withMinute(pointerDate.getMinute()).withSecond(pointerDate.getSecond())
          .withNano(pointerDate.getNano());

      if (localDateToCheck.isBefore(lowerBound) || localDateToCheck.isEqual(upperBound)
          || localDateToCheck.isAfter(upperBound)) {
        iterator.remove();
      }
    }
    return scheduledWorkflows;
  }

  private List<ScheduledWorkflow> getScheduledUserWorkflowsFrequenceWeekly(
      LocalDateTime lowerBound,
      LocalDateTime upperBound) {
    List<ScheduledWorkflow> scheduledWorkflows = getScheduledUserWorkflows(
        ScheduleFrequence.WEEKLY);
    for (Iterator<ScheduledWorkflow> iterator = scheduledWorkflows.iterator();
        iterator.hasNext(); ) {
      ScheduledWorkflow scheduledWorkflow = iterator.next();
      LocalDateTime pointerDate = LocalDateTime
          .ofInstant(scheduledWorkflow.getPointerDate().toInstant(), ZoneId.systemDefault());
      LocalDateTime localDateToCheck = lowerBound.withYear(lowerBound.getYear())
          .withMonth(lowerBound.getMonthValue()).withDayOfMonth(pointerDate.getDayOfMonth())
          .withHour(pointerDate.getHour())
          .withMinute(pointerDate.getMinute()).withSecond(pointerDate.getSecond())
          .withNano(pointerDate.getNano());

      if (lowerBound.getDayOfWeek() == localDateToCheck.getDayOfWeek()) {
        localDateToCheck = localDateToCheck.withDayOfMonth(lowerBound.getDayOfMonth());
      }

      if (localDateToCheck.isBefore(lowerBound) || localDateToCheck.isEqual(upperBound)
          || localDateToCheck.isAfter(upperBound)) {
        iterator.remove();
      }
    }
    return scheduledWorkflows;
  }

  private List<ScheduledWorkflow> getScheduledUserWorkflowsFrequenceMonthly(
      LocalDateTime lowerBound,
      LocalDateTime upperBound) {
    List<ScheduledWorkflow> scheduledWorkflows = getScheduledUserWorkflows(
        ScheduleFrequence.MONTHLY);
    for (Iterator<ScheduledWorkflow> iterator = scheduledWorkflows.iterator();
        iterator.hasNext(); ) {
      ScheduledWorkflow scheduledWorkflow = iterator.next();

      LocalDateTime pointerDate = LocalDateTime
          .ofInstant(scheduledWorkflow.getPointerDate().toInstant(), ZoneId.systemDefault());
      LocalDateTime localDateToCheck = lowerBound.withYear(lowerBound.getYear())
          .withMonth(pointerDate.getMonthValue()).withDayOfMonth(pointerDate.getDayOfMonth())
          .withHour(pointerDate.getHour())
          .withMinute(pointerDate.getMinute()).withSecond(pointerDate.getSecond())
          .withNano(pointerDate.getNano());

      if (lowerBound.getDayOfMonth() == localDateToCheck.getDayOfMonth()) {
        localDateToCheck = localDateToCheck.withMonth(lowerBound.getMonthValue());
      }

      if (localDateToCheck.isBefore(lowerBound) || localDateToCheck.isEqual(upperBound)
          || localDateToCheck.isAfter(upperBound)) {
        iterator.remove();
      }
    }
    return scheduledWorkflows;
  }

  private List<ScheduledWorkflow> getScheduledUserWorkflows(
      ScheduleFrequence scheduleFrequence) {
    int nextPage = 0;
    List<ScheduledWorkflow> scheduledWorkflows = new ArrayList<>();
    ResponseListWrapper<ScheduledWorkflow> scheduledUserWorkflowResponseListWrapper = new ResponseListWrapper<>();
    do {
      scheduledUserWorkflowResponseListWrapper.clear();
      scheduledUserWorkflowResponseListWrapper
          .setResultsAndLastPage(scheduleWorkflowService
                  .getAllScheduledWorkflowsWithoutAuthorization(scheduleFrequence, nextPage),
              scheduleWorkflowService.getScheduledWorkflowsPerRequest(), nextPage);
      scheduledWorkflows
          .addAll(scheduledUserWorkflowResponseListWrapper.getResults());
      nextPage = scheduledUserWorkflowResponseListWrapper.getNextPage();
    } while (nextPage != -1);
    return scheduledWorkflows;
  }

  private void tryAddUserWorkflowInQueueOfUserWorkflowExecutions(
      ScheduledWorkflow scheduledWorkflow) {
    try {
      orchestratorService.addWorkflowInQueueOfWorkflowExecutionsWithoutAuthorization(
          scheduledWorkflow.getDatasetId(), null, scheduledWorkflow.getWorkflowPriority());
    } catch (GenericMetisException e) {
      LOGGER.warn("Scheduled execution was not added to queue", e);
    }
  }
}

