package eu.europeana.indexing.solr.facet.value;

import eu.europeana.indexing.utils.WebResourceWrapper;

/**
 * This categorizes the duration of audio files.
 */
public enum AudioDuration implements FacetValue {

  TINY(1), SHORT(2), MEDIUM(3), LONG(4);

  /**
   * Audio duration (in milliseconds) that is considered shortlong: 1/2 min.
   **/
  private static final long AUDIO_SHORT_DURATION = 30_000;

  /**
   * Audio duration (in milliseconds) that is considered medium: 3 mins.
   **/
  private static final long AUDIO_MEDIUM_DURATION = 180_000;

  /**
   * Audio duration (in milliseconds) that is considered long: 6 mins.
   **/
  private static final long AUDIO_LONG_DURATION = 360_000;

  private int code;

  AudioDuration(int code) {
    this.code = code;
  }

  @Override
  public int getCode() {
    return code;
  }

  /**
   * Categorize the audio duration.
   *
   * @param audioDuration The audio duration.
   * @return The category, or null if none of the categories apply.
   */
  public static AudioDuration categorizeAudioDuration(final Long audioDuration) {
    final AudioDuration result;
    if (audioDuration == null || audioDuration <= 0L) {
      result = null;
    } else if (audioDuration <= AUDIO_SHORT_DURATION) {
      result = TINY;
    } else if (audioDuration <= AUDIO_MEDIUM_DURATION) {
      result = SHORT;
    } else if (audioDuration <= AUDIO_LONG_DURATION) {
      result = MEDIUM;
    } else {
      result = LONG;
    }
    return result;
  }

  /**
   * Categorize the duration of the given audio.
   *
   * @param webResource The web resource.
   * @return The category, or null if none of the categories apply.
   */
  public static AudioDuration categorizeAudioDuration(final WebResourceWrapper webResource) {
    return categorizeAudioDuration(webResource.getDuration());
  }
}
