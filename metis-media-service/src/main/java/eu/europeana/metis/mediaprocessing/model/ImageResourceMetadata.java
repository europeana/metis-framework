package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.corelib.definitions.jibx.ColorSpaceType;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.model.WebResource.Orientation;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Resource metadata for image resources.
 */
public class ImageResourceMetadata extends AbstractResourceMetadata {

  /**
   * Implements {@link java.io.Serializable}.
   */
  private static final long serialVersionUID = -1818426883878915580L;

  private int width;

  private int height;

  private ColorSpaceType colorSpace;

  private List<String> dominantColors;

  /**
   * Constructor.
   *
   * @param mimeType The resource mime type.
   * @param resourceUrl The resource URL.
   * @param contentSize The file content size.
   * @param width The image width.
   * @param height The image height.
   * @param colorSpace The image color space.
   * @param dominantColors The image dominant colors.
   * @param thumbnails The thumbnails generated for this image.
   * @throws MediaExtractionException In case there was a problem with the supplied data.
   */
  public ImageResourceMetadata(String mimeType, String resourceUrl, long contentSize, int width,
      int height, ColorSpaceType colorSpace, List<String> dominantColors,
      List<? extends Thumbnail> thumbnails) throws MediaExtractionException {

    // Call super constructor.
    super(mimeType, resourceUrl, contentSize, thumbnails);

    // Set basic parameters.
    this.width = width;
    this.height = height;
    this.colorSpace = colorSpace;

    // Set dominant colors.
    final Optional<String> badColor = dominantColors.stream()
        .filter(color -> !color.matches("[0-9A-F]{6}")).findAny();
    if (badColor.isPresent()) {
      throw new MediaExtractionException("Unrecognized hex String: " + badColor.get());
    }
    // TODO dominant colors start with '#' due to legacy systems
    this.dominantColors = dominantColors.stream().map(c -> "#" + c).collect(Collectors.toList());
  }

  /**
   * Constructor. Don't use this: it's required for deserialization.
   */
  ImageResourceMetadata() {
  }

  @Override
  protected ResourceMetadata prepareForSerialization() {
    return new ResourceMetadata(this);
  }

  @Override
  protected void updateResource(WebResource resource) {
    super.updateResource(resource);
    resource.setWidth(width);
    resource.setHeight(height);
    resource.setOrientation(width > height ? Orientation.LANDSCAPE : Orientation.PORTRAIT);
    resource.setColorspace(colorSpace);
    resource.setDominantColors(Collections.unmodifiableList(dominantColors));
  }
}
