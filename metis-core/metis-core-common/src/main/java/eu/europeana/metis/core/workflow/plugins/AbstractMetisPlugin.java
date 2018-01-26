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
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME,
    include=JsonTypeInfo.As.PROPERTY,
    property="pluginType")
@JsonSubTypes({
    @JsonSubTypes.Type(value=OaipmhHarvestPlugin.class, name="OAIPMH_HARVEST"),
    @JsonSubTypes.Type(value=HTTPHarvestPlugin.class, name="HTTP_HARVEST"),
    @JsonSubTypes.Type(value=EnrichmentPlugin.class, name="ENRICHMENT"),
    @JsonSubTypes.Type(value=ValidationPlugin.class, name="VALIDATION")
})
public interface AbstractMetisPlugin {

  PluginType getPluginType();

  AbstractMetisPluginMetadata getPluginMetadata();
  void setPluginMetadata(AbstractMetisPluginMetadata abstractMetisPluginMetadata);

  Date getStartedDate();

  void setStartedDate(Date startedDate);

  Date getFinishedDate();

  void setFinishedDate(Date finishedDate);

  Date getUpdatedDate();

  void setUpdatedDate(Date updatedDate);

  PluginStatus getPluginStatus();

  void setPluginStatus(PluginStatus pluginStatus);

  String getExternalTaskId();

  void setExternalTaskId(String externalTaskId);

  ExecutionProgress getExecutionProgress();

  void setExecutionProgress(
      ExecutionProgress executionProgress);

  void execute(DpsClient dpsClient, String ecloudBaseUrl, String ecloudProvider, String ecloudDataset);

  ExecutionProgress monitor(DpsClient dpsClient);

}
