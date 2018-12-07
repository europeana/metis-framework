package eu.europeana.metis.mediaprocessing.model;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * This interface is a representation of binary data associated with a given resource. Objects
 * implementing this interface are not required to be thread-safe. Please note that this extends the
 * {@link Closeable} interface, meaning that a resource file's {@link ResourceFile#close()} method
 * needs to be called when the caller has finished with it.
 */
public interface ResourceFile extends Closeable {

  /**
   * @return The resource URL of the resource with which this file is associated.
   */
  String getResourceUrl();

  /**
   * Obtains a stream for the contents of the file. Multiple streams can be created for the same
   * file. They should be closed by the caller, but closing them will not close this file. Closing
   * this file while content is being read has unspecified consequences and should be avoided.
   *
   * @return The content of the file.
   * @throws IOException In case there was a problem creating the input stream.
   */
  InputStream getContentStream() throws IOException;

  /**
   * @return The length of the content in bytes.
   * @throws IOException In case there was a problem obtaining the content's length.
   */
  long getContentSize() throws IOException;

  @Override
  void close() throws IOException;


}
