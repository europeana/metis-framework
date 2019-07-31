package eu.europeana.metis.mediaprocessing.model;

import java.io.Closeable;
import java.util.List;

/**
 * This interface represents the result of a resource extraction, consisting of metadata and
 * thumbnails. It is closable, which will have the effect of closing the thumbnails.
 */
public interface ResourceExtractionResult extends Closeable {

  /**
   * @return The serializable metadata of this resource. Can be null.
   */
  ResourceMetadata getMetadata();

  /**
   * @return The thumbnails generated for this resource. Can be null or empty.
   */
  List<Thumbnail> getThumbnails();
}
