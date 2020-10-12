package eu.europeana.metis.core.execution;

import org.redisson.api.RedissonClient;
import com.rabbitmq.client.Channel;
import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;

class PersistenceProvider {

  private final Channel rabbitmqPublisherChannel;
  private final Channel rabbitmqConsumerChannel;
  private final WorkflowExecutionDao workflowExecutionDao;
  private final WorkflowPostProcessor workflowPostProcessor;
  private final RedissonClient redissonClient;
  private final DpsClient dpsClient;

  PersistenceProvider(Channel rabbitmqPublisherChannel, Channel rabbitmqConsumerChannel,
      WorkflowExecutionDao workflowExecutionDao, WorkflowPostProcessor workflowPostProcessor,
      RedissonClient redissonClient, DpsClient dpsClient) {
    this.rabbitmqPublisherChannel = rabbitmqPublisherChannel;
    this.rabbitmqConsumerChannel = rabbitmqConsumerChannel;
    this.workflowExecutionDao = workflowExecutionDao;
    this.workflowPostProcessor = workflowPostProcessor;
    this.redissonClient = redissonClient;
    this.dpsClient = dpsClient;
  }

  WorkflowExecutionDao getWorkflowExecutionDao() {
    return workflowExecutionDao;
  }

  public WorkflowPostProcessor getWorkflowPostProcessor() {
    return workflowPostProcessor;
  }

  DpsClient getDpsClient() {
    return dpsClient;
  }

  RedissonClient getRedissonClient() {
    return redissonClient;
  }

  public Channel getRabbitmqPublisherChannel() {
    return rabbitmqPublisherChannel;
  }

  public Channel getRabbitmqConsumerChannel() {
    return rabbitmqConsumerChannel;
  }
}
