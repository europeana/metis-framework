package eu.europeana.metis.core.workflow.plugins;

import java.util.ArrayList;
import java.util.List;

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
  private List<String> datasetIdsToRedirectFrom = new ArrayList<>();

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

  public List<String> getDatasetIdsToRedirectFrom() {
    return new ArrayList<>(datasetIdsToRedirectFrom);
  }

  public void setDatasetIdsToRedirectFrom(List<String> datasetIdsToRedirectFrom) {
    this.datasetIdsToRedirectFrom =
        datasetIdsToRedirectFrom == null ? new ArrayList<>() : new ArrayList<>(
            datasetIdsToRedirectFrom);
  }
}
