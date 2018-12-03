package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import java.io.InputStream;

/**
 * <p>
 * This class contains a representation of a resource file, with the source URL. The file is not
 * necessarily equal to the contents of the resource, just something associated with it.
 * </p>
 * <p>
 * Note that implementations of this interface are not required to be thread-safe.
 * </p>
 * <p>
 * Please note that this represents a temporary file. It is created upon construction and should
 * therefore always be removed (using {@link TemporaryResourceFile#deleteFile()}).
 * </p>
 */
public interface TemporaryResourceFile {

  String getResourceUrl();

  InputStream getContentStream() throws MediaProcessorException;

  long getContentSize() throws MediaProcessorException;

  void deleteFile() throws MediaProcessorException;

  boolean hasContent();
}
