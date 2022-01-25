package eu.europeana.indexing.tiers.view;

import eu.europeana.indexing.tiers.model.Tier;

public class RecordTierCalculationSummary {

  private String europeanaRecordId;
  private String providerRecordId;
  private Tier contentTier;
  private Tier metadataTier;
  private String portalRecordLink;

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

  public Tier getContentTier() {
    return contentTier;
  }

  public void setContentTier(Tier contentTier) {
    this.contentTier = contentTier;
  }

  public Tier getMetadataTier() {
    return metadataTier;
  }

  public void setMetadataTier(Tier metadataTier) {
    this.metadataTier = metadataTier;
  }

  public String getPortalRecordLink() {
    return portalRecordLink;
  }

  public void setPortalRecordLink(String portalRecordLink) {
    this.portalRecordLink = portalRecordLink;
  }
}
