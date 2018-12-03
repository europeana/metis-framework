package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.metis.mediaprocessing.exception.MediaException;
import eu.europeana.metis.mediaprocessing.temp.ThumbnailSource;

public class ResourceImpl extends TemporaryResourceFileImpl implements Resource, ThumbnailSource {

  private String mimeType;

  public ResourceImpl(String resourceUrl, String mimeType)
      throws MediaException {
    super(resourceUrl, "media_resources", "media", null);
    this.mimeType = mimeType;
  }

  @Override
  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }
}
