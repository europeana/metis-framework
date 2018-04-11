package eu.europeana.indexing.solr.property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.apache.solr.common.SolrInputDocument;
import eu.europeana.indexing.solr.EdmLabel;

/**
 * Set of utility methods for creating Solr documents.
 *
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public final class SolrPropertyUtils {

  private SolrPropertyUtils() {}

  private static void addValues(SolrInputDocument document, String label, Object[] values) {

    // Sanity check
    if (values == null) {
      return;
    }

    // Get collection to add to
    Collection<Object> existingValues = document.getFieldValues(label);
    if (existingValues == null) {
      existingValues = new ArrayList<>();
    }

    // Add and store.
    existingValues.addAll(Arrays.asList(values));
    document.setField(label, existingValues);
  }

  /**
   * Add multiple values to the Solr document.
   * 
   * @param document The Solr document to which to add the values.
   * @param label The label of the values to be added.
   * @param values The values to be added.
   */
  public static void addValues(SolrInputDocument document, EdmLabel label, String[] values) {
    addValues(document, label.toString(), values);
  }

  /**
   * Add single value to the Solr document.
   * 
   * @param document The Solr document to which to add the value.
   * @param label The label of the value to be added.
   * @param value The value to be added.
   */
  public static void addValue(SolrInputDocument document, EdmLabel label, String value) {
    if (value != null) {
      addValues(document, label.toString(), new String[] {value});
    }
  }

  /**
   * Add single value to the Solr document.
   * 
   * @param document The Solr document to which to add the value.
   * @param label The label of the value to be added.
   * @param value The value to be added.
   */
  public static void addValue(SolrInputDocument document, EdmLabel label, Float value) {
    if (value != null) {
      addValues(document, label.toString(), new Float[] {value});
    }
  }

  /**
   * Add multiple values to the Solr document. All values in the given map will be added. The label
   * of each key-value pair in the map will be of the form [EDM label].[map key].
   * 
   * @param document The Solr document to which to add the values.
   * @param label The base label of the values to be added (to be qualified with the map key).
   * @param values The values to be added.
   */
  public static void addValues(SolrInputDocument document, EdmLabel label,
      Map<String, List<String>> values) {
    if (values == null) {
      return;
    }
    for (Entry<String, List<String>> entry : values.entrySet()) {
      if (entry.getValue() != null) {
        addValues(document, label.toString() + "." + entry.getKey(),
            entry.getValue().toArray(new String[0]));
      }
    }
  }

  /**
   * This method obtains a stream of the relevant rights (URIs) from the given EDM rights map.
   * 
   * @param edmRights The EDM rights map.
   * @return A non-null stream.
   */
  public static Stream<String> getRightsFromMap(Map<String, List<String>> edmRights) {
    final List<String> resultList = edmRights == null ? null : edmRights.get("def");
    return resultList == null ? Stream.empty() : resultList.stream();
  }

  /**
   * Evaluates whether there is a match between required rights and available licenses.
   * 
   * @param edmRights The list of rights required (URIs).
   * @param hasLicense Predicate to evaluate whether there is a license available for any given web
   *        resource (URI).
   * @return True if there is a license for at least one of the required rights, or no rights are
   *         required.
   */
  public static boolean hasLicenseForRights(Map<String, List<String>> edmRights,
      Predicate<String> hasLicense) {
    final boolean noRightsImposed = getRightsFromMap(edmRights).count() == 0;
    return noRightsImposed || getRightsFromMap(edmRights).anyMatch(hasLicense);
  }
}
