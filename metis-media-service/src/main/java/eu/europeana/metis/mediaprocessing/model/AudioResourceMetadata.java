package eu.europeana.metis.mediaprocessing.model;

/**
 * Resource metadata for audio resources.
 */
public class AudioResourceMetadata extends ResourceMetadata {

  /**
   * Implements {@link java.io.Serializable}
   */
  private static final long serialVersionUID = 7680381750866877618L;

  private final double duration;

  private final int bitRate;

  private final int channels;

  private final int sampleRate;

  private final int sampleSize;

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
   */
  public AudioResourceMetadata(String mimeType, String resourceUrl, long contentSize,
      double duration, int bitRate, int channels, int sampleRate, int sampleSize) {
    super(mimeType, resourceUrl, contentSize);
    this.duration = duration;
    this.bitRate = bitRate;
    this.channels = channels;
    this.sampleRate = sampleRate;
    this.sampleSize = sampleSize;
  }

  @Override
  protected void updateResource(WebResource resource) {
    super.updateResource(resource);
    resource.setDuration(duration);
    resource.setBitrate(bitRate);
    resource.setChannels(channels);
    resource.setSampleRate(sampleRate);
    resource.setSampleSize(sampleSize);
  }
}
