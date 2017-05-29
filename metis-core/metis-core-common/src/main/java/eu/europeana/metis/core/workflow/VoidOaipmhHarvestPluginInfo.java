package eu.europeana.metis.core.workflow;

import java.util.List;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-29
 */
public class VoidOaipmhHarvestPluginInfo {
  private int requestedOrder;
  Map<String, List<String>> parameters;

  public int getRequestedOrder() {
    return requestedOrder;
  }

  public void setRequestedOrder(int requestedOrder) {
    this.requestedOrder = requestedOrder;
  }

  public Map<String, List<String>> getParameters() {
    return parameters;
  }

  public void setParameters(Map<String, List<String>> parameters) {
    this.parameters = parameters;
  }

}
