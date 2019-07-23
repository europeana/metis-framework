package eu.europeana.indexing.utils;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.model.Tier;

/**
 * This enum lists the content and metadata tiers that exist.
 */
public enum RdfTier {

  METADATA_TIER_0("http://www.europeana.eu/schemas/epf/metadataTier0", MetadataTier.T0),
  METADATA_TIER_A("http://www.europeana.eu/schemas/epf/metadataTierA", MetadataTier.TA),
  METADATA_TIER_B("http://www.europeana.eu/schemas/epf/metadataTierB", MetadataTier.TB),
  METADATA_TIER_C("http://www.europeana.eu/schemas/epf/metadataTierC", MetadataTier.TC),

  CONTENT_TIER_0("http://www.europeana.eu/schemas/epf/contentTier0", MediaTier.T0),
  CONTENT_TIER_1("http://www.europeana.eu/schemas/epf/contentTier1", MediaTier.T1),
  CONTENT_TIER_2("http://www.europeana.eu/schemas/epf/contentTier2", MediaTier.T2),
  CONTENT_TIER_3("http://www.europeana.eu/schemas/epf/contentTier3", MediaTier.T3),
  CONTENT_TIER_4("http://www.europeana.eu/schemas/epf/contentTier4", MediaTier.T4);

  private final String uri;
  private final String aboutSuffix;
  private final Enum<? extends Tier> tier;

  RdfTier(String uri, MediaTier tier) {
    this(uri, "#contentTier", tier);
  }

  RdfTier(String uri, MetadataTier tier) {
    this(uri, "#metadataTier", tier);
  }

  RdfTier(String uri, String aboutSuffix, Enum<? extends Tier> tier) {
    this.uri = uri;
    this.aboutSuffix = aboutSuffix;
    this.tier = tier;
  }

  public String getUri() {
    return uri;
  }

  public Enum<? extends Tier> getTier() {
    return tier;
  }

  public String getAboutSuffix() {
    return aboutSuffix;
  }
}
