package eu.europeana.metis.core.workflow.plugins;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This abstract class is the base implementation of {@link ExecutablePluginMetadata} for index
 * tasks. All executable index plugins should inherit from it.
 */
public abstract class AbstractIndexPluginMetadata extends AbstractExecutablePluginMetadata {

  private boolean useAlternativeIndexingEnvironment;
  private boolean preserveTimestamps;
  private boolean performRedirects;
  private List<String> datasetIdsToRedirectFrom = new ArrayList<>();
  private boolean incrementalIndexing; // Default: false (i.e. full processing)
  private Date harvestDate;

  public AbstractIndexPluginMetadata() {
    //Required for json serialization
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

  public boolean isIncrementalIndexing() {
    return incrementalIndexing;
  }

  public void setIncrementalIndexing(boolean incrementalIndexing) {
    this.incrementalIndexing = incrementalIndexing;
  }

  public Date getHarvestDate() {
    return harvestDate;
  }

  public void setHarvestDate(Date harvestDate) {
    this.harvestDate = harvestDate;
  }
}
