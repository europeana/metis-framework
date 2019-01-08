package eu.europeana.metis.mediaprocessing.model;

import java.net.URI;
import java.nio.file.Path;
import java.util.Set;

/**
 * This interface represents the binary content of a resource. Please see {@link ResourceFile} for
 * more information.
 */
public interface Resource extends ResourceFile {

  Set<UrlType> getUrlTypes();

  String getMimeType();

  URI getActualLocation();
  
  Path getContentPath();
  
  boolean hasContent();

}
