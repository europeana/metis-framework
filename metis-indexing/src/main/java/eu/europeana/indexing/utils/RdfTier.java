package eu.europeana.indexing.utils;

import eu.europeana.indexing.solr.EdmLabel;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.model.Tier;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This enum lists the content and metadata tiers that exist.
 */
public enum RdfTier {

  METADATA_TIER_0("http://www.europeana.eu/schemas/epf/metadataTier0", MetadataTier.T0, EdmLabel.METADATA_TIER),
  METADATA_TIER_A("http://www.europeana.eu/schemas/epf/metadataTierA", MetadataTier.TA, EdmLabel.METADATA_TIER),
  METADATA_TIER_B("http://www.europeana.eu/schemas/epf/metadataTierB", MetadataTier.TB, EdmLabel.METADATA_TIER),
  METADATA_TIER_C("http://www.europeana.eu/schemas/epf/metadataTierC", MetadataTier.TC, EdmLabel.METADATA_TIER),

  CONTENT_TIER_0("http://www.europeana.eu/schemas/epf/contentTier0", MediaTier.T0, EdmLabel.CONTENT_TIER),
  CONTENT_TIER_1("http://www.europeana.eu/schemas/epf/contentTier1", MediaTier.T1, EdmLabel.CONTENT_TIER),
  CONTENT_TIER_2("http://www.europeana.eu/schemas/epf/contentTier2", MediaTier.T2, EdmLabel.CONTENT_TIER),
  CONTENT_TIER_3("http://www.europeana.eu/schemas/epf/contentTier3", MediaTier.T3, EdmLabel.CONTENT_TIER),
  CONTENT_TIER_4("http://www.europeana.eu/schemas/epf/contentTier4", MediaTier.T4, EdmLabel.CONTENT_TIER);

  private final String uri;
  private final Tier tier;
  private final EdmLabel edmLabel;

  RdfTier(String uri, Tier tier, EdmLabel edmLabel) {
    this.uri = uri;
    this.tier = tier;
    this.edmLabel = edmLabel;
  }

  public String getUri() {
    return uri;
  }

  public Set<String> getCompetingUris() {
    return Stream.of(values())
                 .filter(value -> value.edmLabel == this.edmLabel)
                 .map(RdfTier::getUri)
                 .collect(Collectors.toSet());
  }

  public Tier getTier() {
    return tier;
  }

  public EdmLabel getEdmLabel() {
    return edmLabel;
  }
}
