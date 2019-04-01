package eu.europeana.enrichment.service.wikidata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.enrichment.service.EntityConverterUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test utils class for entity converter.
 * 
 * @author GrafR
 *
 */
public class EntityConverterUtilsTest { 

  private final String WIKIDATA_TEST_OUTPUT_FILE = "test.out";
  private final String TEST_WIKIDATA_ORGANIZATION_ID = "193563";
  private final String TEST_WIKIDATA_URL = "http://www.wikidata.org/entity/Q" + TEST_WIKIDATA_ORGANIZATION_ID;
  private final String TEST_ACRONYM = "BNF";
  private final String TEST_COUNTRY = "FR";
  private final String TEST_LABEL_FR = "BnF";
  private final String TEST_LABEL_FR2 = "BnF2";
  private final String TEST_LABEL_EN = "British library";
  private final String TEST_LABEL_IT = "Bologna library";

  private Map<String, List<String>> prefLabel = null;
  private Map<String, List<String>> addPrefLabel = null;
  private Map<String, List<String>> altLabel = null;
  
  private EntityConverterUtils entityConverterUtils = new EntityConverterUtils();
  
  private EntityConverterUtils getEntityConverterUtils() {
    return entityConverterUtils;
  }

  @BeforeEach
  void setUp() {
  }

  @AfterEach
  void tearDown() {}

  private void init() {
    prefLabel = new HashMap<>();
    List<String> prefLabelValues = new ArrayList<>();
    prefLabelValues.add(TEST_LABEL_FR);
    prefLabel.put(Locale.FRENCH.getLanguage(), prefLabelValues);

    addPrefLabel = new HashMap<>();
    List<String> addPrefLabelValues = new ArrayList<>();
    addPrefLabelValues.add(TEST_LABEL_EN);
    addPrefLabel.put(Locale.ENGLISH.getLanguage(), addPrefLabelValues);
    List<String> addPrefLabelValues2 = new ArrayList<>();
    addPrefLabelValues2.add(TEST_LABEL_FR2);
    addPrefLabel.put(Locale.FRENCH.getLanguage(), addPrefLabelValues2);

    altLabel = new HashMap<>();
    List<String> addAltLabelValues = new ArrayList<>();
    addAltLabelValues.add(TEST_LABEL_FR);
    altLabel.put(Locale.FRENCH.getLanguage(), addAltLabelValues);
    List<String> addAltLabelValues2 = new ArrayList<>();
    addAltLabelValues2.add(TEST_LABEL_IT);
    altLabel.put(Locale.ITALIAN.getLanguage(), addAltLabelValues2);
  }
  
  @Test
  void mergeTest() {

    init();

    //merge two equal prefLabels
    Map<String, List<String>> mergedFromEqualMap = getEntityConverterUtils().mergeMapsWithLists(
        prefLabel, prefLabel);
    assertNotNull(mergedFromEqualMap);

    // merge unequal prefLabels to an altLabel and check resulting altLabel
    Map<String, List<String>> newValuesMap = new HashMap<>();
    newValuesMap.put(Locale.ENGLISH.getLanguage(), Collections.singletonList(TEST_LABEL_EN));
    newValuesMap.put(Locale.FRENCH.getLanguage(), Arrays.asList(TEST_LABEL_FR, TEST_LABEL_FR2));
    Map<String, List<String>> mergedMap =
        getEntityConverterUtils().mergeMapsWithLists(altLabel, newValuesMap);
    assertNotNull(mergedMap);
    assertEquals(3, mergedMap.size());
    assertEquals(mergedMap.get(Locale.ENGLISH.getLanguage()).get(0), TEST_LABEL_EN);
    assertTrue(mergedMap.get(Locale.FRENCH.getLanguage()).contains(TEST_LABEL_FR));
    assertTrue(mergedMap.get(Locale.FRENCH.getLanguage()).contains(TEST_LABEL_FR2));
    assertEquals(mergedMap.get(Locale.ITALIAN.getLanguage()).get(0), TEST_LABEL_IT);
    
    // merge unequal string arrays and remove duplicates
    String[] base = {"a","b","c"};
    String[] add = {"d","b"};    
    String[] mergedArray = getEntityConverterUtils().mergeStringArrays(base, add);
    assertNotNull(mergedArray);
    assertEquals(4, mergedArray.length);
  }

}
