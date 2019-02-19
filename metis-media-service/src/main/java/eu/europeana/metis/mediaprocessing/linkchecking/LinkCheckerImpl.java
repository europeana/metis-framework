package eu.europeana.metis.mediaprocessing.linkchecking;

import java.io.Closeable;
import java.io.IOException;
import eu.europeana.metis.mediaprocessing.LinkChecker;
import eu.europeana.metis.mediaprocessing.exception.LinkCheckingException;
import eu.europeana.metis.mediaprocessing.http.LinkCheckClient;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;

/**
 * This class performs link checking.
 * 
 * Note: previously it was implemented by first trying a HEAD request, and only if that fails do we
 * do a GET. The thinking was probably that a HEAD is typically faster than a GET. However, since we
 * are not downloading any content it may be more accurate (at the cost of probably only a small
 * delay) to do the GET request right away: this is after all what the link represents. In case we
 * wish to introduce the HEAD request again, the solution would be to maintain two versions of the
 * LinkCheckClient: one that does HEAD requests, and one that does GET requests.
 */
public class LinkCheckerImpl implements LinkChecker {

  private final LinkCheckClient linkCheckClient;

  /**
   * Constructor.
   * 
   * @param maxRedirectCount The maximum number of times we follow a redirect status (status 3xx).
   */
  public LinkCheckerImpl(int maxRedirectCount) {
    linkCheckClient = new LinkCheckClient(maxRedirectCount);
  }

  @Override
  public void performLinkChecking(RdfResourceEntry resourceEntry) throws LinkCheckingException {
    try {
      linkCheckClient.download(resourceEntry);
    } catch (IOException | RuntimeException e) {
      throw new LinkCheckingException(e);
    }
  }

  @Override
  public void close() throws IOException {
    linkCheckClient.close();
  }
}
