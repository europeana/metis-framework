package eu.europeana.indexing.solr.facet.value;

import eu.europeana.indexing.utils.WebResourceWrapper;

/**
 * This categorizes the duration of video filas.
 */
public enum VideoDuration implements FacetValue {

  SHORT(1), MEDIUM(2), LONG(3);

  /**
   * Video duration (in milliseconds) that is considered medium: 4 mins.
   **/
  private static final long VIDEO_MEDIUM_DURATION = 240_000;

  /**
   * Video duration (in milliseconds) that is considered long: 20 mins.
   **/
  private static final long VIDEO_LONG_DURATION = 1_200_000;

  private int code;

  VideoDuration(int code) {
    this.code = code;
  }

  @Override
  public int getCode() {
    return code;
  }

  /**
   * Categorize the video duration.
   *
   * @param videoDuration The video duration.
   * @return The category, or null if none of the categories apply.
   */
  public static VideoDuration categorizeVideoDuration(final Long videoDuration) {
    final VideoDuration result;
    if (videoDuration == null || videoDuration <= 0L) {
      result = null;
    } else if (videoDuration <= VIDEO_MEDIUM_DURATION) {
      result = SHORT;
    } else if (videoDuration <= VIDEO_LONG_DURATION) {
      result = MEDIUM;
    } else {
      result = LONG;
    }
    return result;
  }

  /**
   * Categorize the duration of the given video.
   *
   * @param webResource The web resource.
   * @return The category, or null if none of the categories apply.
   */
  public static VideoDuration categorizeVideoDuration(final WebResourceWrapper webResource) {
    return categorizeVideoDuration(webResource.getDuration());
  }
}
