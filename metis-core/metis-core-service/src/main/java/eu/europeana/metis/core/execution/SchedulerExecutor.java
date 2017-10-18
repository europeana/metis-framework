package eu.europeana.metis.core.execution;

import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoUserWorkflowFoundException;
import eu.europeana.metis.core.exceptions.UserWorkflowExecutionAlreadyExistsException;
import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledUserWorkflow;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.PreDestroy;
import org.apache.solr.common.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
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
      LocalDateTime dateAfterSleep = null;
      try {
        LOGGER.info("Scheduler thread sleeping for {} seconds.", periodicSchedulerCheckInSecs);
        Thread.sleep(periodicSchedulerCheckInSecs * 1000L);

        lock.lock();
        dateAfterSleep = LocalDateTime.now();
        LOGGER.info("Scheduler thread woke up. Date range checking lowerbound: {}, upperBound:{}",
            dateBeforeSleep, dateAfterSleep);
        List<ScheduledUserWorkflow> allCleanedScheduledUserWorkflows = getCleanedScheduledUserWorkflows(
            dateBeforeSleep, dateAfterSleep);

        for (ScheduledUserWorkflow scheduledUserWorkflow :
            allCleanedScheduledUserWorkflows) {
          LOGGER.info(
              "Adding ScheduledUserWorkflow with DatasetName: {}, workflowOwner: {}, workflowName: {}, pointerDate: {}, frequence: {}",
              scheduledUserWorkflow.getDatasetName(), scheduledUserWorkflow.getWorkflowOwner(),
              scheduledUserWorkflow.getWorkflowName(), scheduledUserWorkflow.getPointerDate(),
              scheduledUserWorkflow.getScheduleFrequence());

          tryAddUserWorkflowInQueueOfUserWorkflowExecutions(scheduledUserWorkflow);
        }
      } catch (InterruptedException | RuntimeException e) {
        LOGGER.warn(
            "Thread was interruped or exception thrown from rabbitmq channel disconnection, scheduler thread continues",
            e);
      } finally {
        lock.unlock();
      }
      dateBeforeSleep = dateAfterSleep;
    } while (infiniteLoop);
  }

  private List<ScheduledUserWorkflow> getCleanedScheduledUserWorkflows(LocalDateTime lowerBound,
      LocalDateTime upperBound) {
    List<ScheduledUserWorkflow> scheduledUserWorkflows = new ArrayList<>();
    scheduledUserWorkflows.addAll(getScheduledUserWorkflowsFrequenceOnce(lowerBound, upperBound));
    scheduledUserWorkflows.addAll(getScheduledUserWorkflowsFrequenceDaily(lowerBound, upperBound));
    scheduledUserWorkflows.addAll(getScheduledUserWorkflowsFrequenceWeekly(lowerBound, upperBound));
    scheduledUserWorkflows
        .addAll(getScheduledUserWorkflowsFrequenceMonthly(lowerBound, upperBound));
    return scheduledUserWorkflows;
  }

  private List<ScheduledUserWorkflow> getScheduledUserWorkflowsFrequenceOnce(
      LocalDateTime lowerBound,
      LocalDateTime upperBound) {
    String nextPage = null;
    List<ScheduledUserWorkflow> scheduledUserWorkflows = new ArrayList<>();
    ResponseListWrapper<ScheduledUserWorkflow> scheduledUserWorkflowResponseListWrapper;
    do {
      scheduledUserWorkflowResponseListWrapper = new ResponseListWrapper<>();
      scheduledUserWorkflowResponseListWrapper
          .setResultsAndLastPage(orchestratorService
                  .getAllScheduledUserWorkflowsByDateRangeONCE(lowerBound, upperBound, nextPage),
              orchestratorService.getScheduledUserWorkflowsPerRequest());
      scheduledUserWorkflows
          .addAll(scheduledUserWorkflowResponseListWrapper.getResults());
      nextPage = scheduledUserWorkflowResponseListWrapper.getNextPage();
    } while (!StringUtils.isEmpty(nextPage));
    return scheduledUserWorkflows;
  }

  private List<ScheduledUserWorkflow> getScheduledUserWorkflowsFrequenceDaily(
      LocalDateTime lowerBound,
      LocalDateTime upperBound) {
    List<ScheduledUserWorkflow> scheduledUserWorkflows = getScheduledUserWorkflows(
        ScheduleFrequence.DAILY);
    for (Iterator<ScheduledUserWorkflow> iterator = scheduledUserWorkflows.iterator();
        iterator.hasNext(); ) {
      ScheduledUserWorkflow scheduledUserWorkflow = iterator.next();

      LocalDateTime pointerDate = LocalDateTime
          .ofInstant(scheduledUserWorkflow.getPointerDate().toInstant(), ZoneId.systemDefault());
      LocalDateTime localDateToCheck = lowerBound.withYear(lowerBound.getYear())
          .withMonth(lowerBound.getMonthValue()).withHour(pointerDate.getHour())
          .withMinute(pointerDate.getMinute()).withSecond(pointerDate.getSecond())
          .withNano(pointerDate.getNano());

      if (localDateToCheck.isBefore(lowerBound) || localDateToCheck.isEqual(upperBound)
          || localDateToCheck.isAfter(upperBound)) {
        iterator.remove();
      }
    }
    return scheduledUserWorkflows;
  }

  private List<ScheduledUserWorkflow> getScheduledUserWorkflowsFrequenceWeekly(
      LocalDateTime lowerBound,
      LocalDateTime upperBound) {
    List<ScheduledUserWorkflow> scheduledUserWorkflows = getScheduledUserWorkflows(
        ScheduleFrequence.WEEKLY);
    for (Iterator<ScheduledUserWorkflow> iterator = scheduledUserWorkflows.iterator();
        iterator.hasNext(); ) {
      ScheduledUserWorkflow scheduledUserWorkflow = iterator.next();
      LocalDateTime pointerDate = LocalDateTime
          .ofInstant(scheduledUserWorkflow.getPointerDate().toInstant(), ZoneId.systemDefault());
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
    return scheduledUserWorkflows;
  }

  private List<ScheduledUserWorkflow> getScheduledUserWorkflowsFrequenceMonthly(
      LocalDateTime lowerBound,
      LocalDateTime upperBound) {
    List<ScheduledUserWorkflow> scheduledUserWorkflows = getScheduledUserWorkflows(
        ScheduleFrequence.MONTHLY);
    for (Iterator<ScheduledUserWorkflow> iterator = scheduledUserWorkflows.iterator();
        iterator.hasNext(); ) {
      ScheduledUserWorkflow scheduledUserWorkflow = iterator.next();

      LocalDateTime pointerDate = LocalDateTime
          .ofInstant(scheduledUserWorkflow.getPointerDate().toInstant(), ZoneId.systemDefault());
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
    return scheduledUserWorkflows;
  }

  private List<ScheduledUserWorkflow> getScheduledUserWorkflows(
      ScheduleFrequence scheduleFrequence) {
    String nextPage = null;
    List<ScheduledUserWorkflow> scheduledUserWorkflows = new ArrayList<>();
    ResponseListWrapper<ScheduledUserWorkflow> scheduledUserWorkflowResponseListWrapper;
    do {
      scheduledUserWorkflowResponseListWrapper = new ResponseListWrapper<>();
      scheduledUserWorkflowResponseListWrapper
          .setResultsAndLastPage(orchestratorService
                  .getAllScheduledUserWorkflows(scheduleFrequence, nextPage),
              orchestratorService.getScheduledUserWorkflowsPerRequest());
      scheduledUserWorkflows
          .addAll(scheduledUserWorkflowResponseListWrapper.getResults());
      nextPage = scheduledUserWorkflowResponseListWrapper.getNextPage();
    } while (!StringUtils.isEmpty(nextPage));
    return scheduledUserWorkflows;
  }

  private void tryAddUserWorkflowInQueueOfUserWorkflowExecutions(
      ScheduledUserWorkflow scheduledUserWorkflow) {
    try {
      orchestratorService.addUserWorkflowInQueueOfUserWorkflowExecutions(
          scheduledUserWorkflow.getDatasetName(), scheduledUserWorkflow.getWorkflowOwner(),
          scheduledUserWorkflow.getWorkflowName(),
          scheduledUserWorkflow.getWorkflowPriority());
    } catch (NoDatasetFoundException | NoUserWorkflowFoundException | UserWorkflowExecutionAlreadyExistsException e) {
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

