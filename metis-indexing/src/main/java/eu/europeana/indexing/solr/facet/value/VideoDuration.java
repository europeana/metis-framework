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
   * Categorize the duration of the given video.
   *
   * @param webResource The web resource.
   * @return The category, or null if none of the categories apply.
   */
  public static VideoDuration categorizeVideoDuration(final WebResourceWrapper webResource) {
    final long duration = webResource.getDuration();
    final VideoDuration result;
    if (duration == 0L) {
      result = null;
    } else if (duration <= VIDEO_MEDIUM_DURATION) {
      result = SHORT;
    } else if (duration <= VIDEO_LONG_DURATION) {
      result = MEDIUM;
    } else {
      result = LONG;
    }
    return result;
  }
}
