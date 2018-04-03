package eu.europeana.metis.core.execution;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.OrderField;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
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

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-16
 */
public class TestFailsafeExecutor {

  private static OrchestratorService orchestratorService;
  private static RedissonClient redissonClient;
  private static final String FAILSAFE_LOCK = "failsafeLock";

  @BeforeClass
  public static void prepare() {
    orchestratorService = Mockito.mock(OrchestratorService.class);
    redissonClient = Mockito.mock(RedissonClient.class);
  }

  @After
  public void cleanUp() {
    Mockito.reset(orchestratorService);
    Mockito.reset(redissonClient);
  }

  @Test
  public void run() {
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(FAILSAFE_LOCK)).thenReturn(rlock);
    doNothing().when(rlock).lock();
    FailsafeExecutor failsafeExecutor = new FailsafeExecutor(orchestratorService, redissonClient);

    int userWorkflowExecutionsPerRequest = 5;
    int listSize = userWorkflowExecutionsPerRequest - 1; //To not trigger paging
    List<WorkflowExecution> listOfWorkflowExecutionsWithRunningStatuses = TestObjectFactory
        .createListOfWorkflowExecutions(listSize);
    TestObjectFactory.updateListOfWorkflowExecutionsWithWorkflowStatus(
        listOfWorkflowExecutionsWithRunningStatuses, WorkflowStatus.RUNNING);
    List<WorkflowExecution> listOfWorkflowExecutionsWithInqueueStatuses = TestObjectFactory
        .createListOfWorkflowExecutions(listSize); //To not trigger paging

    when(orchestratorService.getAllWorkflowExecutions(-1, null, EnumSet.of(WorkflowStatus.RUNNING), OrderField.ID, true, 0))
        .thenReturn(listOfWorkflowExecutionsWithRunningStatuses);
    when(orchestratorService.getAllWorkflowExecutions(-1, null, EnumSet.of(WorkflowStatus.INQUEUE), OrderField.ID, true, 0))
        .thenReturn(listOfWorkflowExecutionsWithInqueueStatuses);
    when(orchestratorService.getWorkflowExecutionsPerRequest())
        .thenReturn(userWorkflowExecutionsPerRequest).thenReturn(userWorkflowExecutionsPerRequest);
    doNothing().when(rlock).unlock();

    failsafeExecutor.performFailsafe();

    InOrder inOrder = Mockito.inOrder(orchestratorService);
    inOrder.verify(orchestratorService, times(1))
        .removeActiveWorkflowExecutionsFromList(any(List.class));
    inOrder.verify(orchestratorService, times(listSize * 2))
        .addWorkflowExecutionToQueue(anyString(), anyInt());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void runThatThrowsExceptionDuringLockAndContinues() {
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(FAILSAFE_LOCK)).thenReturn(rlock);
    FailsafeExecutor failsafeExecutor = new FailsafeExecutor(orchestratorService, redissonClient);
    doThrow(new RedisConnectionException("Connection error")).when(rlock).lock();
    doNothing().when(rlock).unlock();
    failsafeExecutor.performFailsafe();
    verifyNoMoreInteractions(orchestratorService);
  }

  @Test
  public void runThatThrowsExceptionDuringLockAndUnlockAndContinues() {
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(FAILSAFE_LOCK)).thenReturn(rlock);
    FailsafeExecutor failsafeExecutor = new FailsafeExecutor(orchestratorService, redissonClient);
    doThrow(new RedisConnectionException("Connection error")).when(rlock).lock();
    doThrow(new RedisConnectionException("Connection error")).when(rlock).unlock();
    failsafeExecutor.performFailsafe();
    verify(rlock, times(1)).unlock();
    verifyNoMoreInteractions(orchestratorService);
  }
}
