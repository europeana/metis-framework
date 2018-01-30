package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.common.model.Revision;
import eu.europeana.cloud.common.model.dps.TaskInfo;
import eu.europeana.cloud.service.dps.DpsTask;
import eu.europeana.cloud.service.dps.InputDataType;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-24
 */
public class ValidationExternalPlugin extends AbstractMetisPlugin {

  private static final Logger LOGGER = LoggerFactory.getLogger(ValidationExternalPlugin.class);

  private final String topologyName = TopologyName.VALIDATION.getTopologyName();

  private AbstractMetisPluginMetadata pluginMetadata;

  public ValidationExternalPlugin() {
    super();
    setPluginType(PluginType.VALIDATION_EXTERNAL);
    //Required for json serialization
  }

  public ValidationExternalPlugin(AbstractMetisPluginMetadata pluginMetadata) {
    setPluginType(PluginType.VALIDATION_EXTERNAL);
    this.pluginMetadata = pluginMetadata;
  }

  @Override
  public AbstractMetisPluginMetadata getPluginMetadata() {
    return pluginMetadata;
  }

  @Override
  public void setPluginMetadata(
      AbstractMetisPluginMetadata pluginMetadata) {
    this.pluginMetadata = pluginMetadata;
  }

  @Override
  public void execute(DpsClient dpsClient, String ecloudBaseUrl, String ecloudProvider,
      String ecloudDataset) {
    if (!pluginMetadata.isMocked()) {
      String pluginTypeName = getPluginType().name();
      LOGGER.info("Starting real execution of {} plugin for ecloudDatasetId {}", pluginTypeName,
          ecloudDataset);

      String urlOfSchemasZip = ((ValidationExternalPluginMetadata) pluginMetadata)
          .getUrlOfSchemasZip();
      String schemaRootPath = ((ValidationExternalPluginMetadata) pluginMetadata)
          .getSchemaRootPath();
      String schematronRootPath = ((ValidationExternalPluginMetadata) pluginMetadata)
          .getSchematronRootPath();
      String revisionNamePreviousPlugin = ((ValidationExternalPluginMetadata) pluginMetadata)
          .getRevisionNamePreviousPlugin();
      String revisionProviderPreviousPlugin = ((ValidationExternalPluginMetadata) pluginMetadata)
          .getRevisionProviderPreviousPlugin();
      String revisionTimestampPreviousPlugin = ((ValidationExternalPluginMetadata) pluginMetadata)
          .getRevisionTimestampPreviousPlugin();

      DpsTask dpsTask = new DpsTask();

      Map<InputDataType, List<String>> inputDataTypeListHashMap = new EnumMap<>(
          InputDataType.class);
      inputDataTypeListHashMap.put(InputDataType.DATASET_URLS,
          Collections.singletonList(String.format("%s/data-providers/%s/data-sets/%s",
              ecloudBaseUrl, ecloudProvider, ecloudDataset)));
      dpsTask.setInputData(inputDataTypeListHashMap);

      Map<String, String> parameters = new HashMap<>();
      parameters.put("REPRESENTATION_NAME", getRepresentationName());
      parameters.put("REVISION_NAME", revisionNamePreviousPlugin);
      parameters.put("REVISION_PROVIDER", revisionProviderPreviousPlugin);
      parameters.put("REVISION_TIMESTAMP", revisionTimestampPreviousPlugin);
      parameters.put("SCHEMA_NAME", "edm_external");
//      parameters.put("SCHEMA_NAME", urlOfSchemasZip);
//      parameters.put("ROOT_LOCATION", schemaRootPath);
//      parameters.put("SCHEMATRON_ROOT_LOCATION", schematronRootPath);
      parameters.put("NEW_REPRESENTATION_NAME", getRepresentationName());

      dpsTask.setParameters(parameters);

      Revision revision = new Revision();
      revision.setRevisionName(pluginTypeName);
      revision.setRevisionProviderId(ecloudProvider);
      revision.setCreationTimeStamp(getStartedDate());
      dpsTask.setOutputRevision(revision);

      setExternalTaskId(Long.toString(dpsClient.submitTask(dpsTask, topologyName)));
      LOGGER.info("Submitted task with externalTaskId: {}", getExternalTaskId());
    }
  }

  @Override
  public ExecutionProgress monitor(DpsClient dpsClient) {
    LOGGER.info("Requesting progress information for externalTaskId: {}", getExternalTaskId());
    TaskInfo taskInfo = dpsClient
        .getTaskProgress(topologyName, Long.parseLong(getExternalTaskId()));
    return getExecutionProgress().copyExternalTaskInformation(taskInfo);
  }
}
