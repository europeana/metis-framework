package eu.europeana.indexing.solr.property;

import eu.europeana.corelib.definitions.edm.entity.QualityAnnotation;
import eu.europeana.indexing.solr.EdmLabel;
import org.apache.solr.common.SolrInputDocument;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2019-06-13
 */
public class QualityAnnotationSolrCreator implements PropertySolrCreator<QualityAnnotation> {

  @Override
  public void addToDocument(SolrInputDocument doc, QualityAnnotation qualityAnnotation) {
    final EdmLabel tierTypeLabel;
    if (qualityAnnotation.getAbout().contains("metadataTier")) {
      tierTypeLabel = EdmLabel.METADATA_TIER;
    } else {
      tierTypeLabel = EdmLabel.CONTENT_TIER;
    }
    SolrPropertyUtils.addValues(doc, tierTypeLabel, qualityAnnotation.getOaHasBody());
  }

}
