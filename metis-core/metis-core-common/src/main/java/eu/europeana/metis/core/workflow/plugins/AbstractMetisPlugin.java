package eu.europeana.metis.core.workflow.plugins;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import eu.europeana.cloud.client.dps.rest.DpsClient;
import java.util.Date;

/**
 * This interface specifies the minimum o plugin should support so that it can be plugged in the
 * Metis workflow registry and can be accessible via the REST API of Metis.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-06-01
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "pluginType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = OaipmhHarvestPlugin.class, name = "OAIPMH_HARVEST"),
    @JsonSubTypes.Type(value = HTTPHarvestPlugin.class, name = "HTTP_HARVEST"),
    @JsonSubTypes.Type(value = EnrichmentPlugin.class, name = "ENRICHMENT"),
    @JsonSubTypes.Type(value = ValidationPlugin.class, name = "VALIDATION")
})
public interface AbstractMetisPlugin {

  /**
   * @return {@link PluginType}
   */
  PluginType getPluginType();

  /**
   * The metadata corresponding to this plugin.
   *
   * @return {@link AbstractMetisPluginMetadata}
   */
  AbstractMetisPluginMetadata getPluginMetadata();

  /**
   * @param abstractMetisPluginMetadata {@link AbstractMetisPluginMetadata} to add for the plugin
   */
  void setPluginMetadata(AbstractMetisPluginMetadata abstractMetisPluginMetadata);

  /**
   * @return started {@link Date} of the execution of the plugin
   */
  Date getStartedDate();

  /**
   * @param startedDate {@link Date}
   */
  void setStartedDate(Date startedDate);

  /**
   * @return finished {@link Date} of the execution of the plugin
   */
  Date getFinishedDate();

  /**
   * @param finishedDate {@link Date}
   */
  void setFinishedDate(Date finishedDate);

  /**
   * @return updated {@link Date} of the execution of the plugin
   */
  Date getUpdatedDate();

  /**
   * @param updatedDate {@link Date}
   */
  void setUpdatedDate(Date updatedDate);

  /**
   * @return status {@link PluginStatus} of the execution of the plugin
   */
  PluginStatus getPluginStatus();

  /**
   * @param pluginStatus {@link PluginStatus}
   */
  void setPluginStatus(PluginStatus pluginStatus);

  /**
   * @return String representation of the external task identifier of the execution
   */
  String getExternalTaskId();

  /**
   * @param externalTaskId String representation of the external task identifier of the execution
   */
  void setExternalTaskId(String externalTaskId);

  /**
   * Progress information of the execution of the plugin
   *
   * @return {@link ExecutionProgress}
   */
  ExecutionProgress getExecutionProgress();

  /**
   * @param executionProgress {@link ExecutionProgress} of the external execution
   */
  void setExecutionProgress(
      ExecutionProgress executionProgress);

  /**
   * Starts the execution of the plugin at the external location.
   * <p>It is non blocking method and the {@link #monitor(DpsClient)} should be used to monitor the external execution</p>
   *
   * @param dpsClient {@link DpsClient} used to submit the external execution
   * @param ecloudBaseUrl the base url of the ecloud apis
   * @param ecloudProvider the ecloud provider to be used for the external task
   * @param ecloudDataset the ecloud dataset identifier to be used for the external task
   */
  void execute(DpsClient dpsClient, String ecloudBaseUrl, String ecloudProvider,
      String ecloudDataset);

  /**
   * Request a monitor call to the external execution.
   *
   * @param dpsClient {@link DpsClient} used to request a monitor call the external execution
   * @return {@link ExecutionProgress} of the plugin.
   */
  ExecutionProgress monitor(DpsClient dpsClient);

}
