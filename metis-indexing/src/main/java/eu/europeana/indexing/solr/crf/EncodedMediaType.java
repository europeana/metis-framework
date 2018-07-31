package eu.europeana.indexing.solr.crf;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class contains the types that are supported as technical metadata within records, along with
 * the facets that are to be applied to web resources of the given type.
 * 
 * @author jochen
 *
 */
public enum EncodedMediaType {

  /** Audio (sound only) **/
  AUDIO(2, TechnicalFacet.MIME_TYPE, TechnicalFacet.SOUND_QUALITY, TechnicalFacet.SOUND_DURATION),

  /** Video **/
  VIDEO(3, TechnicalFacet.MIME_TYPE, TechnicalFacet.VIDEO_QUALITY, TechnicalFacet.VIDEO_DURATION),

  /** Images **/
  IMAGE(1, TechnicalFacet.MIME_TYPE, TechnicalFacet.IMAGE_SIZE, TechnicalFacet.IMAGE_COLOUR_SPACE,
      TechnicalFacet.IMAGE_ASPECT_RATIO, TechnicalFacet.IMAGE_COLOUR_PALETTE),

  /** Text **/
  TEXT(4, TechnicalFacet.MIME_TYPE),

  /** Unknown type: not supported **/
  OTHER(0);

  private final int value;
  private final Set<TechnicalFacet> facets;

  /**
   * Constructor.
   * 
   * @param value The int value of the media type.
   * @param facets The facets that matter for the media type. Note: this should NOT include the
   *        media type facet {@link TechnicalFacet#MEDIA_TYPE} as it should receive a special
   *        treatment.
   */
  EncodedMediaType(final int value, TechnicalFacet... facets) {
    this.value = value;
    this.facets = Stream.of(facets).collect(Collectors.toSet());
  }

  /**
   * Codifies the given media type (but doesn't shift the code).
   * 
   * @return The integer representation of this media type.
   */
  public int getCode() {
    return value;
  }

  /**
   * @return The facets that are to be applied to web resources of this type.
   */
  public Set<TechnicalFacet> getFacets() {
    return Collections.unmodifiableSet(facets);
  }
}
