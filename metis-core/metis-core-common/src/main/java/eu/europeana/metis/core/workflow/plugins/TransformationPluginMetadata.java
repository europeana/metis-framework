package eu.europeana.metis.core.workflow.plugins;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-29
 */
public class TransformationPluginMetadata extends AbstractMetisPluginMetadata {

  private static final PluginType pluginType = PluginType.TRANSFORMATION;
  private String xsltUrl;
  private boolean customXslt = false;

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
}
