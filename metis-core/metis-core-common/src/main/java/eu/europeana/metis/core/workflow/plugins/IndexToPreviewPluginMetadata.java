package eu.europeana.metis.core.workflow.plugins;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-03
 */
public class IndexToPreviewPluginMetadata extends AbstractMetisPluginMetadata {

  private static final PluginType pluginType = PluginType.PREVIEW;
  private boolean useAlternativeIndexingEnvironment;
  private boolean preserveTimestamps;

  public IndexToPreviewPluginMetadata() {
    //Required for json serialization
  }

  @Override
  public PluginType getPluginType() {
    return pluginType;
  }

  public boolean getUseAlternativeIndexingEnvironment() {
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
