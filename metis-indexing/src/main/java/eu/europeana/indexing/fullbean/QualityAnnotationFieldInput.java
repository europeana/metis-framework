package eu.europeana.indexing.fullbean;

import eu.europeana.corelib.definitions.jibx.QualityAnnotation;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
import eu.europeana.corelib.definitions.jibx.ResourceType;
import eu.europeana.corelib.solr.entity.QualityAnnotationImpl;
import java.util.Optional;
import java.util.function.Function;

/**
 * Converts a {@link QualityAnnotation} from an {@link eu.europeana.corelib.definitions.jibx.RDF} to
 * a {@link QualityAnnotationImpl} for a {@link eu.europeana.corelib.definitions.edm.beans.FullBean}.
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
    qualityAnnotationImpl.setDcTermsCreated(
        Optional.ofNullable(qualityAnnotation.getCreated()).map(ResourceOrLiteralType::getString)
            .orElse(null));
    qualityAnnotationImpl.setOaHasTarget(Optional.ofNullable(qualityAnnotation.getHasTarget()).map(
        ResourceType::getResource)
        .orElse(null));
    qualityAnnotationImpl.setOaHasBody(
        Optional.ofNullable(qualityAnnotation.getHasBody()).map(ResourceType::getResource)
            .orElse(null));
    return qualityAnnotationImpl;
  }
}
