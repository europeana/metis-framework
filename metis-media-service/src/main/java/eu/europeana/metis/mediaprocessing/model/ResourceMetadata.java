package eu.europeana.metis.mediaprocessing.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class represents resource metadata.
 */
public abstract class ResourceMetadata implements Serializable {

  /**
   * Implements {@link java.io.Serializable}.
   */
  private static final long serialVersionUID = 4578729338510378084L;

  private final String mimeType;

  private final String resourceUrl;

  private final long contentSize;

  private final Set<String> thumbnailTargetNames;

  /**
   * Constructor for a resource without thumbnails.
   *
   * @param mimeType The resource mime type.
   * @param resourceUrl The resource URL.
   * @param contentSize The file content size.
   */
  protected ResourceMetadata(String mimeType, String resourceUrl, long contentSize) {
    this(mimeType, resourceUrl, contentSize, null);
  }

  /**
   * Constructor.
   *
   * @param mimeType The resource mime type.
   * @param resourceUrl The resource URL.
   * @param contentSize The file content size.
   * @param thumbnails The thumbnails generated for this resource.
   */
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

  /**
   * @return The target names of the thumbnails. This list is not null, but could be empty.
   */
  public Set<String> getThumbnailTargetNames() {
    return Collections.unmodifiableSet(thumbnailTargetNames);
  }

  /**
   * This method copies the metadata to the resource. This method should be extended by subclasses.
   *
   * @param resource The resource to update.
   */
  protected void updateResource(WebResource resource) {
    resource.setMimeType(mimeType);
    resource.setFileSize(contentSize);
  }
}
