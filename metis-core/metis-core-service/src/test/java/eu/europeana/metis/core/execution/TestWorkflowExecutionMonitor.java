package eu.europeana.metis.core.execution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisConnectionException;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.execution.WorkflowExecutionMonitor.WorkflowExecutionEntry;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-16
 */
public class TestWorkflowExecutionMonitor {

  private static final String FAILSAFE_LOCK = "failsafeLock";

  private static WorkflowExecutionDao workflowExecutionDao;
  private static WorkflowExecutorManager workflowExecutorManager;
  private static RedissonClient redissonClient;
  private static RLock lock;

  @BeforeClass
  public static void prepare() {
    workflowExecutionDao = Mockito.mock(WorkflowExecutionDao.class);
    workflowExecutorManager = Mockito.mock(WorkflowExecutorManager.class);
    redissonClient = Mockito.mock(RedissonClient.class);
    lock = mock(RLock.class);
  }

  @Before
  public void setUp() {
    when(redissonClient.getFairLock(FAILSAFE_LOCK)).thenReturn(lock);
    doNothing().when(lock).lock();
    doNothing().when(lock).unlock();
  }

  @After
  public void cleanUp() {
    Mockito.reset(workflowExecutorManager);
    Mockito.reset(redissonClient);
    Mockito.reset(workflowExecutionDao);
    Mockito.reset(lock);
  }

