package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;
import eu.europeana.metis.utils.RestEndpoints;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Transformation Plugin.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-29
 */
public class TransformationPlugin extends AbstractExecutablePlugin<TransformationPluginMetadata> {

  private final String topologyName = Topology.TRANSFORMATION.getTopologyName();

  /**
   * Zero argument constructor that initializes the {@link #pluginType} corresponding to the
   * plugin.
   */
  TransformationPlugin() {
    //Required for json serialization
    super(PluginType.TRANSFORMATION);
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>Initializes the {@link #pluginType} as well.</p>
   *
   * @param pluginMetadata The plugin metadata.
   */
  TransformationPlugin(TransformationPluginMetadata pluginMetadata) {
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
  public DpsTask prepareDpsTask(String datasetId,
      EcloudBasePluginParameters ecloudBasePluginParameters) {
    Map<String, String> extraParameters = new HashMap<>();
    extraParameters.put("XSLT_URL", ecloudBasePluginParameters.getMetisCoreBaseUrl() + RestEndpoints
        .resolve(RestEndpoints.DATASETS_XSLT_XSLTID,
            Collections.singletonList(getPluginMetadata().getXsltId())));
    extraParameters.put("METIS_DATASET_ID", datasetId);
    extraParameters.put("METIS_DATASET_NAME", getPluginMetadata().getDatasetName());
    extraParameters.put("METIS_DATASET_COUNTRY", getPluginMetadata().getCountry());
    extraParameters.put("METIS_DATASET_LANGUAGE", getPluginMetadata().getLanguage());
    return createDpsTaskForProcessPlugin(ecloudBasePluginParameters, extraParameters);
  }
}
