package eu.europeana.metis.core.execution;

import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowFoundException;
import eu.europeana.metis.core.exceptions.WorkflowExecutionAlreadyExistsException;
import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledWorkflow;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
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
 * @since 2017-09-27
 */
public class SchedulerExecutor implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerExecutor.class);

  private int periodicSchedulerCheckInSecs = 90;
  private final OrchestratorService orchestratorService;
  private final RedissonClient redissonClient;
  private static final String SCHEDULER_LOCK = "schedulerLock";
  private final boolean infiniteLoop; //True for infinite loop which is the normal scenario, false for testing

  public SchedulerExecutor(OrchestratorService orchestratorService, RedissonClient redissonClient,
      int periodicSchedulerCheckInSecs, boolean infiniteLoop) {
    this.orchestratorService = orchestratorService;
    this.redissonClient = redissonClient;
    this.periodicSchedulerCheckInSecs = periodicSchedulerCheckInSecs;
    this.infiniteLoop = infiniteLoop;
  }

  @Override
  public void run() {
    RLock lock = redissonClient.getFairLock(SCHEDULER_LOCK);
    LocalDateTime dateBeforeSleep = LocalDateTime.now();
    do {
      LocalDateTime dateAfterSleep;
      try {
        LOGGER.info("Scheduler thread sleeping for {} seconds.", periodicSchedulerCheckInSecs);
        Thread.sleep(periodicSchedulerCheckInSecs * 1000L);

        lock.lock();
        dateAfterSleep = LocalDateTime.now();
        LOGGER.info("Scheduler thread woke up. Date range checking lowerbound: {}, upperBound:{}",
            dateBeforeSleep, dateAfterSleep);
        List<ScheduledWorkflow> allCleanedScheduledWorkflows = getCleanedScheduledUserWorkflows(
            dateBeforeSleep, dateAfterSleep);

        for (ScheduledWorkflow scheduledWorkflow :
            allCleanedScheduledWorkflows) {
          LOGGER.info(
              "Adding ScheduledWorkflow with DatasetName: {}, workflowOwner: {}, workflowName: {}, pointerDate: {}, frequence: {}",
              scheduledWorkflow.getDatasetName(), scheduledWorkflow.getWorkflowOwner(),
              scheduledWorkflow.getWorkflowName(), scheduledWorkflow.getPointerDate(),
              scheduledWorkflow.getScheduleFrequence());

          tryAddUserWorkflowInQueueOfUserWorkflowExecutions(scheduledWorkflow);
        }
        dateBeforeSleep = dateAfterSleep;
      } catch (InterruptedException | RuntimeException e) {
        LOGGER.warn(
            "Thread was interruped or exception thrown from rabbitmq channel or Redis disconnection, scheduler thread continues",
            e);
      } finally {
        try {
          lock.unlock();
        } catch (RedisConnectionException e) {
          LOGGER.warn("Cannot connect to unlock, scheduler thread continues");
        }
      }
    } while (infiniteLoop);
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
    String nextPage = null;
    List<ScheduledWorkflow> scheduledWorkflows = new ArrayList<>();
    ResponseListWrapper<ScheduledWorkflow> scheduledUserWorkflowResponseListWrapper;
    do {
      scheduledUserWorkflowResponseListWrapper = new ResponseListWrapper<>();
      scheduledUserWorkflowResponseListWrapper
          .setResultsAndLastPage(orchestratorService
                  .getAllScheduledUserWorkflowsByDateRangeONCE(lowerBound, upperBound, nextPage),
              orchestratorService.getScheduledUserWorkflowsPerRequest());
      scheduledWorkflows
          .addAll(scheduledUserWorkflowResponseListWrapper.getResults());
      nextPage = scheduledUserWorkflowResponseListWrapper.getNextPage();
    } while (!StringUtils.isEmpty(nextPage));
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
    String nextPage = null;
    List<ScheduledWorkflow> scheduledWorkflows = new ArrayList<>();
    ResponseListWrapper<ScheduledWorkflow> scheduledUserWorkflowResponseListWrapper;
    do {
      scheduledUserWorkflowResponseListWrapper = new ResponseListWrapper<>();
      scheduledUserWorkflowResponseListWrapper
          .setResultsAndLastPage(orchestratorService
                  .getAllScheduledUserWorkflows(scheduleFrequence, nextPage),
              orchestratorService.getScheduledUserWorkflowsPerRequest());
      scheduledWorkflows
          .addAll(scheduledUserWorkflowResponseListWrapper.getResults());
      nextPage = scheduledUserWorkflowResponseListWrapper.getNextPage();
    } while (!StringUtils.isEmpty(nextPage));
    return scheduledWorkflows;
  }

  private void tryAddUserWorkflowInQueueOfUserWorkflowExecutions(
      ScheduledWorkflow scheduledWorkflow) {
    try {
      orchestratorService.addUserWorkflowInQueueOfUserWorkflowExecutions(
          scheduledWorkflow.getDatasetName(), scheduledWorkflow.getWorkflowOwner(),
          scheduledWorkflow.getWorkflowName(),
          scheduledWorkflow.getWorkflowPriority());
    } catch (NoDatasetFoundException | NoWorkflowFoundException | WorkflowExecutionAlreadyExistsException e) {
      LOGGER.warn("Scheduled execution was not added to queue", e);
    }
  }

  @PreDestroy
  public void close() {
    if (!redissonClient.isShutdown()) {
      this.redissonClient.shutdown();
    }
  }
}

