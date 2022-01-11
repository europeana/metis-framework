package eu.europeana.indexing.solr.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import eu.europeana.corelib.definitions.edm.entity.QualityAnnotation;
import eu.europeana.corelib.solr.entity.QualityAnnotationImpl;
import eu.europeana.indexing.utils.RdfTier;
import java.util.Collection;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.junit.jupiter.api.Test;

class QualityAnnotationSolrCreatorTest {

  @Test
  void addToDocumentTest() {
    final SolrInputDocument document = new SolrInputDocument();
    QualityAnnotation qualityAnnotation = new QualityAnnotationImpl();
    final RdfTier metadataTierC = RdfTier.METADATA_TIER_C;
    qualityAnnotation.setBody(metadataTierC.getUri());

    new QualityAnnotationSolrCreator().addToDocument(document, qualityAnnotation);
    final SolrInputField solrInputField = document.get(metadataTierC.getEdmLabel().toString());
    assertNotNull(solrInputField);
    assertEquals(metadataTierC.getEdmLabel().toString(), solrInputField.getName());
    assertEquals(metadataTierC.getTier().toString(), ((Collection<String>)solrInputField.getValue()).iterator().next());
  }
}