  @Test
  public void testFailSafe() {

    // Create workflow executions
    final long updatedTime = 0;
    final Instant id1 = Instant.now();
    final Instant id2 = id1.minusSeconds(1);
    final Instant id3 = id2.minusSeconds(1);
    final Instant id4 = id3.minusSeconds(1);
    final WorkflowExecution workflowExecution1 =
        createWorkflowExecution(id1, new Date(updatedTime));
    final WorkflowExecution workflowExecution2 =
        createWorkflowExecution(id2, new Date(updatedTime));
    final WorkflowExecution workflowExecution3 =
        createWorkflowExecution(id3, new Date(updatedTime));
    final WorkflowExecution workflowExecution4 =
        createWorkflowExecution(id4, new Date(updatedTime));

    // Create monitor
    final Duration leniency = Duration.ofSeconds(10);
    final WorkflowExecutionMonitor monitor =
        Mockito.spy(new WorkflowExecutionMonitor(workflowExecutorManager, workflowExecutionDao,
            redissonClient, leniency));

    // Mock the get entry: 1 is normal, 2 is hanging and 3 and 4 are null.
    WorkflowExecutionEntry entry1 = mock(WorkflowExecutionEntry.class);
    doReturn(false).when(entry1).assumeHanging(leniency);
    WorkflowExecutionEntry entry2 = mock(WorkflowExecutionEntry.class);
    doReturn(true).when(entry2).assumeHanging(leniency);
    doReturn(entry1).when(monitor).getEntry(workflowExecution1);
    doReturn(entry2).when(monitor).getEntry(workflowExecution2);
    doReturn(null).when(monitor).getEntry(workflowExecution3);
    doReturn(null).when(monitor).getEntry(workflowExecution4);

    // Mock the retrieval of executions: 1, 2 and 3 are running, 4 is in queue.
    doReturn(Arrays.asList(workflowExecution1, workflowExecution2, workflowExecution3))
        .when(monitor).updateCurrentRunningExecutions();
    when(workflowExecutionDao.getAllWorkflowExecutions(isNull(),
        eq(EnumSet.of(WorkflowStatus.INQUEUE)), any(), anyBoolean(), eq(0)))
            .thenReturn(Arrays.asList(workflowExecution4));
    when(workflowExecutionDao.getWorkflowExecutionsPerRequest()).thenReturn(4);

    // Perform method and verify the requeued executions
    monitor.performFailsafe();
    verify(workflowExecutorManager, times(1)).addWorkflowExecutionToQueue(
        eq(workflowExecution2.getId().toString()), eq(workflowExecution2.getWorkflowPriority()));
    verify(workflowExecutorManager, times(1)).addWorkflowExecutionToQueue(
        eq(workflowExecution3.getId().toString()), eq(workflowExecution3.getWorkflowPriority()));
    verify(workflowExecutorManager, times(1)).addWorkflowExecutionToQueue(
        eq(workflowExecution4.getId().toString()), eq(workflowExecution4.getWorkflowPriority()));
    verifyNoMoreInteractions(workflowExecutorManager);

    // Verify calls that need to be locked.
    InOrder inOrder = Mockito.inOrder(lock, workflowExecutorManager, workflowExecutionDao, monitor);
    inOrder.verify(monitor).performFailsafe();
    inOrder.verify(lock).lock();
    inOrder.verify(monitor, times(1)).updateCurrentRunningExecutions();
    inOrder.verify(workflowExecutionDao, times(1)).getAllWorkflowExecutions(isNull(),
        eq(EnumSet.of(WorkflowStatus.INQUEUE)), any(), anyBoolean(), eq(0));
    inOrder.verify(lock).unlock();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testFailSafeThatThrowsExceptionDuringLockAndContinues() {
    WorkflowExecutionMonitor monitor = new WorkflowExecutionMonitor(workflowExecutorManager,
        workflowExecutionDao, redissonClient, Duration.ofHours(1));
    doThrow(new RedisConnectionException("Connection error")).when(lock).lock();
    monitor.performFailsafe();
    verifyNoMoreInteractions(workflowExecutorManager);
  }

  @Test
  public void testFailSafeThatThrowsExceptionDuringLockAndUnlockAndContinues() {
    WorkflowExecutionMonitor monitor = new WorkflowExecutionMonitor(workflowExecutorManager,
        workflowExecutionDao, redissonClient, Duration.ofHours(1));
    doThrow(new RedisConnectionException("Connection error")).when(lock).lock();
    doThrow(new RedisConnectionException("Connection error")).when(lock).unlock();
    monitor.performFailsafe();
    verify(lock, times(1)).unlock();
    verifyNoMoreInteractions(workflowExecutorManager);
  }

  @Test
  public void testClaimExecution() {

    // Create monitor.
    WorkflowExecutionMonitor monitor =
        Mockito.spy(new WorkflowExecutionMonitor(workflowExecutorManager, workflowExecutionDao,
            redissonClient, Duration.ofHours(1)));

    // Create workflow execution
    final WorkflowExecution workflowExecution = createWorkflowExecution(Instant.now(), null);
    final String id = workflowExecution.getId().toString();
    doReturn(workflowExecution).when(workflowExecutionDao).getById(id);
    workflowExecution.setWorkflowStatus(WorkflowStatus.INQUEUE);

    // Test when claim succeeds on execution in queue.
    // Note: Don't use Instant.now(): date must be created in the same way to have same precision.
    doReturn(true).when(monitor).mayClaimExecution(workflowExecution);
    final Instant begin1 = new Date().toInstant();
    assertSame(workflowExecution, monitor.claimExecution(id));
    final Instant end1 = new Date().toInstant();

    InOrder inOrder = Mockito.inOrder(lock, redissonClient, workflowExecutorManager,
        workflowExecutionDao, monitor);
    inOrder.verify(monitor, times(1)).claimExecution(any());
    inOrder.verify(lock, times(1)).lock();
    inOrder.verify(workflowExecutionDao, times(1)).getById(id);
    inOrder.verify(monitor, times(1)).mayClaimExecution(workflowExecution);
    inOrder.verify(workflowExecutionDao, times(1)).updateMonitorInformation(workflowExecution);
    inOrder.verify(lock, times(1)).unlock();
    inOrder.verifyNoMoreInteractions();

    // Check values in workflow execution after claim.
    assertNotNull(workflowExecution.getStartedDate());
    assertNotNull(workflowExecution.getUpdatedDate());
    assertFalse(workflowExecution.getStartedDate().toInstant().isAfter(end1));
    assertFalse(workflowExecution.getUpdatedDate().toInstant().isAfter(end1));
    assertFalse(workflowExecution.getStartedDate().toInstant().isBefore(begin1));
    assertFalse(workflowExecution.getUpdatedDate().toInstant().isBefore(begin1));
    assertEquals(WorkflowStatus.RUNNING, workflowExecution.getWorkflowStatus());

    // Test when execution was already running.
    // Note: Don't use Instant.now(): date must be created in the same way to have same precision.
    final Date oldStartedDate = workflowExecution.getStartedDate();
    final Instant begin2 = new Date().toInstant();
    assertSame(workflowExecution, monitor.claimExecution(id));
    final Instant end2 = new Date().toInstant();
    assertEquals(oldStartedDate, workflowExecution.getStartedDate());
    assertNotNull(workflowExecution.getUpdatedDate());
    assertFalse(workflowExecution.getUpdatedDate().toInstant().isAfter(end2));
    assertFalse(workflowExecution.getUpdatedDate().toInstant().isBefore(begin2));
    assertEquals(WorkflowStatus.RUNNING, workflowExecution.getWorkflowStatus());

    // Test when claim fails.
    doReturn(false).when(monitor).mayClaimExecution(workflowExecution);
    assertNull(monitor.claimExecution(id));
  }

  @Test
  public void testClaimExecutionWithException() {
    final String id = "id";
    WorkflowExecutionMonitor monitor =
        Mockito.spy(new WorkflowExecutionMonitor(workflowExecutorManager, workflowExecutionDao,
            redissonClient, Duration.ofHours(1)));
    doThrow(new RuntimeException("Test exception")).when(workflowExecutionDao).getById(id);
    assertNull(monitor.claimExecution(id));
    verify(lock, times(1)).unlock();
  }

  @Test
  public void testEntry() {

    // Create new entry.
    final long updateTime = 0;
    final Instant begin = Instant.now();
    final WorkflowExecutionEntry entry = spy(new WorkflowExecutionEntry(new Date(updateTime)));
    final Instant end = Instant.now();

    // Test value of last value change.
    assertFalse(entry.getLastValueChange().isAfter(end));
    assertFalse(entry.getLastValueChange().isBefore(begin));

    // Test comparison of update time
    assertTrue(entry.updateTimeValueIsEqual(new Date(updateTime)));
    assertFalse(entry.updateTimeValueIsEqual(new Date(updateTime + 1)));
    assertFalse(entry.updateTimeValueIsEqual(null));

    // Test decision on whether it is hanging
    final Duration leniency = Duration.ofSeconds(1);
    final Instant now = Instant.now();
    doReturn(now).when(entry).getLastValueChange();

    doReturn(now).when(entry).getNow();
    assertFalse(entry.assumeHanging(leniency));

    doReturn(now.minus(leniency)).when(entry).getNow();
    assertFalse(entry.assumeHanging(leniency));

    doReturn(now.plus(leniency)).when(entry).getNow();
    assertFalse(entry.assumeHanging(leniency));

    doReturn(now.plus(leniency).plus(leniency)).when(entry).getNow();
    assertTrue(entry.assumeHanging(leniency));

    // Test with a null update time.
    final WorkflowExecutionEntry entryWithoutUpdateTime = new WorkflowExecutionEntry(null);
    assertFalse(entryWithoutUpdateTime.updateTimeValueIsEqual(new Date()));
    assertTrue(entryWithoutUpdateTime.updateTimeValueIsEqual(null));
  }

  @Test
  public void testMayClaimExecution() {

    // Create monitor
    final Duration leniency = Duration.ofSeconds(10);
    final WorkflowExecutionMonitor monitor =
        spy(new WorkflowExecutionMonitor(workflowExecutorManager, workflowExecutionDao,
            redissonClient, leniency));

    // Create workflow execution
    final WorkflowExecution workflowExecution = createWorkflowExecution(Instant.now(), null);

    // Checks of non-RUNNING executions
    workflowExecution.setWorkflowStatus(WorkflowStatus.CANCELLED);
    assertFalse(monitor.mayClaimExecution(workflowExecution));
    workflowExecution.setWorkflowStatus(WorkflowStatus.FAILED);
    assertFalse(monitor.mayClaimExecution(workflowExecution));
    workflowExecution.setWorkflowStatus(WorkflowStatus.FINISHED);
    assertFalse(monitor.mayClaimExecution(workflowExecution));
    workflowExecution.setWorkflowStatus(WorkflowStatus.INQUEUE);
    assertTrue(monitor.mayClaimExecution(workflowExecution));

    // Now check for running executions.
    workflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);

    // Check when entry is not there.
    doReturn(null).when(monitor).getEntry(workflowExecution);
    assertFalse(monitor.mayClaimExecution(workflowExecution));

    // Check when entry has changed
    final Date updateTime = new Date(0);
    workflowExecution.setUpdatedDate(updateTime);
    final WorkflowExecutionEntry entry = spy(new WorkflowExecutionEntry(null));
    doReturn(entry).when(monitor).getEntry(workflowExecution);
    doReturn(false).when(entry).updateTimeValueIsEqual(updateTime);
    assertFalse(monitor.mayClaimExecution(workflowExecution));

    // Check when entry is hanging
    doReturn(true).when(entry).updateTimeValueIsEqual(updateTime);
    doReturn(false).when(entry).assumeHanging(leniency);
    assertFalse(monitor.mayClaimExecution(workflowExecution));

    // Check when all is well
    doReturn(true).when(entry).assumeHanging(leniency);
    assertTrue(monitor.mayClaimExecution(workflowExecution));
  }

