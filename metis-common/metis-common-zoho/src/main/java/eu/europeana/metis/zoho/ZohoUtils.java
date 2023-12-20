package eu.europeana.metis.zoho;

import com.zoho.crm.api.util.Choice;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
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
      if (object instanceof Choice<?>) {
        return ((Choice<?>) object).getValue().toString();
      } else {
        return object.toString();
      }
    }
    return null;
  }

  /**
   * Method that would check if the object provided is of type {@link JSONObject.Null} and will
   * return a correct representation of {@link List} with {@link String} items or empty list.
   *
   * @param object the object to be checked
   * @return the List of strings representation of the object or empty list
   */
  public static List<String> stringListSupplier(Object object) {
    if (!JSONObject.NULL.equals(object) && object instanceof List<?> && CollectionUtils
        .isNotEmpty((List<?>) object) && ((List<?>) object).get(0) instanceof Choice<?>) {
      return ((List<Choice<?>>) object).stream().map(Choice::getValue).map(String.class::cast)
                                       .toList();
    }
    return Collections.emptyList();
  }
}
