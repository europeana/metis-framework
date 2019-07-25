package eu.europeana.metis.utils;

/**
 * This class lists the supported media types.
 *
 * @author jochen
 */
public enum MediaType {

  /**
   * Audio
   **/
  AUDIO,

  /**
   * Video
   **/
  VIDEO,

  /**
   * Text (including PDFs)
   **/
  TEXT,

  /**
   * Graphical
   **/
  IMAGE,

  /**
   * Media that is not of any of the other kinds.
   **/
  OTHER;

  /**
   * Obtains the media type of a given mime type.
   *
   * @param mimeType The mime type.
   * @return The media type to which the mime type belongs.
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
