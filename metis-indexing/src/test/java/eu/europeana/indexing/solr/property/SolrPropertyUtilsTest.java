package eu.europeana.indexing.solr.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;
import eu.europeana.indexing.solr.EdmLabel;
import eu.europeana.indexing.solr.property.SolrPropertyUtils;
import static org.mockito.Mockito.*;

public class SolrPropertyUtilsTest {

  @Test
  public void testAddValues() {

    // Start with empty document: verify that it is empty.
    final SolrInputDocument document = new SolrInputDocument();
    assertTrue(document.getFieldNames().isEmpty());

    // Add single value for first field
    SolrPropertyUtils.addValue(document, EdmLabel.PL_WGS84_POS_LAT, "A");
    assertEquals(1, document.getFieldNames().size());
    assertEquals(1, document.getFieldValues(EdmLabel.PL_WGS84_POS_LAT.toString()).size());
    assertTrue(document.getFieldValues(EdmLabel.PL_WGS84_POS_LAT.toString()).contains("A"));

    // Add value to already existing first field
    SolrPropertyUtils.addValue(document, EdmLabel.PL_WGS84_POS_LAT, 0.0F);
    assertEquals(1, document.getFieldNames().size());
    assertEquals(2, document.getFieldValues(EdmLabel.PL_WGS84_POS_LAT.toString()).size());
    assertTrue(document.getFieldValues(EdmLabel.PL_WGS84_POS_LAT.toString()).contains("A"));
    assertTrue(document.getFieldValues(EdmLabel.PL_WGS84_POS_LAT.toString())
        .contains(Float.valueOf("0.0")));

    // Add multiple values to second field
    SolrPropertyUtils.addValues(document, EdmLabel.PL_WGS84_POS_LONG, new String[] {"C", "D"});
    assertEquals(2, document.getFieldNames().size());
    assertEquals(2, document.getFieldValues(EdmLabel.PL_WGS84_POS_LONG.toString()).size());
    assertTrue(document.getFieldValues(EdmLabel.PL_WGS84_POS_LONG.toString()).contains("C"));
    assertTrue(document.getFieldValues(EdmLabel.PL_WGS84_POS_LONG.toString()).contains("D"));

    // Add map of values
    Map<String, List<String>> inputMap = new HashMap<>();
    inputMap.put("1", Arrays.asList("Q"));
    inputMap.put("2", Arrays.asList("X", "Y"));
    SolrPropertyUtils.addValues(document, EdmLabel.PL_WGS84_POS_ALT, inputMap);
    assertEquals(4, document.getFieldNames().size());
    assertEquals(1, document.getFieldValues(EdmLabel.PL_WGS84_POS_ALT.toString() + ".1").size());
    assertTrue(document.getFieldValues(EdmLabel.PL_WGS84_POS_ALT.toString() + ".1").contains("Q"));
    assertEquals(2, document.getFieldValues(EdmLabel.PL_WGS84_POS_ALT.toString() + ".2").size());
    assertTrue(document.getFieldValues(EdmLabel.PL_WGS84_POS_ALT.toString() + ".2").contains("X"));
    assertTrue(document.getFieldValues(EdmLabel.PL_WGS84_POS_ALT.toString() + ".2").contains("Y"));

  }

  @Test
  public void testAddNullValues() {

    // Start with empty document: verify that it is empty.
    final SolrInputDocument document = new SolrInputDocument();
    assertTrue(document.getFieldNames().isEmpty());

    // Add null values
    SolrPropertyUtils.addValue(document, EdmLabel.PL_WGS84_POS_ALT, (Float) null);
    SolrPropertyUtils.addValue(document, EdmLabel.PL_WGS84_POS_ALT, (String) null);
    SolrPropertyUtils.addValues(document, EdmLabel.PL_WGS84_POS_ALT, (String[]) null);
    SolrPropertyUtils.addValues(document, EdmLabel.PL_WGS84_POS_ALT, (Map<String, List<String>>) null);

    // Add map with null values
    Map<String, List<String>> input = new HashMap<>();
    input.put("A", null);
    SolrPropertyUtils.addValues(document, EdmLabel.PL_WGS84_POS_ALT, input);

    // Verify that document is still empty.
    assertTrue(document.getFieldNames().isEmpty());

  }

  @Test
  public void testRightsForMap() {

    // Test null map
    final Stream<String> fromNullMap = SolrPropertyUtils.getRightsFromMap(null);
    assertNotNull(fromNullMap);
    assertEquals(0L, fromNullMap.count());

    // Test empty map
    final Stream<String> fromEmptyMap = SolrPropertyUtils.getRightsFromMap(Collections.emptyMap());
    assertNotNull(fromEmptyMap);
    assertEquals(0L, fromEmptyMap.count());

    // Test filled map
    final List<String> input = Arrays.asList("A", "B", "C");
    final Map<String, List<String>> rights = new HashMap<>();
    rights.put("def", input);
    final Stream<String> fromFilledMap = SolrPropertyUtils.getRightsFromMap(rights);
    assertNotNull(fromFilledMap);
    final Set<String> output = fromFilledMap.collect(Collectors.toSet());
    assertEquals(3, output.size());
    for (String inputString : input) {
      assertTrue(output.contains(inputString));
    }
  }

  @Test
  public void testHasLicenseForRights() {

    // no rights required
    assertTrue(SolrPropertyUtils.hasLicenseForRights(Collections.emptyMap(), license -> false));
    assertTrue(SolrPropertyUtils.hasLicenseForRights(Collections.emptyMap(), license -> true));

    // rigths required but no licenses
    final List<String> input = Arrays.asList("A", "B", "C");
    final Map<String, List<String>> rights = new HashMap<>();
    rights.put("def", input);
    Predicate<String> contradiction = spy(new Contradiction());
    assertFalse(SolrPropertyUtils.hasLicenseForRights(rights, contradiction));
    verify(contradiction, times(3)).test(anyString());
    for (String inputString : input) {
      verify(contradiction, times(1)).test(eq(inputString));
    }

    // rights required and licenses available
    Predicate<String> hasLicense = license -> Arrays.asList("C", "D", "E").contains(license);
    assertTrue(SolrPropertyUtils.hasLicenseForRights(rights, hasLicense));
  }

  private static class Contradiction implements Predicate<String> {

    @Override
    public boolean test(String arg0) {
      return false;
    }

  }
}
