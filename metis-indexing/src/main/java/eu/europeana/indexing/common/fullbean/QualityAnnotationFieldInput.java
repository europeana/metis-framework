package eu.europeana.indexing.common.fullbean;

import eu.europeana.corelib.solr.entity.QualityAnnotationImpl;
import eu.europeana.metis.schema.jibx.QualityAnnotation;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType;
import eu.europeana.metis.schema.jibx.ResourceType;
import java.util.Optional;
import java.util.function.Function;

/**
 * Converts a {@link QualityAnnotation} from an {@link eu.europeana.metis.schema.jibx.RDF} to a
 * {@link QualityAnnotationImpl} for a {@link eu.europeana.corelib.definitions.edm.beans.FullBean}.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2019-06-13
 */
final class QualityAnnotationFieldInput implements
    Function<QualityAnnotation, QualityAnnotationImpl> {

  @Override
  public QualityAnnotationImpl apply(QualityAnnotation qualityAnnotation) {
    QualityAnnotationImpl qualityAnnotationImpl = new QualityAnnotationImpl();
    qualityAnnotationImpl.setCreated(
        Optional.ofNullable(qualityAnnotation.getCreated()).map(ResourceOrLiteralType::getString)
                .orElse(null));
    qualityAnnotationImpl
        .setTarget(FieldInputUtils.resourceListToArray(qualityAnnotation.getHasTargetList()));
    qualityAnnotationImpl.setBody(
        Optional.ofNullable(qualityAnnotation.getHasBody()).map(ResourceType::getResource)
                .orElse(null));
    return qualityAnnotationImpl;
  }
}
