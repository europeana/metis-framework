package eu.europeana.metis.dereference;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to retrieve a remote unmapped entity Created by ymamakis on 2/11/16.
 */
public class RdfRetriever {

  private static final Logger LOG = LoggerFactory.getLogger(RdfRetriever.class);

  private static final int MAX_NUMBER_OF_REDIRECTS = 5;

  /**
   * Retrieve a remote entity from a resource as a String. We try every suffix in a random order
   * until we find one that works (i.e. yield a non-null result that is not HTML).
   *
   * @param resourceId The remote entity to retrieve (resource IDs are in fact URIs)
   * @param suffix The suffix to append to the entity to form the remote address. Can be null.
   * @return The original entity containing a string representation of the remote entity. This
   * method does not return null.
   * @throws IOException In case there was an issue retrieving the resource.
   */
  public String retrieve(String resourceId, String suffix) throws IOException {
    return retrieveFromSource(resourceId, suffix == null ? "" : suffix);
  }

  private static String retrieveFromSource(String resourceId, String suffix) throws IOException {

    // Check the input
    if (resourceId == null) {
      throw new IllegalArgumentException("Parameter resourceId cannot be null.");
    }
    if (suffix == null) {
      throw new IllegalArgumentException("Parameter suffix cannot be null.");
    }

    // Obtain the response.
    final HttpResponse<String> httpResponse;
    final ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      final HttpClient httpClient = HttpClient.newBuilder().executor(executor).build();
      httpResponse = httpConnection(httpClient, URI.create(resourceId + suffix));
    } finally {
      executor.shutdownNow();
    }

    // Analyze the response and return the result.
    final String contentType = httpResponse.headers().firstValue("Content-Type").orElse(null);
    final String result = httpResponse.body();
    if (StringUtils.isBlank(result)) {
      throw new IOException("Could not retrieve the entity: it is empty.");
    } else if (StringUtils.startsWith(contentType, "text/html") || result
            .contains("<html>")) {
      throw new IOException("Could not retrieve the entity: seems to be an HTML document.");
    }
    return result;
  }

  private static HttpResponse<String> httpConnection(HttpClient httpClient, URI url)
          throws IOException {
    URI currentUrl = url;
    for (int iteration = 0; iteration <= MAX_NUMBER_OF_REDIRECTS; iteration++) {

      // Connect to the URL
      final HttpResponse<String> httpResponse = sendHttpRequest(httpClient, currentUrl);

      // Obtain the response code
      final int responseCode;
      if (httpResponse != null) {
        responseCode = httpResponse.statusCode();
      } else {
        throw new IllegalStateException("Unexpected error trying to connect to URL: " + currentUrl);
      }

      // If we don't need to redirect.
      if (Response.Status.Family.familyOf(responseCode) != Family.REDIRECTION) {
        return httpResponse;
      }

      // So we do need to redirect. Update URL and try again.
      final String location = httpResponse.headers().firstValue("Location").orElse(null);
      if (StringUtils.isBlank(location)) {
        throw new IllegalStateException(
                "Redirect received without Location header for URL: " + currentUrl);
      }
      currentUrl = currentUrl.resolve(location);
    }

    // We can only be here if the maximum number of redirects is reached.
    throw new IOException("Could not retrieve the entity: too many redirects.");
  }

  private static HttpResponse<String> sendHttpRequest(HttpClient httpClient, URI url)
          throws IOException {

    // Make the connection request.
    // Note: we have no choice but to follow the provided URL.
    @SuppressWarnings("findsecbugs:URLCONNECTION_SSRF_FD") final HttpRequest httpRequest = HttpRequest
            .newBuilder()
            .GET()
            .uri(url)
            .setHeader("Accept", "application/rdf+xml")
            .build();

    // Send the request and obtain the response.
    try {
      return httpClient.send(httpRequest, BodyHandlers.ofString());
    } catch (InterruptedException e) {
      LOG.info("There was some problem sending a request to {}", url);
      Thread.currentThread().interrupt();
      throw new IOException("Connection failed due to an interrupt.", e);
    }
  }
}
