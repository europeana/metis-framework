package eu.europeana.indexing.solr.facet.value;

import eu.europeana.indexing.utils.WebResourceWrapper;
import eu.europeana.indexing.utils.WebResourceWrapper.ColorSpace;

/**
 * This categorizes the color space of images.
 */
public enum ImageColorSpace implements FacetValue {

  COLOR(1), GRAYSCALE(2), OTHER(3);

  private int code;

  ImageColorSpace(int code) {
    this.code = code;
  }

  @Override
  public int getCode() {
    return code;
  }

  /**
   * Categorize the color space of the given image.
   *
   * @param webResource The web resource.
   * @return The category, or null if none of the categories apply.
   */
  public static ImageColorSpace categorizeImageColorSpace(final WebResourceWrapper webResource) {
    final ColorSpace colorSpace = webResource.getColorSpace();
    final ImageColorSpace result;
    if (ColorSpace.COLOR == colorSpace) {
      result = COLOR;
    } else if (ColorSpace.GRAYSCALE == colorSpace) {
      result = GRAYSCALE;
    } else if (ColorSpace.OTHER == colorSpace) {
      result = OTHER;
    } else {
      result = null;
    }
    return result;
  }
}
