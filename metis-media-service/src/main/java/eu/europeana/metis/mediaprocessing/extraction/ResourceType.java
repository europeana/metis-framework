package eu.europeana.metis.mediaprocessing.extraction;

/**
 * Instances of this enum represent resource classes.
 *
 * TODO is a duplicate of MediaType in the indexing library.
 */
public enum ResourceType {

  /** Audio resources **/
  AUDIO,

  /** Video resources **/
  VIDEO,

  /** Text resources (including PDFs) **/
  TEXT,

  /** Graphical resources **/
  IMAGE,

  /** Resources that are not of any of the other kinds. **/
  UNKNOWN;

  /**
   * Obtains the resource type of a given mime type.
   * 
   * @param mimeType The mime type.
   * @return The resource type to which the mime type belongs.
   */
  public static ResourceType getResourceType(String mimeType) {
    if (mimeType == null) {
      return ResourceType.UNKNOWN;
    }
    final ResourceType result;
    if (mimeType.startsWith("image/")) {
      result = ResourceType.IMAGE;
    } else if (mimeType.startsWith("audio/")) {
      result = ResourceType.AUDIO;
    } else if (mimeType.startsWith("video/")) {
      result = ResourceType.VIDEO;
    } else if (isText(mimeType)) {
      result = ResourceType.TEXT;
    } else {
      result = ResourceType.UNKNOWN;
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

  /**
   * @return true if and only if resources of the given type need to be downloaded before
   *         processing.
   */
  static boolean shouldDownloadMimetype(String mimeType) {
    final ResourceType resourceType = getResourceType(mimeType);
    return ResourceType.IMAGE == resourceType || ResourceType.TEXT == resourceType;
  }
}
