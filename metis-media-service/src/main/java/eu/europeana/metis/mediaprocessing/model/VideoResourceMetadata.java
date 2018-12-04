package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.metis.mediaservice.WebResource;

public class VideoResourceMetadata extends ResourceMetadata {

  private final double duration;

  private final int bitRate;

  private final int width;

  private final int height;

  private final String codecName;

  private final double frameRate;

  public VideoResourceMetadata(String mimeType, String resourceUrl, long contentSize,
      double duration, int bitRate, int width, int height, String codecName, double frameRate) {
    super(mimeType, resourceUrl, contentSize);
    this.duration = duration;
    this.bitRate = bitRate;
    this.width = width;
    this.height = height;
    this.codecName = codecName;
    this.frameRate = frameRate;
  }

  @Override
  protected void updateResource(WebResource resource) {
    resource.setDuration(duration);
    resource.setBitrate(bitRate);
    resource.setWidth(width);
    resource.setHeight(height);
    resource.setCodecName(codecName);
    resource.setFrameRete(frameRate);
  }
}
