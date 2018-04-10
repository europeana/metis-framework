package eu.europeana.indexing.solr.crf;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import eu.europeana.corelib.definitions.jibx.CodecName;
import eu.europeana.corelib.definitions.jibx.ColorSpaceType;
import eu.europeana.corelib.definitions.jibx.Duration;
import eu.europeana.corelib.definitions.jibx.HasColorSpace;
import eu.europeana.corelib.definitions.jibx.Height;
import eu.europeana.corelib.definitions.jibx.OrientationType;
import eu.europeana.corelib.definitions.jibx.SampleRate;
import eu.europeana.corelib.definitions.jibx.SampleSize;
import eu.europeana.corelib.definitions.jibx.WebResourceType;
import eu.europeana.corelib.definitions.jibx.Width;

public final class TechnicalFacetUtils {

  private static final Integer UNKNOWN = 0;

  private static final Integer IMAGE_TINY = 0;
  private static final Integer IMAGE_SMALL = 1;
  private static final Integer IMAGE_MEDIUM = 2;
  private static final Integer IMAGE_LARGE = 3;
  private static final Integer IMAGE_HUGE = 4;

  private static final Integer IMAGE_SRGB = 1;
  private static final Integer IMAGE_GREYSCALE = 2;

  private static final Integer IMAGE_PORTRAIT = 1;
  private static final Integer IMAGE_LANDSCAPE = 2;

  private static final Integer VIDEO_AUDIO_HIGH_QUALITY_FALSE = 0;
  private static final Integer VIDEO_AUDIO_HIGH_QUALITY_TRUE = 1;

  private static final Integer VIDEO_SHORT = 1;
  private static final Integer VIDEO_MEDIUM = 2;
  private static final Integer VIDEO_LONG = 3;

  private static final Integer AUDIO_TINY = 1;
  private static final Integer AUDIO_SHORT = 1;
  private static final Integer AUDIO_MEDIUM = 2;
  private static final Integer AUDIO_LONG = 3;

  /** Image area (in pixels) that is considered huge: 4mp. **/
  private static final long IMAGE_HUGE_AREA = 4_000_000;

  /** Image area (in pixels) that is considered large: 0.95mp. **/
  private static final long IMAGE_LARGE_AREA = 950_000;

  /** Image area (in pixels) that is considered medium size: 0.42mp. **/
  private static final long IMAGE_MEDIUM_AREA = 420_000;

  /** Image area (in pixels) that is considered small: 0.1mp. **/
  private static final long IMAGE_SMALL_AREA = 100_000;

  /** Video height that is considered high-quality. **/
  private static final int VIDEO_HIGH_QUALITY_HEIGHT = 480;

  /** Video duration (in milliseconds) that is considered medium: 4 mins. **/
  private static final long VIDEO_MEDIUM_DURATION = 240_000;

  /** Video duration (in milliseconds) that is considered long: 20 mins. **/
  private static final long VIDEO_LONG_DURATION = 1_200_000;

  /** Audio sample size that is considered high-quality. **/
  private static final long AUDIO_HIGH_QUALITY_SAMPLE_SIZE = 16;

  /** Audio sample rate that is considered high-quality. **/
  private static final long AUDIO_HIGH_QUALITY_SAMPLE_RATE = 44_100;

  /** Audio formats that are lossless and are therefore considered high-quality. **/
  private static final Set<String> LOSSLESS_AUDIO_FILE_TYPES = Stream
      .of("alac", "flac", "ape", "shn", "wav", "wma", "aiff", "dsd").collect(Collectors.toSet());

  /** Audio duration (in milliseconds) that is considered shortlong: 1/2 min. **/
  private static final long AUDIO_SHORT_DURATION = 30_000;

  /** Audio duration (in milliseconds) that is considered medium: 3 mins. **/
  private static final long AUDIO_MEDIUM_DURATION = 180_000;

  /** Audio duration (in milliseconds) that is considered long: 6 mins. **/
  private static final long AUDIO_LONG_DURATION = 360_000;

  private TechnicalFacetUtils() {}

  // TODO JOCHEN Merge and make library method for existing methods isText, isAudio/Video and
  // isImage in class MediaProcessor. Use this class there.
  /**
   * Determines the media type of the web resource (based on the MIME type).
   * 
   * @param webResource The web resource for which to determine the media type.
   * @return The media type corresponding to the mime type. Does not return null, but may return
   *         {@link MediaType#OTHER}.
   */
  public static MediaType getMediaType(WebResourceType webResource) {
    final String mimeType = getMimeType(webResource);
    final MediaType result;
    if (mimeType == null) {
      result = MediaType.OTHER;
    } else if (mimeType.startsWith("image/")) {
      result = MediaType.IMAGE;
    } else if (mimeType.startsWith("audio/")) {
      result = MediaType.AUDIO;
    } else if (mimeType.startsWith("video/")) {
      result = MediaType.VIDEO;
    } else if (isText(mimeType)) {
      result = MediaType.TEXT;
    } else {
      result = MediaType.OTHER;
    }
    return result;
  }

  private static boolean isText(String mimeType) {
    switch (mimeType) {
      case "application/xml":
      case "application/rtf":
      case "application/epub":
      case "application/pdf":
        return true;
      default:
        return mimeType.startsWith("text/");
    }
  }
  
  public static Set<Integer> getMimeTypeCode(final WebResourceType webResource) {
    return Collections.singleton(MimeTypeEncoding.getMimeTypeCode(getMimeType(webResource)));
  }

