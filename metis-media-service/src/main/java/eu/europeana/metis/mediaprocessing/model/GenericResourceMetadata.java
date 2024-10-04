package eu.europeana.metis.mediaprocessing.model;

import java.io.Serial;
import java.util.List;

/**
 * Resource metadata instance for when there is no specific resource type known.
 */
public class GenericResourceMetadata extends AbstractResourceMetadata {

  @Serial
  private static final long serialVersionUID = 1594698571287313160L;

  /**
   * Constructor for the case no thumbnails are available.
   *
   * @param mimeType The resource mime type.
   * @param resourceUrl The resource URL.
   * @param contentSize The file content size.
   */
  public GenericResourceMetadata(String mimeType, String resourceUrl, Long contentSize) {
    this(mimeType, resourceUrl, contentSize, null);
  }

  /**
   * Constructor.
   *
   * @param mimeType The resource mime type.
   * @param resourceUrl The resource URL.
   * @param contentSize The file content size.
   * @param thumbnails The thumbnails generated for this text resource.
   */
  public GenericResourceMetadata(String mimeType, String resourceUrl, Long contentSize,
      List<? extends Thumbnail> thumbnails) {
    super(mimeType, resourceUrl, contentSize, thumbnails);
  }

  /**
   * Constructor. Don't use this: it's required for deserialization.
   */
  GenericResourceMetadata() {
  }

  @Override
  protected ResourceMetadata prepareForSerialization() {
    return new ResourceMetadata(this);
  }
}
