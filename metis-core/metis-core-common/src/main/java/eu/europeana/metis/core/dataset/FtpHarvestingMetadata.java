package eu.europeana.metis.core.dataset;

import eu.europeana.metis.core.common.HarvestType;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-06-01
 */
public class FtpHarvestingMetadata implements HarvestingMetadata {

  private HarvestType harvestType = HarvestType.FTP_HARVEST;
  private String url;
  private String user;
  private String password;

  @Override
  public HarvestType getHarvestType() {
    return harvestType;
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

}
