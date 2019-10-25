package eu.europeana.indexing.solr.facet.value;

import eu.europeana.indexing.utils.WebResourceWrapper;
import eu.europeana.indexing.utils.WebResourceWrapper.Orientation;

/**
 * This categorizes the aspect ratio of images.
 */
public enum ImageAspectRatio implements FacetValue {

  PORTRAIT(1), LANDSCAPE(2);

  private int code;

  ImageAspectRatio(int code) {
    this.code = code;
  }

  @Override
  public int getCode() {
    return code;
  }

  /**
   * Categorize the aspect ratio.
   *
   * @param orientation The orientation.
   * @return The category, or null if none of the categories apply.
   */
  public static ImageAspectRatio categorizeImageAspectRatio(Orientation orientation) {
    final ImageAspectRatio result;
    if (Orientation.PORTRAIT == orientation) {
      result = PORTRAIT;
    } else if (Orientation.LANDSCAPE == orientation) {
      result = LANDSCAPE;
    } else {
      result = null;
    }
    return result;
  }

  /**
   * Categorize the aspect ratio of the given image.
   *
   * @param webResource The web resource.
   * @return The category, or null if none of the categories apply.
   */
  public static ImageAspectRatio categorizeImageAspectRatio(final WebResourceWrapper webResource) {
    return categorizeImageAspectRatio(webResource.getOrientation());
  }
}
