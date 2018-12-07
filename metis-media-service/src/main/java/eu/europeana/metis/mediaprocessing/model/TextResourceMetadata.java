package eu.europeana.metis.mediaprocessing.model;

import java.util.List;

public class TextResourceMetadata extends ResourceMetadata {

  private final boolean containsText;

  private final Integer resolution;

  public TextResourceMetadata(String mimeType, String resourceUrl, long contentSize,
      boolean containsText, Integer resolution, List<? extends Thumbnail> thumbnails) {
    super(mimeType, resourceUrl, contentSize, thumbnails);
    this.containsText = containsText;
    this.resolution = resolution;
  }

  @Override
  protected void setSpecializedFieldsToResource(WebResource resource) {
    resource.setContainsText(containsText);
    resource.setResolution(resolution);
  }
}
