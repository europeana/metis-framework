package eu.europeana.indexing.utils;

import eu.europeana.indexing.solr.EdmLabel;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.model.Tier;
import java.util.Arrays;

/**
 * This enum lists the content and metadata tiers that exist.
 */
public enum RdfTier {

  /**
   * Metadata tier 0 rdf tier.
   */
  METADATA_TIER_0(MetadataTier.T0, EdmLabel.METADATA_TIER),
  /**
   * Metadata tier A rdf tier.
   */
  METADATA_TIER_A(MetadataTier.TA, EdmLabel.METADATA_TIER),
  /**
   * Metadata tier B rdf tier.
   */
  METADATA_TIER_B(MetadataTier.TB, EdmLabel.METADATA_TIER),
  /**
   * Metadata tier C rdf tier.
   */
  METADATA_TIER_C(MetadataTier.TC, EdmLabel.METADATA_TIER),

  /**
   * Content tier 0 rdf tier.
   */
  CONTENT_TIER_0(MediaTier.T0, EdmLabel.CONTENT_TIER),
  /**
   * Content tier 1 rdf tier.
   */
  CONTENT_TIER_1(MediaTier.T1, EdmLabel.CONTENT_TIER),
  /**
   * Content tier 2 rdf tier.
   */
  CONTENT_TIER_2(MediaTier.T2, EdmLabel.CONTENT_TIER),
  /**
   * Content tier 3 rdf tier.
   */
  CONTENT_TIER_3(MediaTier.T3, EdmLabel.CONTENT_TIER),
  /**
   * Content tier 4 rdf tier.
   */
  CONTENT_TIER_4(MediaTier.T4, EdmLabel.CONTENT_TIER);

  private static final String BASE_URI = "http://www.europeana.eu/schemas/epf/";
  /**
   * The constant METADATA_TIER_BASE_URI.
   */
  public static final String METADATA_TIER_BASE_URI = BASE_URI + "metadataTier";
  /**
   * The constant CONTENT_TIER_BASE_URI.
   */
  public static final String CONTENT_TIER_BASE_URI = BASE_URI + "contentTier";
  private final String uri;
  private final Tier tier;
  private final EdmLabel edmLabel;

  /**
   * Instantiates a new Rdf tier.
   *
   * @param tier the tier
   * @param edmLabel the edm label
   */
  RdfTier(Tier tier, EdmLabel edmLabel) {
    this.tier = tier;
    this.edmLabel = edmLabel;
    this.uri = generateUri(tier);
  }

  private static String generateUri(Tier tier) {
    if (tier instanceof MetadataTier) {
      return METADATA_TIER_BASE_URI + tier;
    } else if (tier instanceof MediaTier) {
      return CONTENT_TIER_BASE_URI + tier;
    } else {
      throw new IllegalArgumentException("Unknown tier type: " + tier);
    }
  }

  /**
   * Gets uri.
   *
   * @return the uri
   */
  public String getUri() {
    return uri;
  }

  /**
   * Gets tier.
   *
   * @return the tier
   */
  public Tier getTier() {
    return tier;
  }

  /**
   * Gets edm label.
   *
   * @return the edm label
   */
  public EdmLabel getEdmLabel() {
    return edmLabel;
  }

  /**
   * From uri to rdf tier.
   *
   * @param uri the uri
   * @return the rdf tier
   */
  public static RdfTier fromUri(String uri) {
    return Arrays.stream(RdfTier.values())
                 .filter( rdfTier -> rdfTier.uri.equals(uri))
                 .findFirst()
                 .orElse(null);
  }
}
