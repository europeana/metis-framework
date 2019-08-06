package eu.europeana.indexing.solr.facet.value;

import eu.europeana.indexing.utils.WebResourceWrapper;

/**
 * This categorizes the quality of video filas.
 */
public enum VideoQuality implements FacetValue {

  HIGH(1);

  /**
   * Video height that is considered high-quality.
   **/
  private static final int VIDEO_HIGH_QUALITY_HEIGHT = 480;

  private int code;

  VideoQuality(int code) {
    this.code = code;
  }

  @Override
  public int getCode() {
    return code;
  }

  /**
   * Categorize the quality of the given video.
   *
   * @param webResource The web resource.
   * @return The category, or null if none of the categories apply.
   */
  public static VideoQuality categorizeVideoQuality(final WebResourceWrapper webResource) {
    final VideoQuality result;
    if (webResource.getHeight() <= VIDEO_HIGH_QUALITY_HEIGHT) {
      result = null;
    } else {
      result = HIGH;
    }
    return result;
  }
}
