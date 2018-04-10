package eu.europeana.indexing.solr.crf;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import eu.europeana.corelib.definitions.jibx.WebResourceType;

/**
 * This enum contains all supported colors.
 */
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

  private final int bitPosition;
  private final int numberOfBits;
  private final Function<WebResourceType, Set<Integer>> facetExtractor;

  private TechnicalFacet(final int bitPosition, final int numberOfBits,
      Function<WebResourceType, Set<Integer>> facetExtractor) {
    this.bitPosition = bitPosition;
    this.numberOfBits = numberOfBits;
    this.facetExtractor = facetExtractor;
  }

  /**
   * Shift the value according to the rules of this facet.
   * 
   * @param value The value to shift.
   * @return The shifted value.
   * @throws IllegalArgumentException if the value falls outside the permitted interval for this
   *         facet.
   */
  int shift(int value) {
    if (value < 0 || value > getMaxValue()) {
      throw new IllegalArgumentException("The input does not fit in this facet's interval. ");
    }
    return value << bitPosition;
  }

  /**
   * Determines the facet's maximum permitted value based on the allowed number of bits.
   * 
   * @return The maximum value for this facet.
   */
  int getMaxValue() {
    return (1 << numberOfBits) - 1;
  }

  /**
   * Evaluate the facet for the given web resource and return the result. The result consists of a
   * number of codes that are all shifted according to this facet's rules.
   * 
   * @param webResource The web resource to evaluate this facet on.
   * @return The codes. May be empty.
   */
  public Set<Integer> evaluateAndShift(WebResourceType webResource) {
    return facetExtractor.apply(webResource).stream().map(this::shift).collect(Collectors.toSet());
  }
}
