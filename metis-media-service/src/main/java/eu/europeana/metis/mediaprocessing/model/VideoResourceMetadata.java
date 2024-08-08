package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.metis.schema.jibx.EdmType;

/**
 * Resource metadata for video resources.
 */
public class VideoResourceMetadata extends AbstractResourceMetadata {

  /**
   * Implements {@link java.io.Serializable}.
   */
  private static final long serialVersionUID = 2909859187992441100L;

  private Double duration;

  private Integer bitRate;

  private Integer width;

  private Integer height;

  private String codecName;

  private Double frameRate;

  /**
   * Constructor for the case no metadata or thumbnails is available.
   *
   * @param mimeType The resource mime type.
   * @param resourceUrl The resource URL.
   * @param contentSize The file content size.
   */
  public VideoResourceMetadata(String mimeType, String resourceUrl, Long contentSize) {
    this(mimeType, resourceUrl, contentSize, null, null, null, null, null, null);
  }

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
  public VideoResourceMetadata(String mimeType, String resourceUrl, Long contentSize,
      Double duration, Integer bitRate, Integer width, Integer height, String codecName,
      Double frameRate) {
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
    resource.setFrameRate(frameRate);
    resource.setEdmType(EdmType.VIDEO);
  }

  public Double getDuration() {
    return duration;
  }

  public Integer getBitRate() {
    return bitRate;
  }

  public Integer getWidth() {
    return width;
  }

  public Integer getHeight() {
    return height;
  }

  public String getCodecName() {
    return codecName;
  }

  public Double getFrameRate() {
    return frameRate;
  }
}
