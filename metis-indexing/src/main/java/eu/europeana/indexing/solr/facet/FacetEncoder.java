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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class provides functionality to extract the facet values from web resources and combine them
 * into facet value and/or filter codes that may be added to the web resource's persistence and thus
 * allow categorizing, filtering and searching them based on the facets' values.
 *
 * @author jochen
 */
public class FacetEncoder {

  /**
   * This class represents a collection of facet values.
   *
   * @param <T> The type of the facet value.
   */
  public static final class FacetWithValues<T extends Enum<T> & FacetValue> {

    private final EncodedFacet<T> facet;
    private final Set<T> values;

    public FacetWithValues(EncodedFacet<T> facet, Set<T> values) {
      this.facet = facet;
      this.values = values == null ? Collections.emptySet() : new HashSet<>(values);
    }

    private Set<Integer> compileIntegerSet() {
      return values.stream().filter(Objects::nonNull).map(facet::encodeValue)
              .collect(Collectors.toSet());
    }
  }

  /**
   * <p>
   * This method returns all possible search combinations of the facet search codes: each facet's
   * value(s) for the given web resource will be collected and combined ('or'-ed) so that the web
   * resource may be matched on any combination of the given facet values (as long as this
   * combination consists of codes from different facets).
   * </p>
   * <p>
   * As an example: suppose the search is for values a1 or a2 for facet a, and b1 for facet b. Then
   * this method will return the following two possible combinations:
   * <ol>
   * <li>a1 | b1</li>
   * <li>a2 | b1</li>
   * </ol>
   * So it will not return [a1 | a2] or [a1 | a2 | b1] as this would combine multiple codes for the
   * same facets. Nor will it return [a1] by itself, as for searching we need one of the values
   * in each facet to appear.
   * </p>
   * <p>
   * Note that all resulting codes will be shifted to the right position and will also have the bits
   * set that mark the media type (see {@link EncodedFacetCollection}).
   * </p>
   *
   * @param mediaType The media type with which to generate the search codes.
   * @param values The list of options to choose from. Each set of options in this list must contain
   * values of the same type. If one of the input sets is empty, it will be ignored.
   * @return The set of facet codes.
   */
  public static Set<Integer> getFacetSearchCodes(EncodedFacetCollection mediaType,
          FacetWithValues<?>... values) {

    // Get all the individual codes from all the facets.
    final List<Set<Integer>> codes = compileIntegerSets(values);

    // Find all the combinations; make sure there is always the media type value 'or'-ed into them.
    return getFacetSearchCodes(mediaType, codes);
  }

  /**
   * This method returns all search combinations of the facet value codes for audio content. See
   * {@link #getFacetSearchCodes(EncodedFacetCollection, FacetWithValues[])} for the details.
   *
   * @param mimeTypes The mime types of the audio content.
   * @param audioQualities The audio quality values of the audio content.
   * @param audioDurations The audio duration values of the audio content.
   * @return The set of facet codes.
   */
  public final Set<Integer> getAudioFacetSearchCodes(Set<MimeTypeEncoding> mimeTypes,
          Set<AudioQuality> audioQualities, Set<AudioDuration> audioDurations) {
    final List<Set<Integer>> codes = compileAudioIntegerSets(mimeTypes, audioQualities,
            audioDurations);
    return getFacetSearchCodes(EncodedFacetCollection.AUDIO, codes);
  }

  /**
   * This method returns all search combinations of the facet value codes for video content. See
   * {@link #getFacetSearchCodes(EncodedFacetCollection, FacetWithValues[])} for the details.
   *
   * @param mimeTypes The mime types of the video content.
   * @param videoQualities The video quality values of the video content.
   * @param videoDurations The video duration values of the video content.
   * @return The set of facet codes.
   */
  public final Set<Integer> getVideoFacetSearchCodes(Set<MimeTypeEncoding> mimeTypes,
          Set<VideoQuality> videoQualities, Set<VideoDuration> videoDurations) {
    final List<Set<Integer>> codes = compileVideoIntegerSets(mimeTypes, videoQualities,
            videoDurations);
    return getFacetSearchCodes(EncodedFacetCollection.VIDEO, codes);
  }

