package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.metis.mediaservice.WebResource;

public class TextResourceMetadata extends ResourceMetadata {

  private final boolean containsText;

  private final Integer resolution;

  public TextResourceMetadata(String mimeType, String resourceUrl, long contentSize,
      boolean containsText, Integer resolution) {
    super(mimeType, resourceUrl, contentSize);
    this.containsText = containsText;
    this.resolution = resolution;
  }

  @Override
  protected void updateResource(WebResource resource) {
    resource.setContainsText(containsText);
    resource.setResolution(resolution);
  }
}
