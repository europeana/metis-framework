package eu.europeana.metis.core.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.jayway.awaitility.Awaitility;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.common.model.dps.TaskState;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.ExecutablePlugin.MonitorResult;
import eu.europeana.metis.core.workflow.plugins.ExecutionProgress;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPlugin;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPluginMetadata;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.redisson.api.RedissonClient;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-17
 */
class TestQueueConsumer {

  private static WorkflowExecutionDao workflowExecutionDao;
  private static RedissonClient redissonClient;
  private static Channel rabbitmqConsumerChannel;
  private static Channel rabbitmqPublisherChannel;
  private static WorkflowExecutionMonitor workflowExecutionMonitor;
  private static WorkflowExecutorManager workflowExecutorManager;

  @BeforeAll
  static void prepare() {
    workflowExecutionDao = Mockito.mock(WorkflowExecutionDao.class);
    workflowExecutionMonitor = Mockito.mock(WorkflowExecutionMonitor.class);
    redissonClient = Mockito.mock(RedissonClient.class);
    rabbitmqPublisherChannel = Mockito.mock(Channel.class);
    rabbitmqConsumerChannel = Mockito.mock(Channel.class);
    DpsClient dpsClient = Mockito.mock(DpsClient.class);
    workflowExecutorManager =
        new WorkflowExecutorManager(workflowExecutionDao, rabbitmqPublisherChannel,
            rabbitmqConsumerChannel, redissonClient, dpsClient);
    workflowExecutorManager.setRabbitmqQueueName("ExampleQueueName");
    workflowExecutorManager.setMaxConcurrentThreads(2);
    workflowExecutorManager.setDpsMonitorCheckIntervalInSecs(1);
    workflowExecutorManager.setEcloudBaseUrl("http://universe.space");
    workflowExecutorManager.setEcloudProvider("providerExample");
  }

  @AfterEach
  void cleanUp() {
    Mockito.reset(workflowExecutionDao);
    Mockito.reset(workflowExecutionMonitor);
    Mockito.reset(redissonClient);
    Mockito.reset(rabbitmqPublisherChannel);
    Mockito.reset(rabbitmqConsumerChannel);
  }

  @Test
  void initiateConsumer() throws Exception {
    final String rabbitmqQueueName = "testname";
    new QueueConsumer(rabbitmqConsumerChannel, rabbitmqQueueName, workflowExecutorManager,
        workflowExecutorManager, workflowExecutionMonitor);
    ArgumentCaptor<Integer> basicQos = ArgumentCaptor.forClass(Integer.class);
    verify(rabbitmqConsumerChannel, times(1)).basicQos(basicQos.capture());
    assertEquals(new Integer(1), basicQos.getValue());
    ArgumentCaptor<Boolean> autoAcknowledge = ArgumentCaptor.forClass(Boolean.class);
    verify(rabbitmqConsumerChannel, times(1)).basicConsume(eq(rabbitmqQueueName),
        autoAcknowledge.capture(), any(QueueConsumer.class));
    assertFalse(autoAcknowledge.getValue());
  }

  @Test
  void initiateConsumerThrowsIOException() throws Exception {
    final String rabbitmqQueueName = "testname";
    when(rabbitmqConsumerChannel.basicConsume(eq(rabbitmqQueueName), anyBoolean(),
        any(QueueConsumer.class))).thenThrow(new IOException("Some Error"));
    assertThrows(IOException.class,
        () -> new QueueConsumer(rabbitmqConsumerChannel, rabbitmqQueueName, workflowExecutorManager,
            workflowExecutorManager, workflowExecutionMonitor));
    ArgumentCaptor<Integer> basicQos = ArgumentCaptor.forClass(Integer.class);
    verify(rabbitmqConsumerChannel, times(1)).basicQos(basicQos.capture());
    verify(rabbitmqConsumerChannel, times(1)).basicConsume(eq(rabbitmqQueueName), eq(false), any());
    assertEquals(new Integer(1), basicQos.getValue());
    verifyNoMoreInteractions(rabbitmqConsumerChannel);
  }

