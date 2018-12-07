package eu.europeana.metis.mediaprocessing.model;

import java.io.IOException;

/**
 * This class implements {@link Thumbnail}.
 */
public class ThumbnailImpl extends TemporaryFile implements Thumbnail {

  private final String targetName;

  /**
   * Constructor.
   *
   * @param resourceUrl The URL of the resource for which this thumbnail is generated.
   * @param targetName The unique (target) name by which this thumbnail is known.
   * @throws IOException In case the thumbnail could not be created.
   */
  public ThumbnailImpl(String resourceUrl, String targetName) throws IOException {
    super(resourceUrl, "media_thumbnails", "thumb", ".tmp");
    this.targetName = targetName;
  }

  @Override
  public String getTargetName() {
    return targetName;
  }
}
