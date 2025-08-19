package eu.europeana.indexing.search.v2.property;

import static eu.europeana.indexing.utils.TestUtils.verifyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.indexing.common.persistence.solr.v2.SolrV2Field;
import java.util.List;
import java.util.Map;
import org.apache.solr.common.SolrInputDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link TimespanSolrCreator} class
 */
class TimespanSolrCreatorTest {

  private TimespanSolrCreator timespanSolrCreator;
  private SolrInputDocument solrInputDocument;
  private TimespanImpl timespan;

  @BeforeEach
  void setup() {
    solrInputDocument = new SolrInputDocument();
    timespanSolrCreator = new TimespanSolrCreator();
    timespan = new TimespanImpl();
  }

  @Test
  void addToDocument_withTimeSpan_PrefValue_AltValue_And_OwlSameAs() {
    timespan.setAbout("timespan");
    timespan.setPrefLabel(Map.of("keyPref", List.of("prefValue1", "prefValue2")));
    timespan.setAltLabel(Map.of("keyAlt", List.of("altValue1", "altValue2")));
    timespan.setOwlSameAs(List.of("value1", "value2").toArray(String[]::new));

    timespanSolrCreator.addToDocument(solrInputDocument, timespan);

    assertTrue(solrInputDocument.containsKey(SolrV2Field.EDM_TIMESPAN.toString()) && solrInputDocument.containsKey(
        SolrV2Field.TS_SKOS_PREF_LABEL + ".keyPref") && solrInputDocument.containsKey(
        SolrV2Field.TS_SKOS_ALT_LABEL + ".keyAlt")
        && solrInputDocument.containsKey(SolrV2Field.TS_SKOS_PREF_LABEL.toString()));
    assertEquals("timespan", solrInputDocument.getFieldValue(SolrV2Field.EDM_TIMESPAN.toString()));
    verifyMap(solrInputDocument, SolrV2Field.TS_SKOS_PREF_LABEL, timespan.getPrefLabel());
    verifyMap(solrInputDocument, SolrV2Field.TS_SKOS_ALT_LABEL, timespan.getAltLabel());
    assertEquals(4, solrInputDocument.size());
  }

  @Test
  void addToDocument_withoutTimeSpan() {
    timespan.setPrefLabel(Map.of("keyPref", List.of("prefValue1", "prefValue2")));
    timespan.setAltLabel(Map.of("keyAlt", List.of("altValue1", "altValue2")));
    timespan.setOwlSameAs(List.of("value1", "value2").toArray(String[]::new));

    timespanSolrCreator.addToDocument(solrInputDocument, timespan);

    assertFalse(solrInputDocument.containsKey(SolrV2Field.EDM_TIMESPAN.toString()));
    assertTrue(solrInputDocument.containsKey(SolrV2Field.TS_SKOS_PREF_LABEL + ".keyPref") && solrInputDocument.containsKey(
        SolrV2Field.TS_SKOS_ALT_LABEL + ".keyAlt") && solrInputDocument.containsKey(SolrV2Field.TS_SKOS_PREF_LABEL.toString()));
    assertNull(solrInputDocument.getFieldValue(SolrV2Field.EDM_TIMESPAN.toString()));
    verifyMap(solrInputDocument, SolrV2Field.TS_SKOS_PREF_LABEL, timespan.getPrefLabel());
    verifyMap(solrInputDocument, SolrV2Field.TS_SKOS_ALT_LABEL, timespan.getAltLabel());
    assertEquals(List.of("value1", "value2"), solrInputDocument.getFieldValues(SolrV2Field.TS_SKOS_PREF_LABEL.toString()));
    assertEquals(3, solrInputDocument.size());
  }

