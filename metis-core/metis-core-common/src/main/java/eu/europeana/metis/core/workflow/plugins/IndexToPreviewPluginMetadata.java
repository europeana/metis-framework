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
  private boolean performRedirects = false;
  private List<String> datasetIdsToRedirectFrom = new ArrayList<>();

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

  public boolean isPerformRedirects() {
    return performRedirects;
  }

  public void setPerformRedirects(boolean performRedirects) {
    this.performRedirects = performRedirects;
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
