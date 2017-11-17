package eu.europeana.metis.core.execution;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.test.utils.TestUtils;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import java.util.Date;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-17
 */
public class TestWorkflowExecutor {
  private static int monitorCheckIntervalInSecs = 0;
  private static WorkflowExecutionDao workflowExecutionDao;
  private static RedissonClient redissonClient;
  private static final String EXECUTION_CHECK_LOCK = "executionCheckLock";

  @BeforeClass
  public static void prepare() {
    workflowExecutionDao = Mockito.mock(WorkflowExecutionDao.class);
    redissonClient = Mockito.mock(RedissonClient.class);
  }

  @After
  public void cleanUp() {
    Mockito.reset(workflowExecutionDao);
    Mockito.reset(redissonClient);
  }

  @Test
  public void call()
  {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();

    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(EXECUTION_CHECK_LOCK)).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(workflowExecutionDao.isExecutionActive(workflowExecution, monitorCheckIntervalInSecs)).thenReturn(false);
    doNothing().when(workflowExecutionDao).updateMonitorInformation(workflowExecution);
    doNothing().when(rlock).unlock();
    when(workflowExecutionDao.isCancelling(workflowExecution.getId())).thenReturn(false);
    doNothing().when(workflowExecutionDao).updateWorkflowPlugins(workflowExecution);
    when(workflowExecutionDao.update(workflowExecution)).thenReturn(anyString());

    WorkflowExecutor workflowExecutor = new WorkflowExecutor(workflowExecution,
        workflowExecutionDao, monitorCheckIntervalInSecs, redissonClient);
    workflowExecutor.call();

    Assert.assertEquals(WorkflowStatus.FINISHED, workflowExecution.getWorkflowStatus());
    Assert.assertNotNull(workflowExecution.getStartedDate());
    Assert.assertNotNull(workflowExecution.getUpdatedDate());
    Assert.assertNotNull(workflowExecution.getFinishedDate());
  }

  @Test
  public void callExecutionInRUNNINGState()
  {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    workflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    workflowExecution.setStartedDate(new Date());
    AbstractMetisPlugin metisPlugin = workflowExecution.getMetisPlugins().get(0);
    metisPlugin.setPluginStatus(PluginStatus.FINISHED);

    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(EXECUTION_CHECK_LOCK)).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(workflowExecutionDao.isExecutionActive(workflowExecution, monitorCheckIntervalInSecs)).thenReturn(false);
    doNothing().when(workflowExecutionDao).updateMonitorInformation(workflowExecution);
    doNothing().when(rlock).unlock();
    when(workflowExecutionDao.isCancelling(workflowExecution.getId())).thenReturn(false);
    doNothing().when(workflowExecutionDao).updateWorkflowPlugins(workflowExecution);
    when(workflowExecutionDao.update(workflowExecution)).thenReturn(anyString());

    WorkflowExecutor workflowExecutor = new WorkflowExecutor(workflowExecution,
        workflowExecutionDao, monitorCheckIntervalInSecs, redissonClient);
    workflowExecutor.call();

    Assert.assertEquals(WorkflowStatus.FINISHED, workflowExecution.getWorkflowStatus());
    Assert.assertNotNull(workflowExecution.getStartedDate());
    Assert.assertNotNull(workflowExecution.getUpdatedDate());
    Assert.assertNotNull(workflowExecution.getFinishedDate());
    Assert.assertNull(workflowExecution.getMetisPlugins().get(0).getFinishedDate());
  }

  @Test
  public void callExecutionInFINISHEDState()
  {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    workflowExecution.setWorkflowStatus(WorkflowStatus.FINISHED);

    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(EXECUTION_CHECK_LOCK)).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(workflowExecutionDao.isExecutionActive(workflowExecution, monitorCheckIntervalInSecs)).thenReturn(false);
    doNothing().when(rlock).lock();

    WorkflowExecutor workflowExecutor = new WorkflowExecutor(workflowExecution,
        workflowExecutionDao, monitorCheckIntervalInSecs, redissonClient);
    workflowExecutor.call();

    verifyNoMoreInteractions(workflowExecutionDao);
    verify(redissonClient, times(1)).getFairLock(anyString());
    verifyNoMoreInteractions(redissonClient);
  }

  @Test
  public void callCancellingStateINQUEUE()
  {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();

    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(EXECUTION_CHECK_LOCK)).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(workflowExecutionDao.isExecutionActive(workflowExecution, monitorCheckIntervalInSecs)).thenReturn(false);
    doNothing().when(rlock).lock();
    when(workflowExecutionDao.isCancelling(workflowExecution.getId())).thenReturn(false).thenReturn(true);

    WorkflowExecutor workflowExecutor = new WorkflowExecutor(workflowExecution,
        workflowExecutionDao, monitorCheckIntervalInSecs, redissonClient);
    workflowExecutor.call();

    Assert.assertEquals(WorkflowStatus.CANCELLED, workflowExecution.getWorkflowStatus());
  }

  @Test
  public void callCancellingStateRUNNING()
  {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    workflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    workflowExecution.setStartedDate(new Date());

    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(EXECUTION_CHECK_LOCK)).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(workflowExecutionDao.isExecutionActive(workflowExecution, monitorCheckIntervalInSecs)).thenReturn(false);
    doNothing().when(rlock).lock();
    when(workflowExecutionDao.isCancelling(workflowExecution.getId())).thenReturn(false).thenReturn(true);

    WorkflowExecutor workflowExecutor = new WorkflowExecutor(workflowExecution,
        workflowExecutionDao, monitorCheckIntervalInSecs, redissonClient);
    workflowExecutor.call();

    Assert.assertEquals(WorkflowStatus.CANCELLED, workflowExecution.getWorkflowStatus());
  }


  @Test
  public void callInterrupted() throws InterruptedException {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();

    int monitorCheckIntervalInSecs = 1;
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(EXECUTION_CHECK_LOCK)).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(workflowExecutionDao.isExecutionActive(workflowExecution, monitorCheckIntervalInSecs)).thenReturn(false);
    doNothing().when(workflowExecutionDao).updateMonitorInformation(workflowExecution);
    doNothing().when(rlock).unlock();
    when(workflowExecutionDao.isCancelling(workflowExecution.getId())).thenReturn(false);
    doNothing().when(workflowExecutionDao).updateWorkflowPlugins(workflowExecution);
    when(workflowExecutionDao.update(workflowExecution)).thenReturn(new ObjectId().toString());

    WorkflowExecutor workflowExecutor = new WorkflowExecutor(workflowExecution,
        workflowExecutionDao, monitorCheckIntervalInSecs, redissonClient);

    Thread t = new Thread(workflowExecutor::call);
    t.start();
    Awaitility.await().atMost(Duration.FIVE_SECONDS).until(() -> TestUtils.untilThreadIsSleeping(t));
    t.interrupt();
    t.join();

    Assert.assertEquals(WorkflowStatus.RUNNING, workflowExecution.getWorkflowStatus());
    Assert.assertNotNull(workflowExecution.getStartedDate());
    Assert.assertNull(workflowExecution.getUpdatedDate());
    Assert.assertNull(workflowExecution.getFinishedDate());
  }
}
