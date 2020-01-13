package eu.europeana.metis.core.workflow.plugins;

import java.util.ArrayList;
import java.util.List;

/**
 * Index to Preview Plugin Metadata.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-03
 */
public class IndexToPreviewPluginMetadata extends AbstractExecutablePluginMetadata {

  private static final ExecutablePluginType pluginType = ExecutablePluginType.PREVIEW;
  private boolean useAlternativeIndexingEnvironment;
  private boolean preserveTimestamps;
  private List<String> datasetIdsForRedirection = new ArrayList<>();

  public IndexToPreviewPluginMetadata() {
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

  public List<String> getDatasetIdsForRedirection() {
    return new ArrayList<>(datasetIdsForRedirection);
  }

  public void setDatasetIdsForRedirection(List<String> datasetIdsForRedirection) {
    this.datasetIdsForRedirection =
        datasetIdsForRedirection == null ? new ArrayList<>() : new ArrayList<>(datasetIdsForRedirection);
  }
}
