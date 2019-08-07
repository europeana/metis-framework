package eu.europeana.indexing.solr.facet;

import eu.europeana.indexing.solr.facet.value.AudioDuration;
import eu.europeana.indexing.solr.facet.value.AudioQuality;
import eu.europeana.indexing.solr.facet.value.FacetValue;
import eu.europeana.indexing.solr.facet.value.ImageAspectRatio;
import eu.europeana.indexing.solr.facet.value.ImageColorEncoding;
import eu.europeana.indexing.solr.facet.value.ImageColorSpace;
import eu.europeana.indexing.solr.facet.value.ImageSize;
import eu.europeana.indexing.solr.facet.value.MimeTypeEncoding;
import eu.europeana.indexing.solr.facet.value.VideoDuration;
import eu.europeana.indexing.solr.facet.value.VideoQuality;
import eu.europeana.indexing.utils.SetUtils;
import eu.europeana.indexing.utils.WebResourceWrapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class provides functionality to extract the facet values from web resources and combine them
 * into facet value and/or filter codes that may be added to the web resource's persistence and thus
 * allow categorizing, filtering and searching them based on the facets' values.
 *
 * @author jochen
 */
public class FacetEncoder {

  /**
   * <p>
   * This method returns all possible combinations of the facet value codes: each facet's value(s)
   * for the given web resource will be collected and combined ('or'-ed) so that the web resource
   * may be queried on any combination of facet codes (as long as this combination consists of codes
   * from different facets).
   * </p>
   * <p>
   * As an example: suppose the web resource has values a1 and a2 for facet a, and b1 for facet b.
   * Then this method will return the following six possible combinations:
   * <ol>
   * <li>0 (the empty value)</li>
   * <li>a1</li>
   * <li>a2</li>
   * <li>b1</li>
   * <li>a1 | b1</li>
   * <li>a2 | b1</li>
   * </ol>
   * So it will not return [a1 | a2] or [a1 | a2 | b1] as this would combine multiple codes for the
   * same facets.
   * </p>
   * <p>
   * Note that all resulting codes will be shifted to the right position and will also have the bits
   * set that mark the media type (see {@link EncodedFacetCollection}).
   * </p>
   *
   * @param webResource The web resource for which to retrieve the facet codes.
   * @return The set of facet codes.
   */
  public final Set<Integer> getFacetFilterCodes(WebResourceWrapper webResource) {

    // Get and check the media type.
    final EncodedFacetCollection facets = EncodedFacetCollection.get(webResource);
    if (facets == null) {
      return Collections.emptySet();
    }

    // Get all the individual codes from all the facets.
    final List<Set<Integer>> codes = facets.getFacets().stream()
        .map(facet -> ((EncodedFacet<?>) facet).encodeValues(webResource))
        .filter(set -> !set.isEmpty()).collect(Collectors.toList());

    // Find all the combinations.
    return getFacetFilterCodes(getShiftedMediaTypeCode(facets), codes);
  }

  /**
   * This method returns all possible combinations of the facet value codes for audio content. See
   * {@link #getFacetFilterCodes(WebResourceWrapper)} for the details.
   *
   * @param mimeTypes The mime types of the audio content.
   * @param audioQualities The audio quality values of the audio content.
   * @param audioDurations The audio duration values of the audio content.
   * @return The set of facet codes.
   */
  public final Set<Integer> getAudioFacetFilterCodes(Set<MimeTypeEncoding> mimeTypes,
      Set<AudioQuality> audioQualities, Set<AudioDuration> audioDurations) {
    final List<Set<Integer>> codes = Arrays.asList(
        compileIntegerSet(EncodedFacet.MIME_TYPE, mimeTypes),
        compileIntegerSet(EncodedFacet.AUDIO_QUALITY, audioQualities),
        compileIntegerSet(EncodedFacet.AUDIO_DURATION, audioDurations));
    return getFacetFilterCodes(getShiftedMediaTypeCode(EncodedFacetCollection.AUDIO), codes);
  }

  /**
   * This method returns all possible combinations of the facet value codes for video content. See
   * {@link #getFacetFilterCodes(WebResourceWrapper)} for the details.
   *
   * @param mimeTypes The mime types of the video content.
   * @param videoQualities The video quality values of the video content.
   * @param videoDurations The video duration values of the video content.
   * @return The set of facet codes.
   */
  public final Set<Integer> getVideoFacetFilterCodes(Set<MimeTypeEncoding> mimeTypes,
      Set<VideoQuality> videoQualities, Set<VideoDuration> videoDurations) {
    final List<Set<Integer>> codes = Arrays.asList(
        compileIntegerSet(EncodedFacet.MIME_TYPE, mimeTypes),
        compileIntegerSet(EncodedFacet.VIDEO_QUALITY, videoQualities),
        compileIntegerSet(EncodedFacet.VIDEO_DURATION, videoDurations));
    return getFacetFilterCodes(getShiftedMediaTypeCode(EncodedFacetCollection.VIDEO), codes);
  }

