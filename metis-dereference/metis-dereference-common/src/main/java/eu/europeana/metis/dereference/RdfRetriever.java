package eu.europeana.metis.dereference;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Helper class to retrieve a remote unmapped entity Created by ymamakis on 2/11/16.
 */
public class RdfRetriever {

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
    final HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
    urlConnection.setRequestProperty("accept", "application/rdf+xml");
    final int responseCode = urlConnection.getResponseCode();
    final String contentType = urlConnection.getContentType();
    
    // Check the response code.
    final String result;
    if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
        || responseCode == HttpURLConnection.HTTP_MOVED_PERM
        || responseCode == HttpURLConnection.HTTP_SEE_OTHER) {

      // Perform redirect
      final String location = urlConnection.getHeaderField("Location");
      if (redirectsLeft > 0 && location != null) {
        result = retrieveFromSource(location, redirectsLeft - 1);
      } else {
        throw new IOException("Could not retrieve the entity: too many redirects.");
      }
    } else {

      // Check that we didn't receive HTML input.
      final String resultString =
              IOUtils.toString(urlConnection.getInputStream(), StandardCharsets.UTF_8);
      if (StringUtils.isBlank(resultString)) {
        throw new IOException("Could not retrieve the entity: it is empty.");
      } else if (StringUtils.startsWith(contentType, "text/html") || resultString
              .contains("<html>")) {
        throw new IOException("Could not retrieve the entity: seems to be an HTML document.");
      } else {
        result = resultString;
      }
    }

    // Done
    return result;
  }
}
