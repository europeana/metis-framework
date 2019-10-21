package eu.europeana.indexing.solr.facet.value;

import eu.europeana.indexing.utils.WebResourceWrapper;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This categorizes the quality of audio files.
 */
public enum AudioQuality implements FacetValue {

  HIGH(1);

  /**
   * Audio sample size that is considered high-quality.
   **/
  private static final long AUDIO_HIGH_QUALITY_SAMPLE_SIZE = 16;

  /**
   * Audio sample rate that is considered high-quality.
   **/
  private static final long AUDIO_HIGH_QUALITY_SAMPLE_RATE = 44_100;

  /**
   * Audio formats that are lossless and are therefore considered high-quality.
   **/
  private static final Set<String> LOSSLESS_AUDIO_FILE_TYPES = Stream
      .of("alac", "flac", "ape", "shn", "wav", "wma", "aiff", "dsd").collect(Collectors.toSet());

  private int code;

  AudioQuality(int code) {
    this.code = code;
  }

  @Override
  public int getCode() {
    return code;
  }

  /**
   * Categorize the quality.
   *
   * @param isHighQuality Whether the audio is high quality.
   * @return The category, or null if none of the categories apply.
   */
  public static AudioQuality categorizeAudioQuality(boolean isHighQuality) {
    return isHighQuality ? HIGH : null;
  }

  /**
   * Categorize the quality.
   *
   * @param sampleSize The sample size.
   * @param sampleRate The sample rate.
   * @param codecName The codec name.
   * @return The category, or null if none of the categories apply.
   */
  public static AudioQuality categorizeAudioQuality(long sampleSize, long sampleRate,
      String codecName) {
    final boolean highDefSampling = sampleSize >= AUDIO_HIGH_QUALITY_SAMPLE_SIZE
        && sampleRate >= AUDIO_HIGH_QUALITY_SAMPLE_RATE;
    final boolean losslessFile = codecName != null
        && LOSSLESS_AUDIO_FILE_TYPES.contains(codecName.toLowerCase(Locale.ENGLISH).trim());
    return categorizeAudioQuality(highDefSampling || losslessFile);
  }

  /**
   * Categorize the quality of the given audio.
   *
   * @param webResource The web resource.
   * @return The category, or null if none of the categories apply.
   */
  public static AudioQuality categorizeAudioQuality(final WebResourceWrapper webResource) {
    return categorizeAudioQuality(webResource.getSampleSize(), webResource.getSampleRate(),
        webResource.getCodecName());
  }
}
