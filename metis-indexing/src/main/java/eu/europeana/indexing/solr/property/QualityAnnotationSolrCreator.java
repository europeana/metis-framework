package eu.europeana.indexing.solr.property;

import eu.europeana.corelib.definitions.edm.entity.QualityAnnotation;
import eu.europeana.indexing.utils.RdfTierUtils;
import java.util.Optional;
import org.apache.solr.common.SolrInputDocument;

/**
 * Property Solr Creator for 'dqv:QualityAnnotation' tags.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2019-06-13
 */
public class QualityAnnotationSolrCreator implements PropertySolrCreator<QualityAnnotation> {

  @Override
  public void addToDocument(SolrInputDocument doc, QualityAnnotation qualityAnnotation) {
    Optional.of(qualityAnnotation).map(RdfTierUtils::getTier).ifPresent(
        tier -> SolrPropertyUtils.addValue(doc, tier.getEdmLabel(), tier.getTier().toString())
    );
  }
}
