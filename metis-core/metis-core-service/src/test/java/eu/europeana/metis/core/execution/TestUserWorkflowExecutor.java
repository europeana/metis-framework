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
import eu.europeana.metis.core.dao.UserWorkflowExecutionDao;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
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
public class TestUserWorkflowExecutor {
  private static int monitorCheckIntervalInSecs = 0;
  private static UserWorkflowExecutionDao userWorkflowExecutionDao;
  private static RedissonClient redissonClient;
  private static final String EXECUTION_CHECK_LOCK = "executionCheckLock";

  @BeforeClass
  public static void prepare() {
    userWorkflowExecutionDao = Mockito.mock(UserWorkflowExecutionDao.class);
    redissonClient = Mockito.mock(RedissonClient.class);
  }

  @After
  public void cleanUp() {
    Mockito.reset(userWorkflowExecutionDao);
    Mockito.reset(redissonClient);
  }

  @Test
  public void call()
  {
    UserWorkflowExecution userWorkflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();

    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(EXECUTION_CHECK_LOCK)).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(userWorkflowExecutionDao.isExecutionActive(userWorkflowExecution, monitorCheckIntervalInSecs)).thenReturn(false);
    doNothing().when(userWorkflowExecutionDao).updateMonitorInformation(userWorkflowExecution);
    doNothing().when(rlock).unlock();
    when(userWorkflowExecutionDao.isCancelling(userWorkflowExecution.getId())).thenReturn(false);
    doNothing().when(userWorkflowExecutionDao).updateWorkflowPlugins(userWorkflowExecution);
    when(userWorkflowExecutionDao.update(userWorkflowExecution)).thenReturn(anyString());

    UserWorkflowExecutor userWorkflowExecutor = new UserWorkflowExecutor(userWorkflowExecution,
        userWorkflowExecutionDao, monitorCheckIntervalInSecs, redissonClient);
    userWorkflowExecutor.call();

