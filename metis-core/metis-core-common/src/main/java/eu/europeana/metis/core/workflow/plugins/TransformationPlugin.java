package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;
import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.core.common.Language;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-29
 */
public class TransformationPlugin extends AbstractMetisPlugin {

  private final String topologyName = Topology.TRANSFORMATION.getTopologyName();

  /**
   * Zero argument constructor that initializes the {@link #pluginType} corresponding to the plugin.
   */
  TransformationPlugin() {
    //Required for json serialization
    super(PluginType.TRANSFORMATION);
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>Initializes the {@link #pluginType} as well.</p>
   *
   * @param pluginMetadata should be {@link TransformationPluginMetadata}
   */
  TransformationPlugin(AbstractMetisPluginMetadata pluginMetadata) {
    super(PluginType.TRANSFORMATION, pluginMetadata);
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
  public DpsTask prepareDpsTask(String ecloudBaseUrl, String ecloudProvider, String ecloudDataset) {
    String xsltUlr = ((TransformationPluginMetadata) getPluginMetadata()).getXsltUrl();
    String datasetId = ((TransformationPluginMetadata) getPluginMetadata()).getDatasetId();
    String datasetName = ((TransformationPluginMetadata) getPluginMetadata()).getDatasetName();
    Country country = ((TransformationPluginMetadata) getPluginMetadata()).getCountry();
    Language language = ((TransformationPluginMetadata) getPluginMetadata()).getLanguage();
    Map<String, String> parameters = new HashMap<>();
    parameters.put("XSLT_URL", xsltUlr);
    parameters.put("METIS_DATASET_ID", datasetId);
    parameters.put("METIS_DATASET_NAME", datasetName);
    parameters.put("METIS_DATASET_COUNTRY", country.getName());
    parameters.put("METIS_DATASET_LANGUAGE", language.name());

    return createDpsTaskForProcessPlugin(parameters, ecloudBaseUrl, ecloudProvider, ecloudDataset);
  }
}
