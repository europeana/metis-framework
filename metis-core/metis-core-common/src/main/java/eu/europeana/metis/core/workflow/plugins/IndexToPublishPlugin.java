package eu.europeana.metis.core.workflow.plugins;

/**
 * Index to Publish Plugin.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-03
 */
public class IndexToPublishPlugin extends AbstractIndexPlugin<IndexToPublishPluginMetadata> {

  /**
   * Zero argument constructor that initializes the {@link #pluginType} corresponding to the
   * plugin.
   */
  IndexToPublishPlugin() {
    //Required for json serialization
    this(null);
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>Initializes the {@link #pluginType} as well.</p>
   *
   * @param pluginMetadata The plugin metadata.
   */
  IndexToPublishPlugin(IndexToPublishPluginMetadata pluginMetadata) {
    super(PluginType.PUBLISH, pluginMetadata);
  }
}
