package eu.europeana.metis.transformation.service;

/**
 * Contains the mapping between a Provider ID and its sanitized version of Europeana Id.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-06-11
 */
public class EuropeanaIdMap {

  private String providerId;
  private String europeanaId;

  public EuropeanaIdMap(String providerId, String europeanaId) {
    this.providerId = providerId;
    this.europeanaId = europeanaId;
  }

  public String getProviderId() {
    return providerId;
  }

  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }

  public String getEuropeanaId() {
    return europeanaId;
  }

  public void setEuropeanaId(String europeanaId) {
    this.europeanaId = europeanaId;
  }
}
