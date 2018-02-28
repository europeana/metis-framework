package eu.europeana.metis.dereference.service.utils;

import org.apache.commons.io.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Helper class to retrieve a remote unmapped entity Created by ymamakis on 2/11/16.
 */
@Service
public class RdfRetriever {

  private static final Logger LOGGER = LoggerFactory.getLogger(RdfRetriever.class);

  /**
   * Retrieve a remote entity from a resource as a String
   * 
   * @param resourceId The remote entity to retrieve (resource IDs are in fact URIs)
   * @return The string representation of the remote entity
   */
  public String retrieve(String resourceId) {
    if (resourceId != null) {
      try {
        URLConnection urlConnection = new URL(resourceId).openConnection();
        urlConnection.setRequestProperty("accept", "application/rdf+xml");
        InputStream inputStream = urlConnection.getInputStream();
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer, StandardCharsets.UTF_8);
        return writer.toString();
      } catch (IOException e) {
        LOGGER.error("Failed to retrieve: {} with message: {}", resourceId, e.getMessage());
      }
    }
    return "";
  }
}
