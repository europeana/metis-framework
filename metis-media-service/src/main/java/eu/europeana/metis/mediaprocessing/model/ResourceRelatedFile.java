package eu.europeana.metis.mediaprocessing.model;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>
 * This interface is a representation of binary data associated with a given resource. Objects
 * implementing this interface are not required to be thread-safe.
 * </p>
 * <p>
 * Please note that this extends the {@link Closeable} interface, meaning that a resource file's
 * {@link ResourceRelatedFile#close()} method needs to be called when the caller has finished with it.
 * </p>
 */
interface ResourceRelatedFile extends Closeable {

  /**
   * @return The resource URL of the resource with which this file is associated.
   */
  String getResourceUrl();

  /**
   * @return Whether or not this resource file has non-empty content.
   * @throws IOException In case there was a problem checking the content file's existence and
   * size.
   */
  boolean hasContent() throws IOException;

  /**
   * Obtains a stream for the contents of the file. Multiple streams can be created for the same
   * file. They should be closed by the caller, but closing them will not close this file. Closing
   * this file while content is being read has unspecified consequences and should be avoided.
   *
   * @return The content of the file. Is not null.
   * @throws IOException In case there was a problem creating the input stream, if there is no
   * content (see {@link #hasContent()}) or if this object's {@link #close()} or {@link
   * #markAsNoContent()} method was called.
   */
  InputStream getContentStream() throws IOException;

  /**
   * @return The length of the content in bytes. Returns a number greater than or equal to 0.
   * @throws IOException In case there was a problem obtaining the content's length or if this
   * object's {@link #close()} or {@link #markAsNoContent()} method was called.
   */
  Long getContentSize() throws IOException;

  /**
   * Remove any content and make this resource one without content. From now on, {@link
   * #hasContent()} will return false. Calling this method a second time has no effect.
   *
   * @throws IOException In case there was a problem removing the content.
   */
  void markAsNoContent() throws IOException;

  /**
   * Add the given content and make this resource one with content. From now on,
   * {@link #hasContent()} will return true. Any already existing content will be overwritten.
   * 
   * @param content The new content. Note: the input stream is not closed.
   * @throws IOException In case there was a problem adding the content.
   */
  void markAsWithContent(InputStream content) throws IOException;

  @Override
  void close() throws IOException;

}
