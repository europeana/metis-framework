package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.common.model.Revision;
import eu.europeana.cloud.common.model.dps.TaskInfo;
import eu.europeana.cloud.service.dps.DpsTask;
import eu.europeana.cloud.service.dps.InputDataType;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

  public ValidationExternalPlugin() {
    super();
    setPluginType(PluginType.VALIDATION_EXTERNAL);
    //Required for json serialization
  }

  public ValidationExternalPlugin(AbstractMetisPluginMetadata pluginMetadata) {
    super(PluginType.VALIDATION_EXTERNAL, pluginMetadata);
  }

  @Override
  public void execute(DpsClient dpsClient, String ecloudBaseUrl, String ecloudProvider,
      String ecloudDataset) {
    if (!getPluginMetadata().isMocked()) {
      String pluginTypeName = getPluginType().name();
      LOGGER.info("Starting real execution of {} plugin for ecloudDatasetId {}", pluginTypeName,
          ecloudDataset);

      String urlOfSchemasZip = ((ValidationExternalPluginMetadata) getPluginMetadata())
          .getUrlOfSchemasZip();
      String schemaRootPath = ((ValidationExternalPluginMetadata) getPluginMetadata())
          .getSchemaRootPath();
      String schematronRootPath = ((ValidationExternalPluginMetadata) getPluginMetadata())
          .getSchematronRootPath();

      DpsTask dpsTask = new DpsTask();

      Map<InputDataType, List<String>> inputDataTypeListHashMap = new EnumMap<>(
          InputDataType.class);
      inputDataTypeListHashMap.put(InputDataType.DATASET_URLS,
          Collections.singletonList(String.format("%s/data-providers/%s/data-sets/%s",
              ecloudBaseUrl, ecloudProvider, ecloudDataset)));
      dpsTask.setInputData(inputDataTypeListHashMap);

      Map<String, String> parameters = new HashMap<>();
      parameters.put("REPRESENTATION_NAME", getRepresentationName());
      parameters.put("REVISION_NAME", getPluginMetadata().getRevisionNamePreviousPlugin());
      parameters.put("REVISION_PROVIDER", ecloudProvider);
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
      parameters.put("REVISION_TIMESTAMP", dateFormat.format(getPluginMetadata().getRevisionTimestampPreviousPlugin()));
      parameters.put("SCHEMA_NAME", "EDM-EXTERNAL");
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
