package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.metis.schema.jibx.EdmType;

/**
 * Resource metadata for audio resources.
 */
public class AudioResourceMetadata extends AbstractResourceMetadata {

  /**
   * Implements {@link java.io.Serializable}
   */
  private static final long serialVersionUID = 7680381750866877618L;

  private Double duration;

  private Integer bitRate;

  private Integer channels;

  private Integer sampleRate;

  private Integer sampleSize;

  private String codecName;

  /**
   * Constructor for the case no metadata or thumbnails is available.
   *
   * @param mimeType The resource mime type.
   * @param resourceUrl The resource URL.
   * @param contentSize The file content size.
   */
  public AudioResourceMetadata(String mimeType, String resourceUrl, Long contentSize) {
    this(mimeType, resourceUrl, contentSize, null, null, null, null, null, null);
  }

  /**
   * Constructor.
   *
   * @param mimeType The resource mime type.
   * @param resourceUrl The resource URL.
   * @param contentSize The file content size.
   * @param duration The audio clip duration.
   * @param bitRate The audio clip bitrate.
   * @param channels The audio clip channel count.
   * @param sampleRate The audio clip sample rate.
   * @param sampleSize The audio clip sample size.
   * @param codecName The audio clip codec name.
   */
  public AudioResourceMetadata(String mimeType, String resourceUrl, Long contentSize,
      Double duration, Integer bitRate, Integer channels, Integer sampleRate, Integer sampleSize, String codecName) {
    super(mimeType, resourceUrl, contentSize);
    this.duration = duration;
    this.bitRate = bitRate;
    this.channels = channels;
    this.sampleRate = sampleRate;
    this.sampleSize = sampleSize;
    this.codecName = codecName;
  }

  /**
   * Constructor. Don't use this: it's required for deserialization.
   */
  AudioResourceMetadata() {
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
    resource.setChannels(channels);
    resource.setSampleRate(sampleRate);
    resource.setSampleSize(sampleSize);
    resource.setCodecName(codecName);
    resource.setEdmType(EdmType.SOUND);
  }

  public Double getDuration() {
    return duration;
  }

  public Integer getBitRate() {
    return bitRate;
  }

  public Integer getChannels() {
    return channels;
  }

  public Integer getSampleRate() {
    return sampleRate;
  }

  public Integer getSampleSize() {
    return sampleSize;
  }

  public String getCodecName() {
    return codecName;
  }
}
