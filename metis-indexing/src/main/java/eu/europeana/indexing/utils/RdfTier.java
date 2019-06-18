package eu.europeana.indexing.utils;

import eu.europeana.corelib.definitions.edm.entity.QualityAnnotation;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This enum lists the content and metadata tiers that exist.
 */
public enum RdfTier {

  METADATA_TIER_0("http://www.europeana.eu/schemas/epf/metadataTier0"),
  METADATA_TIER_A("http://www.europeana.eu/schemas/epf/metadataTierA"),
  METADATA_TIER_B("http://www.europeana.eu/schemas/epf/metadataTierB"),
  METADATA_TIER_C("http://www.europeana.eu/schemas/epf/metadataTierC"),

  CONTENT_TIER_0("http://www.europeana.eu/schemas/epf/contentTier0"),
  CONTENT_TIER_1("http://www.europeana.eu/schemas/epf/contentTier1"),
  CONTENT_TIER_2("http://www.europeana.eu/schemas/epf/contentTier2"),
  CONTENT_TIER_3("http://www.europeana.eu/schemas/epf/contentTier3"),
  CONTENT_TIER_4("http://www.europeana.eu/schemas/epf/contentTier4");

  private static Map<String, RdfTier> tiersByUri = Collections.unmodifiableMap(
      Stream.of(RdfTier.values()).collect(Collectors.toMap(RdfTier::getUri, Function.identity())));

  private final String uri;

  RdfTier(String uri) {
    this.uri = uri;
  }

  public String getUri() {
    return uri;
  }

  /**
   * Find the tier represented by the given quality annotation.
   *
   * @param annotation The annotation.
   * @return The tier, or null if the annotation does not match any tier.
   */
  public static RdfTier getTier(QualityAnnotation annotation) {
    return Optional.ofNullable(annotation).map(QualityAnnotation::getOaHasBody).map(tiersByUri::get)
        .orElse(null);
  }
}
