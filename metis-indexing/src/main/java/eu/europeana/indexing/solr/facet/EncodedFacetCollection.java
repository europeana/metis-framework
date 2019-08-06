package eu.europeana.indexing.solr.facet;

import eu.europeana.indexing.solr.facet.value.MediaTypeEncoding;
import eu.europeana.indexing.utils.WebResourceWrapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This enum contains the various facet encodings for the different media types.
 *
 * @author jochen
 */
enum EncodedFacetCollection {

  /**
   * Audio (sound only)
   **/
  AUDIO(MediaTypeEncoding.AUDIO, EncodedFacet.MIME_TYPE, EncodedFacet.AUDIO_QUALITY,
      EncodedFacet.AUDIO_DURATION),

  /**
   * Video
   **/
  VIDEO(MediaTypeEncoding.VIDEO, EncodedFacet.MIME_TYPE, EncodedFacet.VIDEO_QUALITY,
      EncodedFacet.VIDEO_DURATION),

  /**
   * Images
   **/
  IMAGE(MediaTypeEncoding.IMAGE, EncodedFacet.MIME_TYPE, EncodedFacet.IMAGE_SIZE,
      EncodedFacet.IMAGE_COLOR_SPACE, EncodedFacet.IMAGE_ASPECT_RATIO,
      EncodedFacet.IMAGE_COLOR_ENCODING),

  /**
   * Text
   **/
  TEXT(MediaTypeEncoding.TEXT, EncodedFacet.MIME_TYPE);

  private static final Map<MediaTypeEncoding, EncodedFacetCollection> ENCODERS_BY_MEDIA_TYPE = Arrays
      .stream(EncodedFacetCollection.values())
      .collect(Collectors.toMap(EncodedFacetCollection::getMediaType, Function.identity()));

  private final MediaTypeEncoding mediaType;
  private final Set<EncodedFacet> facets;

  /**
   * Constructor.
   *
   * @param value The value of the media type.
   * @param facets The facets that matter for the media type. Note: this should NOT include the
   * media type facet {@link EncodedFacet#MEDIA_TYPE} as it should receive a special treatment.
   */
  EncodedFacetCollection(final MediaTypeEncoding value, EncodedFacet... facets) {
    this.mediaType = value;
    this.facets = Stream.of(facets).collect(Collectors.toSet());
  }

  /**
   * @return The media type.
   */
  MediaTypeEncoding getMediaType() {
    return mediaType;
  }

  /**
   * @return The facets that are to be applied to web resources of this type.
   */
  Set<EncodedFacet> getFacets() {
    return Collections.unmodifiableSet(facets);
  }

  /**
   * @param webResource The resource of which to encode the facet values
   * @return The collection of applicable facets, or null if no collection is applicable.
   */
  static EncodedFacetCollection get(WebResourceWrapper webResource) {
    return ENCODERS_BY_MEDIA_TYPE.get(MediaTypeEncoding.categorizeMediaType(webResource));
  }
}
