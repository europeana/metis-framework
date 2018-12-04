package eu.europeana.metis.mediaprocessing.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResourceProcessingResult {

  // Can be null.
  private final ResourceMetadata metadata;

  // Can be null.
  private final List<Thumbnail> thumbnails;

  public ResourceProcessingResult(ResourceMetadata metadata, List<? extends Thumbnail> thumbnails) {
    this.metadata = metadata;
    this.thumbnails = thumbnails == null ? null : new ArrayList<>(thumbnails);
  }

  public ResourceMetadata getMetadata() {
    return metadata;
  }

  public List<Thumbnail> getThumbnails() {
    return thumbnails == null ? Collections.emptyList() : Collections.unmodifiableList(thumbnails);
  }
}
