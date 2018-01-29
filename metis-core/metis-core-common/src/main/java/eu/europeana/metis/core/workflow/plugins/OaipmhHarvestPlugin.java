package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.common.model.Revision;
import eu.europeana.cloud.common.model.dps.TaskInfo;
import eu.europeana.cloud.service.dps.DpsTask;
import eu.europeana.cloud.service.dps.InputDataType;
import eu.europeana.cloud.service.dps.OAIPMHHarvestingDetails;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-24
 */
public class OaipmhHarvestPlugin extends AbstractMetisPlugin {

  private static final Logger LOGGER = LoggerFactory.getLogger(OaipmhHarvestPlugin.class);

  private final String topologyName = TopologyName.OAIPMH_HARVEST.getTopologyName();

  private AbstractMetisPluginMetadata pluginMetadata;

  public OaipmhHarvestPlugin() {
    super();
    setPluginType(PluginType.OAIPMH_HARVEST);
    //Required for json serialization
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata required.
   *
   * @param pluginMetadata should be {@link OaipmhHarvestPluginMetadata}
   */
  public OaipmhHarvestPlugin(AbstractMetisPluginMetadata pluginMetadata) {
    setPluginType(PluginType.OAIPMH_HARVEST);
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

  /**
   * Required for json serialization.
   *
   * @return the String representation of the topology
   */
  public String getTopologyName() {
    return topologyName;
  }

  @Override
  public void execute(DpsClient dpsClient, String ecloudBaseUrl, String ecloudProvider,
      String ecloudDataset) {
    if (!pluginMetadata.isMocked()) {
      String pluginTypeName = getPluginType().name();
      LOGGER.info("Starting real execution of {} plugin for ecloudDatasetId {}", pluginTypeName,
          ecloudDataset);
      String oaipmhUrl = ((OaipmhHarvestPluginMetadata) pluginMetadata).getUrl();
      String setSpec = ((OaipmhHarvestPluginMetadata) pluginMetadata).getSetSpec();
      String metadataFormat = ((OaipmhHarvestPluginMetadata) pluginMetadata).getMetadataFormat();
      Date fromDate = ((OaipmhHarvestPluginMetadata) pluginMetadata).getFromDate();
      Date untilDate = ((OaipmhHarvestPluginMetadata) pluginMetadata).getUntilDate();
      DpsTask dpsTask = new DpsTask();

      Map<InputDataType, List<String>> inputDataTypeListHashMap = new EnumMap<>(
          InputDataType.class);
      inputDataTypeListHashMap.put(InputDataType.REPOSITORY_URLS,
          Collections.singletonList(oaipmhUrl));
      dpsTask.setInputData(inputDataTypeListHashMap);

      Map<String, String> parameters = new HashMap<>();
      parameters.put("PROVIDER_ID", ecloudProvider);
      parameters.put("OUTPUT_DATA_SETS", String.format("%s/data-providers/%s/data-sets/%s",
          ecloudBaseUrl, ecloudProvider, ecloudDataset));
      dpsTask.setParameters(parameters);

      OAIPMHHarvestingDetails oaipmhHarvestingDetails = new OAIPMHHarvestingDetails();
      if (StringUtils.isNotEmpty(metadataFormat)) {
        oaipmhHarvestingDetails
            .setSchemas(new HashSet<>(Collections.singletonList(metadataFormat)));
      }
      if (StringUtils.isNotEmpty(setSpec)) {
        oaipmhHarvestingDetails.setSets(new HashSet<>(Collections.singletonList(setSpec)));
      }
      oaipmhHarvestingDetails.setDateFrom(fromDate);
      oaipmhHarvestingDetails.setDateUntil(untilDate);
      dpsTask.setHarvestingDetails(oaipmhHarvestingDetails);

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
