package eu.europeana.metis.core.workflow.plugins;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-29
 */
public class ValidationExternalPluginMetadata extends AbstractMetisPluginMetadata {

  private final PluginType pluginType = PluginType.VALIDATION_EXTERNAL;
  private String urlOfSchemasZip;
  private String schemaRootPath;
  private String schematronRootPath;

  private String revisionNamePreviousPlugin;
  private String revisionProviderPreviousPlugin;
  private String revisionTimestampPreviousPlugin;

  public ValidationExternalPluginMetadata() {
    //Required for json serialization
  }

  public ValidationExternalPluginMetadata(String urlOfSchemasZip, String schemaRootPath,
      String schematronRootPath) {
    this.urlOfSchemasZip = urlOfSchemasZip;
    this.schemaRootPath = schemaRootPath;
    this.schematronRootPath = schematronRootPath;
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

  public String getRevisionNamePreviousPlugin() {
    return revisionNamePreviousPlugin;
  }

  public void setRevisionNamePreviousPlugin(String revisionNamePreviousPlugin) {
    this.revisionNamePreviousPlugin = revisionNamePreviousPlugin;
  }

  public String getRevisionProviderPreviousPlugin() {
    return revisionProviderPreviousPlugin;
  }

  public void setRevisionProviderPreviousPlugin(String revisionProviderPreviousPlugin) {
    this.revisionProviderPreviousPlugin = revisionProviderPreviousPlugin;
  }

  public String getRevisionTimestampPreviousPlugin() {
    return revisionTimestampPreviousPlugin;
  }

  public void setRevisionTimestampPreviousPlugin(String revisionTimestampPreviousPlugin) {
    this.revisionTimestampPreviousPlugin = revisionTimestampPreviousPlugin;
  }
}