  private static String getMimeType(WebResourceType webResource) {
    
    // If there is no mime type, return the generic one.
    if (webResource.getHasMimeType() == null
        || StringUtils.isBlank(webResource.getHasMimeType().getHasMimeType())) {
      return "application/octet-stream";
    }

    // Otherwise, we return the base type (without parameters).
    return webResource.getHasMimeType().getHasMimeType().split(";")[0].trim().toLowerCase();
  }

  public static Set<Integer> getImageSizeCode(final WebResourceType webResource) {

    // Determine size of image (pixel area).
    final Width width = webResource.getWidth();
    final Height height = webResource.getHeight();
    if (width == null || height == null) {
      return Collections.singleton(UNKNOWN);
    }
    final long size = width.getLong() * height.getLong();

    // Classify the size.
    final Integer result;
    if (size < IMAGE_SMALL_AREA) {
      result = IMAGE_TINY;
    } else if (size < IMAGE_MEDIUM_AREA) {
      result = IMAGE_SMALL;
    } else if (size < IMAGE_LARGE_AREA) {
      result = IMAGE_MEDIUM;
    } else if (size < IMAGE_HUGE_AREA) {
      result = IMAGE_LARGE;
    } else {
      result = IMAGE_HUGE;
    }
    return Collections.singleton(result);
  }

  public static Set<Integer> getImageColorSpaceCode(final WebResourceType webResource) {
    final HasColorSpace colorSpace = webResource.getHasColorSpace();
    final Integer result;
    if (colorSpace == null || colorSpace.getHasColorSpace() == null) {
      result = UNKNOWN;
    } else if (ColorSpaceType.S_RGB.equals(colorSpace.getHasColorSpace())) {
      result = IMAGE_SRGB;
    } else if (ColorSpaceType.GRAYSCALE.equals(colorSpace.getHasColorSpace())) {
      result = IMAGE_GREYSCALE;
    } else {
      result = UNKNOWN;
    }
    return Collections.singleton(result);
  }

  public static Set<Integer> getImageAspectRatioCode(final WebResourceType webResource) {
    final OrientationType orientation = webResource.getOrientation();
    final Integer result;
    if (orientation == null || orientation.getString() == null) {
      result = UNKNOWN;
    } else if ("portrait".equals(orientation.getString())) {
      result = IMAGE_PORTRAIT;
    } else if ("landscape".equals(orientation.getString())) {
      result = IMAGE_LANDSCAPE;
    } else {
      result = UNKNOWN;
    }
    return Collections.singleton(result);
  }

  public static Set<Integer> getImageColorPalette(final WebResourceType webResource) {
    return webResource.getComponentColorList().stream()
        .map(color -> color == null ? UNKNOWN : ColorEncoding.getColorCode(color.getString()))
        .collect(Collectors.toSet());
  }

  public static Set<Integer> getVideoQualityCode(final WebResourceType webResource) {
    final Height height = webResource.getHeight();
    final Integer result;
    if (height == null || height.getLong() <= VIDEO_HIGH_QUALITY_HEIGHT) {
      result = VIDEO_AUDIO_HIGH_QUALITY_FALSE;
    } else {
      result = VIDEO_AUDIO_HIGH_QUALITY_TRUE;
    }
    return Collections.singleton(result);
  }

  public static Set<Integer> getAudioQualityCode(final WebResourceType webResource) {

    // Determine whether the format is high-definition sampling or lossless.
    final SampleSize sampleSize = webResource.getSampleSize();
    final SampleRate sampleRate = webResource.getSampleRate();
    final CodecName codecName = webResource.getCodecName();
    final boolean highDefSampling = sampleSize != null && sampleRate != null
        && sampleSize.getLong() >= AUDIO_HIGH_QUALITY_SAMPLE_SIZE
        && sampleRate.getLong() >= AUDIO_HIGH_QUALITY_SAMPLE_RATE;
    final boolean losslessFile = codecName != null && codecName.getCodecName() != null
        && LOSSLESS_AUDIO_FILE_TYPES.contains(codecName.getCodecName().toLowerCase().trim());

    // Classify.
    final Integer result;
    if (highDefSampling || losslessFile) {
      result = VIDEO_AUDIO_HIGH_QUALITY_TRUE;
    } else {
      result = VIDEO_AUDIO_HIGH_QUALITY_FALSE;
    }
    return Collections.singleton(result);
  }

  private static Long getDuration(final WebResourceType webResource) {
    final Duration duration = webResource.getDuration();
    if (duration == null || StringUtils.isBlank(duration.getDuration()))
      return null;
    try {
      return Long.parseLong(duration.getDuration());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public static Set<Integer> getVideoDurationCode(final WebResourceType webResource) {
    final Long durationNumber = getDuration(webResource);
    final Integer result;
    if (durationNumber == null) {
      result = UNKNOWN;
    } else if (durationNumber <= VIDEO_MEDIUM_DURATION) {
      result = VIDEO_SHORT;
    } else if (durationNumber <= VIDEO_LONG_DURATION) {
      result = VIDEO_MEDIUM;
    } else {
      result = VIDEO_LONG;
    }
    return Collections.singleton(result);
  }

  public static Set<Integer> getAudioDurationCode(final WebResourceType webResource) {
    final Long durationNumber = getDuration(webResource);
    final Integer result;
    if (durationNumber == null) {
      result = UNKNOWN;
    } else if (durationNumber <= AUDIO_SHORT_DURATION) {
      result = AUDIO_TINY;
    } else if (durationNumber <= AUDIO_MEDIUM_DURATION) {
      result = AUDIO_SHORT;
    } else if (durationNumber <= AUDIO_LONG_DURATION) {
      result = AUDIO_MEDIUM;
    } else {
      result = AUDIO_LONG;
    }
    return Collections.singleton(result);
  }
}
