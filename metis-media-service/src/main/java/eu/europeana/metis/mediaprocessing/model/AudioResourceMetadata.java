package eu.europeana.metis.mediaprocessing.model;

public class AudioResourceMetadata extends ResourceMetadata {

  private final double duration;

  private final int bitRate;

  private final int channels;

  private final int sampleRate;

  private final int sampleSize;

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
  protected void setSpecializedFieldsToResource(WebResource resource) {
    resource.setDuration(duration);
    resource.setBitrate(bitRate);
    resource.setChannels(channels);
    resource.setSampleRate(sampleRate);
    resource.setSampleSize(sampleSize);
  }
}
