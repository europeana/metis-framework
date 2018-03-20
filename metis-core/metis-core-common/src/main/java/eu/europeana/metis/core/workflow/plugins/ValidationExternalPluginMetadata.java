package eu.europeana.metis.core.workflow.plugins;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-29
 */
public class ValidationExternalPluginMetadata extends AbstractMetisPluginMetadata {

  private static final PluginType pluginType = PluginType.VALIDATION_EXTERNAL;
  private String urlOfSchemasZip;
  private String schemaRootPath;
  private String schematronRootPath;

  public ValidationExternalPluginMetadata() {
    //Required for json serialization
  }

  @Override
  public PluginType getPluginType() {
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
