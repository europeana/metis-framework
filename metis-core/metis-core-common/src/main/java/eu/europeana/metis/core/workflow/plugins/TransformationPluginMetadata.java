package eu.europeana.metis.core.workflow.plugins;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-29
 */
public class TransformationPluginMetadata extends AbstractMetisPluginMetadata {

  private static final PluginType pluginType = PluginType.VALIDATION_EXTERNAL;

  public TransformationPluginMetadata() {
  }

  @Override
  public PluginType getPluginType() {
    return pluginType;
  }
}
