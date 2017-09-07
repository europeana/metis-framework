package eu.europeana.metis.core.service;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import eu.europeana.metis.core.dao.UserWorkflowExecutionDao;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
public class UserWorkflowExecutorManager implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserWorkflowExecutorManager.class);

  private final int maxConcurrentThreads = 2;
  private final int threadPoolSize = 10;
  private final Channel rabbitmqChannel;
  private String rabbitmqQueueName;

  private final ExecutorService threadPool = Executors.newFixedThreadPool(threadPoolSize);
  private ExecutorCompletionService<UserWorkflowExecution> completionService = new ExecutorCompletionService<>(
      threadPool);

  private final Map<String, Future<UserWorkflowExecution>> futuresMap = new ConcurrentHashMap<>();
  private final UserWorkflowExecutionDao userWorkflowExecutionDao;

  @Autowired
  public UserWorkflowExecutorManager(
      UserWorkflowExecutionDao userWorkflowExecutionDao, Channel rabbitmqChannel) {
    this.userWorkflowExecutionDao = userWorkflowExecutionDao;
    this.rabbitmqChannel = rabbitmqChannel;
  }

  @Override
  public void run() {
    Consumer consumer = new DefaultConsumer(rabbitmqChannel) {
      int runningThreadsCounter = 0;

      @Override
      public void handleDelivery(String consumerTag, Envelope rabbitmqEnvelope,
          AMQP.BasicProperties properties, byte[] body)
          throws IOException {
        String objectId = new String(body, "UTF-8");
        LOGGER.info("UserWorkflowExecution id: {} received from queue.", objectId);
        UserWorkflowExecution userWorkflowExecution = userWorkflowExecutionDao.getById(objectId);
        if (!userWorkflowExecution.isCancelling()) {
          UserWorkflowExecutor userWorkflowExecutor = new UserWorkflowExecutor(
              userWorkflowExecution, userWorkflowExecutionDao, rabbitmqChannel,
              rabbitmqEnvelope);
          futuresMap.put(userWorkflowExecution.getId().toString(),
              completionService.submit(userWorkflowExecutor));
          runningThreadsCounter++;
        } else {
          userWorkflowExecution.setAllRunningAndInqueuePluginsToCancelled();
          userWorkflowExecutionDao.update(userWorkflowExecution);
          LOGGER.info(
              "Cancelled inqueue user workflow execution with id: {}", userWorkflowExecution.getId());
          rabbitmqChannel.basicAck(rabbitmqEnvelope.getDeliveryTag(), false);
        }

        while (runningThreadsCounter >= maxConcurrentThreads) {
          synchronized (futuresMap) {
            Iterator<Map.Entry<String, Future<UserWorkflowExecution>>> iterator = futuresMap
                .entrySet().iterator();
            while (iterator.hasNext()) {
              Future<UserWorkflowExecution> future = iterator.next().getValue();
              if (future.isDone() || future.isCancelled()) {
                future.cancel(true);
                iterator.remove();
                runningThreadsCounter--;
              }
            }
          }
        }
      }
    };
    try {
      rabbitmqChannel.basicQos(maxConcurrentThreads); // For correct priority
      rabbitmqChannel.basicConsume(rabbitmqQueueName, false, consumer);
    } catch (IOException e) {
      LOGGER.error("Could not retrieve item from queue.", e);
    }
  }


  public void addUserWorkflowExecutionToQueue(String userWorkflowExecutionObjectId, int priority) {
    BasicProperties basicProperties = MessageProperties.PERSISTENT_TEXT_PLAIN.builder().priority(priority)
        .build();
    try {
      rabbitmqChannel.basicPublish("", rabbitmqQueueName, basicProperties,
          userWorkflowExecutionObjectId.getBytes("UTF-8"));
    } catch (IOException e) {
      LOGGER.error("UserWorkflowExecution with objectId: {} not added in queue..",
          userWorkflowExecutionObjectId, e);
    }
  }

  public void cancelUserWorkflowExecution(UserWorkflowExecution userWorkflowExecution)
      throws ExecutionException {
    if (userWorkflowExecution.getWorkflowStatus() == WorkflowStatus.INQUEUE || userWorkflowExecution.getWorkflowStatus() == WorkflowStatus.RUNNING) {
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
}
