package eu.europeana.metis.core.workflow.plugins;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-26
 */
public class NormalizationPluginMetadata extends AbstractMetisPluginMetadata {

  private static final PluginType pluginType = PluginType.NORMALIZATION;

  public NormalizationPluginMetadata() {
    //Required for json serialization
  }

  @Override
  public PluginType getPluginType() {
    return pluginType;
  }
}
