package eu.europeana.metis.mediaprocessing.extraction;

enum ResourceType {
  
  AUDIO, VIDEO, TEXT, IMAGE, UNKNOWN;

  static ResourceType getResourceType(String mimeType) {
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
  // TODO where should this method be?? In ResourceType?
  static boolean shouldDownloadMimetype(String mimeType) {
    // TODO also when type is UNKNOWN! So that we don't download something that is not processable.
    final ResourceType resourceType = getResourceType(mimeType);
    return ResourceType.AUDIO != resourceType && ResourceType.VIDEO != resourceType;
  }
}