  @Test
  void handleDelivery() throws Exception {
    String objectId = new ObjectId().toString();
    int priority = 0;
    Envelope envelope = new Envelope(1, false, "", "");
    BasicProperties basicProperties = MessageProperties.PERSISTENT_TEXT_PLAIN.builder()
        .priority(priority).build();
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();

    when(workflowExecutionDao.getById(objectId)).thenReturn(workflowExecution);
    doNothing().when(rabbitmqConsumerChannel).basicAck(envelope.getDeliveryTag(), false);

    QueueConsumer queueConsumer = new QueueConsumer(rabbitmqConsumerChannel, null,
        workflowExecutorManager, workflowExecutorManager, workflowExecutionMonitor);
    queueConsumer.handleDelivery("1", envelope, basicProperties,
        objectId.getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void handleDeliveryStateCancelling() throws Exception {
    String objectId = new ObjectId().toString();
    int priority = 0;
    Envelope envelope = new Envelope(1, false, "", "");
    BasicProperties basicProperties = MessageProperties.PERSISTENT_TEXT_PLAIN.builder()
        .priority(priority).build();
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecution.setCancelling(true);

    when(workflowExecutionDao.getById(objectId)).thenReturn(workflowExecution);
    doNothing().when(rabbitmqConsumerChannel).basicAck(envelope.getDeliveryTag(), false);

    QueueConsumer queueConsumer = new QueueConsumer(rabbitmqConsumerChannel, null,
        workflowExecutorManager, workflowExecutorManager, workflowExecutionMonitor);
    queueConsumer.handleDelivery("1", envelope, basicProperties,
        objectId.getBytes(StandardCharsets.UTF_8));

    verify(workflowExecutionDao, times(1)).update(workflowExecution);
  }

  @Test
  void handleDeliveryOverMaxConcurrentThreads() throws Exception {
    ExecutionProgress currentlyProcessingExecutionProgress = new ExecutionProgress();
    currentlyProcessingExecutionProgress.setStatus(TaskState.CURRENTLY_PROCESSING);
    ExecutionProgress processedExecutionProgress = new ExecutionProgress();
    processedExecutionProgress.setStatus(TaskState.PROCESSED);

    OaipmhHarvestPlugin oaipmhHarvestPlugin1 = Mockito.spy(OaipmhHarvestPlugin.class);
    OaipmhHarvestPluginMetadata oaipmhHarvestPluginMetadata1 = new OaipmhHarvestPluginMetadata();
    oaipmhHarvestPlugin1.setPluginMetadata(oaipmhHarvestPluginMetadata1);
    ArrayList<AbstractMetisPlugin> abstractMetisPlugins1 = new ArrayList<>();
    abstractMetisPlugins1.add(oaipmhHarvestPlugin1);

    OaipmhHarvestPlugin oaipmhHarvestPlugin2 = Mockito.spy(OaipmhHarvestPlugin.class);
    OaipmhHarvestPluginMetadata oaipmhHarvestPluginMetadata2 = new OaipmhHarvestPluginMetadata();
    oaipmhHarvestPlugin2.setPluginMetadata(oaipmhHarvestPluginMetadata2);
    ArrayList<AbstractMetisPlugin> abstractMetisPlugins2 = new ArrayList<>();
    abstractMetisPlugins2.add(oaipmhHarvestPlugin2);

    OaipmhHarvestPlugin oaipmhHarvestPlugin3 = Mockito.spy(OaipmhHarvestPlugin.class);
    OaipmhHarvestPluginMetadata oaipmhHarvestPluginMetadata3 = new OaipmhHarvestPluginMetadata();
    oaipmhHarvestPlugin3.setPluginMetadata(oaipmhHarvestPluginMetadata3);
    ArrayList<AbstractMetisPlugin> abstractMetisPlugins3 = new ArrayList<>();
    abstractMetisPlugins3.add(oaipmhHarvestPlugin3);

    int priority = 0;
    BasicProperties basicProperties = MessageProperties.PERSISTENT_TEXT_PLAIN.builder()
        .priority(priority).build();
    Envelope envelope = new Envelope(1, false, "", "");
    ObjectId objectId1 = new ObjectId(Date.from(Instant.now().minusSeconds(1)));
    ObjectId objectId2 = new ObjectId(Date.from(Instant.now()));
    ObjectId objectId3 = new ObjectId(Date.from(Instant.now().plusSeconds(1)));
    byte[] objectIdBytes1 = objectId1.toString().getBytes(StandardCharsets.UTF_8);
    byte[] objectIdBytes2 = objectId2.toString().getBytes(StandardCharsets.UTF_8);
    byte[] objectIdBytes3 = objectId3.toString().getBytes(StandardCharsets.UTF_8);
    WorkflowExecution workflowExecution1 = TestObjectFactory
        .createWorkflowExecutionObject();
    WorkflowExecution workflowExecution2 = TestObjectFactory
        .createWorkflowExecutionObject();
    WorkflowExecution workflowExecution3 = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecution1.setId(objectId1);
    workflowExecution1.setWorkflowStatus(WorkflowStatus.INQUEUE);
    workflowExecution1.setMetisPlugins(abstractMetisPlugins1);
    workflowExecution2.setId(objectId2);
    workflowExecution2.setWorkflowStatus(WorkflowStatus.INQUEUE);
    workflowExecution2.setMetisPlugins(abstractMetisPlugins2);
    workflowExecution3.setId(objectId3);
    workflowExecution3.setWorkflowStatus(WorkflowStatus.INQUEUE);
    workflowExecution3.setMetisPlugins(abstractMetisPlugins3);
    when(workflowExecutionDao.getById(objectId1.toString())).thenReturn(workflowExecution1);
    when(workflowExecutionDao.getById(objectId2.toString())).thenReturn(workflowExecution2);
    when(workflowExecutionDao.getById(objectId3.toString())).thenReturn(workflowExecution3);
    doNothing().when(rabbitmqConsumerChannel).basicNack(envelope.getDeliveryTag(), false, true);

    //For running properly the WorkflowExecution.
    when(workflowExecutionMonitor.claimExecution(workflowExecution1.getId().toString()))
        .thenReturn(workflowExecution1).thenReturn(null);
    when(workflowExecutionMonitor.claimExecution(workflowExecution2.getId().toString()))
        .thenReturn(workflowExecution2).thenReturn(null);
    when(workflowExecutionMonitor.claimExecution(workflowExecution3.getId().toString()))
        .thenReturn(workflowExecution3).thenReturn(null);
    doNothing().when(workflowExecutionDao).updateMonitorInformation(any(WorkflowExecution.class));
    when(workflowExecutionDao.isCancelling(any(ObjectId.class))).thenReturn(false);
    doReturn(new MonitorResult(currentlyProcessingExecutionProgress.getStatus(), null))
        .doReturn(new MonitorResult(processedExecutionProgress.getStatus(), null))
        .when(oaipmhHarvestPlugin1).monitor(any(DpsClient.class));
    doReturn(new MonitorResult(currentlyProcessingExecutionProgress.getStatus(), null))
        .doReturn(new MonitorResult(processedExecutionProgress.getStatus(), null))
        .when(oaipmhHarvestPlugin2).monitor(any(DpsClient.class));
    doReturn(new MonitorResult(currentlyProcessingExecutionProgress.getStatus(), null))
        .doReturn(new MonitorResult(processedExecutionProgress.getStatus(), null))
        .when(oaipmhHarvestPlugin3).monitor(any(DpsClient.class));
    doNothing().when(workflowExecutionDao).updateWorkflowPlugins(any(WorkflowExecution.class));
    when(workflowExecutionDao.update(any(WorkflowExecution.class))).thenReturn(anyString());

    QueueConsumer queueConsumer = new QueueConsumer(rabbitmqConsumerChannel, null,
        workflowExecutorManager, workflowExecutorManager, workflowExecutionMonitor);
    queueConsumer.handleDelivery("1", envelope, basicProperties, objectIdBytes1);
    queueConsumer.handleDelivery("2", envelope, basicProperties, objectIdBytes2);
    Awaitility.await().atMost(30, TimeUnit.SECONDS)
        .until(() -> workflowExecution1.getWorkflowStatus() == WorkflowStatus.FINISHED);
    Awaitility.await().atMost(30, TimeUnit.SECONDS)
        .until(() -> workflowExecution2.getWorkflowStatus() == WorkflowStatus.FINISHED);
    queueConsumer.handleDelivery("3", envelope, basicProperties, objectIdBytes3);
    Awaitility.await().atMost(30, TimeUnit.SECONDS)
        .until(() -> workflowExecution3.getWorkflowStatus() == WorkflowStatus.FINISHED);
    assertEquals(1, queueConsumer.getThreadsCounter());
  }

  @Test
  void handleDeliveryOverMaxConcurrentThreadsSendNack() throws Exception {
    ExecutionProgress currentlyProcessingExecutionProgress = new ExecutionProgress();
    currentlyProcessingExecutionProgress.setStatus(TaskState.CURRENTLY_PROCESSING);
    ExecutionProgress processedExecutionProgress = new ExecutionProgress();
    processedExecutionProgress.setStatus(TaskState.PROCESSED);

    OaipmhHarvestPlugin oaipmhHarvestPlugin1 = Mockito.spy(OaipmhHarvestPlugin.class);
    OaipmhHarvestPluginMetadata oaipmhHarvestPluginMetadata1 = new OaipmhHarvestPluginMetadata();
    oaipmhHarvestPlugin1.setPluginMetadata(oaipmhHarvestPluginMetadata1);
    ArrayList<AbstractMetisPlugin> abstractMetisPlugins1 = new ArrayList<>();
    abstractMetisPlugins1.add(oaipmhHarvestPlugin1);

    OaipmhHarvestPlugin oaipmhHarvestPlugin2 = Mockito.spy(OaipmhHarvestPlugin.class);
    OaipmhHarvestPluginMetadata oaipmhHarvestPluginMetadata2 = new OaipmhHarvestPluginMetadata();
    oaipmhHarvestPlugin2.setPluginMetadata(oaipmhHarvestPluginMetadata2);
    ArrayList<AbstractMetisPlugin> abstractMetisPlugins2 = new ArrayList<>();
    abstractMetisPlugins2.add(oaipmhHarvestPlugin2);

    workflowExecutorManager.setPollingTimeoutForCleaningCompletionServiceInSecs(0);

    int priority = 0;
    BasicProperties basicProperties = MessageProperties.PERSISTENT_TEXT_PLAIN.builder()
        .priority(priority).build();
    Envelope envelope = new Envelope(1, false, "", "");
    ObjectId objectId1 = new ObjectId(Date.from(Instant.now().minusSeconds(1)));
    ObjectId objectId2 = new ObjectId(Date.from(Instant.now()));
    ObjectId objectId3 = new ObjectId(Date.from(Instant.now().plusSeconds(1)));
    byte[] objectIdBytes1 = objectId1.toString().getBytes(StandardCharsets.UTF_8);
    byte[] objectIdBytes2 = objectId2.toString().getBytes(StandardCharsets.UTF_8);
    byte[] objectIdBytes3 = objectId3.toString().getBytes(StandardCharsets.UTF_8);
    WorkflowExecution workflowExecution1 = TestObjectFactory
        .createWorkflowExecutionObject();
    WorkflowExecution workflowExecution2 = TestObjectFactory
        .createWorkflowExecutionObject();
    WorkflowExecution workflowExecution3 = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecution1.setId(objectId1);
    workflowExecution1.setWorkflowStatus(WorkflowStatus.INQUEUE);
    workflowExecution1.setMetisPlugins(abstractMetisPlugins1);
    workflowExecution2.setId(objectId2);
    workflowExecution2.setWorkflowStatus(WorkflowStatus.INQUEUE);
    workflowExecution2.setMetisPlugins(abstractMetisPlugins2);
    workflowExecution3.setId(objectId3);
    workflowExecution3.setWorkflowStatus(WorkflowStatus.INQUEUE);
    workflowExecution3.setMetisPlugins(abstractMetisPlugins2);
    when(workflowExecutionDao.getById(objectId1.toString())).thenReturn(workflowExecution1);
    when(workflowExecutionDao.getById(objectId2.toString())).thenReturn(workflowExecution2);
    when(workflowExecutionDao.getById(objectId3.toString())).thenReturn(workflowExecution3);
    doNothing().when(rabbitmqConsumerChannel).basicNack(envelope.getDeliveryTag(), false, true);

    //For running properly the WorkflowExecution.
    when(workflowExecutionMonitor.claimExecution(workflowExecution1.getId().toString()))
        .thenReturn(workflowExecution1).thenReturn(null);
    when(workflowExecutionMonitor.claimExecution(workflowExecution2.getId().toString()))
        .thenReturn(workflowExecution2).thenReturn(null);
    when(workflowExecutionMonitor.claimExecution(workflowExecution3.getId().toString()))
        .thenReturn(workflowExecution3).thenReturn(null);
    doNothing().when(workflowExecutionDao).updateMonitorInformation(any(WorkflowExecution.class));
    when(workflowExecutionDao.isCancelling(any(ObjectId.class))).thenReturn(false);
    doReturn(new MonitorResult(currentlyProcessingExecutionProgress.getStatus(), null))
        .doReturn(new MonitorResult(processedExecutionProgress.getStatus(), null))
        .when(oaipmhHarvestPlugin1).monitor(any(DpsClient.class));
    doReturn(new MonitorResult(currentlyProcessingExecutionProgress.getStatus(), null))
        .doReturn(new MonitorResult(processedExecutionProgress.getStatus(), null))
        .when(oaipmhHarvestPlugin2).monitor(any(DpsClient.class));
    doNothing().when(workflowExecutionDao).updateWorkflowPlugins(any(WorkflowExecution.class));
    when(workflowExecutionDao.update(any(WorkflowExecution.class))).thenReturn(anyString());

    QueueConsumer queueConsumer = new QueueConsumer(rabbitmqConsumerChannel, null,
        workflowExecutorManager, workflowExecutorManager, workflowExecutionMonitor);
    queueConsumer.handleDelivery("1", envelope, basicProperties, objectIdBytes1);
    queueConsumer.handleDelivery("2", envelope, basicProperties, objectIdBytes2);
    assertEquals(2, queueConsumer.getThreadsCounter());
    queueConsumer.handleDelivery("3", envelope, basicProperties, objectIdBytes3);
//    verify(rabbitmqChannel, times(1)).basicNack(envelope.getDeliveryTag(), false, true);
    Awaitility.await().atMost(30, TimeUnit.SECONDS)
        .until(() -> workflowExecution1.getWorkflowStatus() == WorkflowStatus.FINISHED);
    Awaitility.await().atMost(30, TimeUnit.SECONDS)
        .until(() -> workflowExecution2.getWorkflowStatus() == WorkflowStatus.FINISHED);
  }

  @Test
  void handleDeliveryOverMaxConcurrentThreadsInterruptWillPolling() throws Exception {

    ExecutionProgress currentlyProcessingExecutionProgress = new ExecutionProgress();
    currentlyProcessingExecutionProgress.setStatus(TaskState.CURRENTLY_PROCESSING);
    ExecutionProgress processedExecutionProgress = new ExecutionProgress();
    processedExecutionProgress.setStatus(TaskState.PROCESSED);

    OaipmhHarvestPlugin oaipmhHarvestPlugin1 = Mockito.spy(OaipmhHarvestPlugin.class);
    OaipmhHarvestPluginMetadata oaipmhHarvestPluginMetadata1 = new OaipmhHarvestPluginMetadata();
    oaipmhHarvestPlugin1.setPluginMetadata(oaipmhHarvestPluginMetadata1);
    ArrayList<AbstractMetisPlugin> abstractMetisPlugins1 = new ArrayList<>();
    abstractMetisPlugins1.add(oaipmhHarvestPlugin1);

    OaipmhHarvestPlugin oaipmhHarvestPlugin2 = Mockito.spy(OaipmhHarvestPlugin.class);
    OaipmhHarvestPluginMetadata oaipmhHarvestPluginMetadata2 = new OaipmhHarvestPluginMetadata();
    oaipmhHarvestPlugin2.setPluginMetadata(oaipmhHarvestPluginMetadata2);
    ArrayList<AbstractMetisPlugin> abstractMetisPlugins2 = new ArrayList<>();
    abstractMetisPlugins2.add(oaipmhHarvestPlugin2);

    int priority = 0;
    BasicProperties basicProperties = MessageProperties.PERSISTENT_TEXT_PLAIN.builder()
        .priority(priority).build();
    Envelope envelope = new Envelope(1, false, "", "");
    ObjectId objectId1 = new ObjectId(Date.from(Instant.now().minusSeconds(1)));
    ObjectId objectId2 = new ObjectId(Date.from(Instant.now()));
    ObjectId objectId3 = new ObjectId(Date.from(Instant.now().plusSeconds(1)));
    byte[] objectIdBytes1 = objectId1.toString().getBytes(StandardCharsets.UTF_8);
    byte[] objectIdBytes2 = objectId2.toString().getBytes(StandardCharsets.UTF_8);
    byte[] objectIdBytes3 = objectId3.toString().getBytes(StandardCharsets.UTF_8);
    WorkflowExecution workflowExecution1 = TestObjectFactory
        .createWorkflowExecutionObject();
    WorkflowExecution workflowExecution2 = TestObjectFactory
        .createWorkflowExecutionObject();
    WorkflowExecution workflowExecution3 = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecution1.setId(objectId1);
    workflowExecution1.setWorkflowStatus(WorkflowStatus.INQUEUE);
    workflowExecution1.setMetisPlugins(abstractMetisPlugins1);
    workflowExecution2.setId(objectId2);
    workflowExecution2.setWorkflowStatus(WorkflowStatus.INQUEUE);
    workflowExecution2.setMetisPlugins(abstractMetisPlugins2);
    workflowExecution3.setId(objectId3);
    workflowExecution3.setWorkflowStatus(WorkflowStatus.INQUEUE);
    workflowExecution3.setMetisPlugins(abstractMetisPlugins2);
    when(workflowExecutionDao.getById(objectId1.toString())).thenReturn(workflowExecution1);
    when(workflowExecutionDao.getById(objectId2.toString())).thenReturn(workflowExecution2);
    when(workflowExecutionDao.getById(objectId3.toString())).thenReturn(workflowExecution3);
    doNothing().when(rabbitmqConsumerChannel).basicNack(envelope.getDeliveryTag(), false, true);

    //For running properly the WorkflowExecution.
    when(workflowExecutionMonitor.claimExecution(workflowExecution1.getId().toString()))
        .thenReturn(workflowExecution1).thenReturn(null);
    when(workflowExecutionMonitor.claimExecution(workflowExecution2.getId().toString()))
        .thenReturn(workflowExecution2).thenReturn(null);
    when(workflowExecutionMonitor.claimExecution(workflowExecution3.getId().toString()))
        .thenReturn(workflowExecution3).thenReturn(null);
    doNothing().when(workflowExecutionDao).updateMonitorInformation(any(WorkflowExecution.class));
    when(workflowExecutionDao.isCancelling(any(ObjectId.class))).thenReturn(false);
    doReturn(new MonitorResult(currentlyProcessingExecutionProgress.getStatus(), null))
        .doReturn(new MonitorResult(processedExecutionProgress.getStatus(), null))
        .when(oaipmhHarvestPlugin1).monitor(any(DpsClient.class));
    doReturn(new MonitorResult(currentlyProcessingExecutionProgress.getStatus(), null))
        .doReturn(new MonitorResult(processedExecutionProgress.getStatus(), null))
        .when(oaipmhHarvestPlugin2).monitor(any(DpsClient.class));
    doNothing().when(workflowExecutionDao).updateWorkflowPlugins(any(WorkflowExecution.class));
    when(workflowExecutionDao.update(any(WorkflowExecution.class))).thenReturn(anyString());

    QueueConsumer queueConsumer = new QueueConsumer(rabbitmqConsumerChannel, null,
        workflowExecutorManager, workflowExecutorManager, workflowExecutionMonitor);
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
    Awaitility.await().atMost(30, TimeUnit.SECONDS)
        .until(() -> workflowExecution1.getWorkflowStatus() == WorkflowStatus.FINISHED);
    Awaitility.await().atMost(30, TimeUnit.SECONDS)
        .until(() -> workflowExecution2.getWorkflowStatus() == WorkflowStatus.FINISHED);
    assertEquals(2, queueConsumer.getThreadsCounter());
  }
}
