package eu.europeana.metis.core.workflow;

/**
 * This class represents extra properties that are needed for validation.
 * 
 * @author jochen
 *
 */
public class ValidationProperties {

  private final String urlOfSchemasZip;
  private final String schemaRootPath;
  private final String schematronRootPath;

  /**
   * Constructor.
   * 
   * @param urlOfSchemasZip The URL of the schemas Zip-file.
   * @param schemaRootPath The path of the root schema XSL (within the Zip-file).
   * @param schematronRootPath The path of the root Schematron XSL (within the Zip-file).
   */
  public ValidationProperties(String urlOfSchemasZip, String schemaRootPath,
      String schematronRootPath) {
    this.urlOfSchemasZip = urlOfSchemasZip;
    this.schemaRootPath = schemaRootPath;
    this.schematronRootPath = schematronRootPath;
  }

  public String getUrlOfSchemasZip() {
    return urlOfSchemasZip;
  }

  public String getSchemaRootPath() {
    return schemaRootPath;
  }

  public String getSchematronRootPath() {
    return schematronRootPath;
  }
}
