package eu.europeana.metis.core.workflow.plugins;


import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Index to Publish Plugin Metadata.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2020-06-16
 */
public class DepublishPluginMetadata extends AbstractExecutablePluginMetadata {

  private static final ExecutablePluginType pluginType = ExecutablePluginType.DEPUBLISH;
  private boolean datasetDepublish;
  private Set<String> recordIdsToDepublish;
  private DepublicationReason depublicationReason;

  public DepublishPluginMetadata() {
    //Required for json serialization
  }

  @Override
  public ExecutablePluginType getExecutablePluginType() {
    return pluginType;
  }

  public boolean isDatasetDepublish() {
    return datasetDepublish;
  }

  public void setDatasetDepublish(boolean datasetDepublish) {
    this.datasetDepublish = datasetDepublish;
  }

  public Set<String> getRecordIdsToDepublish() {
    return Optional.ofNullable(recordIdsToDepublish).map(Collections::unmodifiableSet)
            .orElseGet(Collections::emptySet);
  }

  public void setRecordIdsToDepublish(Set<String> recordIdsToDepublish) {
    this.recordIdsToDepublish = new HashSet<>(recordIdsToDepublish);
  }

  public void setDepublicationReason(DepublicationReason depublicationReason) {
    this.depublicationReason = depublicationReason;
  }

  public DepublicationReason getDepublicationReason() {
    return depublicationReason;
  }
}