  @Test
  void addToDocument_without_PrefValue() {
    timespan.setAbout("timespan");
    timespan.setAltLabel(Map.of("keyAlt", List.of("altValue1", "altValue2")));
    timespan.setOwlSameAs(List.of("value1", "value2").toArray(String[]::new));

    timespanSolrCreator.addToDocument(solrInputDocument, timespan);

    assertTrue(solrInputDocument.containsKey(SolrV2Field.EDM_TIMESPAN.toString()) && solrInputDocument.containsKey(
        SolrV2Field.TS_SKOS_ALT_LABEL + ".keyAlt") && solrInputDocument.containsKey(SolrV2Field.TS_SKOS_PREF_LABEL.toString()));
    assertFalse(solrInputDocument.containsKey(SolrV2Field.TS_SKOS_PREF_LABEL + ".keyPref"));
    assertEquals("timespan", solrInputDocument.getFieldValue(SolrV2Field.EDM_TIMESPAN.toString()));
    assertNull(solrInputDocument.getFieldValues(SolrV2Field.TS_SKOS_PREF_LABEL + ".keyPref"));
    verifyMap(solrInputDocument, SolrV2Field.TS_SKOS_ALT_LABEL, timespan.getAltLabel());
    assertEquals(List.of("value1", "value2"), solrInputDocument.getFieldValues(SolrV2Field.TS_SKOS_PREF_LABEL.toString()));
    assertEquals(3, solrInputDocument.size());
  }

  @Test
  void addToDocument_without_AltValue() {
    timespan.setAbout("timespan");
    timespan.setPrefLabel(Map.of("keyPref", List.of("prefValue1", "prefValue2")));
    timespan.setOwlSameAs(List.of("value1", "value2").toArray(String[]::new));

    timespanSolrCreator.addToDocument(solrInputDocument, timespan);

    assertTrue(solrInputDocument.containsKey(SolrV2Field.EDM_TIMESPAN.toString()) && solrInputDocument.containsKey(
        SolrV2Field.TS_SKOS_PREF_LABEL + ".keyPref") && solrInputDocument.containsKey(SolrV2Field.TS_SKOS_PREF_LABEL.toString()));
    assertFalse(solrInputDocument.containsKey(SolrV2Field.TS_SKOS_ALT_LABEL + ".keyAlt"));
    assertEquals("timespan", solrInputDocument.getFieldValue(SolrV2Field.EDM_TIMESPAN.toString()));
    verifyMap(solrInputDocument, SolrV2Field.TS_SKOS_PREF_LABEL, timespan.getPrefLabel());
    assertNull(solrInputDocument.getFieldValues(SolrV2Field.TS_SKOS_ALT_LABEL + ".keyAlt"));
    assertEquals(List.of("value1", "value2"), solrInputDocument.getFieldValues(SolrV2Field.TS_SKOS_PREF_LABEL.toString()));
    assertEquals(3, solrInputDocument.size());
  }

  @Test
  void addToDocument_without_OwlSameAs() {
    timespan.setAbout("timespan");
    timespan.setPrefLabel(Map.of("keyPref", List.of("prefValue1", "prefValue2")));
    timespan.setAltLabel(Map.of("keyAlt", List.of("altValue1", "altValue2")));

    timespanSolrCreator.addToDocument(solrInputDocument, timespan);

    assertTrue(solrInputDocument.containsKey(SolrV2Field.EDM_TIMESPAN.toString()) && solrInputDocument.containsKey(
        SolrV2Field.TS_SKOS_PREF_LABEL + ".keyPref") && solrInputDocument.containsKey(
        SolrV2Field.TS_SKOS_ALT_LABEL + ".keyAlt"));
    assertFalse(solrInputDocument.containsKey(SolrV2Field.TS_SKOS_PREF_LABEL.toString()));
    assertEquals("timespan", solrInputDocument.getFieldValue(SolrV2Field.EDM_TIMESPAN.toString()));
    verifyMap(solrInputDocument, SolrV2Field.TS_SKOS_PREF_LABEL, timespan.getPrefLabel());
    verifyMap(solrInputDocument, SolrV2Field.TS_SKOS_ALT_LABEL, timespan.getAltLabel());
    assertNull(solrInputDocument.getFieldValues(SolrV2Field.TS_SKOS_PREF_LABEL.toString()));
    assertEquals(3, solrInputDocument.size());
  }
}
