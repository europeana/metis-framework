package eu.europeana.indexing.utils;

/**
 * This class lists the supported media types.
 *
 * TODO is a duplicate of ResourceType in the media processing library.
 * 
 * @author jochen
 *
 */
public enum MediaType {

  /** Audio (sound only) **/
  AUDIO,

  /** Video **/
  VIDEO,

  /** Images **/
  IMAGE,

  /** Text **/
  TEXT,

  /** Unknown type: not supported **/
  OTHER;

  /**
   * Matches the MIME type to one of the supported media types.
   *
   * @param mimeType The mime type as a string.
   * @return The media type corresponding to the mime type. Does not return null, but may return
   *         {@link MediaType#OTHER}.
   */
  public static MediaType getMediaType(String mimeType) {
    final MediaType result;
    if (mimeType == null) {
      result = OTHER;
    } else if (mimeType.startsWith("image/")) {
      result = IMAGE;
    } else if (mimeType.startsWith("audio/")) {
      result = AUDIO;
    } else if (mimeType.startsWith("video/")) {
      result = VIDEO;
    } else if (isText(mimeType)) {
      result = TEXT;
    } else {
      result = OTHER;
    }
    return result;
  }

  private static boolean isText(String mimeType) {
    switch (mimeType) {
      case "application/xml":
      case "application/rtf":
      case "application/epub":
      case "application/pdf":
      case "application/xhtml+xml":
        return true;
      default:
        return mimeType.startsWith("text/");
    }
  }
}
