package eu.europeana.indexing.solr.crf;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import eu.europeana.corelib.definitions.jibx.WebResourceType;

public enum TechnicalFacet {
  
  MEDIA_TYPE(25, 3, resource -> {
    throw new UnsupportedOperationException();
  }),

  MIME_TYPE(15, 10, TechnicalFacetUtils::getMimeTypeCode),

  IMAGE_SIZE(12, 3, TechnicalFacetUtils::getImageSizeCode),
  IMAGE_COLOUR_SPACE(10, 2, TechnicalFacetUtils::getImageColorSpaceCode),
  IMAGE_ASPECT_RATIO(8, 2, TechnicalFacetUtils::getImageAspectRatioCode),
  IMAGE_COLOUR_PALETTE(0, 8, TechnicalFacetUtils::getImageColorPalette),

  SOUND_QUALITY(13, 2, TechnicalFacetUtils::getAudioQualityCode),
  VIDEO_QUALITY(13, 2, TechnicalFacetUtils::getVideoQualityCode),

  SOUND_DURATION(10, 3, TechnicalFacetUtils::getAudioDurationCode),
  VIDEO_DURATION(10, 3, TechnicalFacetUtils::getVideoDurationCode);

  private final int bitPos;
  private final int numOfBits;
  private final Function<WebResourceType, Set<Integer>> facetExtractor;

  private TechnicalFacet(final int bitPos, final int numOfBits,
      Function<WebResourceType, Set<Integer>> facetExtractor) {
    this.bitPos = bitPos;
    this.numOfBits = numOfBits;
    this.facetExtractor = facetExtractor;
  }

  // TODO JOCHEN should become private method?
  public int shift(int value) {

    // Check that the input fits in the required segment length.
    final int mask = ((1 << numOfBits) - 1);
    if (value != (value & mask)) {
      throw new IllegalArgumentException("The input does not fit in this facet's segment length. ");
    }

    // Shift and return.
    return value << bitPos;
  }

  public Set<Integer> evaluateAndShift(WebResourceType webResource) {
    return facetExtractor.apply(webResource).stream().map(this::shift).collect(Collectors.toSet());
  }
}
