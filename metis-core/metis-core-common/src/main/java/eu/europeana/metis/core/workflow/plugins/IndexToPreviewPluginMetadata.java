package eu.europeana.metis.core.workflow.plugins;

/**
 * Index to Preview Plugin Metadata.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-03
 */
public class IndexToPreviewPluginMetadata extends AbstractIndexPluginMetadata {

  private static final ExecutablePluginType pluginType = ExecutablePluginType.PREVIEW;

  public IndexToPreviewPluginMetadata() {
    //Required for json serialization
  }

  @Override
  public ExecutablePluginType getExecutablePluginType() {
    return pluginType;
  }
}
