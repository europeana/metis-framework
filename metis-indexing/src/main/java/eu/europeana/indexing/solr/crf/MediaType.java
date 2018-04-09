package eu.europeana.indexing.solr.crf;

/**
 * This class contains the types that are supported as technical metadata within records.
 * 
 * @author jochen
 *
 */
public enum MediaType {

  /** Audio (sound only) **/
  AUDIO(2),

  /** Video **/
  VIDEO(3),

  /** Images **/
  IMAGE(1),

  /** Text **/
  TEXT(4),

  /** Unknown type: not supported **/
  OTHER(0);

  private final int value;

  MediaType(final int value) {
    this.value = value;
  }

  public int getEncodedValue() {
    return value << TechnicalFacet.MEDIA_TYPE.getBitPos();
  }

  // TODO JOCHEN Merge and make library method for existing methods isText, isAudio/Video and
  // isImage in class MediaProcessor. Use this class there.
  /**
   * Converts the MIME type to a media type.
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
