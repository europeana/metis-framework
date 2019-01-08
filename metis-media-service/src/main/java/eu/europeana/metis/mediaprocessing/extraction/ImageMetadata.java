package eu.europeana.metis.mediaprocessing.extraction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Objects of this type represent image information obtained while creating the thumbnails.
 * 
 * @author jochen
 */
class ImageMetadata {

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
  ImageMetadata(int width, int height, String colorSpace, List<String> dominantColors) {
    this.width = width;
    this.height = height;
    this.colorSpace = colorSpace;
    this.dominantColors = new ArrayList<>(dominantColors);
  }

  int getWidth() {
    return width;
  }

  int getHeight() {
    return height;
  }

  String getColorSpace() {
    return colorSpace;
  }

  List<String> getDominantColors() {
    return Collections.unmodifiableList(dominantColors);
  }
}
