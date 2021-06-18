package eu.europeana.metis.mediaprocessing.http;

import eu.europeana.metis.network.AbstractHttpClient;
import java.net.URI;

/**
 * An {@link AbstractHttpClient} that checks whether a given resource link actually points to
 * content. It doesn't download the content, but just checks whether the link resolves. It expects
 * the URL itself as resource entry input.
 */
public class LinkCheckClient extends AbstractHttpClient<String, Void> {

  private static final int CONNECT_TIMEOUT = 2_000;
  private static final int RESPONSE_TIMEOUT = 5_000;
  private static final int REQUEST_TIMEOUT = 20_000;

  /**
   * Constructor.
   *
   * @param maxRedirectCount The maximum number of times we follow a redirect status (status 3xx).
   */
  public LinkCheckClient(int maxRedirectCount) {
    super(maxRedirectCount, CONNECT_TIMEOUT, RESPONSE_TIMEOUT, REQUEST_TIMEOUT);
  }

  @Override
  protected String getResourceUrl(String resourceEntry) {
    return resourceEntry;
  }

  @Override
  protected Void createResult(String providedLink, URI actualUri, String mimeType, Long fileSize,
      ContentRetriever contentRetriever) {
    return null;
  }
}
