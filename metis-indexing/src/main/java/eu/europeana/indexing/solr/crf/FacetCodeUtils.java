package eu.europeana.indexing.solr.crf;

import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import eu.europeana.indexing.solr.crf.WebResourceWrapper.ColorSpace;
import eu.europeana.indexing.solr.crf.WebResourceWrapper.Orientation;

/**
 * This class contains methods to convert web resource properties to codes that may be used as
 * facets. None of the codes that are created here are shifted.
 * 
 * @author jochen
 *
 */
public final class FacetCodeUtils {

  private static final Integer UNKNOWN = 0;

  private static final Integer IMAGE_TINY = 0;
  private static final Integer IMAGE_SMALL = 1;
  private static final Integer IMAGE_MEDIUM = 2;
  private static final Integer IMAGE_LARGE = 3;
  private static final Integer IMAGE_HUGE = 4;

  private static final Integer IMAGE_SRGB = 1;
  private static final Integer IMAGE_GRAYSCALE = 2;

  private static final Integer IMAGE_PORTRAIT = 1;
  private static final Integer IMAGE_LANDSCAPE = 2;

  private static final Integer VIDEO_AUDIO_HIGH_QUALITY_FALSE = 0;
  private static final Integer VIDEO_AUDIO_HIGH_QUALITY_TRUE = 1;

  private static final Integer VIDEO_SHORT = 1;
  private static final Integer VIDEO_MEDIUM = 2;
  private static final Integer VIDEO_LONG = 3;

  private static final Integer AUDIO_TINY = 1;
  private static final Integer AUDIO_SHORT = 2;
  private static final Integer AUDIO_MEDIUM = 3;
  private static final Integer AUDIO_LONG = 4;

  private static final String DEFAULT_MIME_TYPE = "application/octet-stream";
  
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

  private FacetCodeUtils() {}

  /**
   * Codify the mime type of the given web resource.
   * 
   * @param webResource The web resource.
   * @return The (non-shifted) code.
   */
  public static Set<Integer> getMimeTypeCode(final WebResourceWrapper webResource) {
    final String mimeType =
        Optional.ofNullable(webResource.getMimeType()).orElse(DEFAULT_MIME_TYPE);
    return Collections.singleton(MimeTypeEncoding.getMimeTypeCode(mimeType));
  }

  /**
   * Codify the size of the given image.
   * 
   * @param webResource The web resource.
   * @return The (non-shifted) code.
   */
  public static Set<Integer> getImageSizeCode(final WebResourceWrapper webResource) {
    final long size = webResource.getWidth() * webResource.getHeight();
    final Integer result;
    if (size == 0) {
      result = UNKNOWN;
    } else if (size < IMAGE_SMALL_AREA) {
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

  /**
   * Codify the color space of the given image.
   * 
   * @param webResource The web resource.
   * @return The (non-shifted) code.
   */
  public static Set<Integer> getImageColorSpaceCode(final WebResourceWrapper webResource) {
    final ColorSpace colorSpace = webResource.getColorSpace();
    final Integer result;
    if (ColorSpace.SRGB == colorSpace) {
      result = IMAGE_SRGB;
    } else if (ColorSpace.GRAYSCALE == colorSpace) {
      result = IMAGE_GRAYSCALE;
    } else {
      result = UNKNOWN;
    }
    return Collections.singleton(result);
  }

  /**
   * Codify the aspect ratio of the given image.
   * 
   * @param webResource The web resource.
   * @return The (non-shifted) code.
   */
  public static Set<Integer> getImageAspectRatioCode(final WebResourceWrapper webResource) {
    final Orientation orientation = webResource.getOrientation();
    final Integer result;
    if (Orientation.PORTRAIT == orientation) {
      result = IMAGE_PORTRAIT;
    } else if (Orientation.LANDSCAPE == orientation) {
      result = IMAGE_LANDSCAPE;
    } else {
      result = UNKNOWN;
    }
    return Collections.singleton(result);
  }

  /**
   * Codify the color palette of the given image.
   * 
   * @param webResource The web resource.
   * @return The (non-shifted) code.
   */
  public static Set<Integer> getImageColorPalette(final WebResourceWrapper webResource) {
    final Set<String> colorCodes = webResource.getColorHexCodes();
    if (colorCodes.isEmpty()) {
      return Collections.singleton(UNKNOWN);
    }
    return webResource.getColorHexCodes().stream().map(ColorEncoding::getColorCode)
        .collect(Collectors.toSet());
  }

  /**
   * Codify the quality of the given video.
   * 
   * @param webResource The web resource.
   * @return The (non-shifted) code.
   */
  public static Set<Integer> getVideoQualityCode(final WebResourceWrapper webResource) {
    final Integer result;
    if (webResource.getHeight() <= VIDEO_HIGH_QUALITY_HEIGHT) {
      result = VIDEO_AUDIO_HIGH_QUALITY_FALSE;
    } else {
      result = VIDEO_AUDIO_HIGH_QUALITY_TRUE;
    }
    return Collections.singleton(result);
  }

  /**
   * Codify the quality of the given audio.
   * 
   * @param webResource The web resource.
   * @return The (non-shifted) code.
   */
  public static Set<Integer> getAudioQualityCode(final WebResourceWrapper webResource) {

    // Determine whether the format is high-definition sampling or lossless.
    final long sampleSize = webResource.getSampleSize();
    final long sampleRate = webResource.getSampleRate();
    final String codecName = webResource.getCodecName();
    final boolean highDefSampling = sampleSize >= AUDIO_HIGH_QUALITY_SAMPLE_SIZE
        && sampleRate >= AUDIO_HIGH_QUALITY_SAMPLE_RATE;
    final boolean losslessFile = codecName != null
        && LOSSLESS_AUDIO_FILE_TYPES.contains(codecName.toLowerCase(Locale.ENGLISH).trim());

    // Classify.
    final Integer result;
    if (highDefSampling || losslessFile) {
      result = VIDEO_AUDIO_HIGH_QUALITY_TRUE;
    } else {
      result = VIDEO_AUDIO_HIGH_QUALITY_FALSE;
    }
    return Collections.singleton(result);
  }

  /**
   * Codify the duration of the given video.
   * 
   * @param webResource The web resource.
   * @return The (non-shifted) code.
   */
  public static Set<Integer> getVideoDurationCode(final WebResourceWrapper webResource) {
    final long duration = webResource.getDuration();
    final Integer result;
    if (duration == 0L) {
      result = UNKNOWN;
    } else if (duration <= VIDEO_MEDIUM_DURATION) {
      result = VIDEO_SHORT;
    } else if (duration <= VIDEO_LONG_DURATION) {
      result = VIDEO_MEDIUM;
    } else {
      result = VIDEO_LONG;
    }
    return Collections.singleton(result);
  }

  /**
   * Codify the duration of the given audio.
   * 
   * @param webResource The web resource.
   * @return The (non-shifted) code.
   */
  public static Set<Integer> getAudioDurationCode(final WebResourceWrapper webResource) {
    final long duration = webResource.getDuration();
    final Integer result;
    if (duration == 0L) {
      result = UNKNOWN;
    } else if (duration <= AUDIO_SHORT_DURATION) {
      result = AUDIO_TINY;
    } else if (duration <= AUDIO_MEDIUM_DURATION) {
      result = AUDIO_SHORT;
    } else if (duration <= AUDIO_LONG_DURATION) {
      result = AUDIO_MEDIUM;
    } else {
      result = AUDIO_LONG;
    }
    return Collections.singleton(result);
  }
}
