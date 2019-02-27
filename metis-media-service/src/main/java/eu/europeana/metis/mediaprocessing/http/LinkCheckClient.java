package eu.europeana.metis.mediaprocessing.http;

import java.net.URI;

/**
 * An {@link AbstractHttpClient} that checks whether a given resource link actually points to content. It
 * doesn't download the content, but just checks whether the link resolves. It expects the URL
 * itself as resource entry input.
 */
public class LinkCheckClient extends AbstractHttpClient<String, Void> {

  private static final int CONNECT_TIMEOUT = 2_000;
  private static final int SOCKET_TIMEOUT = 5_000;

  /**
   * Constructor.
   *
   * @param maxRedirectCount The maximum number of times we follow a redirect status (status 3xx).
   */
  public LinkCheckClient(int maxRedirectCount) {
    super(maxRedirectCount, CONNECT_TIMEOUT, SOCKET_TIMEOUT);
  }

  @Override
  protected String getResourceUrl(String resourceEntry) {
    return resourceEntry;
  }

  @Override
  protected Void createResult(String resourceEntry, URI actualUri, String mimeType,
      ContentRetriever contentRetriever) {
    return null;
  }
}
