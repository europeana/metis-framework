package eu.europeana.metis.core.execution;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import eu.europeana.metis.core.dao.UserWorkflowExecutionDao;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import java.io.IOException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-30
 */
@Component
public class UserWorkflowExecutorManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserWorkflowExecutorManager.class);

  private final int maxConcurrentThreads = 2;
  private final int threadPoolSize = 10;
  private final Channel rabbitmqChannel;
  private String rabbitmqQueueName;

  private final ExecutorService threadPool = Executors.newFixedThreadPool(threadPoolSize);
  private final ExecutorCompletionService<UserWorkflowExecution> completionService = new ExecutorCompletionService<>(
      threadPool);

  private final UserWorkflowExecutionDao userWorkflowExecutionDao;

  @Autowired
  public UserWorkflowExecutorManager(
      UserWorkflowExecutionDao userWorkflowExecutionDao, Channel rabbitmqChannel) {
    this.userWorkflowExecutionDao = userWorkflowExecutionDao;
    this.rabbitmqChannel = rabbitmqChannel;
  }

  public void initiateConsumer() {
    try {
      rabbitmqChannel.basicQos(
          1); // For correct priority. Keep in mind this pre-fetches a message before going into handleDelivery
      //Auto acknowledge off because of Qos.
      rabbitmqChannel.basicConsume(rabbitmqQueueName, false, new QueueConsumer(rabbitmqChannel));
    } catch (IOException e) {
      LOGGER.error("Could not retrieve item from queue.", e);
    }
  }


  public synchronized void addUserWorkflowExecutionToQueue(String userWorkflowExecutionObjectId,
      int priority) {
    //Based on Rabbitmq the basicPublish between threads should be controlled
    BasicProperties basicProperties = MessageProperties.PERSISTENT_TEXT_PLAIN.builder()
        .priority(priority)
        .build();
    try {
      rabbitmqChannel.basicPublish("", rabbitmqQueueName, basicProperties,
          userWorkflowExecutionObjectId.getBytes("UTF-8"));
    } catch (IOException e) {
      LOGGER.error("UserWorkflowExecution with objectId: {} not added in queue..",
          userWorkflowExecutionObjectId, e);
    }
  }

  public void cancelUserWorkflowExecution(UserWorkflowExecution userWorkflowExecution) {
    if (userWorkflowExecution.getWorkflowStatus() == WorkflowStatus.INQUEUE
        || userWorkflowExecution.getWorkflowStatus() == WorkflowStatus.RUNNING) {
      userWorkflowExecutionDao.setCancellingState(userWorkflowExecution);
      LOGGER.info(
          "Cancelling user workflow execution with id: {}", userWorkflowExecution.getId());
    }
  }

  public void setRabbitmqQueueName(String rabbitmqQueueName) {
    this.rabbitmqQueueName = rabbitmqQueueName;
  }

  @PreDestroy
  public void close() {
    threadPool.shutdown();
  }

  class QueueConsumer extends DefaultConsumer{
    AtomicInteger runningThreadsCounter = new AtomicInteger(0);

    QueueConsumer(Channel channel) {
      super(channel);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope rabbitmqEnvelope,
        AMQP.BasicProperties properties, byte[] body) throws IOException {
      String objectId = new String(body, "UTF-8");
      LOGGER.info("UserWorkflowExecution id: {} received from queue.", objectId);
      UserWorkflowExecution userWorkflowExecution = userWorkflowExecutionDao.getById(objectId);
      if (!userWorkflowExecution.isCancelling()) {
        UserWorkflowExecutor userWorkflowExecutor = new UserWorkflowExecutor(
            userWorkflowExecution, userWorkflowExecutionDao);
        completionService.submit(userWorkflowExecutor);
        runningThreadsCounter.incrementAndGet();
      } else {
        userWorkflowExecution.setAllRunningAndInqueuePluginsToCancelled();
        userWorkflowExecutionDao.update(userWorkflowExecution);
        LOGGER.info("Cancelled inqueue user workflow execution with id: {}",
            userWorkflowExecution.getId());
      }
      rabbitmqChannel
          .basicAck(rabbitmqEnvelope.getDeliveryTag(), false);//Send ACK back to remove from queue
      LOGGER.info("ACK sent for {} with tag {}", userWorkflowExecution.getId(),
          rabbitmqEnvelope.getDeliveryTag());

      checkCompletionService();
    }

    private synchronized void checkCompletionService()
    {
      //Block until one of the executions has finished and then release to do another handleDelivery
      if (runningThreadsCounter.get() >= maxConcurrentThreads) {
        try {
          completionService.take();
          runningThreadsCounter.decrementAndGet();
          while (completionService.poll() != null) {
            runningThreadsCounter.decrementAndGet();
          }
        } catch (InterruptedException e) {
          LOGGER.error(
              "Interrupted while waiting for taking a Future from the ExecutorCompletionService",
              e);
        }
      }
    }
  }
}
