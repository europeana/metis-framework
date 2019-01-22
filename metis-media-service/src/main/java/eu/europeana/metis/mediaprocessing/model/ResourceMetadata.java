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

  private AudioResourceMetadata audioResourceMetadata = null;
  private ImageResourceMetadata imageResourceMetadata = null;
  private TextResourceMetadata textResourceMetadata = null;
  private VideoResourceMetadata videoResourceMetadata = null;

  ResourceMetadata(AudioResourceMetadata audioResourceMetadata) {
    if (audioResourceMetadata == null) {
      throw new IllegalArgumentException();
    }
    this.audioResourceMetadata = audioResourceMetadata;
  }

  ResourceMetadata(ImageResourceMetadata imageResourceMetadata) {
    if (imageResourceMetadata == null) {
      throw new IllegalArgumentException();
    }
    this.imageResourceMetadata = imageResourceMetadata;
  }

  ResourceMetadata(TextResourceMetadata textResourceMetadata) {
    if (textResourceMetadata == null) {
      throw new IllegalArgumentException();
    }
    this.textResourceMetadata = textResourceMetadata;
  }

  ResourceMetadata(VideoResourceMetadata videoResourceMetadata) {
    if (videoResourceMetadata == null) {
      throw new IllegalArgumentException();
    }
    this.videoResourceMetadata = videoResourceMetadata;
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
    return getMetaData().getResourceUrl();
  }

  @Override
  public Set<String> getThumbnailTargetNames() {
    return getMetaData().getThumbnailTargetNames();
  }
}
