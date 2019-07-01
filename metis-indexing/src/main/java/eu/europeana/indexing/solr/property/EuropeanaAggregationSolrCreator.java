package eu.europeana.indexing.solr.property;

import eu.europeana.corelib.definitions.edm.entity.EuropeanaAggregation;
import eu.europeana.corelib.definitions.edm.entity.License;
import eu.europeana.corelib.definitions.edm.entity.QualityAnnotation;
import eu.europeana.indexing.solr.EdmLabel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrInputDocument;

/**
 * Property Solr Creator for 'edm:EuropeanaAggregation' tags.
 */
public class EuropeanaAggregationSolrCreator implements PropertySolrCreator<EuropeanaAggregation> {

  private final List<? extends License> licenses;
  private final Function<String, QualityAnnotation> qualityAnnotationGetter;

  /**
   * Constructor.
   *
   * @param licenses the list of licenses for the record. Is not null and does not contain null
   * values.
   * @param qualityAnnotationGetter Function to obtain the quality annotation for a given about
   * value. This function will only return a non-null value if the annotation is suitable for adding
   * to the solr.
   */
  public EuropeanaAggregationSolrCreator(List<? extends License> licenses,
      Function<String, QualityAnnotation> qualityAnnotationGetter) {
    this.licenses = new ArrayList<>(licenses);
    this.qualityAnnotationGetter = qualityAnnotationGetter;
  }

  @Override
  public void addToDocument(SolrInputDocument doc, EuropeanaAggregation europeanaAggregation) {
    SolrPropertyUtils.addValues(doc, EdmLabel.EUROPEANA_AGGREGATION_EDM_COUNTRY,
        europeanaAggregation.getEdmCountry());
    SolrPropertyUtils.addValues(doc, EdmLabel.EUROPEANA_AGGREGATION_EDM_LANGUAGE,
        europeanaAggregation.getEdmLanguage());
    SolrPropertyUtils.addValue(doc, EdmLabel.EUROPEANA_AGGREGATION_EDM_PREVIEW,
        europeanaAggregation.getEdmPreview());
    new WebResourceSolrCreator(licenses)
        .addAllToDocument(doc, europeanaAggregation.getWebResources());
    final List<QualityAnnotation> annotationsToAdd = Optional
        .ofNullable(europeanaAggregation.getDqvHasQualityAnnotation()).map(Arrays::stream)
        .orElseGet(Stream::empty).filter(StringUtils::isNotBlank).distinct()
        .map(qualityAnnotationGetter).filter(Objects::nonNull).collect(Collectors.toList());
    new QualityAnnotationSolrCreator().addAllToDocument(doc, annotationsToAdd);
  }
}
