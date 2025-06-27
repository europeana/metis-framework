package eu.europeana.metis.mediaprocessing.extraction.iiif;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;

/**
 * The type IIIf info json model v2.
 */
public class IIIFInfoJsonV2 {

  @JsonProperty("@context")
  private String context;
  @JsonProperty("@id")
  private String id;
  private String protocol;
  private int width;
  private int height;
  private List<String> profile;
  private List<Size> sizes;
  private List<Tile> tiles;

  /**
   * Gets context.
   *
   * @return the context
   */
  public String getContext() {
    return context;
  }

  /**
   * Sets context.
   *
   * @param context the context
   */
  public void setContext(String context) {
    this.context = context;
  }

  /**
   * Gets id.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets id.
   *
   * @param id the id
   */
  public void setId(String id) {
    this.id = id;
  }

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

  /**
   * Gets profile.
   *
   * @return the profile
   */
  public List<String> getProfile() {
    return Collections.unmodifiableList(profile);
  }

  /**
   * Sets profile.
   *
   * @param profile the profile
   */
  public void setProfile(List<String> profile) {
    this.profile = Collections.unmodifiableList(profile);
  }

  /**
   * The type Size.
   */
  // Nested Classes
  public static class Size {
    private int width;
    private int height;

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
  }

  /**
   * The type Tile.
   */
  public static class Tile {
    private int width;
    private int height;
    private List<Integer> scaleFactors;

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
  }
}
