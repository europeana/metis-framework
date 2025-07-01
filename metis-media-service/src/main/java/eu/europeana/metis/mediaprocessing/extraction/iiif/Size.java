package eu.europeana.metis.mediaprocessing.extraction.iiif;

/**
 * The type Size.
 */
public class Size {

  private int width;
  private int height;

  /**
   * Instantiates a new Size.
   */
  public Size() {
    // constructor
  }

  /**
   * Instantiates a new Size.
   *
   * @param width the width
   * @param height the height
   */
  public Size(int width, int height) {
    this.width = width;
    this.height = height;
  }

  /**
   * Gets width.
   *
   * @return the width
   */
  public int getWidth() {
    return width;
  }

  /**
   * Sets width.
   *
   * @param width the width
   */
  public void setWidth(int width) {
    this.width = width;
  }

  /**
   * Gets height.
   *
   * @return the height
   */
  public int getHeight() {
    return height;
  }

  /**
   * Sets height.
   *
   * @param height the height
   */
  public void setHeight(int height) {
    this.height = height;
  }

  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof Size size)) {
      return false;
    }

    return width == size.width && height == size.height;
  }

  @Override
  public int hashCode() {
    int result = width;
    result = 31 * result + height;
    return result;
  }
}
