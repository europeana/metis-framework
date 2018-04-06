package eu.europeana.indexing.solr.crf;

/**
 * This class contains the types that are supported as technical metadata within records.
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

  // TODO JOCHEN Merge and make library method for existing methods isText, isAudio/Video and
  // isImage in class MediaProcessor.
  /**
   * Converts the mime type to a media type.
   * 
   * @param mimeType The mime type to convert. Can be null.
   * @return The media type corresponding to the mime type. Does not return null, but may return
   *         {@link MediaType#OTHER}.
   */
  public static MediaType getType(String mimeType) {
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
        return true;
      default:
        return mimeType.startsWith("text/");
    }
  }
}
