package eu.europeana.indexing.solr.facet.value;

import eu.europeana.indexing.utils.WebResourceWrapper;

/**
 * This categorizes the size of images.
 */
public enum ImageSize implements FacetValue {

  SMALL(1), MEDIUM(2), LARGE(3), HUGE(4);

  /**
   * Image area (in pixels) that is considered huge: 4mp.
   **/
  private static final long IMAGE_HUGE_AREA = 4_000_000;

  /**
   * Image area (in pixels) that is considered large: 0.95mp.
   **/
  private static final long IMAGE_LARGE_AREA = 950_000;

  /**
   * Image area (in pixels) that is considered medium size: 0.42mp.
   **/
  private static final long IMAGE_MEDIUM_AREA = 420_000;

  /**
   * Image area (in pixels) that is considered small: 0.1mp.
   **/
  private static final long IMAGE_SMALL_AREA = 100_000;

  private int code;

  ImageSize(int code) {
    this.code = code;
  }

  @Override
  public int getCode() {
    return code;
  }

  /**
   * Categorize the size of the given image.
   *
   * @param imageSize The image size.
   * @return The category, or null if none of the categories apply.
   */
  public static ImageSize categorizeImageSize(final Long imageSize) {
    final ImageSize result;
    if (imageSize < IMAGE_SMALL_AREA) {
      result = null;
    } else if (imageSize < IMAGE_MEDIUM_AREA) {
      result = SMALL;
    } else if (imageSize < IMAGE_LARGE_AREA) {
      result = MEDIUM;
    } else if (imageSize < IMAGE_HUGE_AREA) {
      result = LARGE;
    } else {
      result = HUGE;
    }
    return result;
  }

  /**
   * Categorize the size of the given image.
   *
   * @param webResource The web resource.
   * @return The category, or null if none of the categories apply.
   */
  public static ImageSize categorizeImageSize(final WebResourceWrapper webResource) {
    return categorizeImageSize(webResource.getSize());
  }
}
