package eu.europeana.metis.mediaprocessing.model;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains the result of a resource extraction, consisting of metadata and thumbnails.
 */
public class ResourceExtractionResult implements Closeable {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceExtractionResult.class);
  
  private final AbstractResourceMetadata metadata;

  private final List<Thumbnail> thumbnails;

  /**
   * Constructor.
   *
   * @param metadata The metadata extracted for this resource. Can be null.
   * @param thumbnails The thumbnails generated for this resource. Can be null or empty, but does
   * not contain null values or thumbnails that have no content (see {@link
   * Thumbnail#hasContent()}).
   */
  public ResourceExtractionResult(AbstractResourceMetadata metadata,
      List<? extends Thumbnail> thumbnails) {
    this.metadata = metadata;
    this.thumbnails = thumbnails == null ? null : new ArrayList<>(thumbnails);
  }

  /**
   * @return The metadata of this resource. Can be null. Note: this object is not serializable. For
   * a serializable version please use {@link #getMetadata()}.
   */
  public AbstractResourceMetadata getOriginalMetadata() {
    return metadata;
  }

  /**
   * @return The serializable metadata of this resource. Can be null.
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
  
  @Override
  public void close() throws IOException {
    
    // Sanity check.
    if (thumbnails == null) {
      return;
    }
    
    // Save the first exception
    IOException exception = null;
    for (Thumbnail thumbnail : thumbnails) {
      try {
        thumbnail.close();
      } catch (IOException e) {
        exception = Optional.ofNullable(exception).orElse(e);
        LOGGER.warn("Could not close thumbnail: {}", thumbnail.getResourceUrl(), e);
      }
    }
    
    // If there is an exception, throw it.
    if (exception != null) {
      throw exception;
    }
  }
}
