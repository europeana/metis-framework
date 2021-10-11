package eu.europeana.metis.core.workflow.plugins;

/**
 * Index to Publish Plugin Metadata.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-03
 */
public class IndexToPublishPluginMetadata extends AbstractIndexPluginMetadata {

  private static final ExecutablePluginType pluginType = ExecutablePluginType.PUBLISH;

  public IndexToPublishPluginMetadata() {
    //Required for json serialization
  }

  @Override
  public ExecutablePluginType getExecutablePluginType() {
    return pluginType;
  }
}
