package eu.europeana.metis.mediaprocessing.extraction.iiif;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The type Iiif info json base.
 */
public class IIIFInfoJsonBase {

  private String protocol;
  private int width;
  private int height;
  private List<Size> sizes;
  private List<Tile> tiles;

  /**
   * Gets protocol.
   *
   * @return the protocol
   */
  public String getProtocol() {
    return protocol;
  }

  /**
   * Sets protocol.
   *
   * @param protocol the protocol
   */
  public void setProtocol(String protocol) {
    this.protocol = protocol;
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
   * Gets sizes.
   *
   * @return the sizes
   */
  public List<Size> getSizes() {
    return Collections.unmodifiableList(sizes);
  }

  /**
   * Sets sizes.
   *
   * @param sizes the sizes
   */
  public void setSizes(List<Size> sizes) {
    this.sizes = Collections.unmodifiableList(sizes);
  }

  /**
   * Gets tiles.
   *
   * @return the tiles
   */
  public List<Tile> getTiles() {
    return Collections.unmodifiableList(tiles);
  }

  /**
   * Sets tiles.
   *
   * @param tiles the tiles
   */
  public void setTiles(List<Tile> tiles) {
    this.tiles = Collections.unmodifiableList(tiles);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof IIIFInfoJsonBase that)) {
      return false;
    }

    return width == that.width && height == that.height && protocol.equals(that.protocol) && Objects.equals(sizes,
        that.sizes) && Objects.equals(tiles, that.tiles);
  }

  @Override
  public int hashCode() {
    return Objects.hash(protocol, width, height, Objects.hashCode(sizes), Objects.hashCode(tiles));
  }
}
