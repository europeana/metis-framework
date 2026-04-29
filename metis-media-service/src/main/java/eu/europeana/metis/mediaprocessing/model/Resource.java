package eu.europeana.metis.mediaprocessing.model;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

/**
 * This interface represents the binary content of a resource. Please see {@link ResourceRelatedFile} for
 * more information.
 */
public interface Resource extends ResourceRelatedFile, RemoteResourceMetadata {

  /**
   * @return The resource URL types with which this resource is referenced.
   */
  Set<UrlType> getUrlTypes();

  /**
   * @return A reference to the file containing this resource. Can be null.
   */
  Path getContentPath();

  /**
   * @return A reference to the file containing this resource. Can be null.
   */
  File getContentFile();

}
