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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class contains all supported facets, including the functionality to evaluate them and shift
 * them into their assigned position.
 *
 * @param <T> The type of the facet value that this encoded facet represents.
 */
public final class EncodedFacet<T extends Enum<T> & FacetValue> {

  public static final EncodedFacet<MediaTypeEncoding> MEDIA_TYPE = new EncodedFacet<>(25, 3,
      MediaTypeEncoding::categorizeMediaType, MediaTypeEncoding.class);

  public static final EncodedFacet<MimeTypeEncoding> MIME_TYPE = new EncodedFacet<>(15, 10,
      MimeTypeEncoding::categorizeMimeType, MimeTypeEncoding.class);

  public static final EncodedFacet<ImageSize> IMAGE_SIZE = new EncodedFacet<>(12, 3,
      ImageSize::categorizeImageSize, ImageSize.class);

  public static final EncodedFacet<ImageColorSpace> IMAGE_COLOR_SPACE = new EncodedFacet<>(10, 2,
      ImageColorSpace::categorizeImageColorSpace, ImageColorSpace.class);

  public static final EncodedFacet<ImageAspectRatio> IMAGE_ASPECT_RATIO = new EncodedFacet<>(8, 2,
      ImageAspectRatio::categorizeImageAspectRatio, ImageAspectRatio.class);

  public static final EncodedFacet<ImageColorEncoding> IMAGE_COLOR_ENCODING = new EncodedFacet<>(0,
      8, ImageColorEncoding.class, ImageColorEncoding::categorizeImageColors);

  public static final EncodedFacet<AudioQuality> AUDIO_QUALITY = new EncodedFacet<>(13, 2,
      AudioQuality::categorizeAudioQuality, AudioQuality.class);

  public static final EncodedFacet<AudioDuration> AUDIO_DURATION = new EncodedFacet<>(10, 3,
      AudioDuration::categorizeAudioDuration, AudioDuration.class);

  public static final EncodedFacet<VideoQuality> VIDEO_QUALITY = new EncodedFacet<>(13, 2,
      VideoQuality::categorizeVideoQuality, VideoQuality.class);

  public static final EncodedFacet<VideoDuration> VIDEO_DURATION = new EncodedFacet<>(10, 3,
      VideoDuration::categorizeVideoDuration, VideoDuration.class);

  private final int bitPosition;
  private final int numberOfBits;
  private final Function<WebResourceWrapper, Set<T>> resourceCategorizer;
  private final Map<Integer, T> codeToValueMap;

  private EncodedFacet(final int bitPosition, final int numberOfBits, Class<T> valueType,
      Function<WebResourceWrapper, Set<T>> resourceCategorizer) {
    this.bitPosition = bitPosition;
    this.numberOfBits = numberOfBits;
    this.resourceCategorizer = resourceCategorizer;
    this.codeToValueMap = EnumSet.allOf(valueType).stream()
        .collect(Collectors.toMap(this::getCodeFromValue, Function.identity()));
  }

  private EncodedFacet(final int bitPosition, final int numberOfBits,
      Function<WebResourceWrapper, T> resourceCategorizer, Class<T> valueType) {
    this(bitPosition, numberOfBits, valueType,
        resourceCategorizer.andThen(value -> toSet(value, valueType)));
  }

  /**
   * This method gets the code from the value. It can be used as an object reference. Note that we
   * should not replace this by the method reference <code>T::getCode</code> because of a bug in
   * Java version 8 and 9:
   * <ul>
   * <li> <a>https://bugs.java.com/bugdatabase/view_bug.do?bug_id=8141508</a> </li>
   * <li> <a>https://bugs.java.com/bugdatabase/view_bug.do?bug_id=8142476</a> </li>
   * </ul>
   * And the equivalent bugs in openJDK:
   * <ul>
   * <li> <a>https://bugs.openjdk.java.net/browse/JDK-8141508</a> </li>
   * <li> <a>https://bugs.openjdk.java.net/browse/JDK-8142476</a> </li>
   * </ul>
   * We can get rid of this workaround when we upgrade to java 9 (after a certain version) or
   * higher.
   *
   * TODO JV once we upgrade to a higher java version, remove this workaround.
   *
   * @param value The value to get the code from.
   * @return The code.
   */
  private int getCodeFromValue(T value) {
    return value.getCode();
  }

  private static <T extends Enum<T> & FacetValue> Set<T> toSet(T value, Class<T> valueType) {
    return Optional.ofNullable(value).map(EnumSet::of).orElseGet(() -> EnumSet.noneOf(valueType));
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
   * Shift the code of this facet value according to the rules of this facet.
   *
   * @param value The value to shift.
   * @return The shifted value.
   * @throws IllegalArgumentException if the value's code falls outside the permitted interval for
   * this facet.
   */
  int encodeValue(T value) {
    final int code = value.getCode();
    if (code < 0 || code > getMaxValue()) {
      throw new IllegalArgumentException("The input does not fit in this facet's interval. ");
    }
    return code << bitPosition;
  }

  /**
   * Evaluate the facet for the given web resource and return the result. The result consists of a
   * number of codes that are all shifted according to this facet's rules.
   *
   * @param webResource The web resource to evaluate this facet on.
   * @return The codes. Is not null, but may be empty.
   */
  Set<Integer> encodeValues(WebResourceWrapper webResource) {
    return resourceCategorizer.apply(webResource).stream().map(this::encodeValue)
        .collect(Collectors.toSet());
  }

  /**
   * Determines the facet's bit mask based on the rules of this facet.
   *
   * @return The maximum value for this facet.
   */
  int getBitMask() {
    return ((1 << numberOfBits) - 1) << bitPosition;
  }

  /**
   * Extract the value code from an encoded integer (that could also contain other values). This
   * method isolates and shifts the component for this facet according to this facet's rules.
   *
   * @param code The encoded integer.
   * @return The value code (equivalent to {@link FacetValue#getCode()}).
   */
  int extractValueCode(int code) {
    return (code & getBitMask()) >> bitPosition;
  }

  /**
   * Extract the value from an encoded integer (that could also contain other values). This method
   * isolates and shifts the component for this facet according to this facet's rules and matches it
   * with the facet value.
   *
   * @param code The encoded integer.
   * @return The value.
   */
  public T decodeValue(int code) {
    return this.codeToValueMap.get(extractValueCode(code));
  }
}
