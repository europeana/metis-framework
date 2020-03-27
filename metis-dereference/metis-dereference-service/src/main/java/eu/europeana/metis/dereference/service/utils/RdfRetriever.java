package eu.europeana.metis.dereference.service.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Helper class to retrieve a remote unmapped entity Created by ymamakis on 2/11/16.
 */
@Service
public class RdfRetriever {

  private static final int MAX_NUMBER_OF_REDIRECTS = 5;

  private static final Logger LOGGER = LoggerFactory.getLogger(RdfRetriever.class);

  /**
   * Retrieve a remote entity from a resource as a String. We try every suffix in a random order
   * until we find one that works (i.e. yield a non-null result that is not HTML).
   *
   * @param resourceId The remote entity to retrieve (resource IDs are in fact URIs)
   * @param possibleSuffixes The suffixes we will try to append to the entity to form the remote
   * address.
   * @return The original entity containing a string representation of the remote entity. This
   * method may return null.
   */
  public String retrieve(String resourceId, Set<String> possibleSuffixes) {

    // Sanity check for null values.
    if (resourceId == null) {
      throw new IllegalArgumentException("Parameter resourceId cannot be null.");
    }

    // Try to retrieve the entity for the different suffixes, stopping when we succeed.
    return possibleSuffixes.stream().map(suffix -> retrieveFromSource(resourceId, suffix))
            .filter(Objects::nonNull).findAny().orElse(null);
  }

  private static String retrieveFromSource(String resourceId, String suffix) {
    try {
      return retrieveFromSource(resourceId, resourceId + suffix, MAX_NUMBER_OF_REDIRECTS);
    } catch (IOException e) {
      LOGGER.warn("Failed to retrieve: {} with message: {}", resourceId, e.getMessage());
      LOGGER.debug("Problem retrieving resource.", e);
      return null;
    }
  }

  private static String retrieveFromSource(String resourceId, String url, int redirectsLeft)
      throws IOException {

    // Make the connection and retrieve the result.
    final HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
    urlConnection.setRequestProperty("accept", "application/rdf+xml");
    final String resultString =
        IOUtils.toString(urlConnection.getInputStream(), StandardCharsets.UTF_8);
    final int responseCode = urlConnection.getResponseCode();
    final String contentType = urlConnection.getContentType();
    
    // Log in case of unexpected issues.
    if (responseCode != HttpURLConnection.HTTP_OK) {
      LOGGER.info("Status code {} for url {} for resource {}.", responseCode, url, resourceId);
    }

    // Check the response code.
    final String result;
    if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
        || responseCode == HttpURLConnection.HTTP_MOVED_PERM
        || responseCode == HttpURLConnection.HTTP_SEE_OTHER) {

      // Perform redirect
      final String location = urlConnection.getHeaderField("Location");
      if (redirectsLeft > 0 && location != null) {
        result = retrieveFromSource(resourceId, location, redirectsLeft - 1);
      } else {
        result = null;
      }
    } else {

      // Check that we didn't receive HTML input.
      if (contentType != null && contentType.startsWith("text/html")) {
        result = null;
      } else if (resultString != null && resultString.contains("<html>")) {
        result = null;
      } else {
        result = resultString;
      }
    }

    // Done
    return result;
  }
}
