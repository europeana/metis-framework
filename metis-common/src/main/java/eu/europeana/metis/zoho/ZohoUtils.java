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

  public static String stringFieldSupplier(Object jsonObject) {
    if (!JSONObject.NULL.equals(jsonObject)) {
      return jsonObject.toString();
    }
    return null;
  }
}
