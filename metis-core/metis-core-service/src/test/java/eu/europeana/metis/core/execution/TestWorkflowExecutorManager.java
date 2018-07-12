package eu.europeana.metis.core.execution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.io.IOException;
import java.util.Arrays;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.redisson.api.RedissonClient;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-17
 */
public class TestWorkflowExecutorManager {

  private static WorkflowExecutionDao workflowExecutionDao;
  private static RedissonClient redissonClient;
  private static Channel rabbitmqPublisherChannel;
  private static Channel rabbitmqConsumerChannel;
  private static WorkflowExecutorManager workflowExecutorManager;
  private static DpsClient dpsClient;

  @BeforeClass
  public static void prepare() {
    workflowExecutionDao = Mockito.mock(WorkflowExecutionDao.class);
    redissonClient = Mockito.mock(RedissonClient.class);
    rabbitmqPublisherChannel = Mockito.mock(Channel.class);
    rabbitmqConsumerChannel = Mockito.mock(Channel.class);
    dpsClient = Mockito.mock(DpsClient.class);
    workflowExecutorManager =
        new WorkflowExecutorManager(workflowExecutionDao, rabbitmqPublisherChannel,
            rabbitmqConsumerChannel, redissonClient, dpsClient);
    workflowExecutorManager.setRabbitmqQueueName("ExampleQueueName");
    workflowExecutorManager.setMaxConcurrentThreads(10);
    workflowExecutorManager.setDpsMonitorCheckIntervalInSecs(5);
    workflowExecutorManager.setEcloudBaseUrl("http://universe.space");
    workflowExecutorManager.setEcloudProvider("providerExample");
    assertEquals(5, workflowExecutorManager.getDpsMonitorCheckIntervalInSecs());
  }

  @After
  public void cleanUp() {
    Mockito.reset(workflowExecutionDao);
    Mockito.reset(redissonClient);
    Mockito.reset(rabbitmqPublisherChannel);
    Mockito.reset(rabbitmqConsumerChannel);
  }

  @Test
  public void addUserWorkflowExecutionToQueue() throws Exception {
    String objectId = new ObjectId().toString();
    int priority = 0;
    workflowExecutorManager.addWorkflowExecutionToQueue(objectId, priority);
    ArgumentCaptor<byte[]> byteArrayArgumentCaptor = ArgumentCaptor.forClass(byte[].class);
    verify(rabbitmqPublisherChannel, times(1))
        .basicPublish(anyString(), anyString(), any(AMQP.BasicProperties.class),
            byteArrayArgumentCaptor.capture());
    assertTrue(Arrays.equals(objectId.getBytes("UTF-8"), byteArrayArgumentCaptor.getValue()));
  }

  @Test
  public void addUserWorkflowExecutionToQueueThrowsIOException() throws Exception {
    String objectId = new ObjectId().toString();
    int priority = 0;
    doThrow(new IOException("Some Error")).when(rabbitmqPublisherChannel)
        .basicPublish(anyString(), anyString(), any(AMQP.BasicProperties.class), any(byte[].class));
    workflowExecutorManager.addWorkflowExecutionToQueue(objectId, priority);
  }
}
