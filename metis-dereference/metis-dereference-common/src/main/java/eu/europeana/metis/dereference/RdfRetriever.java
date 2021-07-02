package eu.europeana.metis.dereference;

import eu.europeana.metis.network.StringHttpClient;
import eu.europeana.metis.network.StringHttpClient.StringContent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * Helper class to retrieve a remote unmapped entity Created by ymamakis on 2/11/16.
 */
public class RdfRetriever {

  private static final int MAX_NUMBER_OF_REDIRECTS = 5;
  private static final int CONNECT_TIMEOUT = 10_000;
  private static final int RESPONSE_TIMEOUT = 20_000;
  private static final int REQUEST_TIMEOUT = 60_000;

  /**
   * Retrieve a remote entity from a resource as a String. We try every suffix in a random order
   * until we find one that works (i.e. yield a non-null result that is not HTML).
   *
   * @param resourceId The remote entity to retrieve (resource IDs are in fact URIs)
   * @param suffix The suffix to append to the entity to form the remote address. Can be null.
   * @return The original entity containing a string representation of the remote entity. This
   * method does not return null.
   * @throws IOException In case there was an issue retrieving the resource.
   * @throws URISyntaxException In case the provided resource ID - suffix combination does not form
   * a valid URI.
   */
  public String retrieve(String resourceId, String suffix) throws IOException, URISyntaxException {
    return retrieveFromSource(resourceId, suffix == null ? "" : suffix);
  }

  private static String retrieveFromSource(String resourceId, String suffix)
          throws IOException, URISyntaxException {

    // Check the input
    if (resourceId == null) {
      throw new IllegalArgumentException("Parameter resourceId cannot be null.");
    }
    if (suffix == null) {
      throw new IllegalArgumentException("Parameter suffix cannot be null.");
    }

    // Obtain the response.
    final StringContent result;
    try (final StringHttpClient client = new StringHttpClient(MAX_NUMBER_OF_REDIRECTS,
            CONNECT_TIMEOUT, RESPONSE_TIMEOUT, REQUEST_TIMEOUT)) {
      result = client
              .download(new URI(resourceId + suffix), Map.of("Accept", "application/rdf+xml"));
    }

    // Process the response.
    if (StringUtils.isBlank(result.getContent())) {
      throw new IOException("Could not retrieve the entity: it is empty.");
    } else if (StringUtils.startsWith(result.getContentType(), "text/html") || result.getContent()
            .contains("<html>")) {
      throw new IOException("Could not retrieve the entity: seems to be an HTML document.");
    }
    return result.getContent();
  }
}
