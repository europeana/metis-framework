package eu.europeana.metis.dereference;

import eu.europeana.metis.dereference.wrappers.BodyHandlerWrapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to retrieve a remote unmapped entity Created by ymamakis on 2/11/16.
 */
public class RdfRetriever {

  private static final Logger LOG = LoggerFactory.getLogger(RdfRetriever.class);

  private static final int MAX_NUMBER_OF_REDIRECTS = 5;

  private static HttpClient httpClient = HttpClient.newBuilder()
      .followRedirects(Redirect.NORMAL)
      .build();

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
    if (resourceId == null) {
      throw new IllegalArgumentException("Parameter resourceId cannot be null.");
    }
    if (suffix == null) {
      throw new IllegalArgumentException("Parameter suffix cannot be null.");
    }
    return retrieveFromSource(resourceId + suffix, MAX_NUMBER_OF_REDIRECTS);
  }

  private static String retrieveFromSource(String url, int redirectsLeft) throws IOException {

    // Make the connection and retrieve the result.
    // Note: we have no choice but to follow the provided URL.
    @SuppressWarnings("findsecbugs:URLCONNECTION_SSRF_FD")
    HttpRequest httpRequest = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(url))
        .setHeader("Accept", "application/rdf+xml")
        .build();

    HttpResponse<String> httpResponse = null;

    BodyHandler<String> handler = BodyHandlers.ofString();
    BodyHandlerWrapper handleWrapper = new BodyHandlerWrapper(handler);

    try {
      httpResponse = httpClient.send(httpRequest, handleWrapper);
    } catch (InterruptedException e) {
      LOG.info(String.format("That was some problem sending a request to %s", url));

    }
    final int responseCode;

    if (httpResponse != null) {
      responseCode = httpResponse.statusCode();
    } else {
      responseCode = 0;
    }

    // Check the response code.
    final String result;
    if (responseCode == HttpStatus.SC_MOVED_TEMPORARILY
        || responseCode == HttpStatus.SC_MOVED_PERMANENTLY
        || responseCode == HttpStatus.SC_SEE_OTHER) {

      // Perform redirect
      final String location = httpResponse.headers().map().get("Location").get(0);
      handleWrapper.cancel();
      if (redirectsLeft > 0 && location != null) {
        result = retrieveFromSource(location, redirectsLeft - 1);
      } else {
        throw new IOException("Could not retrieve the entity: too many redirects.");
      }
    } else {
      String contentType = httpResponse.headers().map().get("Content-Type").get(0);

      // Check that we didn't receive HTML input.
      result = httpResponse.body();
      handleWrapper.cancel();
      if (StringUtils.isBlank(result)) {
        throw new IOException("Could not retrieve the entity: it is empty.");
      } else if (StringUtils.startsWith(contentType, "text/html") || result
          .contains("<html>")) {
        throw new IOException("Could not retrieve the entity: seems to be an HTML document.");
      }
    }

    // Done
    return result;
  }
}
