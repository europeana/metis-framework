package eu.europeana.metis.mediaprocessing.model;

import java.util.List;

public class Media3dResourceMetadata extends AbstractResourceMetadata{

  /**
   * Constructor.
   *
   * @param mimeType The resource mime type.
   * @param resourceUrl The resource URL.
   * @param contentSize The file content size.
   * @param thumbnails The thumbnails generated for this text resource.
   */
  public Media3dResourceMetadata(String mimeType, String resourceUrl, Long contentSize,
      List<? extends Thumbnail> thumbnails) {
    super(mimeType, resourceUrl, contentSize, thumbnails);
  }

  public Media3dResourceMetadata(String mimeType, String resourceUrl, Long contentSize) {
    super(mimeType, resourceUrl, contentSize, null);
  }
  @Override
  protected ResourceMetadata prepareForSerialization() {
    return new ResourceMetadata(this);
  }
}
