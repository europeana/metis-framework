package eu.europeana.indexing.solr.facet.value;

import eu.europeana.indexing.utils.WebResourceWrapper;
import eu.europeana.metis.schema.model.MediaType;

/**
 * This categorizes the media type.
 */
public enum MediaTypeEncoding implements FacetValue {

  IMAGE(1), AUDIO(2), VIDEO(3), TEXT(4), THREE_D(5);

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
   * @param mediaType The media type.
   * @return The category, or null if none of the categories apply.
   */
  public static MediaTypeEncoding categorizeMediaType(final MediaType mediaType) {
    final MediaTypeEncoding result;
    if (mediaType == null) {
      result = null;
    } else {
      result = switch (mediaType) {
        case AUDIO -> MediaTypeEncoding.AUDIO;
        case IMAGE -> MediaTypeEncoding.IMAGE;
        case TEXT -> MediaTypeEncoding.TEXT;
        case VIDEO -> MediaTypeEncoding.VIDEO;
        default -> null;
      };
    }
    return result;
  }

  /**
   * Categorize the media type.
   *
   * @param webResource The web resource.
   * @return The category, or null if none of the categories apply.
   */
  public static MediaTypeEncoding categorizeMediaType(final WebResourceWrapper webResource) {
    return categorizeMediaType(webResource.getMediaType());
  }
}
