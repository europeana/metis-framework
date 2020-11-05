package eu.europeana.metis.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

/**
 * Utility class with helpful methods for tests
 */
public final class TestUtils {

  private TestUtils() {
  }

  /**
   * Convert a java {@link Object} to a byte array.
   *
   * @param object the object to convert
   * @return the byte array
   * @throws IOException if an exception occurred during the conversion
   */
  public static byte[] convertObjectToJsonBytes(Object object) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    return mapper.writeValueAsBytes(object);
  }
}