  /**
   * This method returns all search combinations of the facet value codes for image content. See
   * {@link #getFacetSearchCodes(EncodedFacetCollection, FacetWithValues[])} for the details.
   *
   * @param mimeTypes The mime types of the image content.
   * @param imageSizes The image size values of the image content.
   * @param imageColorSpaces The color space values of the image content.
   * @param imageAspectRatios The aspect ratio values of the image content.
   * @param imageColorEncodings The color encoding values of the image content.
   * @return The set of facet codes.
   */
  public final Set<Integer> getImageFacetSearchCodes(Set<MimeTypeEncoding> mimeTypes,
          Set<ImageSize> imageSizes, Set<ImageColorSpace> imageColorSpaces,
          Set<ImageAspectRatio> imageAspectRatios, Set<ImageColorEncoding> imageColorEncodings) {
    final List<Set<Integer>> codes = compileImageIntegerSets(mimeTypes, imageSizes,
            imageColorSpaces, imageAspectRatios, imageColorEncodings);
    return getFacetSearchCodes(EncodedFacetCollection.IMAGE, codes);
  }

  /**
   * This method returns all search combinations of the facet value codes for text content. See
   * {@link #getFacetSearchCodes(EncodedFacetCollection, FacetWithValues[])} for the details.
   *
   * @param mimeTypes The mime types of the text content.
   * @return The set of facet codes.
   */
  public final Set<Integer> getTextFacetSearchCodes(Set<MimeTypeEncoding> mimeTypes) {
    final List<Set<Integer>> codes = compileTextIntegerSets(mimeTypes);
    return getFacetSearchCodes(EncodedFacetCollection.TEXT, codes);
  }

  private static Set<Integer> getFacetSearchCodes(EncodedFacetCollection mediaType,
          List<Set<Integer>> codes) {
    if (mediaType == null) {
      return Collections.emptySet();
    }
    // Filter the code lists so that empty sets or null sets are ignored.
    final List<Set<Integer>> filteredCodes = codes.stream().filter(Objects::nonNull)
            .filter(set->!set.isEmpty()).collect(Collectors.toList());
    final int shiftedMediaTypeCode = getShiftedMediaTypeCode(mediaType);
    return SetUtils.generateForcedCombinations(filteredCodes, shiftedMediaTypeCode,
            (combination, code) -> combination | code);
  }

  /**
   * <p>
   * This method returns all possible filter combinations of the facet value codes: each facet's
   * value(s) for the given web resource will be collected and combined ('or'-ed) so that the web
   * resource may be queried on any combination of facet codes (as long as this combination consists
   * of codes from different facets).
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

    // Get all the individual codes from all the facets.
    final List<Set<Integer>> codes = compileIntegerSets(webResource);

    // Find all the combinations; make sure there is always the media type value 'or'-ed into them.
    return getFacetFilterCodes(EncodedFacetCollection.get(webResource), codes);
  }

  /**
   * This method returns all filter combinations of the facet value codes for audio content. See
   * {@link #getFacetFilterCodes(WebResourceWrapper)} for the details.
   *
   * @param mimeTypes The mime types of the audio content.
   * @param audioQualities The audio quality values of the audio content.
   * @param audioDurations The audio duration values of the audio content.
   * @return The set of facet codes.
   */
  public final Set<Integer> getAudioFacetFilterCodes(Set<MimeTypeEncoding> mimeTypes,
      Set<AudioQuality> audioQualities, Set<AudioDuration> audioDurations) {
    final List<Set<Integer>> codes = compileAudioIntegerSets(mimeTypes, audioQualities,
        audioDurations);
    return getFacetFilterCodes(EncodedFacetCollection.AUDIO, codes);
  }

  /**
   * This method returns all filter combinations of the facet value codes for video content. See
   * {@link #getFacetFilterCodes(WebResourceWrapper)} for the details.
   *
   * @param mimeTypes The mime types of the video content.
   * @param videoQualities The video quality values of the video content.
   * @param videoDurations The video duration values of the video content.
   * @return The set of facet codes.
   */
  public final Set<Integer> getVideoFacetFilterCodes(Set<MimeTypeEncoding> mimeTypes,
      Set<VideoQuality> videoQualities, Set<VideoDuration> videoDurations) {
    final List<Set<Integer>> codes = compileVideoIntegerSets(mimeTypes, videoQualities,
        videoDurations);
    return getFacetFilterCodes(EncodedFacetCollection.VIDEO, codes);
  }

