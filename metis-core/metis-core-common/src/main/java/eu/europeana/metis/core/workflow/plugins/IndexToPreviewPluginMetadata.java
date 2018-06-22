package eu.europeana.metis.core.workflow.plugins;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-03
 */
public class IndexToPreviewPluginMetadata extends AbstractMetisPluginMetadata {

  private static final PluginType pluginType = PluginType.PREVIEW;
  private boolean useAlternativeIndexingEnvironment;

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
}
