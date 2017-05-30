package eu.europeana.metis.core.service;

import eu.europeana.metis.core.dao.UserWorkflowExecutionDao;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-30
 */
public class UserWorkflowExecutorManager implements Runnable {

  private final int maxConcurrentThreads = 2;
  private final int threadPoolSize = 10;
  private final BlockingQueue<UserWorkflowExecution> userWorkflowExecutionBlockingQueue = new PriorityBlockingQueue<>(
      10, new UserWorkflowExecution.UserWorkflowExecutionPriorityComparator());
  private final ExecutorService threadPool = Executors.newFixedThreadPool(threadPoolSize);
  ExecutorCompletionService<UserWorkflowExecution> completionService = new ExecutorCompletionService<>(
      threadPool);
  private final List<Future<UserWorkflowExecution>> futures = new ArrayList<>();
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
          Iterator<Future<UserWorkflowExecution>> iterator = futures.iterator();
          while (iterator.hasNext()) {
            Future<UserWorkflowExecution> future = iterator.next();
            if (future.isDone()) {
              future.cancel(true);
              iterator.remove();
              runningThreadsCounter--;
            }
          }
        }

        UserWorkflowExecution userWorkflowExecution = userWorkflowExecutionBlockingQueue.take();
        UserWorkflowExecutor userWorkflowExecutor = new UserWorkflowExecutor(userWorkflowExecution,
            userWorkflowExecutionDao);
        futures.add(completionService.submit(userWorkflowExecutor));
        runningThreadsCounter++;
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }


  public void addUserWorkflowExecutionToQueue(UserWorkflowExecution userWorkflowExecution) {
    userWorkflowExecutionBlockingQueue.add(userWorkflowExecution);
  }

  @PreDestroy
  public void close() {
    if (threadPool != null) {
      threadPool.shutdown();
    }
  }
}
