package eu.europeana.metis.mediaprocessing.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Should be interface?
public class ResourceExtractionResult {

  // Can be null.
  private final ResourceMetadata metadata;

  // Can be null.
  private final List<Thumbnail> thumbnails;

  public ResourceExtractionResult(ResourceMetadata metadata, List<? extends Thumbnail> thumbnails) {
    this.metadata = metadata;
    this.thumbnails = thumbnails == null ? null : new ArrayList<>(thumbnails);
  }

  // Can be null.
  public ResourceMetadata getMetadata() {
    return metadata;
  }

  // Can be null.
  public List<Thumbnail> getThumbnails() {
    return thumbnails == null ? Collections.emptyList() : Collections.unmodifiableList(thumbnails);
  }
}
