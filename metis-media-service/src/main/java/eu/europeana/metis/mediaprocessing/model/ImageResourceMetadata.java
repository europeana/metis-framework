package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.metis.mediaprocessing.exception.MediaException;
import eu.europeana.metis.mediaprocessing.model.WebResource.ColorSpace;
import eu.europeana.metis.mediaprocessing.model.WebResource.Orientation;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ImageResourceMetadata extends ResourceMetadata {

  private final int width;

  private final int height;

  private final ColorSpace colorSpace;

  private final List<String> dominantColors;

  public ImageResourceMetadata(String mimeType, String resourceUrl, long contentSize, int width,
      int height, String colorSpace, List<String> dominantColors,
      List<? extends Thumbnail> thumbnails) throws MediaException {

    // Call super constructor.
    super(mimeType, resourceUrl, contentSize, thumbnails);

    // Set basic parameters.
    this.width = width;
    this.height = height;

    // Set dominant colors.
    final Optional<String> badColor = dominantColors.stream()
        .filter(color -> !color.matches("[0-9A-F]{6}")).findAny();
    if (badColor.isPresent()) {
      throw new MediaException("Color does not match the hexademic template",
          "Unrecognized hex String: " + badColor.get());
    }
    // TODO dominant colors start with '#' due to legacy systems
    this.dominantColors = dominantColors.stream().map(c -> "#" + c).collect(Collectors.toList());

    // Set color space.
    if ("grayscale".equals(colorSpace)) {
      this.colorSpace = ColorSpace.GRAYSCALE;
    } else if ("sRGB".equals(colorSpace)) {
      this.colorSpace = ColorSpace.S_RGB;
    } else {
      throw new MediaException("Failed to recognize color space",
          "Unrecognized color space: " + colorSpace);
    }
  }

  @Override
  protected void setSpecializedFieldsToResource(WebResource resource) {
    resource.setWidth(width);
    resource.setHeight(height);
    resource.setOrientation(width > height ? Orientation.LANDSCAPE : Orientation.PORTRAIT);
    resource.setColorspace(colorSpace);
    resource.setDominantColors(Collections.unmodifiableList(dominantColors));
  }
}
