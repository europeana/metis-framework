package eu.europeana.metis.mediaprocessing.model;

public class Media3dResourceMetadata extends AbstractResourceMetadata{

  /**
   * Implements {@link java.io.Serializable}.
   */
  private static final long serialVersionUID = -2887423565606864723L;

  /**
   * Constructor.
   *
   * @param mimeType The resource mime type.
   * @param resourceUrl The resource URL.
   * @param contentSize The file content size.
   */
  public Media3dResourceMetadata(String mimeType, String resourceUrl, Long contentSize) {
    super(mimeType, resourceUrl, contentSize, null);
  }
  @Override
  protected ResourceMetadata prepareForSerialization() {
    return new ResourceMetadata(this);
  }
}
