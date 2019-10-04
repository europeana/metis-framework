package eu.europeana.metis.mediaprocessing.model;

/**
 * This class implements {@link Thumbnail}.
 */
public class ThumbnailImpl extends AbstractInMemoryFile implements Thumbnail {

  private final String mimeType;
  private final String targetName;

  /**
   * Constructor.
   *
   * @param resourceUrl The URL of the resource for which this thumbnail is generated.
   * @param mimeType The mime type of the thumbnail.
   * @param targetName The unique (target) name by which this thumbnail is known.
   */
  public ThumbnailImpl(String resourceUrl, String mimeType, String targetName) {
    super(resourceUrl);
    this.mimeType = mimeType;
    this.targetName = targetName;
  }

  @Override
  public String getTargetName() {
    return targetName;
  }

  @Override
  public String getMimeType() {
    return mimeType;
  }
}
