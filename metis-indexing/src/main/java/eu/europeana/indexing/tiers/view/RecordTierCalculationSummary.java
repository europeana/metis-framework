package eu.europeana.indexing.tiers.view;

public class RecordTierCalculationSummary {

  private String europeanaRecordId;
  private String providerRecordId;
  private String contentTier;
  private String metadataTier;
  private String portalRecordLink;
  private String harvestedRecordLink;

  public RecordTierCalculationSummary() {
  }

  public String getEuropeanaRecordId() {
    return europeanaRecordId;
  }

  public void setEuropeanaRecordId(String europeanaRecordId) {
    this.europeanaRecordId = europeanaRecordId;
  }

  public String getProviderRecordId() {
    return providerRecordId;
  }

  public void setProviderRecordId(String providerRecordId) {
    this.providerRecordId = providerRecordId;
  }

  public String getContentTier() {
    return contentTier;
  }

  public void setContentTier(String contentTier) {
    this.contentTier = contentTier;
  }

  public String getMetadataTier() {
    return metadataTier;
  }

  public void setMetadataTier(String metadataTier) {
    this.metadataTier = metadataTier;
  }

  public String getPortalRecordLink() {
    return portalRecordLink;
  }

  public void setPortalRecordLink(String portalRecordLink) {
    this.portalRecordLink = portalRecordLink;
  }

  public String getHarvestedRecordLink() {
    return harvestedRecordLink;
  }

  public void setHarvestedRecordLink(String harvestedRecordLink) {
    this.harvestedRecordLink = harvestedRecordLink;
  }
}
