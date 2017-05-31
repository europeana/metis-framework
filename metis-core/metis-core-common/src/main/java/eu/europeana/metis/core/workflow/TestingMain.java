package eu.europeana.metis.core.workflow;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import org.bson.types.ObjectId;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-26
 */
public class TestingMain {

  public static void main(String[] args) throws IOException, InterruptedException {
    final BlockingQueue<UserWorkflowExecution> userWorkflowExecutionBlockingQueue = new PriorityBlockingQueue<>(
        10, new UserWorkflowExecution.UserWorkflowExecutionPriorityComparator());

    UserWorkflowExecution use1 = new UserWorkflowExecution();
    ObjectId objectId1 = new ObjectId();
    use1.setId(objectId1);
    use1.setDatasetName("dataset1");
    use1.setOwner("owner1");
    use1.setWorkflowName("use1");
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, -2);
    use1.setCreatedDate(cal.getTime());
    use1.setWorkflowPriority(1);
    UserWorkflowExecution use2 = new UserWorkflowExecution();
    ObjectId objectId2 = new ObjectId();
    use2.setId(objectId2);
    use2.setDatasetName("dataset2");
    use2.setOwner("owner2");
    use2.setWorkflowName("use2");
//    cal = Calendar.getInstance();
//    cal.add(Calendar.DATE, -1);
    use2.setCreatedDate(cal.getTime());
    use2.setWorkflowPriority(0);
    UserWorkflowExecution use3 = new UserWorkflowExecution();
    ObjectId objectId3 = new ObjectId();
    use3.setId(objectId3);
    use3.setDatasetName("dataset3");
    use3.setOwner("owner3");
    use3.setWorkflowName("use3");
    use3.setCreatedDate(new Date());
    use3.setWorkflowPriority(0);

    System.out.println("use1 date: " + use1.getCreatedDate() + " priority: " + use1.getWorkflowPriority());
    System.out.println("use2 date: " + use2.getCreatedDate() + " priority: " + use2.getWorkflowPriority());
    System.out.println("use3 date: " + use3.getCreatedDate() + " priority: " + use2.getWorkflowPriority());

    userWorkflowExecutionBlockingQueue.add(use2);
    userWorkflowExecutionBlockingQueue.add(use3);
    userWorkflowExecutionBlockingQueue.add(use1);
//
//    System.out.println(userWorkflowExecutionBlockingQueue.take().getWorkflowName());
//    System.out.println(userWorkflowExecutionBlockingQueue.take().getWorkflowName());
//    System.out.println(userWorkflowExecutionBlockingQueue.take().getWorkflowName());

    UserWorkflowExecution use4 = new UserWorkflowExecution();
    use4.setId(objectId3);
    use4.setDatasetName("dataset3");
    use4.setOwner("owner3");
    use4.setWorkflowName("use3");

    userWorkflowExecutionBlockingQueue.remove(use4);

    System.out.println(userWorkflowExecutionBlockingQueue.take().getWorkflowName());
    System.out.println(userWorkflowExecutionBlockingQueue.take().getWorkflowName());
    System.out.println(userWorkflowExecutionBlockingQueue.take().getWorkflowName());

  }
}
