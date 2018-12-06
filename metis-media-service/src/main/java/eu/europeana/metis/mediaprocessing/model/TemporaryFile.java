package eu.europeana.metis.mediaprocessing.model;

import java.io.Closeable;
import java.io.IOException;
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
 * therefore always be removed (using {@link TemporaryFile#close()}).
 * </p>
 */
public interface TemporaryFile extends Closeable {

  String getResourceUrl();

  InputStream getContentStream() throws IOException;

  long getContentSize() throws IOException;

  boolean hasContent();

  @Override
  void close() throws IOException;
}