  /**
   * This method returns all possible combinations of the facet value codes for image content. See
   * {@link #getFacetFilterCodes(WebResourceWrapper)} for the details.
   *
   * @param mimeTypes The mime types of the image content.
   * @param imageSizes The image size values of the image content.
   * @param imageColorSpaces The color space values of the image content.
   * @param imageAspectRatios The aspect ratio values of the image content.
   * @param imageColorEncodings The color encoding values of the image content.
   * @return The set of facet codes.
   */
  public final Set<Integer> getImageFacetFilterCodes(Set<MimeTypeEncoding> mimeTypes,
      Set<ImageSize> imageSizes, Set<ImageColorSpace> imageColorSpaces,
      Set<ImageAspectRatio> imageAspectRatios, Set<ImageColorEncoding> imageColorEncodings) {
    final List<Set<Integer>> codes = Arrays.asList(
        compileIntegerSet(EncodedFacet.MIME_TYPE, mimeTypes),
        compileIntegerSet(EncodedFacet.IMAGE_SIZE, imageSizes),
        compileIntegerSet(EncodedFacet.IMAGE_COLOR_SPACE, imageColorSpaces),
        compileIntegerSet(EncodedFacet.IMAGE_ASPECT_RATIO, imageAspectRatios),
        compileIntegerSet(EncodedFacet.IMAGE_COLOR_ENCODING, imageColorEncodings));
    return getFacetFilterCodes(getShiftedMediaTypeCode(EncodedFacetCollection.IMAGE), codes);
  }

  /**
   * This method returns all possible combinations of the facet value codes for text content. See
   * {@link #getFacetFilterCodes(WebResourceWrapper)} for the details.
   *
   * @param mimeTypes The mime types of the text content.
   * @return The set of facet codes.
   */
  public final Set<Integer> getTextFacetFilterCodes(Set<MimeTypeEncoding> mimeTypes) {
    final List<Set<Integer>> codes = Collections.singletonList(
        compileIntegerSet(EncodedFacet.MIME_TYPE, mimeTypes));
    return getFacetFilterCodes(getShiftedMediaTypeCode(EncodedFacetCollection.TEXT), codes);
  }

  private static <T extends Enum<T> & FacetValue> Set<Integer> compileIntegerSet(
      EncodedFacet<T> encodedFacet, Set<T> values) {
    if (values == null) {
      return Collections.emptySet();
    }
    return values.stream().filter(Objects::nonNull).map(encodedFacet::encodeValue)
        .collect(Collectors.toSet());
  }

  private Set<Integer> getFacetFilterCodes(int shiftedMediaTypeCode, List<Set<Integer>> codes) {
    return SetUtils.generateCombinations(codes, shiftedMediaTypeCode,
        (combination, code) -> combination | code);
  }

  /**
   * <p>
   * This method returns all the web resource's facet value codes: each facet's value value(s) for
   * the given web resource will be collected and returned, so that they may be used to list and
   * search through the facet values of the web resource.
   * </p>
   * <p>
   * As an example: suppose the web resource has values a1 and a2 for facet a, and b1 for facet b.
   * Then this method will return the following three codes:
   * <ol>
   * <li>a1</li>
   * <li>a2</li>
   * <li>b1</li>
   * </ol>
   * As opposed to {@link #getFacetFilterCodes(WebResourceWrapper)}, this method returns only the
   * individual codes, not any combination of them. As such, this result will be a subset of the
   * result of {@link #getFacetFilterCodes(WebResourceWrapper)}.
   * </p>
   * <p>
   * Note that all resulting codes will be shifted to the right position and will also have the bits
   * set that mark the media type (see {@link EncodedFacetCollection}).
   * </p>
   *
   * @param webResource The web resource for which to retrieve the facet codes.
   * @return The set of facet codes.
   */
  public final Set<Integer> getFacetValueCodes(WebResourceWrapper webResource) {

    // Get and check the media type.
    final EncodedFacetCollection facets = EncodedFacetCollection.get(webResource);
    if (facets == null) {
      return Collections.emptySet();
    }

    // Get all the individual codes from all the facets and make sure there is always the media type
    // value 'or'-ed into them.
    final int shiftedMediaTypeCode = getShiftedMediaTypeCode(facets);
    return facets.getFacets().stream()
        .map(facet -> ((EncodedFacet<?>) facet).encodeValues(webResource)).flatMap(Set::stream)
        .map(code -> shiftedMediaTypeCode | code).collect(Collectors.toSet());
  }

  private static int getShiftedMediaTypeCode(EncodedFacetCollection encoder) {
    return EncodedFacet.MEDIA_TYPE.encodeValue(encoder.getMediaType());
  }
}
