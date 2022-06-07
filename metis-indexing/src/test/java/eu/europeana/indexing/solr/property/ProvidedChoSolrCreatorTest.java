package eu.europeana.indexing.solr.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;
import eu.europeana.indexing.solr.EdmLabel;
import org.apache.solr.common.SolrInputDocument;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link ProvidedChoSolrCreator} class
 */
class ProvidedChoSolrCreatorTest {

  private ProvidedChoSolrCreator providedChoSolrCreator;
  private SolrInputDocument solrInputDocument;
  private ProvidedCHOImpl providedCHO;

  @BeforeEach
  void setup() {
    solrInputDocument = new SolrInputDocument();
    providedCHO = new ProvidedCHOImpl();
    providedChoSolrCreator = new ProvidedChoSolrCreator();
  }

  @Test
  void addProvidedChoToDocument_withAbout() {
    providedCHO.setId(new ObjectId(String.valueOf(ObjectId.get())));
    providedCHO.setAbout("about");
    providedCHO.setOwlSameAs(new String[]{"data1", "data2"});

    providedChoSolrCreator.addToDocument(solrInputDocument, providedCHO);

    assertTrue(solrInputDocument.containsKey(EdmLabel.EUROPEANA_ID.toString()));
    assertEquals("about", solrInputDocument.getFieldValue(EdmLabel.EUROPEANA_ID.toString()));
    assertEquals(1, solrInputDocument.size());
  }

  @Test
  void addProvidedChoToDocument_withoutAbout() {
    providedCHO.setId(new ObjectId(String.valueOf(ObjectId.get())));
    providedCHO.setOwlSameAs(new String[]{"data1", "data2"});

    providedChoSolrCreator.addToDocument(solrInputDocument, providedCHO);

    assertFalse(solrInputDocument.containsKey(EdmLabel.EUROPEANA_ID.toString()));
    assertNull(solrInputDocument.getFieldValue(EdmLabel.EUROPEANA_ID.toString()));
    assertEquals(0, solrInputDocument.size());
  }
}
