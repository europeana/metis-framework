package eu.europeana.metis.core.execution;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
class TestWorkflowExecutorManager {

  private static WorkflowExecutionDao workflowExecutionDao;
  private static WorkflowPostProcessor workflowPostProcessor;
  private static RedissonClient redissonClient;
  private static Channel rabbitmqPublisherChannel;
  private static Channel rabbitmqConsumerChannel;
  private static WorkflowExecutorManager workflowExecutorManager;

  @BeforeAll
  static void prepare() {
    workflowExecutionDao = Mockito.mock(WorkflowExecutionDao.class);
    workflowPostProcessor = Mockito.mock(WorkflowPostProcessor.class);
    redissonClient = Mockito.mock(RedissonClient.class);
    rabbitmqPublisherChannel = Mockito.mock(Channel.class);
    rabbitmqConsumerChannel = Mockito.mock(Channel.class);
    DpsClient dpsClient = Mockito.mock(DpsClient.class);
    workflowExecutorManager = new WorkflowExecutorManager(workflowExecutionDao,
            workflowPostProcessor, rabbitmqPublisherChannel, rabbitmqConsumerChannel,
            redissonClient, dpsClient);
    workflowExecutorManager.setRabbitmqQueueName("ExampleQueueName");
    workflowExecutorManager.setMaxConcurrentThreads(10);
    workflowExecutorManager.setDpsMonitorCheckIntervalInSecs(5);
    workflowExecutorManager.setEcloudBaseUrl("http://universe.space");
    workflowExecutorManager.setEcloudProvider("providerExample");
    assertEquals(5, workflowExecutorManager.getDpsMonitorCheckIntervalInSecs());
  }

  @AfterEach
  void cleanUp() {
    Mockito.reset(workflowExecutionDao);
    Mockito.reset(workflowPostProcessor);
    Mockito.reset(redissonClient);
    Mockito.reset(rabbitmqPublisherChannel);
    Mockito.reset(rabbitmqConsumerChannel);
  }

  @Test
  void addUserWorkflowExecutionToQueue() throws Exception {
    String objectId = new ObjectId().toString();
    int priority = 0;
    workflowExecutorManager.addWorkflowExecutionToQueue(objectId, priority);
    ArgumentCaptor<byte[]> byteArrayArgumentCaptor = ArgumentCaptor.forClass(byte[].class);
    verify(rabbitmqPublisherChannel, times(1))
        .basicPublish(anyString(), anyString(), any(AMQP.BasicProperties.class),
            byteArrayArgumentCaptor.capture());
    assertArrayEquals(objectId.getBytes(StandardCharsets.UTF_8), byteArrayArgumentCaptor.getValue());
  }

  @Test
  void addUserWorkflowExecutionToQueueThrowsIOException() throws Exception {
    String objectId = new ObjectId().toString();
    int priority = 0;
    doThrow(new IOException("Some Error")).when(rabbitmqPublisherChannel)
        .basicPublish(anyString(), anyString(), any(AMQP.BasicProperties.class), any(byte[].class));
    workflowExecutorManager.addWorkflowExecutionToQueue(objectId, priority);
  }
}
