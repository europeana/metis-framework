package eu.europeana.indexing.solr.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import eu.europeana.corelib.definitions.edm.entity.QualityAnnotation;
import eu.europeana.corelib.solr.entity.QualityAnnotationImpl;
import eu.europeana.indexing.utils.RdfTier;
import eu.europeana.indexing.utils.RdfTierUtils;
import eu.europeana.indexing.utils.SolrTier;
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
    final SolrTier solrMetadataTierC = RdfTierUtils.getSolrTier(metadataTierC);
    qualityAnnotation.setBody(metadataTierC.getUri());

    new QualityAnnotationSolrCreator().addToDocument(document, qualityAnnotation);
    final SolrInputField solrInputField = document.get(solrMetadataTierC.getTierLabel().toString());
    assertNotNull(solrInputField);
    assertEquals(solrMetadataTierC.getTierLabel().toString(), solrInputField.getName());
    assertEquals(solrMetadataTierC.getTierValue(), ((Collection<String>)solrInputField.getValue()).iterator().next());
  }
}