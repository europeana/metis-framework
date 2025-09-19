package eu.europeana.metis.dereference;

import eu.europeana.metis.network.StringHttpClient;
import eu.europeana.metis.network.StringHttpClient.StringContent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

/**
 * Helper class to retrieve a remote unmapped entity Created by ymamakis on 2/11/16.
 */
public class RdfRetriever {

  private static final int MAX_NUMBER_OF_REDIRECTS = 5;
  private static final int CONNECT_TIMEOUT = 10_000;
  private static final int RESPONSE_TIMEOUT = 20_000;
  private static final int REQUEST_TIMEOUT = 60_000;
  private static final String DEFAULT_USER_AGENT = "MetisDereferencer/1.0 (Europeana Foundation)";

  /**
   * Retrieve a remote entity from a resource as a String. We try every suffix in a random order
   * until we find one that works (i.e. yield a non-null result that is not HTML).
   *
   * @param resourceId The remote entity to retrieve (resource IDs are in fact URIs)
   * @param suffix     The suffix to append to the entity to form the remote address. Can be null.
   * @param userAgent  The custom user agent to use. If null, the default user agent will be set.
   * @return The original entity containing a string representation of the remote entity. This
   * method does not return null.
   * @throws IOException If there was an issue retrieving the resource or if the provided resource
   *                     ID - suffix combination does not form a valid URI.
   */
  public String retrieve(String resourceId, String suffix, String userAgent) throws IOException {
    if (resourceId == null) {
      throw new IllegalArgumentException("Parameter resourceId cannot be null.");
    }
    final URI resourceUri;
    try {
      resourceUri = new URI(resourceId + (suffix == null ? "" : suffix));
    } catch (URISyntaxException e) {
      throw new IOException(e.getMessage(), e);
    }
    return retrieveFromSource(resourceUri, userAgent == null ? DEFAULT_USER_AGENT : userAgent);
  }

  private static String retrieveFromSource(URI resourceUri, String userAgent) throws IOException {

    // Obtain the response.
    final Map<String, String> headers = Map.of(
        "Accept", "application/rdf+xml",
        "User-Agent", Optional.ofNullable(userAgent).orElse(DEFAULT_USER_AGENT));
    final StringContent result;
    try (final StringHttpClient client = new StringHttpClient(MAX_NUMBER_OF_REDIRECTS,
        CONNECT_TIMEOUT, RESPONSE_TIMEOUT, REQUEST_TIMEOUT)) {
      result = client.download(resourceUri, headers);
    }

    // Process the response.
    if (StringUtils.isBlank(result.getContent())) {
      throw new IOException("Could not retrieve the entity: it is empty.");
    } else if (Strings.CS.startsWith(result.getContentType(), "text/html")
        || result.getContent().contains("<html>")) {
      throw new IOException("Could not retrieve the entity: seems to be an HTML document.");
    }
    return result.getContent();
  }
}
