package eu.europeana.metis.core.workflow.plugins;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-29
 */
public class ValidationExternalPluginMetadata extends AbstractMetisPluginMetadata {

  private static final PluginType pluginType = PluginType.VALIDATION_EXTERNAL;

  public ValidationExternalPluginMetadata() {
  }

  @Override
  public PluginType getPluginType() {
    return pluginType;
  }
}
