package eu.europeana.metis.core.execution;

import org.redisson.api.RedissonClient;
import com.rabbitmq.client.Channel;
import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;

abstract class PersistenceProvider {

  private final Channel rabbitmqChannel;
  private final WorkflowExecutionDao workflowExecutionDao;
  private final RedissonClient redissonClient;
  private final DpsClient dpsClient;

  PersistenceProvider(Channel rabbitmqChannel, WorkflowExecutionDao workflowExecutionDao,
      RedissonClient redissonClient, DpsClient dpsClient) {
    this.rabbitmqChannel = rabbitmqChannel;
    this.workflowExecutionDao = workflowExecutionDao;
    this.redissonClient = redissonClient;
    this.dpsClient = dpsClient;
  }

  WorkflowExecutionDao getWorkflowExecutionDao() {
    return workflowExecutionDao;
  }

  DpsClient getDpsClient() {
    return dpsClient;
  }

  RedissonClient getRedissonClient() {
    return redissonClient;
  }

  Channel getRabbitmqChannel() {
    return rabbitmqChannel;
  }

}
