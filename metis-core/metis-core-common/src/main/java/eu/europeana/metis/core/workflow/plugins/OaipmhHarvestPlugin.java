package eu.europeana.metis.core.workflow.plugins;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import org.apache.commons.lang3.StringUtils;
import eu.europeana.cloud.service.dps.DpsTask;
import eu.europeana.cloud.service.dps.OAIPMHHarvestingDetails;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-24
 */
public class OaipmhHarvestPlugin extends AbstractMetisPlugin {

  private final String topologyName = Topology.OAIPMH_HARVEST.getTopologyName();

  /**
   * Zero argument constructor that initializes the {@link #pluginType} corresponding to the plugin.
   */
  OaipmhHarvestPlugin() {
    //Required for json serialization
    super(PluginType.OAIPMH_HARVEST);
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>Initializes the {@link #pluginType} as well.</p>
   *
   * @param pluginMetadata should be {@link OaipmhHarvestPluginMetadata}
   */
  OaipmhHarvestPlugin(AbstractMetisPluginMetadata pluginMetadata) {
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
    
    String targetUrl = ((OaipmhHarvestPluginMetadata) getPluginMetadata()).getUrl();
    DpsTask dpsTask = createDpsTaskForHarvestPlugin(targetUrl, ecloudBaseUrl, ecloudProvider, ecloudDataset);

    String setSpec = ((OaipmhHarvestPluginMetadata) getPluginMetadata()).getSetSpec();
    String metadataFormat = ((OaipmhHarvestPluginMetadata) getPluginMetadata()).getMetadataFormat();
    Date fromDate = ((OaipmhHarvestPluginMetadata) getPluginMetadata()).getFromDate();
    Date untilDate = ((OaipmhHarvestPluginMetadata) getPluginMetadata()).getUntilDate();

    OAIPMHHarvestingDetails oaipmhHarvestingDetails = new OAIPMHHarvestingDetails();
    if (StringUtils.isNotEmpty(metadataFormat)) {
      oaipmhHarvestingDetails.setSchemas(new HashSet<>(Collections.singletonList(metadataFormat)));
    }
    if (StringUtils.isNotEmpty(setSpec)) {
      oaipmhHarvestingDetails.setSets(new HashSet<>(Collections.singletonList(setSpec)));
    }
    oaipmhHarvestingDetails.setDateFrom(fromDate);
    oaipmhHarvestingDetails.setDateUntil(untilDate);
    dpsTask.setHarvestingDetails(oaipmhHarvestingDetails);
    
    return dpsTask;
  }
}
