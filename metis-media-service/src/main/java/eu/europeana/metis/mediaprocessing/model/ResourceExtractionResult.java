package eu.europeana.metis.mediaprocessing.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class contains the result of a resource extraction, consisting of metadata and thumbnails.
 */
public class ResourceExtractionResult {

  private final AbstractResourceMetadata metadata;

  private final List<Thumbnail> thumbnails;

  /**
   * Constructor.
   *
   * @param metadata The metadata extracted for this resource. Can be null.
   * @param thumbnails The thumbnails generated for this resource. Can be null.
   */
  public ResourceExtractionResult(AbstractResourceMetadata metadata,
      List<? extends Thumbnail> thumbnails) {
    this.metadata = metadata;
    this.thumbnails = thumbnails == null ? null : new ArrayList<>(thumbnails);
  }

  /**
   * @return The metadata of this resource. Can be null.
   */
  public ResourceMetadata getMetadata() {
    return metadata == null ? null : metadata.prepareForSerialization();
  }

  /**
   * @return The thumbnails generated for this resource. Can be null or empty.
   */
  public List<Thumbnail> getThumbnails() {
    return thumbnails == null ? null : Collections.unmodifiableList(thumbnails);
  }
}
