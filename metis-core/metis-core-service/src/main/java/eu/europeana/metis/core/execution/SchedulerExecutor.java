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

  private final int periodicSchedulerCheckInSecs = 80;
  private final OrchestratorService orchestratorService;
  private final RedissonClient redissonClient;

  public SchedulerExecutor(OrchestratorService orchestratorService, RedissonClient redissonClient) {
    this.orchestratorService = orchestratorService;
    this.redissonClient = redissonClient;
  }

  @Override
  public void run() {
    final String failsafeLock = "schedulerLock";
    RLock lock = redissonClient.getFairLock(failsafeLock);
    while (true) {
      try {
        LocalDateTime dateBeforeSleep = LocalDateTime.now();
        LOGGER.info("Scheduler thread sleeping for {} seconds.", periodicSchedulerCheckInSecs);
        Thread.sleep(periodicSchedulerCheckInSecs * 1000);

        lock.lock();
        LocalDateTime dateAfterSleep = LocalDateTime.now();
        LOGGER.info("Scheduler thread woke up.");
        List<ScheduledUserWorkflow> allCleanedScheduledUserWorkflows = getCleanedScheduledUserWorkflows(
            dateBeforeSleep, dateAfterSleep);

        for (ScheduledUserWorkflow scheduledUserWorkflow :
            allCleanedScheduledUserWorkflows) {
          LOGGER.info(
              "Adding ScheduledUserWorkflow with DatasetName: {}, workflowOwner: {}, workflowName: {}, pointerDate: {}, frequence: {}",
              scheduledUserWorkflow.getDatasetName(), scheduledUserWorkflow.getWorkflowOwner(),
              scheduledUserWorkflow.getWorkflowName(), scheduledUserWorkflow.getPointerDate(),
              scheduledUserWorkflow.getScheduleFrequence());

          try {
            orchestratorService.addUserWorkflowInQueueOfUserWorkflowExecutions(
                scheduledUserWorkflow.getDatasetName(), scheduledUserWorkflow.getWorkflowOwner(),
                scheduledUserWorkflow.getWorkflowName(),
                scheduledUserWorkflow.getWorkflowPriority());
          } catch (NoDatasetFoundException | NoUserWorkflowFoundException | UserWorkflowExecutionAlreadyExistsException e) {
            LOGGER.warn("Scheduled execution was not added to queue", e);
          }
        }
        lock.unlock();
      } catch (Exception e) {
        LOGGER.warn(
            "Thread was interruped or exception thrown from rabbitmq channel disconnection, scheduler thread continues",
            e);
      }
    }
  }

  private List<ScheduledUserWorkflow> getCleanedScheduledUserWorkflows(LocalDateTime lowBound,
      LocalDateTime highBound) {
    List<ScheduledUserWorkflow> scheduledUserWorkflows = new ArrayList<>();
    scheduledUserWorkflows.addAll(getScheduledUserWorkflowsFrequenceOnce(lowBound, highBound));
    scheduledUserWorkflows.addAll(getScheduledUserWorkflowsFrequenceDaily(lowBound, highBound));
    scheduledUserWorkflows.addAll(getScheduledUserWorkflowsFrequenceWeekly(lowBound, highBound));
    scheduledUserWorkflows.addAll(getScheduledUserWorkflowsFrequenceMonthly(lowBound, highBound));
    return scheduledUserWorkflows;
  }

  private List<ScheduledUserWorkflow> getScheduledUserWorkflowsFrequenceOnce(LocalDateTime lowBound,
      LocalDateTime highBound) {
    String nextPage = null;
    List<ScheduledUserWorkflow> scheduledUserWorkflows = new ArrayList<>();
    do {
      ResponseListWrapper<ScheduledUserWorkflow> scheduledUserWorkflowResponseListWrapper = new ResponseListWrapper<>();
      scheduledUserWorkflowResponseListWrapper
          .setResultsAndLastPage(orchestratorService
                  .getAllScheduledUserWorkflowsByDateRangeONCE(lowBound, highBound, nextPage),
              orchestratorService.getScheduledUserWorkflowsPerRequest());
      scheduledUserWorkflows
          .addAll(scheduledUserWorkflowResponseListWrapper.getResults());
      nextPage = scheduledUserWorkflowResponseListWrapper.getNextPage();
    } while (!StringUtils.isEmpty(nextPage));
    return scheduledUserWorkflows;
  }

  private List<ScheduledUserWorkflow> getScheduledUserWorkflowsFrequenceDaily(
      LocalDateTime lowBound,
      LocalDateTime highBound) {
    List<ScheduledUserWorkflow> scheduledUserWorkflows = getScheduledUserWorkflows(
        ScheduleFrequence.DAILY);
    for (Iterator<ScheduledUserWorkflow> iterator = scheduledUserWorkflows.iterator();
        iterator.hasNext(); ) {
      ScheduledUserWorkflow scheduledUserWorkflow = iterator.next();

      LocalDateTime pointerDate = LocalDateTime
          .ofInstant(scheduledUserWorkflow.getPointerDate().toInstant(), ZoneId.systemDefault());
      LocalDateTime localDateToCheck = lowBound.withYear(lowBound.getYear())
          .withMonth(lowBound.getMonthValue()).withHour(pointerDate.getHour())
          .withMinute(pointerDate.getMinute()).withSecond(pointerDate.getSecond())
          .withNano(pointerDate.getNano());

      if (localDateToCheck.isBefore(lowBound) || localDateToCheck.isEqual(highBound)
          || localDateToCheck.isAfter(highBound)) {
        iterator.remove();
      }
    }
    return scheduledUserWorkflows;
  }

  private List<ScheduledUserWorkflow> getScheduledUserWorkflowsFrequenceWeekly(
      LocalDateTime lowBound,
      LocalDateTime highBound) {
    List<ScheduledUserWorkflow> scheduledUserWorkflows = getScheduledUserWorkflows(
        ScheduleFrequence.WEEKLY);
    for (Iterator<ScheduledUserWorkflow> iterator = scheduledUserWorkflows.iterator();
        iterator.hasNext(); ) {
      ScheduledUserWorkflow scheduledUserWorkflow = iterator.next();
      LocalDateTime pointerDate = LocalDateTime
          .ofInstant(scheduledUserWorkflow.getPointerDate().toInstant(), ZoneId.systemDefault());
      LocalDateTime localDateToCheck = lowBound.withYear(lowBound.getYear())
          .withMonth(lowBound.getMonthValue()).withDayOfMonth(pointerDate.getDayOfMonth())
          .withHour(pointerDate.getHour())
          .withMinute(pointerDate.getMinute()).withSecond(pointerDate.getSecond())
          .withNano(pointerDate.getNano());

      if (lowBound.getDayOfWeek() == localDateToCheck.getDayOfWeek()) {
        localDateToCheck = localDateToCheck.withDayOfMonth(lowBound.getDayOfMonth());
      }

      if (localDateToCheck.isBefore(lowBound) || localDateToCheck.isEqual(highBound)
          || localDateToCheck.isAfter(highBound)) {
        iterator.remove();
      }
    }
    return scheduledUserWorkflows;
  }

  private List<ScheduledUserWorkflow> getScheduledUserWorkflowsFrequenceMonthly(
      LocalDateTime lowBound,
      LocalDateTime highBound) {
    List<ScheduledUserWorkflow> scheduledUserWorkflows = getScheduledUserWorkflows(
        ScheduleFrequence.MONTHLY);
    for (Iterator<ScheduledUserWorkflow> iterator = scheduledUserWorkflows.iterator();
        iterator.hasNext(); ) {
      ScheduledUserWorkflow scheduledUserWorkflow = iterator.next();

      LocalDateTime pointerDate = LocalDateTime
          .ofInstant(scheduledUserWorkflow.getPointerDate().toInstant(), ZoneId.systemDefault());
      LocalDateTime localDateToCheck = lowBound.withYear(lowBound.getYear())
          .withMonth(pointerDate.getMonthValue()).withDayOfMonth(pointerDate.getDayOfMonth())
          .withHour(pointerDate.getHour())
          .withMinute(pointerDate.getMinute()).withSecond(pointerDate.getSecond())
          .withNano(pointerDate.getNano());

      if (lowBound.getDayOfMonth() == localDateToCheck.getDayOfMonth()) {
        localDateToCheck = localDateToCheck.withMonth(lowBound.getMonthValue());
      }

      if (localDateToCheck.isBefore(lowBound) || localDateToCheck.isEqual(highBound)
          || localDateToCheck.isAfter(highBound)) {
        iterator.remove();
      }
    }
    return scheduledUserWorkflows;
  }

  private List<ScheduledUserWorkflow> getScheduledUserWorkflows(
      ScheduleFrequence scheduleFrequence) {
    String nextPage = null;
    List<ScheduledUserWorkflow> scheduledUserWorkflows = new ArrayList<>();
    do {
      ResponseListWrapper<ScheduledUserWorkflow> scheduledUserWorkflowResponseListWrapper = new ResponseListWrapper<>();
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

  @PreDestroy
  private void close() {
    if (!redissonClient.isShutdown()) {
      this.redissonClient.shutdown();
    }
  }
}

