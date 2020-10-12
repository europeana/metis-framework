package eu.europeana.metis.mediaprocessing.model;

/**
 * The various thumbnail kinds we generate.
 */
public enum ThumbnailKind {

  /**
   * The medium sized thumbnail.
   **/
  MEDIUM(200, "-MEDIUM"),

  /**
   * The large sized thumbnail.
   **/
  LARGE(400, "-LARGE");

  private final int imageSize;
  private final String nameSuffix;

  ThumbnailKind(int imageSize, String nameSuffix) {
    this.imageSize = imageSize;
    this.nameSuffix = nameSuffix;
  }

  /**
   * @return The image size.
   */
  public int getImageSize() {
    return imageSize;
  }

  /**
   * @return The name suffix. Thumbnails of this kind must contain (end with) this suffix.
   */
  public String getNameSuffix() {
    return nameSuffix;
  }
}
