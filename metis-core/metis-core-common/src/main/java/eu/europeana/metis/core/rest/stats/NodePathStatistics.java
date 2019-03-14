package eu.europeana.metis.core.rest.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Statistics object that reflect the node level: statistics cover all nodes with the same xPath.
 */
public class NodePathStatistics {

  private String xPath;
  private List<NodeValueStatistics> nodeValueStatistics;

  public String getxPath() {
    return xPath;
  }

  public void setxPath(String xPath) {
    this.xPath = xPath;
  }

  public List<NodeValueStatistics> getNodeValueStatistics() {
    return Collections.unmodifiableList(nodeValueStatistics);
  }

  public void setNodeValueStatistics(
      List<NodeValueStatistics> nodeValueStatistics) {
    this.nodeValueStatistics = new ArrayList<>(nodeValueStatistics);
  }
}
