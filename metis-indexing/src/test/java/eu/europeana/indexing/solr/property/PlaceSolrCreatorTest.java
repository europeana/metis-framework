package eu.europeana.indexing.solr.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.corelib.definitions.edm.entity.Place;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.indexing.solr.EdmLabel;
import java.util.List;
import java.util.Map;
import org.apache.solr.common.SolrInputDocument;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlaceSolrCreatorTest {

  private SolrInputDocument solrInputDocument;
  private PlaceSolrCreator placeSolrCreator;

  @BeforeEach
  void setup() {
    solrInputDocument = new SolrInputDocument();
    placeSolrCreator = new PlaceSolrCreator();
  }

  @Test
  void addToDocumentWithPlaceSolrCreator() {

    Place place = new PlaceImpl();
    place.setId(new ObjectId(String.valueOf(ObjectId.get())));
    place.setAbout("Netherlands");
    place.setPrefLabel(Map.of("Municipalities", List.of("place", "another place", "really nice place")));
    place.setAltLabel(Map.of("Region", List.of("altLabel1", "altLabel2")));
    place.setHiddenLabel(Map.of("Term", List.of("hiddenLabel1", "hiddenLabel2")));
    place.setDcTermsHasPart(Map.of("Term", List.of("part1", "part2")));
    place.setIsPartOf(Map.of("Term", List.of("isPartOf1", "isPartOf2")));
    place.setOwlSameAs(new String[]{"sameAs1", "sameAs2"});
    place.setPosition(Map.of("x", 1.0f));
    place.setPosition(Map.of("y", 2.0f));
    place.setPosition(Map.of("z", 3.0f));
    place.setAltitude(22.5f);
    place.setLatitude(44.5f);
    place.setLongitude(55.5f);
    place.setFoafDepiction("whatever this means");

    // the method to test
    placeSolrCreator.addToDocument(solrInputDocument, place);

    // assertions
    assertTrue(solrInputDocument.containsKey(EdmLabel.EDM_PLACE.toString()));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PL_SKOS_PREF_LABEL + ".Municipalities"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PL_SKOS_ALT_LABEL + ".Region"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PL_WGS84_POS_LAT.toString()));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PL_WGS84_POS_LONG.toString()));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PL_WGS84_POS_ALT.toString()));

    assertEquals("Netherlands", solrInputDocument.getFieldValue(EdmLabel.EDM_PLACE.toString()));
    assertEquals("place", solrInputDocument.getFieldValue(EdmLabel.PL_SKOS_PREF_LABEL + ".Municipalities"));
    assertEquals("altLabel1", solrInputDocument.getFieldValue(EdmLabel.PL_SKOS_ALT_LABEL + ".Region"));
    assertEquals(44.5f, solrInputDocument.getFieldValue(EdmLabel.PL_WGS84_POS_LAT.toString()));
    assertEquals(55.5f, solrInputDocument.getFieldValue(EdmLabel.PL_WGS84_POS_LONG.toString()));
    assertEquals(22.5f, solrInputDocument.getFieldValue(EdmLabel.PL_WGS84_POS_ALT.toString()));

  }


}
