package eu.europeana.metis.mediaprocessing.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ResourceMetadata {

  private final String mimeType;

  private final String resourceUrl;

  private final long contentSize;

  private final Set<String> thumbnailTargetNames;

  protected ResourceMetadata(String mimeType, String resourceUrl, long contentSize) {
    this(mimeType, resourceUrl, contentSize, null);
  }

  protected ResourceMetadata(String mimeType, String resourceUrl, long contentSize,
      Collection<? extends Thumbnail> thumbnails) {
    this.mimeType = mimeType;
    this.resourceUrl = resourceUrl;
    this.contentSize = contentSize;
    final Stream<? extends Thumbnail> thumbnailStream =
        thumbnails == null ? Stream.empty() : thumbnails.stream();
    this.thumbnailTargetNames = thumbnailStream.map(Thumbnail::getTargetName)
        .collect(Collectors.toSet());
  }

  public String getResourceUrl() {
    return resourceUrl;
  }

  public String getMimeType() {
    return mimeType;
  }

  // Is not null.
  public Set<String> getThumbnailTargetNames() {
    return Collections.unmodifiableSet(thumbnailTargetNames);
  }

  // Does not set or check the resource URL: the caller should make sure that the right
  // web resource is passed.
  final void updateResource(WebResource resource) {
    resource.setMimeType(mimeType);
    resource.setFileSize(contentSize);
    setSpecializedFieldsToResource(resource);
  }

  protected abstract void setSpecializedFieldsToResource(WebResource resource);
}
