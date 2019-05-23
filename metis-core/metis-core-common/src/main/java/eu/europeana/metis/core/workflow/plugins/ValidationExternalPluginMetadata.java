package eu.europeana.metis.core.workflow.plugins;

/**
 * Validation External Plugin Metadata.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-29
 */
public class ValidationExternalPluginMetadata extends AbstractExecutablePluginMetadata {

  private static final ExecutablePluginType pluginType = ExecutablePluginType.VALIDATION_EXTERNAL;
  private String urlOfSchemasZip;
  private String schemaRootPath;
  private String schematronRootPath;

  public ValidationExternalPluginMetadata() {
    //Required for json serialization
  }

  @Override
  public ExecutablePluginType getExecutablePluginType() {
    return pluginType;
  }

  public String getUrlOfSchemasZip() {
    return urlOfSchemasZip;
  }

  public void setUrlOfSchemasZip(String urlOfSchemasZip) {
    this.urlOfSchemasZip = urlOfSchemasZip;
  }

  public String getSchemaRootPath() {
    return schemaRootPath;
  }

  public void setSchemaRootPath(String schemaRootPath) {
    this.schemaRootPath = schemaRootPath;
  }

  public String getSchematronRootPath() {
    return schematronRootPath;
  }

  public void setSchematronRootPath(String schematronRootPath) {
    this.schematronRootPath = schematronRootPath;
  }
}
