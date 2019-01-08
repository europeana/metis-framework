package eu.europeana.metis.mediaprocessing.model;

import java.io.Serializable;
import java.util.Set;

/**
 * This interface represents resource metadata.
 */
interface IResourceMetadata extends Serializable {

  String getResourceUrl();

  String getMimeType();

  /**
   * @return The target names of the thumbnails. This list is not null, but could be empty.
   */
  Set<String> getThumbnailTargetNames();

}
