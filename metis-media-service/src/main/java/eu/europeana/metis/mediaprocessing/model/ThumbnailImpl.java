package eu.europeana.metis.mediaprocessing.model;

/**
 * This class implements {@link Thumbnail}.
 */
public class ThumbnailImpl extends AbstractInMemoryFile implements Thumbnail {

  private final String targetName;

  /**
   * Constructor.
   *
   * @param resourceUrl The URL of the resource for which this thumbnail is generated.
   * @param targetName The unique (target) name by which this thumbnail is known.
   */
  public ThumbnailImpl(String resourceUrl, String targetName) {
    super(resourceUrl);
    this.targetName = targetName;
  }

  @Override
  public String getTargetName() {
    return targetName;
  }
}
