package eu.europeana.metis.core.rest.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecordStatistics {

  private long taskId;
  private List<NodePathStatistics> nodePathStatistics;

  public long getTaskId() {
    return taskId;
  }

  public void setTaskId(long taskId) {
    this.taskId = taskId;
  }

  public List<NodePathStatistics> getNodePathStatistics() {
    return Collections.unmodifiableList(nodePathStatistics);
  }

  public void setNodePathStatistics(
      List<NodePathStatistics> nodePathStatistics) {
    this.nodePathStatistics = new ArrayList<>(nodePathStatistics);
  }
}
