package eu.europeana.metis.core.workflow.plugins;

/**
 * Validation Internal Plugin Metadata.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-29
 */
public class ValidationInternalPluginMetadata extends AbstractExecutablePluginMetadata {

  private static final ExecutablePluginType pluginType = ExecutablePluginType.VALIDATION_INTERNAL;
  private String urlOfSchemasZip;
  private String schemaRootPath;
  private String schematronRootPath;

  public ValidationInternalPluginMetadata() {
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
