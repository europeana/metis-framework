package eu.europeana.metis.mediaprocessing.linkchecking;

import java.io.Closeable;
import java.io.IOException;
import eu.europeana.metis.mediaprocessing.LinkChecker;
import eu.europeana.metis.mediaprocessing.exception.LinkCheckingException;
import eu.europeana.metis.mediaprocessing.http.LinkCheckClient;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;

// TODO should we first try a HEAD request before a GET? Pro: it may be faster. Con: it is not
// definitive as maybe the HEAD works but the GET doesn't. Solution: Maintain two LinkCheckClient
// objects: one for HEAD requests and one for GET requests.
public class LinkCheckerImpl implements LinkChecker, Closeable {

  private final LinkCheckClient linkCheckClient;

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
