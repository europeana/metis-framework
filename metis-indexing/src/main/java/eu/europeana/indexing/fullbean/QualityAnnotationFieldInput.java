package eu.europeana.indexing.fullbean;

import eu.europeana.metis.schema.jibx.QualityAnnotation;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType;
import eu.europeana.metis.schema.jibx.ResourceType;
import eu.europeana.corelib.solr.entity.QualityAnnotationImpl;
import java.util.Optional;
import java.util.function.Function;

/**
 * Converts a {@link QualityAnnotation} from an {@link eu.europeana.metis.schema.jibx.RDF} to
 * a {@link QualityAnnotationImpl} for a {@link eu.europeana.metis.schema.edm.beans.FullBean}.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2019-06-13
 */
public class QualityAnnotationFieldInput implements
    Function<QualityAnnotation, QualityAnnotationImpl> {

  @Override
  public QualityAnnotationImpl apply(QualityAnnotation qualityAnnotation) {
    QualityAnnotationImpl qualityAnnotationImpl = new QualityAnnotationImpl();
    qualityAnnotationImpl.setAbout(qualityAnnotation.getAbout());
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
