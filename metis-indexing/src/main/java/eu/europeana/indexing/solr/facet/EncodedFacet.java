package eu.europeana.indexing.solr.facet;

import eu.europeana.indexing.solr.facet.value.AudioDuration;
import eu.europeana.indexing.solr.facet.value.AudioQuality;
import eu.europeana.indexing.solr.facet.value.FacetValue;
import eu.europeana.indexing.solr.facet.value.ImageAspectRatio;
import eu.europeana.indexing.solr.facet.value.ImageColorEncoding;
import eu.europeana.indexing.solr.facet.value.ImageColorSpace;
import eu.europeana.indexing.solr.facet.value.ImageSize;
import eu.europeana.indexing.solr.facet.value.MediaTypeEncoding;
import eu.europeana.indexing.solr.facet.value.MimeTypeEncoding;
import eu.europeana.indexing.solr.facet.value.VideoDuration;
import eu.europeana.indexing.solr.facet.value.VideoQuality;
import eu.europeana.indexing.utils.WebResourceWrapper;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class contains all supported facets, including the functionality to evaluate them and shift
 * them into their assigned position.
 */
final class EncodedFacet <T extends Enum<T> & FacetValue> {

  static final EncodedFacet<MediaTypeEncoding> MEDIA_TYPE = new EncodedFacet<>(25, 3,
      MediaTypeEncoding::categorizeMediaType, MediaTypeEncoding.class);

  static final EncodedFacet<MimeTypeEncoding> MIME_TYPE = new EncodedFacet<>(15, 10,
      MimeTypeEncoding::categorizeMimeType, MimeTypeEncoding.class);

  static final EncodedFacet<ImageSize> IMAGE_SIZE = new EncodedFacet<>(12, 3,
      ImageSize::categorizeImageSize, ImageSize.class);

  static final EncodedFacet<ImageColorSpace> IMAGE_COLOR_SPACE = new EncodedFacet<>(10, 2,
      ImageColorSpace::categorizeImageColorSpace, ImageColorSpace.class);

  static final EncodedFacet<ImageAspectRatio> IMAGE_ASPECT_RATIO = new EncodedFacet<>(8, 2,
      ImageAspectRatio::categorizeImageAspectRatio, ImageAspectRatio.class);

  static final EncodedFacet<ImageColorEncoding> IMAGE_COLOR_ENCODING = new EncodedFacet<>(0, 8,
      ImageColorEncoding::categorizeImageColors);

  static final EncodedFacet<AudioQuality> AUDIO_QUALITY = new EncodedFacet<>(13, 2,
      AudioQuality::categorizeAudioQuality, AudioQuality.class);

  static final EncodedFacet<AudioDuration> AUDIO_DURATION = new EncodedFacet<>(10, 3,
      AudioDuration::categorizeAudioDuration, AudioDuration.class);

  static final EncodedFacet<VideoQuality> VIDEO_QUALITY = new EncodedFacet<>(13, 2,
      VideoQuality::categorizeVideoQuality, VideoQuality.class);

  static final EncodedFacet<VideoDuration> VIDEO_DURATION = new EncodedFacet<>(10, 3,
      VideoDuration::categorizeVideoDuration, VideoDuration.class);

  private final int bitPosition;
  private final int numberOfBits;
  private final Function<WebResourceWrapper, Set<T>> resourceCategorizer;

  private EncodedFacet(final int bitPosition, final int numberOfBits,
      Function<WebResourceWrapper, Set<T>> resourceCategorizer) {
    this.bitPosition = bitPosition;
    this.numberOfBits = numberOfBits;
    this.resourceCategorizer = resourceCategorizer;
  }

  private EncodedFacet(final int bitPosition, final int numberOfBits,
      Function<WebResourceWrapper, T> resourceCategorizer, Class<T> valueType) {
    this(bitPosition, numberOfBits, resourceCategorizer.andThen(value -> toSet(value, valueType)));
  }

  private static <T extends Enum<T> & FacetValue> Set<T> toSet(T value, Class<T> valueType) {
    return Optional.ofNullable(value).map(EnumSet::of).orElseGet(() -> EnumSet.noneOf(valueType));
  }

  /**
   * Shift the code of this facet value according to the rules of this facet.
   *
   * @param value The value to shift.
   * @return The shifted value.
   * @throws IllegalArgumentException if the value's code falls outside the permitted interval for
   * this facet.
   */
  int encodeAndShift(T value) {
    final int code = value.getCode();
    if (code < 0 || code > getMaxValue()) {
      throw new IllegalArgumentException("The input does not fit in this facet's interval. ");
    }
    return code << bitPosition;
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
   * @return The codes. Is not null, but may be empty.
   */
  Set<Integer> evaluateAndShift(WebResourceWrapper webResource) {
    return resourceCategorizer.apply(webResource).stream().map(this::encodeAndShift)
        .collect(Collectors.toSet());
  }
}
