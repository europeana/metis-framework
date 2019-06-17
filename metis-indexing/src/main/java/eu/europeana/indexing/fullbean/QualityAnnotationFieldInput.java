package eu.europeana.indexing.fullbean;

import eu.europeana.corelib.definitions.jibx.QualityAnnotation;
import eu.europeana.corelib.solr.entity.QualityAnnotationImpl;
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
        FieldInputUtils.resourceOrLiteralListToArray(qualityAnnotation.getCreatedList()));
    qualityAnnotationImpl.setOaHasTarget(
        FieldInputUtils.resourceListToArray(qualityAnnotation.getHasTargetList()));
    qualityAnnotationImpl.setOaHasBody(
        FieldInputUtils.resourceListToArray(qualityAnnotation.getHasBodyList()));
    return qualityAnnotationImpl;
  }
}
