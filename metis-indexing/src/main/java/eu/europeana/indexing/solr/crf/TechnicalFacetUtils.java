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

  private static final Integer IMAGE_TINY = 1;
  private static final Integer IMAGE_SMALL = 2;
  private static final Integer IMAGE_MEDIUM = 3;
  private static final Integer IMAGE_LARGE = 4;

  private static final Integer IMAGE_SRGB = 1;
  private static final Integer IMAGE_GREYSCALE = 2;

  private static final Integer IMAGE_LANDSCAPE = 1;
  private static final Integer IMAGE_PORTRAIT = 2;

  private static final Integer VIDEO_AUDIO_HIGH_QUALITY_FALSE = 0;
  private static final Integer VIDEO_AUDIO_HIGH_QUALITY_TRUE = 1;

  private static final Integer VIDEO_SHORT = 1;
  private static final Integer VIDEO_MEDIUM = 2;
  private static final Integer VIDEO_LONG = 3;

  private static final Integer AUDIO_TINY = 1;
  private static final Integer AUDIO_SHORT = 1;
  private static final Integer AUDIO_MEDIUM = 2;
  private static final Integer AUDIO_LONG = 3;

  /** Image area (in pixels) that is considered large: 2^22. **/
  private static final long IMAGE_LARGE_AREA = 4_194_304;

  /** Image area (in pixels) that is considered medium size: 2^20. **/
  private static final long IMAGE_MEDIUM_AREA = 1_048_576;

  /** Image area (in pixels) that is considered small: 2^19. **/
  private static final long IMAGE_SMALL_AREA = 524_288;

  /** Video height that is considered high-quality. **/
  private static final int VIDEO_HIGH_QUALITY_HEIGHT = 480;

  /** Video duration (in milliseconds) that is considered medium. **/
  private static final long VIDEO_MEDIUM_DURATION = 240_000;

  /** Video duration (in milliseconds) that is considered long. **/
  private static final long VIDEO_LONG_DURATION = 1_200_000;

  /** Audiio sample size that is considered high-quality. **/
  private static final long AUDIO_HIGH_QUALITY_SAMPLE_SIZE = 16;

  /** Audiio sample rate that is considered high-quality. **/
  private static final long AUDIO_HIGH_QUALITY_SAMPLE_RATE = 44_100;

  /** Audio formats that are lossless and are therefore considered high-quality. **/
  private static final Set<String> LOSSLESS_AUDIO_FILE_TYPES = Stream
      .of("alac", "flac", "ape", "shn", "wav", "wma", "aiff", "dsd").collect(Collectors.toSet());

  /** Audio duration (in milliseconds) that is considered short. **/
  private static final long AUDIO_SHORT_DURATION = 30_000;

  /** Audio duration (in milliseconds) that is considered medium. **/
  private static final long AUDIO_MEDIUM_DURATION = 180_000;

  /** Audio duration (in milliseconds) that is considered long. **/
  private static final long AUDIO_LONG_DURATION = 360_000;

  private TechnicalFacetUtils() {}

  public static Set<Integer> getMimeTypeCode(final WebResourceType webResource) {
    return Collections.singleton(MimeTypeEncoding.getMimeTypeCode(getMimeType(webResource)));
  }

  public static String getMimeType(WebResourceType webResource) {
    if (webResource.getHasMimeType() == null
        || webResource.getHasMimeType().getHasMimeType() == null
        || webResource.getHasMimeType().getHasMimeType().trim().isEmpty()) {
      return "application/octet-stream";
    }
    return webResource.getHasMimeType().getHasMimeType();
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
      result = IMAGE_MEDIUM; // 2048^2?
    } else {
      result = IMAGE_LARGE;
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
    } else if ("landscape".equals(orientation.getString())) {
      result = IMAGE_LANDSCAPE;
    } else if ("portrait".equals(orientation.getString())) {
      result = IMAGE_PORTRAIT;
    } else {
      result = UNKNOWN;
    }
    return Collections.singleton(result);
  }

  public static Set<Integer> getImageColorCodes(final WebResourceType webResource) {
    return webResource.getComponentColorList().stream()
        .map(color -> color == null ? UNKNOWN : ColorEncoding.getColorCode(color.getString()))
        .collect(Collectors.toSet());
  }

  public static Set<Integer> getVideoQualityCode(final WebResourceType webResource) {
    final Height height = webResource.getHeight();
    final Integer result;
    if (height == null || height.getLong() < VIDEO_HIGH_QUALITY_HEIGHT) {
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
