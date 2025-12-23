package eu.europeana.metis.mediaprocessing.model;

import java.net.URI;
import org.springframework.http.ContentDisposition;

/**
 * Implementations of this class represent the metadata of a remote resource that has been
 * accessed.
 */
public interface RemoteResourceMetadata {

  /**
   * @return The mime type that has been provided for this resource by the source server. If none is
   * provided, or a default type, null is returned.
   */
  String getProvidedMimeType();

  /**
   * @return The file size that has been provided for this resource by the source server. If none is
   * provided, the value 0 is returned.
   */
  Long getProvidedFileSize();

  /**
   * @return The content disposition header that has been provided for this resource by the source
   * server. If none is provided, the value <code>null</code> is returned.
   */
  ContentDisposition getProvidedContentDisposition();

  /**
   * @return The actual location where this resource has been found.
   */
  URI getActualLocation();
}
