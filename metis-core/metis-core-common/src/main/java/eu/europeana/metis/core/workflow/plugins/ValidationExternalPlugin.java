package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.common.model.Revision;
import eu.europeana.cloud.common.model.dps.TaskInfo;
import eu.europeana.cloud.service.dps.DpsTask;
import eu.europeana.cloud.service.dps.InputDataType;
import eu.europeana.cloud.service.dps.exception.DpsException;
import eu.europeana.metis.exception.ExternalTaskException;
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

  private final String topologyName = Topology.VALIDATION.getTopologyName();

  /**
   * Zero argument constructor that initializes the {@link #pluginType} corresponding to the plugin.
   */
  public ValidationExternalPlugin() {
    //Required for json serialization
    super(PluginType.VALIDATION_EXTERNAL);

  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>Initializes the {@link #pluginType} as well.</p>
   *
   * @param pluginMetadata should be {@link ValidationExternalPluginMetadata}
   */
  public ValidationExternalPlugin(AbstractMetisPluginMetadata pluginMetadata) {
    super(PluginType.VALIDATION_EXTERNAL, pluginMetadata);
  }

  /**
   * Required for json serialization.
   *
   * @return the String representation of the topology
   */
  @Override
  public String getTopologyName() {
    return topologyName;
  }

  @Override
  public void execute(DpsClient dpsClient, String ecloudBaseUrl, String ecloudProvider,
      String ecloudDataset) throws ExternalTaskException {
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
      parameters.put("REVISION_TIMESTAMP",
          dateFormat.format(getPluginMetadata().getRevisionTimestampPreviousPlugin()));
      parameters.put("SCHEMA_NAME", urlOfSchemasZip);
      parameters.put("ROOT_LOCATION", schemaRootPath);
      parameters.put("SCHEMATRON_LOCATION", schematronRootPath);
      parameters.put("NEW_REPRESENTATION_NAME", getRepresentationName());
      parameters.put("OUTPUT_DATA_SETS", String.format("%s/data-providers/%s/data-sets/%s",
          ecloudBaseUrl, ecloudProvider, ecloudDataset));

      dpsTask.setParameters(parameters);

      Revision revision = new Revision();
      revision.setRevisionName(pluginTypeName);
      revision.setRevisionProviderId(ecloudProvider);
      revision.setCreationTimeStamp(getStartedDate());
      dpsTask.setOutputRevision(revision);

      try {
        setExternalTaskId(Long.toString(dpsClient.submitTask(dpsTask, topologyName)));
      } catch (DpsException e) {
        throw new ExternalTaskException("Submitting task failed", e);
      }
      LOGGER.info("Submitted task with externalTaskId: {}", getExternalTaskId());
    }
  }

  @Override
  public ExecutionProgress monitor(DpsClient dpsClient) throws ExternalTaskException {
    LOGGER.info("Requesting progress information for externalTaskId: {}", getExternalTaskId());
    TaskInfo taskInfo;
    try {
      taskInfo = dpsClient.getTaskProgress(topologyName, Long.parseLong(getExternalTaskId()));
    } catch (DpsException e) {
      throw new ExternalTaskException("Requesting task progress failed", e);
    }
    return getExecutionProgress().copyExternalTaskInformation(taskInfo);
  }
}