  @Test
  public void testUpdateCurrentExecutions() {

    // Create workflow executions
    final long updatedTime = 0;
    final Instant id1 = Instant.now();
    final Instant id2 = id1.minusSeconds(1);
    final Instant id3 = id2.minusSeconds(1);
    final Instant id4 = id3.minusSeconds(1);
    final WorkflowExecution workflowExecution1 =
        createWorkflowExecution(id1, new Date(updatedTime));
    final WorkflowExecution workflowExecution2 =
        createWorkflowExecution(id2, new Date(updatedTime));
    final WorkflowExecution workflowExecution3 =
        createWorkflowExecution(id3, new Date(updatedTime));
    final WorkflowExecution workflowExecution4 =
        createWorkflowExecution(id4, new Date(updatedTime));

    // Create monitor
    final WorkflowExecutionMonitor monitor =
        spy(new WorkflowExecutionMonitor(workflowExecutorManager, workflowExecutionDao,
            redissonClient, Duration.ofSeconds(10)));

    // Set base for current executions: include workflows 1, 2 and 3. Verify.
    doReturn(Arrays.asList(workflowExecution1, workflowExecution2, workflowExecution3))
        .when(monitor).getWorkflowExecutionsWithStatus(WorkflowStatus.RUNNING);
    monitor.updateCurrentRunningExecutions();

    // Check that all data was processed correctly.
    final WorkflowExecutionEntry executionRecord1 = monitor.getEntry(workflowExecution1);
    assertNotNull(executionRecord1);
    assertTrue(executionRecord1.updateTimeValueIsEqual(new Date(updatedTime)));
    assertNotNull(monitor.getEntry(workflowExecution2));
    assertTrue(monitor.getEntry(workflowExecution2).updateTimeValueIsEqual(new Date(updatedTime)));
    assertNotNull(monitor.getEntry(workflowExecution3));
    assertTrue(monitor.getEntry(workflowExecution3).updateTimeValueIsEqual(new Date(updatedTime)));
    assertNull(monitor.getEntry(workflowExecution4));

    // Change the current executions: include workflows 1 (updated), 2 (unchanged) and 4.
    final long newUpdatedTime = updatedTime + 1;
    workflowExecution1.setUpdatedDate(new Date(newUpdatedTime));
    doReturn(Arrays.asList(workflowExecution1, workflowExecution2, workflowExecution4))
        .when(monitor).getWorkflowExecutionsWithStatus(WorkflowStatus.RUNNING);
    monitor.updateCurrentRunningExecutions();

    // Check that all data was processed correctly.
    assertNotNull(monitor.getEntry(workflowExecution1));
    assertTrue(
        monitor.getEntry(workflowExecution1).updateTimeValueIsEqual(new Date(newUpdatedTime)));
    assertNotNull(monitor.getEntry(workflowExecution2));
    assertTrue(monitor.getEntry(workflowExecution2).updateTimeValueIsEqual(new Date(updatedTime)));
    assertNull(monitor.getEntry(workflowExecution3));
    assertNotNull(monitor.getEntry(workflowExecution4));
    assertTrue(monitor.getEntry(workflowExecution4).updateTimeValueIsEqual(new Date(updatedTime)));
  }

  private static WorkflowExecution createWorkflowExecution(Instant id, Date updatedDate) {
    WorkflowExecution workflowExecution = TestObjectFactory.createWorkflowExecutionObject();
    workflowExecution.setId(new ObjectId(Date.from(id)));
    workflowExecution.setUpdatedDate(updatedDate);
    return workflowExecution;
  }
}