    Assert.assertEquals(WorkflowStatus.FINISHED, userWorkflowExecution.getWorkflowStatus());
    Assert.assertNotNull(userWorkflowExecution.getStartedDate());
    Assert.assertNotNull(userWorkflowExecution.getUpdatedDate());
    Assert.assertNotNull(userWorkflowExecution.getFinishedDate());
  }

  @Test
  public void callExecutionInRUNNINGState()
  {
    UserWorkflowExecution userWorkflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    userWorkflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    userWorkflowExecution.setStartedDate(new Date());
    AbstractMetisPlugin metisPlugin = userWorkflowExecution.getMetisPlugins().get(0);
    metisPlugin.setPluginStatus(PluginStatus.FINISHED);

    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(EXECUTION_CHECK_LOCK)).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(userWorkflowExecutionDao.isExecutionActive(userWorkflowExecution, monitorCheckIntervalInSecs)).thenReturn(false);
    doNothing().when(userWorkflowExecutionDao).updateMonitorInformation(userWorkflowExecution);
    doNothing().when(rlock).unlock();
    when(userWorkflowExecutionDao.isCancelling(userWorkflowExecution.getId())).thenReturn(false);
    doNothing().when(userWorkflowExecutionDao).updateWorkflowPlugins(userWorkflowExecution);
    when(userWorkflowExecutionDao.update(userWorkflowExecution)).thenReturn(anyString());

    UserWorkflowExecutor userWorkflowExecutor = new UserWorkflowExecutor(userWorkflowExecution,
        userWorkflowExecutionDao, monitorCheckIntervalInSecs, redissonClient);
    userWorkflowExecutor.call();

    Assert.assertEquals(WorkflowStatus.FINISHED, userWorkflowExecution.getWorkflowStatus());
    Assert.assertNotNull(userWorkflowExecution.getStartedDate());
    Assert.assertNotNull(userWorkflowExecution.getUpdatedDate());
    Assert.assertNotNull(userWorkflowExecution.getFinishedDate());
    Assert.assertNull(userWorkflowExecution.getMetisPlugins().get(0).getFinishedDate());
  }

  @Test
  public void callExecutionInFINISHEDState()
  {
    UserWorkflowExecution userWorkflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    userWorkflowExecution.setWorkflowStatus(WorkflowStatus.FINISHED);

    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(EXECUTION_CHECK_LOCK)).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(userWorkflowExecutionDao.isExecutionActive(userWorkflowExecution, monitorCheckIntervalInSecs)).thenReturn(false);
    doNothing().when(rlock).lock();

    UserWorkflowExecutor userWorkflowExecutor = new UserWorkflowExecutor(userWorkflowExecution,
        userWorkflowExecutionDao, monitorCheckIntervalInSecs, redissonClient);
    userWorkflowExecutor.call();

    verifyNoMoreInteractions(userWorkflowExecutionDao);
    verify(redissonClient, times(1)).getFairLock(anyString());
    verifyNoMoreInteractions(redissonClient);
  }

  @Test
  public void callCancellingStateINQUEUE()
  {
    UserWorkflowExecution userWorkflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();

    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(EXECUTION_CHECK_LOCK)).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(userWorkflowExecutionDao.isExecutionActive(userWorkflowExecution, monitorCheckIntervalInSecs)).thenReturn(false);
    doNothing().when(rlock).lock();
    when(userWorkflowExecutionDao.isCancelling(userWorkflowExecution.getId())).thenReturn(false).thenReturn(true);

    UserWorkflowExecutor userWorkflowExecutor = new UserWorkflowExecutor(userWorkflowExecution,
        userWorkflowExecutionDao, monitorCheckIntervalInSecs, redissonClient);
    userWorkflowExecutor.call();

    Assert.assertEquals(WorkflowStatus.CANCELLED, userWorkflowExecution.getWorkflowStatus());
  }

  @Test
  public void callCancellingStateRUNNING()
  {
    UserWorkflowExecution userWorkflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    userWorkflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    userWorkflowExecution.setStartedDate(new Date());

    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(EXECUTION_CHECK_LOCK)).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(userWorkflowExecutionDao.isExecutionActive(userWorkflowExecution, monitorCheckIntervalInSecs)).thenReturn(false);
    doNothing().when(rlock).lock();
    when(userWorkflowExecutionDao.isCancelling(userWorkflowExecution.getId())).thenReturn(false).thenReturn(true);

    UserWorkflowExecutor userWorkflowExecutor = new UserWorkflowExecutor(userWorkflowExecution,
        userWorkflowExecutionDao, monitorCheckIntervalInSecs, redissonClient);
    userWorkflowExecutor.call();

    Assert.assertEquals(WorkflowStatus.CANCELLED, userWorkflowExecution.getWorkflowStatus());
  }


  @Test
  public void callInterrupted() throws InterruptedException {
    UserWorkflowExecution userWorkflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();

    int monitorCheckIntervalInSecs = 1;
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(EXECUTION_CHECK_LOCK)).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(userWorkflowExecutionDao.isExecutionActive(userWorkflowExecution, monitorCheckIntervalInSecs)).thenReturn(false);
    doNothing().when(userWorkflowExecutionDao).updateMonitorInformation(userWorkflowExecution);
    doNothing().when(rlock).unlock();
    when(userWorkflowExecutionDao.isCancelling(userWorkflowExecution.getId())).thenReturn(false);
    doNothing().when(userWorkflowExecutionDao).updateWorkflowPlugins(userWorkflowExecution);
    when(userWorkflowExecutionDao.update(userWorkflowExecution)).thenReturn(new ObjectId().toString());

    UserWorkflowExecutor userWorkflowExecutor = new UserWorkflowExecutor(userWorkflowExecution,
        userWorkflowExecutionDao, monitorCheckIntervalInSecs, redissonClient);

    Thread t = new Thread(userWorkflowExecutor::call);
    t.start();
    Awaitility.await().atMost(Duration.FIVE_SECONDS).until(() -> untilThreadIsSleeping(t));
    t.interrupt();
    t.join();

    Assert.assertEquals(WorkflowStatus.RUNNING, userWorkflowExecution.getWorkflowStatus());
    Assert.assertNotNull(userWorkflowExecution.getStartedDate());
    Assert.assertNull(userWorkflowExecution.getUpdatedDate());
    Assert.assertNull(userWorkflowExecution.getFinishedDate());
  }

  private void untilThreadIsSleeping(Thread t) {
    Assert.assertEquals("java.lang.Thread", t.getStackTrace()[0].getClassName());
    Assert.assertEquals("sleep", t.getStackTrace()[0].getMethodName());
  }

}
