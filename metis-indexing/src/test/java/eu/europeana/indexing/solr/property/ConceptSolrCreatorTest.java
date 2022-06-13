package eu.europeana.indexing.solr.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.indexing.solr.EdmLabel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.solr.common.SolrInputDocument;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link ConceptSolrCreator} class
 */
class ConceptSolrCreatorTest {

  private ConceptSolrCreator conceptSolrCreator;
  private SolrInputDocument solrInputDocument;
  private ConceptImpl concept;

  @BeforeEach
  void setup() {
    solrInputDocument = new SolrInputDocument();
    conceptSolrCreator = new ConceptSolrCreator();
    concept = new ConceptImpl();
  }

  @Test
  void addToDocument_withConceptPrefValueAndAltValue() {
    concept.setId(new ObjectId("6294c725de3fe70c48388a88"));
    concept.setAbout("concept");
    concept.setPrefLabel(Map.of("keyPref", List.of("prefValue1", "prefValue2")));
    concept.setAltLabel(Map.of("keyAlt", List.of("altValue1", "altValue2")));

    conceptSolrCreator.addToDocument(solrInputDocument, concept);

    assertTrue(solrInputDocument.containsKey(EdmLabel.SKOS_CONCEPT.toString()) &&
        solrInputDocument.containsKey(EdmLabel.CC_SKOS_PREF_LABEL + ".keyPref") &&
        solrInputDocument.containsKey(EdmLabel.CC_SKOS_ALT_LABEL + ".keyAlt"));
    assertEquals("concept", solrInputDocument.getFieldValue(EdmLabel.SKOS_CONCEPT.toString()));
    assertEquals(List.of("prefValue1", "prefValue2"),
        solrInputDocument.getFieldValues(EdmLabel.CC_SKOS_PREF_LABEL + ".keyPref"));
    assertEquals(List.of("altValue1", "altValue2"),
        solrInputDocument.getFieldValues(EdmLabel.CC_SKOS_ALT_LABEL + ".keyAlt"));
    assertEquals(3, solrInputDocument.size());
  }

  @Test
  void addToDocument_withoutConcept() {
    concept.setId(new ObjectId("6294c725de3fe70c48388a88"));
    concept.setPrefLabel(Map.of("keyPref", List.of("prefValue1", "prefValue2")));
    concept.setAltLabel(Map.of("keyAlt", List.of("altValue1", "altValue2")));

    conceptSolrCreator.addToDocument(solrInputDocument, concept);

    assertFalse(solrInputDocument.containsKey(EdmLabel.SKOS_CONCEPT.toString()));
    assertTrue(solrInputDocument.containsKey(EdmLabel.CC_SKOS_PREF_LABEL + ".keyPref") &&
        solrInputDocument.containsKey(EdmLabel.CC_SKOS_ALT_LABEL + ".keyAlt"));
    assertNull(solrInputDocument.getFieldValue(EdmLabel.SKOS_CONCEPT.toString()));
    assertEquals(List.of("prefValue1", "prefValue2"),
        solrInputDocument.getFieldValues(EdmLabel.CC_SKOS_PREF_LABEL + ".keyPref"));
    assertEquals(List.of("altValue1", "altValue2"),
        solrInputDocument.getFieldValues(EdmLabel.CC_SKOS_ALT_LABEL + ".keyAlt"));
    assertEquals(2, solrInputDocument.size());
  }

  @Test
  void addToDocument_withoutPrefValue() {
    concept.setId(new ObjectId("6294c725de3fe70c48388a88"));
    concept.setAbout("concept");
    concept.setAltLabel(Map.of("keyAlt", List.of("altValue1", "altValue2")));

    conceptSolrCreator.addToDocument(solrInputDocument, concept);

    assertFalse(solrInputDocument.containsKey(EdmLabel.CC_SKOS_PREF_LABEL + ".keyPref"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.SKOS_CONCEPT.toString()) &&
        solrInputDocument.containsKey(EdmLabel.CC_SKOS_ALT_LABEL + ".keyAlt"));
    assertEquals("concept", solrInputDocument.getFieldValue(EdmLabel.SKOS_CONCEPT.toString()));
    assertNull(solrInputDocument.getFieldValues(EdmLabel.CC_SKOS_PREF_LABEL + ".keyPref"));
    assertEquals(List.of("altValue1", "altValue2"),
        solrInputDocument.getFieldValues(EdmLabel.CC_SKOS_ALT_LABEL + ".keyAlt"));
    assertEquals(2, solrInputDocument.size());
  }

  @Test
  void addToDocument_withoutAltValue() {
    concept.setId(new ObjectId("6294c725de3fe70c48388a88"));
    concept.setAbout("concept");
    concept.setPrefLabel(Map.of("keyPref", List.of("prefValue1", "prefValue2")));

    conceptSolrCreator.addToDocument(solrInputDocument, concept);

    assertFalse(solrInputDocument.containsKey(EdmLabel.CC_SKOS_ALT_LABEL + ".keyAlt"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.SKOS_CONCEPT.toString()) &&
        solrInputDocument.containsKey(EdmLabel.CC_SKOS_PREF_LABEL + ".keyPref"));
    assertEquals("concept", solrInputDocument.getFieldValue(EdmLabel.SKOS_CONCEPT.toString()));
    assertEquals(List.of("prefValue1", "prefValue2"),
        solrInputDocument.getFieldValues(EdmLabel.CC_SKOS_PREF_LABEL + ".keyPref"));
    assertNull(solrInputDocument.getFieldValues(EdmLabel.CC_SKOS_ALT_LABEL + ".keyAlt"));
    assertEquals(2, solrInputDocument.size());
  }
}
