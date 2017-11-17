package eu.europeana.metis.core.execution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.jayway.awaitility.Awaitility;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import eu.europeana.metis.core.dao.UserWorkflowExecutionDao;
import eu.europeana.metis.core.execution.UserWorkflowExecutorManager.QueueConsumer;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-17
 */
public class TestWorkflowExecutorManager {

  private static UserWorkflowExecutionDao userWorkflowExecutionDao;
  private static RedissonClient redissonClient;
  private static Channel rabbitmqChannel;
  private static UserWorkflowExecutorManager userWorkflowExecutorManager;
  private static final String EXECUTION_CHECK_LOCK = "executionCheckLock";

  @BeforeClass
  public static void prepare() {
    userWorkflowExecutionDao = Mockito.mock(UserWorkflowExecutionDao.class);
    redissonClient = Mockito.mock(RedissonClient.class);
    rabbitmqChannel = Mockito.mock(Channel.class);
    userWorkflowExecutorManager = new UserWorkflowExecutorManager(
        userWorkflowExecutionDao, rabbitmqChannel, redissonClient);
    userWorkflowExecutorManager.setRabbitmqQueueName("ExampleQueueName");
    userWorkflowExecutorManager.setMaxConcurrentThreads(10);
    userWorkflowExecutorManager.setMonitorCheckIntervalInSecs(5);
    assertEquals(5,userWorkflowExecutorManager.getMonitorCheckIntervalInSecs());
  }

  @AfterClass
  public static void shutdown() {
    userWorkflowExecutorManager.close();
  }

  @After
  public void cleanUp() {
    Mockito.reset(userWorkflowExecutionDao);
    Mockito.reset(redissonClient);
    Mockito.reset(rabbitmqChannel);
  }

  @Test
  public void initiateConsumer() throws Exception {
    userWorkflowExecutorManager.initiateConsumer();
    ArgumentCaptor<Integer> basicQos = ArgumentCaptor.forClass(Integer.class);
    verify(rabbitmqChannel, times(1)).basicQos(basicQos.capture());
    assertEquals(new Integer(1), basicQos.getValue());
    ArgumentCaptor<Boolean> autoAcknowledge = ArgumentCaptor.forClass(Boolean.class);
    verify(rabbitmqChannel, times(1))
        .basicConsume(or(anyString(), isNull()), autoAcknowledge.capture(), any(
            QueueConsumer.class));
    assertEquals(false, autoAcknowledge.getValue());
  }

  @Test(expected = IOException.class)
  public void initiateConsumerThrowsIOException() throws Exception {
    when(rabbitmqChannel
        .basicConsume(or(anyString(), isNull()), anyBoolean(), any(QueueConsumer.class)))
        .thenThrow(new IOException("Some Error"));
    userWorkflowExecutorManager.initiateConsumer();
    ArgumentCaptor<Integer> basicQos = ArgumentCaptor.forClass(Integer.class);
    verify(rabbitmqChannel, times(1)).basicQos(basicQos.capture());
    assertEquals(new Integer(1), basicQos.getValue());
    verifyNoMoreInteractions(rabbitmqChannel);
  }

  @Test
  public void addUserWorkflowExecutionToQueue() throws Exception {
    String objectId = new ObjectId().toString();
    int priority = 0;
    userWorkflowExecutorManager.addUserWorkflowExecutionToQueue(objectId, priority);
    ArgumentCaptor<byte[]> byteArrayArgumentCaptor = ArgumentCaptor.forClass(byte[].class);
    verify(rabbitmqChannel, times(1))
        .basicPublish(anyString(), anyString(), any(AMQP.BasicProperties.class),
            byteArrayArgumentCaptor.capture());
    assertTrue(Arrays.equals(objectId.getBytes("UTF-8"), byteArrayArgumentCaptor.getValue()));
  }

  @Test
  public void addUserWorkflowExecutionToQueueThrowsIOException() throws Exception {
    String objectId = new ObjectId().toString();
    int priority = 0;
    doThrow(new IOException("Some Error")).when(rabbitmqChannel)
        .basicPublish(anyString(), anyString(), any(AMQP.BasicProperties.class), any(byte[].class));
    userWorkflowExecutorManager.addUserWorkflowExecutionToQueue(objectId, priority);
  }

