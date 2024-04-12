package eu.europeana.metis.mediaprocessing.model;

import java.util.Set;

/**
 * This class wraps an instance of {@link AbstractResourceMetadata} in a class that can be used for
 * deserialization without knowing the type of the class.
 */
public class ResourceMetadata implements IResourceMetadata {

  /**
   * Implements {@link java.io.Serializable}.
   */
  private static final long serialVersionUID = 1648797505550562988L;

  private AudioResourceMetadata audioResourceMetadata;
  private ImageResourceMetadata imageResourceMetadata;
  private TextResourceMetadata textResourceMetadata;
  private VideoResourceMetadata videoResourceMetadata;
  private Media3dResourceMetadata threeDResourceMetadata;

  /**
   * Constructor for audio resources.
   *
   * @param audioResourceMetadata The resource metadata.
   */
  public ResourceMetadata(AudioResourceMetadata audioResourceMetadata) {
    if (audioResourceMetadata == null) {
      throw new IllegalArgumentException();
    }
    this.audioResourceMetadata = audioResourceMetadata;
  }

  /**
   * Constructor for image resources.
   *
   * @param imageResourceMetadata The resource metadata.
   */
  public ResourceMetadata(ImageResourceMetadata imageResourceMetadata) {
    if (imageResourceMetadata == null) {
      throw new IllegalArgumentException();
    }
    this.imageResourceMetadata = imageResourceMetadata;
  }

  /**
   * Constructor for text resources.
   *
   * @param textResourceMetadata The resource metadata.
   */
  public ResourceMetadata(TextResourceMetadata textResourceMetadata) {
    if (textResourceMetadata == null) {
      throw new IllegalArgumentException();
    }
    this.textResourceMetadata = textResourceMetadata;
  }

  /**
   * Constructor for video resources.
   *
   * @param videoResourceMetadata The resource metadata.
   */
  public ResourceMetadata(VideoResourceMetadata videoResourceMetadata) {
    if (videoResourceMetadata == null) {
      throw new IllegalArgumentException();
    }
    this.videoResourceMetadata = videoResourceMetadata;
  }

  /**
   * Constructor for 3D resources.
   *
   * @param threeDResourceMetadata The resource metadata.
   */
  public ResourceMetadata(Media3dResourceMetadata threeDResourceMetadata) {
    if (threeDResourceMetadata == null) {
      throw new IllegalArgumentException();
    }
    this.threeDResourceMetadata = threeDResourceMetadata;
  }

  /**
   * Constructor. Don't use this: it's required for deserialization.
   */
  ResourceMetadata() {
  }

  AbstractResourceMetadata getMetaData() {
    final AbstractResourceMetadata result;
    if (audioResourceMetadata != null) {
      result = audioResourceMetadata;
    } else if (imageResourceMetadata != null) {
      result = imageResourceMetadata;
    } else if (textResourceMetadata != null) {
      result = textResourceMetadata;
    } else if (videoResourceMetadata != null) {
      result = videoResourceMetadata;
    } else if (threeDResourceMetadata != null){
      result = threeDResourceMetadata;
    } else {
      throw new IllegalStateException();
    }
    return result;
  }

  @Override
  public String getResourceUrl() {
    return getMetaData().getResourceUrl();
  }

  @Override
  public String getMimeType() {
    return getMetaData().getMimeType();
  }

  @Override
  public Set<String> getThumbnailTargetNames() {
    return getMetaData().getThumbnailTargetNames();
  }
}
