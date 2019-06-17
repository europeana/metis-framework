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
    EdmLabel tierTypeLabel = null;
    if (qualityAnnotation.getAbout().endsWith("#metadataTier")) {
      tierTypeLabel = EdmLabel.METADATA_TIER;
    } else if (qualityAnnotation.getAbout().endsWith("#contentTier")) {
      tierTypeLabel = EdmLabel.CONTENT_TIER;
    }

    if (tierTypeLabel != null) {
      SolrPropertyUtils.addValues(doc, tierTypeLabel, qualityAnnotation.getOaHasBody());
    }
  }

}
