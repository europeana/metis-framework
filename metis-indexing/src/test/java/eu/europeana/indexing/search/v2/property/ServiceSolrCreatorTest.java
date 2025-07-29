package eu.europeana.indexing.search.v2.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.corelib.solr.entity.ServiceImpl;
import eu.europeana.indexing.search.v2.EdmLabel;
import java.util.List;
import org.apache.solr.common.SolrInputDocument;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link ServiceSolrCreator} class
 */
class ServiceSolrCreatorTest {

  private ServiceSolrCreator serviceSolrCreator;
  private SolrInputDocument solrInputDocument;
  private ServiceImpl service;

  @BeforeEach
  void setup() {
    solrInputDocument = new SolrInputDocument();
    serviceSolrCreator = new ServiceSolrCreator();
    service = new ServiceImpl();
  }

  @Test
  void addToDocument_withServiceAndWithDcTerms() {
    service.setId(new ObjectId("6294c725de3fe70c48362a88"));
    service.setAbout("service");
    service.setDcTermsConformsTo(new String[]{"data1", "data2"});

    serviceSolrCreator.addToDocument(solrInputDocument, service);

    assertTrue(solrInputDocument.containsKey(EdmLabel.SV_SERVICE.toString()) &&
        solrInputDocument.containsKey(EdmLabel.SV_DCTERMS_CONFORMS_TO.toString()));
    assertEquals("service", solrInputDocument.getFieldValue(EdmLabel.SV_SERVICE.toString()));
    assertEquals(List.of("data1", "data2"), solrInputDocument.getFieldValues(EdmLabel.SV_DCTERMS_CONFORMS_TO.toString()));
    assertEquals(2, solrInputDocument.size());
  }

  @Test
  void addToDocument_withServiceAndWithoutDcTerms() {
    service.setId(new ObjectId("6294c725de3fe70c48362a88"));
    service.setAbout("service");

    serviceSolrCreator.addToDocument(solrInputDocument, service);

    assertTrue(solrInputDocument.containsKey(EdmLabel.SV_SERVICE.toString()));
    assertFalse(solrInputDocument.containsKey(EdmLabel.SV_DCTERMS_CONFORMS_TO.toString()));
    assertEquals("service", solrInputDocument.getFieldValue(EdmLabel.SV_SERVICE.toString()));
    assertNull(solrInputDocument.getFieldValues(EdmLabel.SV_DCTERMS_CONFORMS_TO.toString()));
    assertEquals(1, solrInputDocument.size());
  }

  @Test
  void addToDocument_withoutServiceAndWithDcTerms() {
    service.setId(new ObjectId("6294c725de3fe70c48362a88"));
    service.setDcTermsConformsTo(new String[]{"data1", "data2"});

    serviceSolrCreator.addToDocument(solrInputDocument, service);

    assertFalse(solrInputDocument.containsKey(EdmLabel.SV_SERVICE.toString()));
    assertTrue(solrInputDocument.containsKey(EdmLabel.SV_DCTERMS_CONFORMS_TO.toString()));
    assertNull(solrInputDocument.getFieldValue(EdmLabel.SV_SERVICE.toString()));
    assertEquals(List.of("data1", "data2"), solrInputDocument.getFieldValues(EdmLabel.SV_DCTERMS_CONFORMS_TO.toString()));
    assertEquals(1, solrInputDocument.size());
  }

  @Test
  void addToDocument_withoutServiceAndWithoutDcTerms() {
    service.setId(new ObjectId("6294c725de3fe70c48362a88"));

    serviceSolrCreator.addToDocument(solrInputDocument, service);

    assertFalse(solrInputDocument.containsKey(EdmLabel.SV_SERVICE.toString()));
    assertFalse(solrInputDocument.containsKey(EdmLabel.SV_DCTERMS_CONFORMS_TO.toString()));
    assertNull(solrInputDocument.getFieldValue(EdmLabel.SV_SERVICE.toString()));
    assertNull(solrInputDocument.getFieldValues(EdmLabel.SV_DCTERMS_CONFORMS_TO.toString()));
    assertEquals(0, solrInputDocument.size());
  }
}
