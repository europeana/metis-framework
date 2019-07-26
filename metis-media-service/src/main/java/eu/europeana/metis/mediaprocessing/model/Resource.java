package eu.europeana.metis.mediaprocessing.model;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Set;

/**
 * This interface represents the binary content of a resource. Please see {@link ResourceRelatedFile} for
 * more information.
 */
public interface Resource extends ResourceRelatedFile {

  /**
   * @return The resource URL types with which this resource is referenced.
   */
  Set<UrlType> getUrlTypes();

  /**
   * @return The mime type that has been provided for this resource by the source server. If none is
   * provided, a default (non-null and non-blank) value is returned.
   */
  String getProvidedMimeType();

  /**
   * @return The file size that has been provided for this resource by the source server. If none is
   * provided, the value 0 is returned.
   */
  long getProvidedFileSize();

  /**
   * @return The actual location where this resource has been found.
   */
  URI getActualLocation();

  /**
   * @return A reference to the file containing this resource. Can be null.
   */
  Path getContentPath();

  /**
   * @return A reference to the file containing this resource. Can be null.
   */
  File getContentFile();

}
