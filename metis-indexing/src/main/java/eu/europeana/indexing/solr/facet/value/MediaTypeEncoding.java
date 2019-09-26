package eu.europeana.indexing.solr.facet.value;

import eu.europeana.indexing.utils.WebResourceWrapper;

/**
 * This categorizes the media type.
 */
public enum MediaTypeEncoding implements FacetValue {

  IMAGE(1), AUDIO(2), VIDEO(3), TEXT(4);

  private int code;

  MediaTypeEncoding(int code) {
    this.code = code;
  }

  @Override
  public int getCode() {
    return code;
  }

  /**
   * Categorize the media type.
   *
   * @param webResource The web resource.
   * @return The category, or null if none of the categories apply.
   */
  public static MediaTypeEncoding categorizeMediaType(final WebResourceWrapper webResource) {
    final MediaTypeEncoding result;
    switch (webResource.getMediaType()) {
      case AUDIO:
        result = MediaTypeEncoding.AUDIO;
        break;
      case IMAGE:
        result = MediaTypeEncoding.IMAGE;
        break;
      case TEXT:
        result = MediaTypeEncoding.TEXT;
        break;
      case VIDEO:
        result = MediaTypeEncoding.VIDEO;
        break;
      default:
        result = null;
        break;
    }
    return result;
  }
}
