package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;
import java.util.Map;

/**
 * Validation Internal Plugin.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-29
 */
public class ValidationInternalPlugin extends
    AbstractExecutablePlugin<ValidationInternalPluginMetadata> {

  private final String topologyName = Topology.VALIDATION.getTopologyName();

  /**
   * Zero argument constructor that initializes the {@link #pluginType} corresponding to the
   * plugin.
   */
  ValidationInternalPlugin() {
    //Required for json serialization
    super(PluginType.VALIDATION_INTERNAL);
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>Initializes the {@link #pluginType} as well.</p>
   *
   * @param pluginMetadata The plugin metadata.
   */
  ValidationInternalPlugin(ValidationInternalPluginMetadata pluginMetadata) {
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
  DpsTask prepareDpsTask(String datasetId, EcloudBasePluginParameters ecloudBasePluginParameters) {
    String urlOfSchemasZip = getPluginMetadata().getUrlOfSchemasZip();
    String schemaRootPath = getPluginMetadata().getSchemaRootPath();
    String schematronRootPath = getPluginMetadata().getSchematronRootPath();
    Map<String, String> extraParameters = createParametersForValidationInternal(urlOfSchemasZip,
        schemaRootPath, schematronRootPath);
    return createDpsTaskForProcessPlugin(ecloudBasePluginParameters, extraParameters);
  }
}
