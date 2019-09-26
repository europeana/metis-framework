package eu.europeana.metis.core.workflow.plugins;

/**
 * Index to Publish Plugin Metadata.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-03
 */
public class IndexToPublishPluginMetadata extends AbstractExecutablePluginMetadata {

  private static final ExecutablePluginType pluginType = ExecutablePluginType.PUBLISH;
  private boolean useAlternativeIndexingEnvironment;
  private boolean preserveTimestamps;

  public IndexToPublishPluginMetadata() {
    //Required for json serialization
  }

  @Override
  public ExecutablePluginType getExecutablePluginType() {
    return pluginType;
  }

  public boolean isUseAlternativeIndexingEnvironment() {
    return useAlternativeIndexingEnvironment;
  }

  public void setUseAlternativeIndexingEnvironment(boolean useAlternativeIndexingEnvironment) {
    this.useAlternativeIndexingEnvironment = useAlternativeIndexingEnvironment;
  }

  public boolean isPreserveTimestamps() {
    return preserveTimestamps;
  }

  public void setPreserveTimestamps(boolean preserveTimestamps) {
    this.preserveTimestamps = preserveTimestamps;
  }
}
