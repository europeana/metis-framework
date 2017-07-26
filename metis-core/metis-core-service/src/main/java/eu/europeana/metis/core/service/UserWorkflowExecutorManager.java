package eu.europeana.metis.core.service;

import eu.europeana.metis.core.dao.UserWorkflowExecutionDao;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-30
 */
@Component
public class UserWorkflowExecutorManager implements Runnable {

  private final int maxConcurrentThreads = 2;
  private final int threadPoolSize = 10;
  private final BlockingQueue<UserWorkflowExecution> userWorkflowExecutionBlockingQueue = new PriorityBlockingQueue<>(
      10, new UserWorkflowExecution.UserWorkflowExecutionPriorityComparator());

  private final ExecutorService threadPool = Executors.newFixedThreadPool(threadPoolSize);
  private ExecutorCompletionService<UserWorkflowExecution> completionService = new ExecutorCompletionService<>(
      threadPool);

  private final Map<String, Future<UserWorkflowExecution>> futuresMap = new ConcurrentHashMap<>();
  private final UserWorkflowExecutionDao userWorkflowExecutionDao;

  @Autowired
  public UserWorkflowExecutorManager(
      UserWorkflowExecutionDao userWorkflowExecutionDao) {
    this.userWorkflowExecutionDao = userWorkflowExecutionDao;
  }


  @Override
  public void run() {
    int runningThreadsCounter = 0;
    while (true) {
      try {
        while (runningThreadsCounter >= maxConcurrentThreads) {
          synchronized (futuresMap) {
            Iterator<Map.Entry<String,Future<UserWorkflowExecution>>> iterator = futuresMap.entrySet().iterator();
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

        UserWorkflowExecution userWorkflowExecution = userWorkflowExecutionBlockingQueue.take();
        UserWorkflowExecutor userWorkflowExecutor = new UserWorkflowExecutor(userWorkflowExecution,
            userWorkflowExecutionDao);
        futuresMap.put(userWorkflowExecution.getId().toString(), completionService.submit(userWorkflowExecutor));
        runningThreadsCounter++;
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }


  public void addUserWorkflowExecutionToQueue(UserWorkflowExecution userWorkflowExecution) {
    userWorkflowExecutionBlockingQueue.add(userWorkflowExecution);
  }

  private boolean removeUserWorkflowExecutionFromQueue(
      UserWorkflowExecution userWorkflowExecution) {
    return userWorkflowExecutionBlockingQueue.remove(userWorkflowExecution);
  }

  public void cancelUserWorkflowExecution(UserWorkflowExecution userWorkflowExecution)
      throws ExecutionException {
    removeUserWorkflowExecutionFromQueue(userWorkflowExecution);
    //Stop the thread running the execution
    synchronized (futuresMap) {
      Iterator<Map.Entry<String,Future<UserWorkflowExecution>>> iterator = futuresMap.entrySet().iterator();
      while (iterator.hasNext()) {
        Entry<String, Future<UserWorkflowExecution>> futureEntry = iterator.next();
        if (futureEntry.getKey().equals(userWorkflowExecution.getId().toString())) {
          futureEntry.getValue().cancel(true);
        }
      }
    }
  }

  @PreDestroy
  public void close() {
    if (threadPool != null) {
      threadPool.shutdown();
    }
  }
}
