package eu.europeana.indexing.search.v2.facet.value;

import eu.europeana.indexing.utils.WebResourceWrapper;
import eu.europeana.indexing.utils.WebResourceWrapper.ColorSpace;

/**
 * This categorizes the color space of images.
 */
public enum ImageColorSpace implements FacetValue {

  COLOR(1), GRAYSCALE(2), OTHER(3);

  private final int code;

  ImageColorSpace(int code) {
    this.code = code;
  }

  @Override
  public int getCode() {
    return code;
  }

  /**
   * Categorize the color space.
   *
   * @param colorSpace The color space.
   * @return The category, or null if none of the categories apply.
   */
  public static ImageColorSpace categorizeImageColorSpace(final ColorSpace colorSpace) {
      return switch (colorSpace) {
        case null -> null;
        case ColorSpace.COLOR -> ImageColorSpace.COLOR;
        case ColorSpace.GRAYSCALE -> ImageColorSpace.GRAYSCALE;
        case ColorSpace.OTHER -> ImageColorSpace.OTHER;
      };
  }

  /**
   * Categorize the color space of the given image.
   *
   * @param webResource The web resource.
   * @return The category, or null if none of the categories apply.
   */
  public static ImageColorSpace categorizeImageColorSpace(final WebResourceWrapper webResource) {
    return categorizeImageColorSpace(webResource.getColorSpace());
  }
}
