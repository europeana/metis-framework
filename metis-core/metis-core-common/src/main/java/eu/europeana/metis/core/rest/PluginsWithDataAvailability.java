package eu.europeana.metis.core.rest;

import eu.europeana.metis.core.workflow.plugins.PluginType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class represents a list of plugins with an indication for each on whether there is
 * successfully processed data.
 */
public class PluginsWithDataAvailability {

  private List<PluginWithDataAvailability> plugins;

  public void setPlugins(List<PluginWithDataAvailability> plugins) {
    this.plugins = new ArrayList<>(plugins);
  }

  public List<PluginWithDataAvailability> getPlugins() {
    return Collections.unmodifiableList(plugins);
  }

  /**
   * This class represents a plugin with an indication on whether there is successfully processed
   * data.
   */
  public static class PluginWithDataAvailability {

    private PluginType pluginType;
    private boolean hasSuccessfulData;

    public void setPluginType(PluginType pluginType) {
      this.pluginType = pluginType;
    }

    public PluginType getPluginType() {
      return pluginType;
    }

    public void setHasSuccessfulData(boolean hasSuccessfulData) {
      this.hasSuccessfulData = hasSuccessfulData;
    }

    public boolean isHasSuccessfulData() {
      return hasSuccessfulData;
    }
  }
}
