package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;
import java.util.Map;

/**
 * Validation External Plugin.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-24
 */
public class ValidationExternalPlugin extends
    AbstractExecutablePlugin<ValidationExternalPluginMetadata> {

  private final String topologyName = Topology.VALIDATION.getTopologyName();

  /**
   * Zero argument constructor that initializes the {@link #pluginType} corresponding to the
   * plugin.
   */
  ValidationExternalPlugin() {
    //Required for json serialization
    super(PluginType.VALIDATION_EXTERNAL);

  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>Initializes the {@link #pluginType} as well.</p>
   *
   * @param pluginMetadata The plugin metadata.
   */
  ValidationExternalPlugin(ValidationExternalPluginMetadata pluginMetadata) {
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
  DpsTask prepareDpsTask(String datasetId, DpsTaskSettings dpsTaskSettings) {
    String urlOfSchemasZip = getPluginMetadata().getUrlOfSchemasZip();
    String schemaRootPath = getPluginMetadata().getSchemaRootPath();
    String schematronRootPath = getPluginMetadata().getSchematronRootPath();
    Map<String, String> extraParameters = createParametersForValidationExternal(urlOfSchemasZip,
        schemaRootPath, schematronRootPath);
    return createDpsTaskForProcessPlugin(dpsTaskSettings, extraParameters);
  }
}
