package eu.europeana.metis.mediaprocessing.extraction.iiif;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The type Tile.
 */
public class Tile {

  private int width;
  private int height;
  private List<Integer> scaleFactors;

  /**
   * Instantiates a new Tile.
   */
  public Tile() {
    // constructor
  }

  /**
   * Instantiates a new Tile.
   *
   * @param width the width
   * @param height the height
   * @param scaleFactors the scale factors
   */
  public Tile(int width, int height, List<Integer> scaleFactors) {
    this.width = width;
    this.height = height;
    this.scaleFactors = Collections.unmodifiableList(scaleFactors);
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

  /**
   * Gets scale factors.
   *
   * @return the scale factors
   */
  public List<Integer> getScaleFactors() {
    return Collections.unmodifiableList(scaleFactors);
  }

  /**
   * Sets scale factors.
   *
   * @param scaleFactors the scale factors
   */
  public void setScaleFactors(List<Integer> scaleFactors) {
    this.scaleFactors = Collections.unmodifiableList(scaleFactors);
  }

  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof Tile tile)) {
      return false;
    }

    return width == tile.width && height == tile.height && Objects.equals(scaleFactors, tile.scaleFactors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(width, height, scaleFactors);
  }
}
