package eu.europeana.metis.core.workflow.plugins;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-29
 */
public class ValidationInternalPluginMetadata extends AbstractMetisPluginMetadata {

  private static final PluginType pluginType = PluginType.VALIDATION_EXTERNAL;

  public ValidationInternalPluginMetadata() {
  }

  @Override
  public PluginType getPluginType() {
    return pluginType;
  }
}