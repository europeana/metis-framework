package eu.europeana.metis.mediaservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Objects of this type represent image information obtained while creating the thumbnails.
 * 
 * @author jochen
 */
public class ImageMetadata {

  private final int width;
  private final int height;
  private final String colorSpace;
  private final List<String> dominantColors;

  /**
   * Constructor.
   * 
   * @param width The width of the image.
   * @param height The height of the image.
   * @param colorSpace The color space.
   * @param dominantColors The dominant colors.
   */
  public ImageMetadata(int width, int height, String colorSpace, List<String> dominantColors) {
    this.width = width;
    this.height = height;
    this.colorSpace = colorSpace;
    this.dominantColors = new ArrayList<>(dominantColors);
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public String getColorSpace() {
    return colorSpace;
  }

  public List<String> getDominantColors() {
    return Collections.unmodifiableList(dominantColors);
  }
}
