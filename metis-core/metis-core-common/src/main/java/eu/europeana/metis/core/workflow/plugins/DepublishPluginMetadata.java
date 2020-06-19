package eu.europeana.metis.core.workflow.plugins;


import java.util.List;
import java.util.Set;

/**
 * Index to Publish Plugin Metadata.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2020-06-16
 */
public class DepublishPluginMetadata extends AbstractExecutablePluginMetadata {

  private static final ExecutablePluginType pluginType = ExecutablePluginType.DEPUBLISH;
  private boolean allRecordsDepublish;
  private Set<String> recordIdsToDepublish;

  public DepublishPluginMetadata() {
    //Required for json serialization
  }

  @Override
  public ExecutablePluginType getExecutablePluginType() {
    return pluginType;
  }

  public boolean isAllRecordsDepublish() {
    return allRecordsDepublish;
  }

  public void setAllRecordsDepublish(boolean allRecordsDepublish) {
    this.allRecordsDepublish = allRecordsDepublish;
  }

  public Set<String> getRecordIdsToDepublish() {
    return recordIdsToDepublish;
  }

  public void setRecordIdsToDepublish(Set<String> recordIdsToDepublish) {
    this.recordIdsToDepublish = recordIdsToDepublish;
  }
}
