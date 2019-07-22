package eu.europeana.metis.core.workflow.plugins;

/**
 * Index to Preview Plugin Metadata.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-03
 */
public class IndexToPreviewPluginMetadata extends AbstractExecutablePluginMetadata {

  private static final ExecutablePluginType pluginType = ExecutablePluginType.PREVIEW;
  // TODO: 22-7-19 Once 1.5.0 version is released, datasetId field can be removed
  private String datasetId;
  private boolean useAlternativeIndexingEnvironment;
  private boolean preserveTimestamps;

  public IndexToPreviewPluginMetadata() {
    //Required for json serialization
  }

  @Override
  public ExecutablePluginType getExecutablePluginType() {
    return pluginType;
  }

  @Deprecated
  public String getDatasetId() {
    return datasetId;
  }

  @Deprecated
  public void setDatasetId(String datasetId) {
    this.datasetId = datasetId;
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
