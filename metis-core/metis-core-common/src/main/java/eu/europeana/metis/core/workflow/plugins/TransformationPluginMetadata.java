package eu.europeana.metis.core.workflow.plugins;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-29
 */
public class TransformationPluginMetadata extends AbstractMetisPluginMetadata {

  private static final PluginType pluginType = PluginType.TRANSFORMATION;
  private String xsltUrl;
  private boolean customXslt;
  private String datasetId;
  private String datasetName;
  private String country;
  private String language;

  public TransformationPluginMetadata() {
    //Required for json serialization
  }

  @Override
  public PluginType getPluginType() {
    return pluginType;
  }

  public boolean isCustomXslt() {
    return customXslt;
  }

  public void setCustomXslt(boolean customXslt) {
    this.customXslt = customXslt;
  }

  public String getXsltUrl() {
    return xsltUrl;
  }

  public void setXsltUrl(String xsltUrl) {
    this.xsltUrl = xsltUrl;
  }

  public String getDatasetId() {
    return datasetId;
  }

  public void setDatasetId(String datasetId) {
    this.datasetId = datasetId;
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
