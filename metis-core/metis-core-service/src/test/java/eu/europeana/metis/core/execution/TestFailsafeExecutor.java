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

import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.test.utils.TestUtils;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
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

  private static int periodicFailsafeCheckInSecs = 1;
  private static OrchestratorService orchestratorService;
  private static RedissonClient redissonClient;
  private static FailsafeExecutor failsafeExecutor;
  private static final String FAILSAFE_LOCK = "failsafeLock";

  @BeforeClass
  public static void prepare() {
    orchestratorService = Mockito.mock(OrchestratorService.class);
    redissonClient = Mockito.mock(RedissonClient.class);
    failsafeExecutor = new FailsafeExecutor(orchestratorService, redissonClient,
        periodicFailsafeCheckInSecs, false);
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

    int userWorkflowExecutionsPerRequest = 5;
    int listSize = userWorkflowExecutionsPerRequest - 1; //To not trigger paging
    List<UserWorkflowExecution> listOfUserWorkflowExecutionsWithRunningStatus = TestObjectFactory
        .createListOfUserWorkflowExecutions(listSize);
    TestObjectFactory.updateListOfUserWorkflowExecutionsWithWorkflowStatus(
        listOfUserWorkflowExecutionsWithRunningStatus, WorkflowStatus.RUNNING);
    List<UserWorkflowExecution> listOfUserWorkflowExecutionsWithInqueueStatus = TestObjectFactory
        .createListOfUserWorkflowExecutions(listSize); //To not trigger paging

    when(orchestratorService.getAllUserWorkflowExecutions(WorkflowStatus.RUNNING, null))
        .thenReturn(listOfUserWorkflowExecutionsWithRunningStatus);
    when(orchestratorService.getAllUserWorkflowExecutions(WorkflowStatus.INQUEUE, null))
        .thenReturn(listOfUserWorkflowExecutionsWithInqueueStatus);
    when(orchestratorService.getUserWorkflowExecutionsPerRequest())
        .thenReturn(userWorkflowExecutionsPerRequest).thenReturn(userWorkflowExecutionsPerRequest);
    doNothing().when(rlock).unlock();

    failsafeExecutor.run();

    InOrder inOrder = Mockito.inOrder(orchestratorService);
    inOrder.verify(orchestratorService, times(1))
        .removeActiveUserWorkflowExecutionsFromList(any(List.class));
    inOrder.verify(orchestratorService, times(listSize * 2))
        .addUserWorkflowExecutionToQueue(anyString(), anyInt());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void runThatThrowsExceptionAndContinues() throws Exception {
    FailsafeExecutor failsafeExecutor = new FailsafeExecutor(orchestratorService, redissonClient,
        10, false);
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(FAILSAFE_LOCK)).thenReturn(rlock);
    doNothing().when(rlock).lock();
    doNothing().when(rlock).unlock();

    Thread t = new Thread(failsafeExecutor);
    t.start();
    Awaitility.await().atMost(Duration.TWO_SECONDS).until(() -> TestUtils.untilThreadIsSleeping(t));
    t.interrupt();
    t.join();
    verifyNoMoreInteractions(orchestratorService);
  }

  @Test
  public void runThatThrowsExceptionDuringLockAndUnlockAndContinues() {
    FailsafeExecutor failsafeExecutor = new FailsafeExecutor(orchestratorService, redissonClient,
        10, false);
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(FAILSAFE_LOCK)).thenReturn(rlock);
    doThrow(new RedisConnectionException("Connection error")).when(rlock).lock();
    doThrow(new RedisConnectionException("Connection error")).when(rlock).unlock();
    failsafeExecutor.run();
    verifyNoMoreInteractions(orchestratorService);
  }

  @Test
  public void close() {
    when(redissonClient.isShutdown()).thenReturn(false);
    failsafeExecutor.close();
    verify(redissonClient, times(1)).shutdown();
  }
}
