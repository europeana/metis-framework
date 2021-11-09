package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.metis.indexing.TargetIndexingDatabase;

/**
 * Index to Preview Plugin.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-03
 */
public class IndexToPreviewPlugin extends AbstractIndexPlugin<IndexToPreviewPluginMetadata> {

  /**
   * Zero argument constructor that initializes the {@link #pluginType} corresponding to the
   * plugin.
   */
  IndexToPreviewPlugin() {
    //Required for json serialization
    this(null);
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>Initializes the {@link #pluginType} as well.</p>
   *
   * @param pluginMetadata The plugin metadata.
   */
  IndexToPreviewPlugin(IndexToPreviewPluginMetadata pluginMetadata) {
    super(PluginType.PREVIEW, pluginMetadata);
  }

  public TargetIndexingDatabase getTargetIndexingDatabase() {
    return TargetIndexingDatabase.PREVIEW;
  }
}
