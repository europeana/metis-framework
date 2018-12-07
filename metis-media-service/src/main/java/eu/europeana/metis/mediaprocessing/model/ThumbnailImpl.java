package eu.europeana.metis.mediaprocessing.model;

import java.io.IOException;

public class ThumbnailImpl extends TemporaryFileImpl implements Thumbnail {

  /**
   * The name this thumbnail should be stored under
   */
  private final String targetName;

  public ThumbnailImpl(String resourceUrl, String targetName) throws IOException {
    super(resourceUrl, "media_thumbnails", "thumb", ".tmp");
    this.targetName = targetName;
  }

  @Override
  public String getTargetName() {
    return targetName;
  }
}
