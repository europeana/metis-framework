package eu.europeana.indexing.utils;

import eu.europeana.indexing.solr.EdmLabel;

public enum SolrTier {

  METADATA_TIER_0(RdfTier.METADATA_TIER_0, EdmLabel.METADATA_TIER, "0"),
  METADATA_TIER_A(RdfTier.METADATA_TIER_A, EdmLabel.METADATA_TIER, "A"),
  METADATA_TIER_B(RdfTier.METADATA_TIER_B, EdmLabel.METADATA_TIER, "B"),
  METADATA_TIER_C(RdfTier.METADATA_TIER_C, EdmLabel.METADATA_TIER, "C"),

  CONTENT_TIER_0(RdfTier.CONTENT_TIER_0, EdmLabel.CONTENT_TIER, "0"),
  CONTENT_TIER_1(RdfTier.CONTENT_TIER_1, EdmLabel.CONTENT_TIER, "1"),
  CONTENT_TIER_2(RdfTier.CONTENT_TIER_2, EdmLabel.CONTENT_TIER, "2"),
  CONTENT_TIER_3(RdfTier.CONTENT_TIER_3, EdmLabel.CONTENT_TIER, "3"),
  CONTENT_TIER_4(RdfTier.CONTENT_TIER_4, EdmLabel.CONTENT_TIER, "4");

  private final RdfTier tier;
  private final EdmLabel tierLabel;
  private final String tierValue;

  SolrTier(RdfTier tier, EdmLabel tierLabel, String tierValue) {
    this.tier = tier;
    this.tierLabel = tierLabel;
    this.tierValue = tierValue;
  }

  public RdfTier getTier() {
    return tier;
  }

  public EdmLabel getTierLabel() {
    return tierLabel;
  }

  public String getTierValue() {
    return tierValue;
  }
}
