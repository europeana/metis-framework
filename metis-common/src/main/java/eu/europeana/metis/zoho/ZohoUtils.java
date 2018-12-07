package eu.europeana.metis.zoho;

import org.json.JSONObject;

/**
 * Class that contains general utility methods for Zoho returned objects.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-12-07
 */
public final class ZohoUtils {

  private ZohoUtils() {
  }

  /**
   * Method that would check if the object provided is of type {@link JSONObject.Null} and will
   * return a correct representation of {@link String} or null.
   *
   * @param object the object to be checked
   * @return the string representation of the object or null
   */
  public static String stringFieldSupplier(Object object) {
    if (!JSONObject.NULL.equals(object)) {
      return object.toString();
    }
    return null;
  }
}
