package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.schema.jibx.ColorSpaceType;
import eu.europeana.metis.schema.model.Orientation;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Resource metadata for image resources.
 */
public class ImageResourceMetadata extends AbstractResourceMetadata {

  /**
   * Implements {@link java.io.Serializable}.
   */
  private static final long serialVersionUID = -1818426883878915580L;

  private Integer width;

  private Integer height;

  private ColorSpaceType colorSpace;

  private List<String> dominantColors;

  /**
   * Constructor for the case no metadata or thumbnails is available.
   *
   * @param mimeType The resource mime type.
   * @param resourceUrl The resource URL.
   * @param contentSize The file content size.
   * @throws MediaExtractionException In case there was a problem with the supplied data.
   */
  public ImageResourceMetadata(String mimeType, String resourceUrl, Long contentSize)
      throws MediaExtractionException {
    this(mimeType, resourceUrl, contentSize, null, null, null, null, null);
  }

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
  public ImageResourceMetadata(String mimeType, String resourceUrl, Long contentSize, Integer width,
      Integer height, ColorSpaceType colorSpace, List<String> dominantColors,
      List<? extends Thumbnail> thumbnails) throws MediaExtractionException {

    // Call super constructor.
    super(mimeType, resourceUrl, contentSize, thumbnails);

    // Set basic parameters.
    this.width = width;
    this.height = height;
    this.colorSpace = colorSpace;

    // Set dominant colors.
    if (dominantColors != null) {
      final Optional<String> badColor = dominantColors.stream()
          .filter(color -> !color.matches("[0-9A-F]{6}")).findAny();
      if (badColor.isPresent()) {
        throw new MediaExtractionException("Unrecognized hex String: " + badColor.get());
      }
      // TODO dominant colors start with '#' due to legacy systems
      this.dominantColors = dominantColors.stream().map(c -> "#" + c).toList();
    }
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
    final Orientation orientation =
        (width == null || height == null) ? null : Orientation.calculate(width, height);
    resource.setOrientation(orientation);
    resource.setColorspace(colorSpace);
    resource.setDominantColors(getDominantColors());
  }

  public Integer getWidth() {
    return width;
  }

  public Integer getHeight() {
    return height;
  }

  public ColorSpaceType getColorSpace() {
    return colorSpace;
  }

  public List<String> getDominantColors() {
    return dominantColors == null ? Collections.emptyList()
        : Collections.unmodifiableList(dominantColors);
  }
}
