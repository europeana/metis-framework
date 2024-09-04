package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.metis.schema.jibx.EdmType;
import java.util.List;

/**
 * Resource metadata for text resources.
 */
public class TextResourceMetadata extends AbstractResourceMetadata {

  /**
   * Implements {@link java.io.Serializable}.
   */
  private static final long serialVersionUID = 96571759753604500L;

  private boolean containsText;

  private Integer resolution;

  /**
   * Constructor for the case no metadata or thumbnails is available.
   *
   * @param mimeType The resource mime type.
   * @param resourceUrl The resource URL.
   * @param contentSize The file content size.
   */
  public TextResourceMetadata(String mimeType, String resourceUrl, Long contentSize) {
    this(mimeType, resourceUrl, contentSize, false, null, null);
  }

  /**
   * Constructor.
   *
   * @param mimeType The resource mime type.
   * @param resourceUrl The resource URL.
   * @param contentSize The file content size.
   * @param containsText Whether the resource contains text.
   * @param resolution The resolution of the text resource.
   * @param thumbnails The thumbnails generated for this text resource.
   */
  public TextResourceMetadata(String mimeType, String resourceUrl, Long contentSize,
      boolean containsText, Integer resolution, List<? extends Thumbnail> thumbnails) {
    super(mimeType, resourceUrl, contentSize, thumbnails);
    this.containsText = containsText;
    this.resolution = resolution;
  }

  /**
   * Constructor. Don't use this: it's required for deserialization.
   */
  TextResourceMetadata() {
  }

  @Override
  protected ResourceMetadata prepareForSerialization() {
    return new ResourceMetadata(this);
  }

  @Override
  protected void updateResource(WebResource resource) {
    super.updateResource(resource);
    resource.setContainsText(containsText);
    resource.setResolution(resolution);
    resource.setEdmType(EdmType.TEXT);
  }

  /**
   * @return Whether or not this resource contains text.
   */
  public boolean containsText() {
    return containsText;
  }

  /**
   * @return The (graphic) resolution of this text resource, or null if this resource doesn't
   * contain graphics.
   */
  public Integer getResolution() {
    return resolution;
  }
}
