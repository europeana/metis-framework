package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;
import eu.europeana.cloud.service.dps.OAIPMHHarvestingDetails;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * OAIPMH Harvest Plugin.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-24
 */
public class OaipmhHarvestPlugin extends AbstractExecutablePlugin<OaipmhHarvestPluginMetadata> {

  private final String topologyName = Topology.OAIPMH_HARVEST.getTopologyName();

  /**
   * Zero argument constructor that initializes the {@link #pluginType} corresponding to the
   * plugin.
   */
  public OaipmhHarvestPlugin() {
    //Required for json serialization
    super(PluginType.OAIPMH_HARVEST);
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>Initializes the {@link #pluginType} as well.</p>
   *
   * @param pluginMetadata The plugin metadata.
   */
  OaipmhHarvestPlugin(OaipmhHarvestPluginMetadata pluginMetadata) {
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
  DpsTask prepareDpsTask(String datasetId, EcloudBasePluginParameters ecloudBasePluginParameters) {
    String targetUrl = getPluginMetadata().getUrl();
    boolean useDefaultIdentifiers = getPluginMetadata().isUseDefaultIdentifiers();
    String identifierPrefixRemoval = getPluginMetadata().getIdentifierPrefixRemoval();
    Map<String, String> parameters = new HashMap<>();
    parameters.put("METIS_DATASET_ID", datasetId);
    parameters.put("USE_DEFAULT_IDENTIFIERS", String.valueOf(useDefaultIdentifiers));
    if (useDefaultIdentifiers && !StringUtils.isBlank(identifierPrefixRemoval)) {
      parameters.put("MIGRATION_IDENTIFIER_PREFIX", identifierPrefixRemoval);
    }
    DpsTask dpsTask = createDpsTaskForHarvestPlugin(ecloudBasePluginParameters, parameters, targetUrl);

    String setSpec = getPluginMetadata().getSetSpec();
    String metadataFormat = getPluginMetadata().getMetadataFormat();
    Date fromDate = getPluginMetadata().getFromDate();
    Date untilDate = getPluginMetadata().getUntilDate();

    OAIPMHHarvestingDetails oaipmhHarvestingDetails = new OAIPMHHarvestingDetails();
    if (StringUtils.isNotEmpty(metadataFormat)) {
      oaipmhHarvestingDetails.setSchema(metadataFormat);
    }
    if (StringUtils.isNotEmpty(setSpec)) {
      oaipmhHarvestingDetails.setSet(setSpec);
    }
    oaipmhHarvestingDetails.setDateFrom(fromDate);
    oaipmhHarvestingDetails.setDateUntil(untilDate);
    dpsTask.setHarvestingDetails(oaipmhHarvestingDetails);

    return dpsTask;
  }
}
