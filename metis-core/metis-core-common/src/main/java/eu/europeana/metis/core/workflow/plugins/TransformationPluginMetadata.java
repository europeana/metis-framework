package eu.europeana.metis.core.workflow.plugins;

/**
 * Transformation Plugin Metadata.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-29
 */
public class TransformationPluginMetadata extends AbstractExecutablePluginMetadata {

  private static final ExecutablePluginType pluginType = ExecutablePluginType.TRANSFORMATION;
  private String xsltId;
  private boolean customXslt;
  private String datasetName;
  private String country;
  private String language;

  public TransformationPluginMetadata() {
    //Required for json serialization
  }

  @Override
  public ExecutablePluginType getExecutablePluginType() {
    return pluginType;
  }

  public boolean isCustomXslt() {
    return customXslt;
  }

  public void setCustomXslt(boolean customXslt) {
    this.customXslt = customXslt;
  }

  public String getXsltId() {
    return xsltId;
  }

  public void setXsltId(String xsltId) {
    this.xsltId = xsltId;
  }

  public String getDatasetName() {
    return datasetName;
  }

  public void setDatasetName(String datasetName) {
    this.datasetName = datasetName;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }
}
