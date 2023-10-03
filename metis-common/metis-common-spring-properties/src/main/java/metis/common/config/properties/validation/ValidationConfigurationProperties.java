package metis.common.config.properties.validation;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Class using {@link ConfigurationProperties} loading.
 */
@ConfigurationProperties(prefix = "validation")
public class ValidationConfigurationProperties {

  private String validationExternalSchemaZip;
  private String validationExternalSchemaRoot;
  private String validationExternalSchematronRoot;
  private String validationInternalSchemaZip;
  private String validationInternalSchemaRoot;
  private String validationInternalSchematronRoot;

  public String getValidationExternalSchemaZip() {
    return validationExternalSchemaZip;
  }

  public void setValidationExternalSchemaZip(String validationExternalSchemaZip) {
    this.validationExternalSchemaZip = validationExternalSchemaZip;
  }

  public String getValidationExternalSchemaRoot() {
    return validationExternalSchemaRoot;
  }

  public void setValidationExternalSchemaRoot(String validationExternalSchemaRoot) {
    this.validationExternalSchemaRoot = validationExternalSchemaRoot;
  }

  public String getValidationExternalSchematronRoot() {
    return validationExternalSchematronRoot;
  }

  public void setValidationExternalSchematronRoot(String validationExternalSchematronRoot) {
    this.validationExternalSchematronRoot = validationExternalSchematronRoot;
  }

  public String getValidationInternalSchemaZip() {
    return validationInternalSchemaZip;
  }

  public void setValidationInternalSchemaZip(String validationInternalSchemaZip) {
    this.validationInternalSchemaZip = validationInternalSchemaZip;
  }

  public String getValidationInternalSchemaRoot() {
    return validationInternalSchemaRoot;
  }

  public void setValidationInternalSchemaRoot(String validationInternalSchemaRoot) {
    this.validationInternalSchemaRoot = validationInternalSchemaRoot;
  }

  public String getValidationInternalSchematronRoot() {
    return validationInternalSchematronRoot;
  }

  public void setValidationInternalSchematronRoot(String validationInternalSchematronRoot) {
    this.validationInternalSchematronRoot = validationInternalSchematronRoot;
  }
}
