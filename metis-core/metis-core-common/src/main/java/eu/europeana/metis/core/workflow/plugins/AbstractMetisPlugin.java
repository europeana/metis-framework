package eu.europeana.metis.core.workflow.plugins;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import eu.europeana.metis.core.workflow.CloudStatistics;
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
    @JsonSubTypes.Type(value=VoidOaipmhHarvestPlugin.class, name="OAIPMH_HARVEST"),
    @JsonSubTypes.Type(value=VoidHTTPHarvestPlugin.class, name="HTTP_HARVEST"),
    @JsonSubTypes.Type(value=VoidDereferencePlugin.class, name="DEREFERENCE"),
    @JsonSubTypes.Type(value=VoidMetisPlugin.class, name="VOID")
})
public interface AbstractMetisPlugin {

  PluginStatus getPluginStatus();

  PluginType getPluginType();

  AbstractMetisPluginMetadata getPluginMetadata();
  void setPluginMetadata(AbstractMetisPluginMetadata abstractMetisPluginMetadata);

  Date getStartedDate();

  void setStartedDate(Date startedDate);

  Date getFinishedDate();

  void setFinishedDate(Date finishedDate);

  Date getUpdatedDate();

  void setUpdatedDate(Date updatedDate);

  void setPluginStatus(PluginStatus pluginStatus);

  ExecutionRecordsStatistics getExecutionRecordsStatistics();

  void setExecutionRecordsStatistics(
      ExecutionRecordsStatistics executionRecordsStatistics);

  /**
   * The business logic that the UserWorkflow implements. This is where the connection to the
   * Europeana Cloud DPS REST API is implemented.
   */
  void execute();

  CloudStatistics monitor(String dataseId);

}
