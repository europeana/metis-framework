package eu.europeana.metis.core.workflow;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-26
 */
public class TestingMain {

  public static void main(String[] args) throws IOException, InterruptedException {
    final BlockingQueue<UserWorkflowExecution> userWorkflowExecutionBlockingQueue = new PriorityBlockingQueue<>(
        10, new UserWorkflowExecution.UserWorkflowExecutionPriorityComparator());

    UserWorkflowExecution use1 = new UserWorkflowExecution();
    use1.setWorkflowName("use1");
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, -2);
    use1.setCreatedDate(cal.getTime());
    use1.setPriority(1);
    UserWorkflowExecution use2 = new UserWorkflowExecution();
    use2.setWorkflowName("use2");
//    cal = Calendar.getInstance();
//    cal.add(Calendar.DATE, -1);
    use2.setCreatedDate(cal.getTime());
    use2.setPriority(0);
    UserWorkflowExecution use3 = new UserWorkflowExecution();
    use3.setWorkflowName("use3");
    use3.setCreatedDate(new Date());
    use3.setPriority(0);

    System.out.println("use1 date: " + use1.getCreatedDate() + " priority: " + use1.getPriority());
    System.out.println("use2 date: " + use2.getCreatedDate() + " priority: " + use2.getPriority());
    System.out.println("use3 date: " + use3.getCreatedDate() + " priority: " + use2.getPriority());

    userWorkflowExecutionBlockingQueue.add(use2);
    userWorkflowExecutionBlockingQueue.add(use3);
    userWorkflowExecutionBlockingQueue.add(use1);

    System.out.println(userWorkflowExecutionBlockingQueue.take().getWorkflowName());
    System.out.println(userWorkflowExecutionBlockingQueue.take().getWorkflowName());
    System.out.println(userWorkflowExecutionBlockingQueue.take().getWorkflowName());

  }
}
