package eu.europeana.indexing.solr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.solr.common.SolrInputDocument;

/**
 * Set of utils for SOLR queries
 *
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public final class SolrUtils {
  
  private SolrUtils() {

  }

  private static void addValues(SolrInputDocument document, String label, String[] values) {

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

  public static void addValues(SolrInputDocument document, EdmLabel label, String[] values) {
    addValues(document, label.toString(), values);
  }

  public static void addValue(SolrInputDocument document, EdmLabel label, String value) {
    if (value != null) {
      addValues(document, label, new String[] {value});
    }
  }

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

  public static Stream<String> getRightsFromMap(Map<String, List<String>> edmRights) {
    final List<String> resultList = edmRights == null ? null : edmRights.get("def");
    return resultList == null ? Stream.empty() : resultList.stream();
  }

  public static boolean hasLicenseForRights(Map<String, List<String>> edmRights,
      Set<String> licenseIds) {
    final boolean noRightsImposed = getRightsFromMap(edmRights).count() == 0;
    return noRightsImposed || getRightsFromMap(edmRights).anyMatch(licenseIds::contains);
  }
}
