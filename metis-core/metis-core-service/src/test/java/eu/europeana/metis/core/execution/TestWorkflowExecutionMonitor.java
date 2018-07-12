package eu.europeana.metis.core.execution;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import java.time.Duration;
import java.util.EnumSet;
import java.util.List;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisConnectionException;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-16
 */
public class TestWorkflowExecutionMonitor {

  private static WorkflowExecutionDao workflowExecutionDao;
  private static WorkflowExecutorManager workflowExecutorManager;
  private static RedissonClient redissonClient;
  private static final String FAILSAFE_LOCK = "failsafeLock";

  @BeforeClass
  public static void prepare() {
    workflowExecutionDao = Mockito.mock(WorkflowExecutionDao.class);
    workflowExecutorManager = Mockito.mock(WorkflowExecutorManager.class);
    redissonClient = Mockito.mock(RedissonClient.class);
  }

  @After
  public void cleanUp() {
    Mockito.reset(workflowExecutorManager);
    Mockito.reset(redissonClient);
    Mockito.reset(workflowExecutionDao);
  }

  @Test
  public void run() {
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(FAILSAFE_LOCK)).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(rlock.isHeldByCurrentThread()).thenReturn(true);
    
    WorkflowExecutionMonitor failsafeExecutor =
        Mockito.spy(new WorkflowExecutionMonitor(workflowExecutorManager, workflowExecutionDao,
            redissonClient, Duration.ofHours(1)));
    doReturn(null).when(failsafeExecutor).getEntry(any());
    
    int userWorkflowExecutionsPerRequest = 5;
    int listSize = userWorkflowExecutionsPerRequest - 1; //To not trigger paging
    List<WorkflowExecution> listOfWorkflowExecutionsWithRunningStatuses = TestObjectFactory
        .createListOfWorkflowExecutions(listSize);
    TestObjectFactory.updateListOfWorkflowExecutionsWithWorkflowStatus(
        listOfWorkflowExecutionsWithRunningStatuses, WorkflowStatus.RUNNING);
    List<WorkflowExecution> listOfWorkflowExecutionsWithInqueueStatuses = TestObjectFactory
        .createListOfWorkflowExecutions(listSize);

    when(workflowExecutionDao.getAllWorkflowExecutions(isNull(), EnumSet.of(WorkflowStatus.RUNNING),
        any(), anyBoolean(), 0)).thenReturn(listOfWorkflowExecutionsWithRunningStatuses);
    when(workflowExecutionDao.getAllWorkflowExecutions(isNull(), EnumSet.of(WorkflowStatus.INQUEUE),
        any(), anyBoolean(), 0)).thenReturn(listOfWorkflowExecutionsWithInqueueStatuses);
    when(workflowExecutionDao.getWorkflowExecutionsPerRequest())
        .thenReturn(userWorkflowExecutionsPerRequest).thenReturn(userWorkflowExecutionsPerRequest);
    doNothing().when(rlock).unlock();

    failsafeExecutor.performFailsafe();

    InOrder inOrder = Mockito.inOrder(workflowExecutorManager, workflowExecutionDao);
    inOrder.verify(workflowExecutionDao, times(1)).getAllWorkflowExecutions(isNull(),
        eq(EnumSet.of(WorkflowStatus.RUNNING)), any(), anyBoolean(), eq(0));
    inOrder.verify(workflowExecutionDao, times(1)).getAllWorkflowExecutions(isNull(),
        eq(EnumSet.of(WorkflowStatus.INQUEUE)), any(), anyBoolean(), eq(0));
    inOrder.verify(workflowExecutorManager, times(listSize * 2))
        .addWorkflowExecutionToQueue(anyString(), anyInt());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void runThatThrowsExceptionDuringLockAndContinues() {
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(FAILSAFE_LOCK)).thenReturn(rlock);
    WorkflowExecutionMonitor failsafeExecutor = new WorkflowExecutionMonitor(workflowExecutorManager,
        workflowExecutionDao, redissonClient, Duration.ofHours(1));
    doThrow(new RedisConnectionException("Connection error")).when(rlock).lock();
    doNothing().when(rlock).unlock();
    failsafeExecutor.performFailsafe();
    verifyNoMoreInteractions(workflowExecutorManager);
  }

  @Test
  public void runThatThrowsExceptionDuringLockAndUnlockAndContinues() {
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(FAILSAFE_LOCK)).thenReturn(rlock);
    WorkflowExecutionMonitor failsafeExecutor = new WorkflowExecutionMonitor(workflowExecutorManager,
        workflowExecutionDao, redissonClient, Duration.ofHours(1));
    doThrow(new RedisConnectionException("Connection error")).when(rlock).lock();
    doThrow(new RedisConnectionException("Connection error")).when(rlock).unlock();
    failsafeExecutor.performFailsafe();
    verify(rlock, times(1)).unlock();
    verifyNoMoreInteractions(workflowExecutorManager);
  }
}
