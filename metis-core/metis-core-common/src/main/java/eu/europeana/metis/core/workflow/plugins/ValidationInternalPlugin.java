package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-29
 */
public class ValidationInternalPlugin extends AbstractMetisPlugin {

  private final String topologyName = Topology.VALIDATION.getTopologyName();

  /**
   * Zero argument constructor that initializes the {@link #pluginType} corresponding to the plugin.
   */
  ValidationInternalPlugin() {
    //Required for json serialization
    super(PluginType.VALIDATION_INTERNAL);
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>Initializes the {@link #pluginType} as well.</p>
   *
   * @param pluginMetadata should be {@link ValidationInternalPluginMetadata}
   */
  ValidationInternalPlugin(AbstractMetisPluginMetadata pluginMetadata) {
    super(PluginType.VALIDATION_INTERNAL, pluginMetadata);
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
    String urlOfSchemasZip = ((ValidationInternalPluginMetadata) getPluginMetadata())
        .getUrlOfSchemasZip();
    String schemaRootPath = ((ValidationInternalPluginMetadata) getPluginMetadata())
        .getSchemaRootPath();
    String schematronRootPath = ((ValidationInternalPluginMetadata) getPluginMetadata())
        .getSchematronRootPath();
    Map<String, String> parameters = new HashMap<>();
    parameters.put("SCHEMA_NAME", urlOfSchemasZip);
    parameters.put("ROOT_LOCATION", schemaRootPath);
    parameters.put("SCHEMATRON_LOCATION", schematronRootPath);
    return createDpsTaskForProcessPlugin(parameters, ecloudBaseUrl, ecloudProvider, ecloudDataset);
  }
}
