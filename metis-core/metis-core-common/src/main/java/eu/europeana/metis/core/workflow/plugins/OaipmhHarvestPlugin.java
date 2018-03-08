package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;
import eu.europeana.cloud.service.dps.InputDataType;
import eu.europeana.cloud.service.dps.OAIPMHHarvestingDetails;
import eu.europeana.metis.CommonStringValues;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-24
 */
public class OaipmhHarvestPlugin extends AbstractMetisPlugin {

  private final String topologyName = Topology.OAIPMH_HARVEST.getTopologyName();

  /**
   * Zero argument constructor that initializes the {@link #pluginType} corresponding to the plugin.
   */
  public OaipmhHarvestPlugin() {
    //Required for json serialization
    super(PluginType.OAIPMH_HARVEST);
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>Initializes the {@link #pluginType} as well.</p>
   *
   * @param pluginMetadata should be {@link OaipmhHarvestPluginMetadata}
   */
  public OaipmhHarvestPlugin(AbstractMetisPluginMetadata pluginMetadata) {
    super(PluginType.OAIPMH_HARVEST, pluginMetadata);
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
  DpsTask prepareDpsTask(String ecloudBaseUrl, String ecloudProvider, String ecloudDataset) {
    String oaipmhUrl = ((OaipmhHarvestPluginMetadata) getPluginMetadata()).getUrl();
    String setSpec = ((OaipmhHarvestPluginMetadata) getPluginMetadata()).getSetSpec();
    String metadataFormat = ((OaipmhHarvestPluginMetadata) getPluginMetadata())
        .getMetadataFormat();
    Date fromDate = ((OaipmhHarvestPluginMetadata) getPluginMetadata()).getFromDate();
    Date untilDate = ((OaipmhHarvestPluginMetadata) getPluginMetadata()).getUntilDate();
    DpsTask dpsTask = new DpsTask();

    Map<InputDataType, List<String>> inputDataTypeListHashMap = new EnumMap<>(
        InputDataType.class);
    inputDataTypeListHashMap.put(InputDataType.REPOSITORY_URLS,
        Collections.singletonList(oaipmhUrl));
    dpsTask.setInputData(inputDataTypeListHashMap);

    Map<String, String> parameters = new HashMap<>();
    parameters.put("PROVIDER_ID", ecloudProvider);
    parameters.put("OUTPUT_DATA_SETS",
        String.format(CommonStringValues.S_DATA_PROVIDERS_S_DATA_SETS_S_TEMPLATE,
            ecloudBaseUrl, ecloudProvider, ecloudDataset));
    parameters.put("NEW_REPRESENTATION_NAME", getRepresentationName());
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
    dpsTask.setOutputRevision(createOutputRevisionForExecution(ecloudProvider));
    return dpsTask;
  }
}