  /**
   * This method returns all filter combinations of the facet value codes for image content. See
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
    final List<Set<Integer>> codes = compileImageIntegerSets(mimeTypes, imageSizes,
        imageColorSpaces, imageAspectRatios, imageColorEncodings);
    return getFacetFilterCodes(EncodedFacetCollection.IMAGE, codes);
  }

  /**
   * This method returns all filter combinations of the facet value codes for text content. See
   * {@link #getFacetFilterCodes(WebResourceWrapper)} for the details.
   *
   * @param mimeTypes The mime types of the text content.
   * @return The set of facet codes.
   */
  public final Set<Integer> getTextFacetFilterCodes(Set<MimeTypeEncoding> mimeTypes) {
    final List<Set<Integer>> codes = compileTextIntegerSets(mimeTypes);
    return getFacetFilterCodes(EncodedFacetCollection.TEXT, codes);
  }

  private static Set<Integer> getFacetFilterCodes(EncodedFacetCollection mediaType,
      List<Set<Integer>> codes) {
    if (mediaType == null) {
      return Collections.emptySet();
    }
    final int shiftedMediaTypeCode = getShiftedMediaTypeCode(mediaType);
    return SetUtils.generateOptionalCombinations(codes, shiftedMediaTypeCode,
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

    // Get all the individual codes from all the facets.
    final List<Set<Integer>> codes = compileIntegerSets(webResource);

    // Combine the codes and make sure there is always the media type value 'or'-ed into them.
    return getFacetValueCodes(EncodedFacetCollection.get(webResource), codes);
  }

  /**
   * This method returns all facet value codes for audio content. See {@link
   * #getFacetValueCodes(WebResourceWrapper)} for the details.
   *
   * @param mimeTypes The mime types of the audio content.
   * @param audioQualities The audio quality values of the audio content.
   * @param audioDurations The audio duration values of the audio content.
   * @return The set of facet codes.
   */
  public final Set<Integer> getAudioFacetValueCodes(Set<MimeTypeEncoding> mimeTypes,
      Set<AudioQuality> audioQualities, Set<AudioDuration> audioDurations) {
    final List<Set<Integer>> codes = compileAudioIntegerSets(mimeTypes, audioQualities,
        audioDurations);
    return getFacetValueCodes(EncodedFacetCollection.AUDIO, codes);
  }

  /**
   * This method returns all facet value codes for video content. See {@link
   * #getFacetValueCodes(WebResourceWrapper)} for the details.
   *
   * @param mimeTypes The mime types of the video content.
   * @param videoQualities The video quality values of the video content.
   * @param videoDurations The video duration values of the video content.
   * @return The set of facet codes.
   */
  public final Set<Integer> getVideoFacetValueCodes(Set<MimeTypeEncoding> mimeTypes,
      Set<VideoQuality> videoQualities, Set<VideoDuration> videoDurations) {
    final List<Set<Integer>> codes = compileVideoIntegerSets(mimeTypes, videoQualities,
        videoDurations);
    return getFacetValueCodes(EncodedFacetCollection.VIDEO, codes);
  }

  /**
   * This method returns all facet value codes for image content. See {@link
   * #getFacetValueCodes(WebResourceWrapper)} for the details.
   *
   * @param mimeTypes The mime types of the image content.
   * @param imageSizes The image size values of the image content.
   * @param imageColorSpaces The color space values of the image content.
   * @param imageAspectRatios The aspect ratio values of the image content.
   * @param imageColorEncodings The color encoding values of the image content.
   * @return The set of facet codes.
   */
  public final Set<Integer> getImageFacetValueCodes(Set<MimeTypeEncoding> mimeTypes,
      Set<ImageSize> imageSizes, Set<ImageColorSpace> imageColorSpaces,
      Set<ImageAspectRatio> imageAspectRatios, Set<ImageColorEncoding> imageColorEncodings) {
    final List<Set<Integer>> codes = compileImageIntegerSets(mimeTypes, imageSizes,
        imageColorSpaces, imageAspectRatios, imageColorEncodings);
    return getFacetValueCodes(EncodedFacetCollection.IMAGE, codes);
  }

  /**
   * This method returns all facet value codes for text content. See {@link
   * #getFacetValueCodes(WebResourceWrapper)} for the details.
   *
   * @param mimeTypes The mime types of the text content.
   * @return The set of facet codes.
   */
  public final Set<Integer> getTextFacetValueCodes(Set<MimeTypeEncoding> mimeTypes) {
    final List<Set<Integer>> codes = compileTextIntegerSets(mimeTypes);
    return getFacetValueCodes(EncodedFacetCollection.TEXT, codes);
  }

  private static Set<Integer> getFacetValueCodes(EncodedFacetCollection mediaType,
      List<Set<Integer>> codes) {
    if (mediaType == null) {
      return Collections.emptySet();
    }
    final int shiftedMediaTypeCode = getShiftedMediaTypeCode(mediaType);
    return codes.stream().flatMap(Set::stream)
        .map(code -> shiftedMediaTypeCode | code).collect(Collectors.toSet());
  }

  private static List<Set<Integer>> compileIntegerSets(WebResourceWrapper webResource) {
    final EncodedFacetCollection facets = EncodedFacetCollection.get(webResource);
    if (facets == null) {
      return Collections.emptyList();
    }
    return facets.getFacets().stream().map(facet -> facet.encodeValues(webResource))
        .filter(set -> !set.isEmpty()).collect(Collectors.toList());
  }

  private static List<Set<Integer>> compileIntegerSets(FacetWithValues<?>... values) {
    return Stream.of(values).map(FacetWithValues::compileIntegerSet).collect(Collectors.toList());
  }

  private static List<Set<Integer>> compileAudioIntegerSets(Set<MimeTypeEncoding> mimeTypes,
          Set<AudioQuality> audioQualities, Set<AudioDuration> audioDurations) {
    return compileIntegerSets(new FacetWithValues<>(EncodedFacet.MIME_TYPE, mimeTypes),
            new FacetWithValues<>(EncodedFacet.AUDIO_QUALITY, audioQualities),
            new FacetWithValues<>(EncodedFacet.AUDIO_DURATION, audioDurations));
  }

  private static List<Set<Integer>> compileVideoIntegerSets(Set<MimeTypeEncoding> mimeTypes,
          Set<VideoQuality> videoQualities, Set<VideoDuration> videoDurations) {
    return compileIntegerSets(new FacetWithValues<>(EncodedFacet.MIME_TYPE, mimeTypes),
            new FacetWithValues<>(EncodedFacet.VIDEO_QUALITY, videoQualities),
            new FacetWithValues<>(EncodedFacet.VIDEO_DURATION, videoDurations));
  }

  private static List<Set<Integer>> compileImageIntegerSets(Set<MimeTypeEncoding> mimeTypes,
          Set<ImageSize> imageSizes, Set<ImageColorSpace> imageColorSpaces,
          Set<ImageAspectRatio> imageAspectRatios, Set<ImageColorEncoding> imageColorEncodings) {
    return compileIntegerSets(new FacetWithValues<>(EncodedFacet.MIME_TYPE, mimeTypes),
            new FacetWithValues<>(EncodedFacet.IMAGE_SIZE, imageSizes),
            new FacetWithValues<>(EncodedFacet.IMAGE_COLOR_SPACE, imageColorSpaces),
            new FacetWithValues<>(EncodedFacet.IMAGE_ASPECT_RATIO, imageAspectRatios),
            new FacetWithValues<>(EncodedFacet.IMAGE_COLOR_ENCODING, imageColorEncodings));
  }

  private static List<Set<Integer>> compileTextIntegerSets(Set<MimeTypeEncoding> mimeTypes) {
    return compileIntegerSets(new FacetWithValues<>(EncodedFacet.MIME_TYPE, mimeTypes));
  }

  private static int getShiftedMediaTypeCode(EncodedFacetCollection encoder) {
    return EncodedFacet.MEDIA_TYPE.encodeValue(encoder.getMediaType());
  }
}
