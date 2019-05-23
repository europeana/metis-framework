package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.mediaprocessing.exception.LinkCheckingException;
import java.io.Closeable;

/**
 * Implementations of this interface provide the link checking functionality. This object can be
 * reused multiple times, as the construction of it incurs overhead. Please note that this object is
 * not guaranteed to be thread-safe. Access to this object should be from one thread only, or
 * synchronized/locked.
 */
public interface LinkChecker extends Closeable {

  /**
   * Perform link checking on the given resource link.
   *
   * @param resourceEntry The resource entry (obtained from an RDF)
   * @throws LinkCheckingException In case of issues occurring during link checking.
   */
  void performLinkChecking(String resourceEntry) throws LinkCheckingException;

}
