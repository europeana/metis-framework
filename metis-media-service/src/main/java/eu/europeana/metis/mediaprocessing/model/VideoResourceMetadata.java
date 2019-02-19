package eu.europeana.metis.mediaprocessing.model;

/**
 * Resource metadata for video resources.
 */
public class VideoResourceMetadata extends AbstractResourceMetadata {

  /**
   * Implements {@link java.io.Serializable}.
   */
  private static final long serialVersionUID = 2909859187992441100L;

  private double duration;

  private int bitRate;

  private int width;

  private int height;

  private String codecName;

  private double frameRate;

  /**
   * Constructor.
   *
   * @param mimeType The resource mime type.
   * @param resourceUrl The resource URL.
   * @param contentSize The file content size.
   * @param duration The video clip duration.
   * @param bitRate The video clip bitrate.
   * @param width The video clip width.
   * @param height The video clip height.
   * @param codecName The video clip codec name.
   * @param frameRate The video clip frame rate.
   */
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
  
  /**
   * Constructor. Don't use this: it's required for deserialization.
   */
  VideoResourceMetadata() {
  }

  @Override
  protected ResourceMetadata prepareForSerialization() {
    return new ResourceMetadata(this);
  }

  @Override
  protected void updateResource(WebResource resource) {
    super.updateResource(resource);
    resource.setDuration(duration);
    resource.setBitrate(bitRate);
    resource.setWidth(width);
    resource.setHeight(height);
    resource.setCodecName(codecName);
    resource.setFrameRete(frameRate);
  }

  public double getDuration() {
    return duration;
  }

  public int getBitRate() {
    return bitRate;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public String getCodecName() {
    return codecName;
  }

  public double getFrameRate() {
    return frameRate;
  }
}
