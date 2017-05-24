package eu.europeana.metis.core.workflow;

import java.util.List;
import java.util.Map;
import org.springframework.core.annotation.Order;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-24
 */
@Order(1)
public class VoidOaipmhHarvestPlugin implements AbstractMetisPlugin {

  private String name;
  private long sleepMillis;

  public VoidOaipmhHarvestPlugin(String name, long sleepMillis) {
    this.name = name;
    this.sleepMillis = sleepMillis;
  }

  @Override
  public void setParameters(Map<String, List<String>> parameters) {

  }

  @Override
  public Map<String, List<String>> getParameters() {
    return null;
  }

  @Override
  public void execute() {
    try {
      Thread.sleep(sleepMillis);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public CloudStatistics monitor(String dataseId) {
    return null;
  }

  @Override
  public boolean supports(PluginType pluginType) {
    return pluginType == PluginType.OAIPMH_HARVEST;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getVersion() {
    return null;
  }
}
