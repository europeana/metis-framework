package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.metis.mediaprocessing.exception.MediaException;

public class ThumbnailImpl extends TemporaryResourceFileImpl implements Thumbnail {

  /**
   * The name this thumbnail should be stored under
   */
  private final String targetName;

  public ThumbnailImpl(String resourceUrl, String targetName) throws MediaException {
    super(resourceUrl, "media_thumbnails", "thumb", ".tmp");
    this.targetName = targetName;
  }

  @Override
  public String getTargetName() {
    return targetName;
  }
}
