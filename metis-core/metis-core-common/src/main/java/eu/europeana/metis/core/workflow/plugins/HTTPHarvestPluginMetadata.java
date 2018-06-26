package eu.europeana.metis.core.workflow.plugins;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-29
 */
public class HTTPHarvestPluginMetadata extends AbstractMetisPluginMetadata {
  private static final PluginType pluginType = PluginType.HTTP_HARVEST;
  private String url;
  private String user;
  private String password;
  private String datasetId;
  //If false, it indicates that the ProvidedCHO rdf:about should be used to set the identifier for ECloud
  private boolean useDefaultIdentifiers = false;

  public HTTPHarvestPluginMetadata() {
    //Required for json serialization
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getDatasetId() {
    return datasetId;
  }

  public void setDatasetId(String datasetId) {
    this.datasetId = datasetId;
  }

  public boolean isUseDefaultIdentifiers() {
    return useDefaultIdentifiers;
  }

  public void setUseDefaultIdentifiers(boolean useDefaultIdentifiers) {
    this.useDefaultIdentifiers = useDefaultIdentifiers;
  }

  @Override
  public PluginType getPluginType() {
    return pluginType;
  }

}