  @Test
  public void cancelUserWorkflowExecutionWasINQUEUE() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    doNothing().when(userWorkflowExecutionDao).setCancellingState(workflowExecution);
    userWorkflowExecutorManager.cancelUserWorkflowExecution(workflowExecution);
  }

  @Test
  public void cancelUserWorkflowExecutionWasRUNNING() {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    workflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    doNothing().when(userWorkflowExecutionDao).setCancellingState(workflowExecution);
    userWorkflowExecutorManager.cancelUserWorkflowExecution(workflowExecution);
  }

  @Test
  public void handleDelivery() throws Exception {
    String objectId = new ObjectId().toString();
    int priority = 0;
    Envelope envelope = new Envelope(1, false, "", "");
    BasicProperties basicProperties = MessageProperties.PERSISTENT_TEXT_PLAIN.builder()
        .priority(priority).build();
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();

    when(userWorkflowExecutionDao.getById(objectId)).thenReturn(workflowExecution);
    doNothing().when(rabbitmqChannel).basicAck(envelope.getDeliveryTag(), false);

    QueueConsumer queueConsumer = userWorkflowExecutorManager.new QueueConsumer(rabbitmqChannel);
    queueConsumer.handleDelivery("1", envelope, basicProperties,
        objectId.getBytes("UTF-8"));
  }

  @Test
  public void handleDeliveryStateCancelling() throws Exception {
    String objectId = new ObjectId().toString();
    int priority = 0;
    Envelope envelope = new Envelope(1, false, "", "");
    BasicProperties basicProperties = MessageProperties.PERSISTENT_TEXT_PLAIN.builder()
        .priority(priority).build();
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    workflowExecution.setCancelling(true);

    when(userWorkflowExecutionDao.getById(objectId)).thenReturn(workflowExecution);
    doNothing().when(rabbitmqChannel).basicAck(envelope.getDeliveryTag(), false);

    QueueConsumer queueConsumer = userWorkflowExecutorManager.new QueueConsumer(rabbitmqChannel);
    queueConsumer.handleDelivery("1", envelope, basicProperties,
        objectId.getBytes("UTF-8"));

    verify(userWorkflowExecutionDao, times(1)).update(workflowExecution);
  }

  @Test
  public void handleDeliveryOverMaxConcurrentThreads() throws Exception {
    userWorkflowExecutorManager = new UserWorkflowExecutorManager(
        userWorkflowExecutionDao, rabbitmqChannel, redissonClient);
    userWorkflowExecutorManager.setRabbitmqQueueName("ExampleQueueName");
    userWorkflowExecutorManager.setMaxConcurrentThreads(2);
    userWorkflowExecutorManager.setMonitorCheckIntervalInSecs(1);

    int priority = 0;
    BasicProperties basicProperties = MessageProperties.PERSISTENT_TEXT_PLAIN.builder()
        .priority(priority).build();
    Envelope envelope = new Envelope(1, false, "", "");
    ObjectId objectId1 = new ObjectId();
    ObjectId objectId2 = new ObjectId();
    ObjectId objectId3 = new ObjectId();
    byte[] objectIdBytes1 = objectId1.toString().getBytes("UTF-8");
    byte[] objectIdBytes2 = objectId2.toString().getBytes("UTF-8");
    byte[] objectIdBytes3 = objectId3.toString().getBytes("UTF-8");
    WorkflowExecution workflowExecution1 = TestObjectFactory
        .createUserWorkflowExecutionObject();
    WorkflowExecution workflowExecution2 = TestObjectFactory
        .createUserWorkflowExecutionObject();
    WorkflowExecution workflowExecution3 = TestObjectFactory
        .createUserWorkflowExecutionObject();
    workflowExecution1.setId(objectId1);
    workflowExecution2.setId(objectId2);
    workflowExecution3.setId(objectId3);
    when(userWorkflowExecutionDao.getById(objectId1.toString())).thenReturn(workflowExecution1);
    when(userWorkflowExecutionDao.getById(objectId2.toString())).thenReturn(workflowExecution2);
    when(userWorkflowExecutionDao.getById(objectId3.toString())).thenReturn(workflowExecution3);
    doNothing().when(rabbitmqChannel).basicNack(envelope.getDeliveryTag(), false, true);

    //For running properly the WorkflowExecution.
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(EXECUTION_CHECK_LOCK)).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(userWorkflowExecutionDao.isExecutionActive(any(WorkflowExecution.class), anyInt())).thenReturn(false);
    doNothing().when(userWorkflowExecutionDao).updateMonitorInformation(any(WorkflowExecution.class));
    doNothing().when(rlock).unlock();
    when(userWorkflowExecutionDao.isCancelling(any(ObjectId.class))).thenReturn(false);
    doNothing().when(userWorkflowExecutionDao).updateWorkflowPlugins(any(WorkflowExecution.class));
    when(userWorkflowExecutionDao.update(any(WorkflowExecution.class))).thenReturn(anyString());

    QueueConsumer queueConsumer = userWorkflowExecutorManager.new QueueConsumer(rabbitmqChannel);
    queueConsumer.handleDelivery("1", envelope, basicProperties, objectIdBytes1);
    queueConsumer.handleDelivery("2", envelope, basicProperties, objectIdBytes2);
    Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() -> workflowExecution1.getWorkflowStatus() == WorkflowStatus.FINISHED);
    Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() -> workflowExecution2.getWorkflowStatus() == WorkflowStatus.FINISHED);
    queueConsumer.handleDelivery("3", envelope, basicProperties, objectIdBytes3);
    Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() -> workflowExecution3.getWorkflowStatus() == WorkflowStatus.FINISHED);
    assertEquals(1, userWorkflowExecutorManager.getThreadsCounter());
  }

  @Test
  public void handleDeliveryOverMaxConcurrentThreadsSendNack() throws Exception {
    userWorkflowExecutorManager = new UserWorkflowExecutorManager(
        userWorkflowExecutionDao, rabbitmqChannel, redissonClient);
    userWorkflowExecutorManager.setRabbitmqQueueName("ExampleQueueName");
    userWorkflowExecutorManager.setMaxConcurrentThreads(2);
    userWorkflowExecutorManager.setMonitorCheckIntervalInSecs(1);
    userWorkflowExecutorManager.setPollingTimeoutForCleaningCompletionServiceInSecs(0);

    int priority = 0;
    BasicProperties basicProperties = MessageProperties.PERSISTENT_TEXT_PLAIN.builder()
        .priority(priority).build();
    Envelope envelope = new Envelope(1, false, "", "");
    ObjectId objectId1 = new ObjectId();
    ObjectId objectId2 = new ObjectId();
    ObjectId objectId3 = new ObjectId();
    byte[] objectIdBytes1 = objectId1.toString().getBytes("UTF-8");
    byte[] objectIdBytes2 = objectId2.toString().getBytes("UTF-8");
    byte[] objectIdBytes3 = objectId3.toString().getBytes("UTF-8");
    WorkflowExecution workflowExecution1 = TestObjectFactory
        .createUserWorkflowExecutionObject();
    WorkflowExecution workflowExecution2 = TestObjectFactory
        .createUserWorkflowExecutionObject();
    WorkflowExecution workflowExecution3 = TestObjectFactory
        .createUserWorkflowExecutionObject();
    workflowExecution1.setId(objectId1);
    workflowExecution2.setId(objectId2);
    workflowExecution3.setId(objectId3);
    when(userWorkflowExecutionDao.getById(objectId1.toString())).thenReturn(workflowExecution1);
    when(userWorkflowExecutionDao.getById(objectId2.toString())).thenReturn(workflowExecution2);
    when(userWorkflowExecutionDao.getById(objectId3.toString())).thenReturn(workflowExecution3);
    doNothing().when(rabbitmqChannel).basicNack(envelope.getDeliveryTag(), false, true);

    //For running properly the WorkflowExecution.
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(EXECUTION_CHECK_LOCK)).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(userWorkflowExecutionDao.isExecutionActive(any(WorkflowExecution.class), anyInt())).thenReturn(false);
    doNothing().when(userWorkflowExecutionDao).updateMonitorInformation(any(WorkflowExecution.class));
    doNothing().when(rlock).unlock();
    when(userWorkflowExecutionDao.isCancelling(any(ObjectId.class))).thenReturn(false);
    doNothing().when(userWorkflowExecutionDao).updateWorkflowPlugins(any(WorkflowExecution.class));
    when(userWorkflowExecutionDao.update(any(WorkflowExecution.class))).thenReturn(anyString());

    QueueConsumer queueConsumer = userWorkflowExecutorManager.new QueueConsumer(rabbitmqChannel);
    queueConsumer.handleDelivery("1", envelope, basicProperties, objectIdBytes1);
    queueConsumer.handleDelivery("2", envelope, basicProperties, objectIdBytes2);
    queueConsumer.handleDelivery("3", envelope, basicProperties, objectIdBytes3);
    assertEquals(2, userWorkflowExecutorManager.getThreadsCounter());
    verify(rabbitmqChannel, times(1)).basicNack(envelope.getDeliveryTag(), false, true);
    Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() -> workflowExecution1.getWorkflowStatus() == WorkflowStatus.FINISHED);
    Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() -> workflowExecution2.getWorkflowStatus() == WorkflowStatus.FINISHED);
  }

  @Test
  public void handleDeliveryOverMaxConcurrentThreadsInterruptWillPolling() throws Exception {
    userWorkflowExecutorManager = new UserWorkflowExecutorManager(
        userWorkflowExecutionDao, rabbitmqChannel, redissonClient);
    userWorkflowExecutorManager.setRabbitmqQueueName("ExampleQueueName");
    userWorkflowExecutorManager.setMaxConcurrentThreads(2);
    userWorkflowExecutorManager.setMonitorCheckIntervalInSecs(1);

    int priority = 0;
    BasicProperties basicProperties = MessageProperties.PERSISTENT_TEXT_PLAIN.builder()
        .priority(priority).build();
    Envelope envelope = new Envelope(1, false, "", "");
    ObjectId objectId1 = new ObjectId();
    ObjectId objectId2 = new ObjectId();
    ObjectId objectId3 = new ObjectId();
    byte[] objectIdBytes1 = objectId1.toString().getBytes("UTF-8");
    byte[] objectIdBytes2 = objectId2.toString().getBytes("UTF-8");
    byte[] objectIdBytes3 = objectId3.toString().getBytes("UTF-8");
    WorkflowExecution workflowExecution1 = TestObjectFactory
        .createUserWorkflowExecutionObject();
    WorkflowExecution workflowExecution2 = TestObjectFactory
        .createUserWorkflowExecutionObject();
    WorkflowExecution workflowExecution3 = TestObjectFactory
        .createUserWorkflowExecutionObject();
    workflowExecution1.setId(objectId1);
    workflowExecution2.setId(objectId2);
    workflowExecution3.setId(objectId3);
    when(userWorkflowExecutionDao.getById(objectId1.toString())).thenReturn(workflowExecution1);
    when(userWorkflowExecutionDao.getById(objectId2.toString())).thenReturn(workflowExecution2);
    when(userWorkflowExecutionDao.getById(objectId3.toString())).thenReturn(workflowExecution3);
    doNothing().when(rabbitmqChannel).basicNack(envelope.getDeliveryTag(), false, true);

    //For running properly the WorkflowExecution.
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(EXECUTION_CHECK_LOCK)).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(userWorkflowExecutionDao.isExecutionActive(any(WorkflowExecution.class), anyInt())).thenReturn(false);
    doNothing().when(userWorkflowExecutionDao).updateMonitorInformation(any(WorkflowExecution.class));
    doNothing().when(rlock).unlock();
    when(userWorkflowExecutionDao.isCancelling(any(ObjectId.class))).thenReturn(false);
    doNothing().when(userWorkflowExecutionDao).updateWorkflowPlugins(any(WorkflowExecution.class));
    when(userWorkflowExecutionDao.update(any(WorkflowExecution.class))).thenReturn(anyString());

    QueueConsumer queueConsumer = userWorkflowExecutorManager.new QueueConsumer(rabbitmqChannel);
    queueConsumer.handleDelivery("1", envelope, basicProperties, objectIdBytes1);
    queueConsumer.handleDelivery("2", envelope, basicProperties, objectIdBytes2);

    Thread t = new Thread(() -> {
      try {
        queueConsumer.handleDelivery("3", envelope, basicProperties, objectIdBytes3);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    t.start();
    t.interrupt();
    t.join();
    Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() -> workflowExecution1.getWorkflowStatus() == WorkflowStatus.FINISHED);
    Awaitility.await().atMost(30, TimeUnit.SECONDS).until(() -> workflowExecution2.getWorkflowStatus() == WorkflowStatus.FINISHED);
    assertEquals(2, userWorkflowExecutorManager.getThreadsCounter());
  }

}
