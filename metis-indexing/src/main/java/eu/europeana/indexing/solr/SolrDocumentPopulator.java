package eu.europeana.indexing.solr;

import static eu.europeana.indexing.solr.EdmLabel.CREATED_DATE;
import static eu.europeana.indexing.solr.EdmLabel.CREATED_DATE_BEGIN;
import static eu.europeana.indexing.solr.EdmLabel.CREATED_DATE_END;
import static eu.europeana.indexing.solr.EdmLabel.ISSUED_DATE;
import static eu.europeana.indexing.solr.EdmLabel.ISSUED_DATE_BEGIN;
import static eu.europeana.indexing.solr.EdmLabel.ISSUED_DATE_END;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

import eu.europeana.corelib.definitions.edm.entity.QualityAnnotation;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.LicenseImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.indexing.solr.facet.FacetEncoder;
import eu.europeana.indexing.solr.property.AgentSolrCreator;
import eu.europeana.indexing.solr.property.AggregationSolrCreator;
import eu.europeana.indexing.solr.property.ConceptSolrCreator;
import eu.europeana.indexing.solr.property.EuropeanaAggregationSolrCreator;
import eu.europeana.indexing.solr.property.FullBeanSolrProperties;
import eu.europeana.indexing.solr.property.LicenseSolrCreator;
import eu.europeana.indexing.solr.property.PlaceSolrCreator;
import eu.europeana.indexing.solr.property.ProvidedChoSolrCreator;
import eu.europeana.indexing.solr.property.ProxySolrCreator;
import eu.europeana.indexing.solr.property.QualityAnnotationSolrCreator;
import eu.europeana.indexing.solr.property.ServiceSolrCreator;
import eu.europeana.indexing.solr.property.SolrPropertyUtils;
import eu.europeana.indexing.solr.property.TimespanSolrCreator;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.WebResourceLinkType;
import eu.europeana.indexing.utils.WebResourceWrapper;
import eu.europeana.metis.schema.jibx.Begin;
import eu.europeana.metis.schema.jibx.End;
import eu.europeana.metis.schema.jibx.EuropeanaType.Choice;
import eu.europeana.metis.schema.jibx.ProxyType;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType.Resource;
import eu.europeana.metis.schema.jibx.TimeSpanType;
import eu.europeana.metis.schema.model.MediaType;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrInputDocument;

/**
 * This class provides functionality to populate Solr documents. Both methods in this class should be called to fill the Solr
 * document. The method {@link #populateWithProperties(SolrInputDocument, FullBeanImpl)} copies properties from the source to the
 * Solr document. The method {@link #populateWithFacets(SolrInputDocument, RdfWrapper)} on the other hand performs some analysis
 * and sets technical metadata.
 *
 * @author jochen
 */
public class SolrDocumentPopulator {

  /**
   * Populates a Solr document with the properties of the full bean. Please note: this method should only be called once on a
   * given document, otherwise the behavior is not defined.
   *
   * @param document The Solr document to populate.
   * @param fullBean The FullBean to populate from.
   */
  public void populateWithProperties(SolrInputDocument document, FullBeanImpl fullBean) {

    new FullBeanSolrProperties().setProperties(document, fullBean);

    // Gather the licenses.
    final List<LicenseImpl> licenses = ofNullable(fullBean.getLicenses()).stream()
                                                                         .flatMap(List::stream).filter(Objects::nonNull)
                                                                         .toList();

    // Gather the quality annotations.
    final Set<String> acceptableTargets = ofNullable(fullBean.getAggregations()).stream()
                                                                                .flatMap(Collection::stream)
                                                                                .filter(Objects::nonNull)
                                                                                .map(AggregationImpl::getAbout)
                                                                                .filter(Objects::nonNull)
                                                                                .collect(Collectors.toSet());
    final Predicate<QualityAnnotation> hasAcceptableTarget = annotation -> ofNullable(annotation.getTarget()).stream().flatMap(
                                                                                                                 Arrays::stream)
                                                                                                             .anyMatch(
                                                                                                                 acceptableTargets::contains);
    final List<QualityAnnotation> annotationsToAdd = ofNullable(fullBean.getQualityAnnotations()).map(List::stream)
                                                                                                 .orElseGet(Stream::empty)
                                                                                                 .filter(Objects::nonNull)
                                                                                                 .filter(
                                                                                                     annotation -> StringUtils.isNotBlank(
                                                                                                         annotation.getBody()))
                                                                                                 .filter(hasAcceptableTarget)
                                                                                                 .collect(Collectors.toList());
    new QualityAnnotationSolrCreator().addAllToDocument(document, annotationsToAdd);

    // Add the containing objects.
    new ProvidedChoSolrCreator().addToDocument(document, fullBean.getProvidedCHOs().getFirst());
    new AggregationSolrCreator(licenses, fullBean.getOrganizations())
        .addToDocument(document, getDataProviderAggregations(fullBean).getFirst());
    new EuropeanaAggregationSolrCreator(licenses)
        .addToDocument(document, fullBean.getEuropeanaAggregation());
    new ProxySolrCreator().addAllToDocument(document, fullBean.getProxies());
    new ConceptSolrCreator().addAllToDocument(document, fullBean.getConcepts());
    new TimespanSolrCreator().addAllToDocument(document, fullBean.getTimespans());
    new AgentSolrCreator().addAllToDocument(document, fullBean.getAgents());
    new PlaceSolrCreator().addAllToDocument(document, fullBean.getPlaces());
    new ServiceSolrCreator().addAllToDocument(document, fullBean.getServices());

    // Add the licenses.
    final Set<String> defRights = fullBean.getAggregations().stream()
                                          .map(AggregationImpl::getEdmRights).filter(Objects::nonNull)
                                          .flatMap(SolrPropertyUtils::getRightsFromMap).collect(Collectors.toSet());
    new LicenseSolrCreator(license -> defRights.contains(license.getAbout()))
        .addAllToDocument(document, fullBean.getLicenses());
  }

  /**
   * Populates a Solr document with the CRF fields of the RDF. Please note: this method should only be called once on a given
   * document, otherwise the behavior is not defined.
   *
   * @param document The document to populate.
   * @param rdf The RDF to populate from.
   */
  public void populateWithFacets(SolrInputDocument document, RdfWrapper rdf) {

    // has_thumbnails is true if and only if edm:EuropeanaAggregation/edm:preview is filled and the
    // associated edm:webResource exists with technical metadata (i.e. ebucore:hasMimetype is set).
    document.addField(EdmLabel.FACET_HAS_THUMBNAILS.toString(), rdf.hasThumbnails());

    // has_media is true if and only if there is at least one web resource of type 'isShownBy'
    // or 'hasView' representing technical metadata of a known type.
    final List<WebResourceWrapper> webResourcesWithMedia = rdf.getWebResourceWrappers(
        EnumSet.of(WebResourceLinkType.IS_SHOWN_BY, WebResourceLinkType.HAS_VIEW));
    final boolean hasMedia = webResourcesWithMedia.stream().map(WebResourceWrapper::getMediaType)
                                                  .anyMatch(type -> type != MediaType.OTHER);
    document.addField(EdmLabel.FACET_HAS_MEDIA.toString(), hasMedia);

    // has_landingPage is true if and only if there is at least one web resource of type
    // 'isShownAt', representing technical metadata of some (non-empty) mime type.
    document.addField(EdmLabel.FACET_HAS_LANDING_PAGE.toString(), rdf.hasLandingPage());

    // is_fulltext is true if and only if there is at least one web resource of type 'isShownBy'
    // or 'hasView' with 'rdf:type' equal to 'edm:FullTextResource'.
    final boolean isFullText = webResourcesWithMedia.stream().map(WebResourceWrapper::getType)
                                                    .anyMatch("http://www.europeana.eu/schemas/edm/FullTextResource"::equals);
    document.addField(EdmLabel.FACET_IS_FULL_TEXT.toString(), isFullText);

    // Compose the filter and facet tags. Only use the web resources of type 'isShownBy' or 'hasView'.
    final Set<Integer> filterCodes = new HashSet<>();
    final Set<Integer> valueCodes = new HashSet<>();
    final FacetEncoder encoder = new FacetEncoder();
    for (WebResourceWrapper webResource : webResourcesWithMedia) {
      filterCodes.addAll(encoder.getFacetFilterCodes(webResource));
      valueCodes.addAll(encoder.getFacetValueCodes(webResource));
    }

    // Add the filter and facet tags to the Solr document.
    for (Integer code : filterCodes) {
      document.addField(EdmLabel.FACET_FILTER_CODES.toString(), code);
    }
    for (Integer code : valueCodes) {
      document.addField(EdmLabel.FACET_VALUE_CODES.toString(), code);
    }
  }

  /**
   * Populates Solr document with the date range fields. Please note: this method should only be called once on a * given
   * document, otherwise the behavior is not defined.
   *
   * @param document The document to populate.
   * @param rdfWrapper The RDF to populate from.
   */
  public void populateWithDateRanges(SolrInputDocument document, RdfWrapper rdfWrapper) {
    populateWithDateRanges(document, rdfWrapper, Choice::ifCreated, Choice::getCreated, CREATED_DATE, CREATED_DATE_BEGIN,
        CREATED_DATE_END);
    populateWithDateRanges(document, rdfWrapper, Choice::ifIssued, Choice::getIssued, ISSUED_DATE, ISSUED_DATE_BEGIN,
        ISSUED_DATE_END);
  }

  private void populateWithDateRanges(SolrInputDocument document, RdfWrapper rdfWrapper, Predicate<Choice> choiceTypePredicate,
      Function<Choice, ResourceOrLiteralType> choiceValueGetter, EdmLabel edmLabelDate, EdmLabel edmLabelDateBegin,
      EdmLabel edmLabelDateEnd) {
    final List<TimeSpanType> normalizedTimeSpans
        = rdfWrapper.getTimeSpans().stream().filter(timeSpanType -> timeSpanType.getNotation() != null).toList();

    final ProxyType europeanaProxy = rdfWrapper.getProxies().stream().filter(RdfWrapper::isEuropeanaProxy).findFirst()
                                               .orElseThrow();

    final List<String> proxyChoiceLinks = europeanaProxy.getChoiceList().stream().filter(choiceTypePredicate)
                                                        .map(choiceValueGetter).map(ResourceOrLiteralType::getResource)
                                                        .map(Resource::getResource).toList();

    final List<TimeSpanType> proxyChoiceMatchingTimeSpans = normalizedTimeSpans.stream().filter(
        timeSpanType -> proxyChoiceLinks.contains(timeSpanType.getAbout())).toList();

    Optional<LocalDate> earliestBegin = empty();
    Optional<LocalDate> latestEnd = empty();
    for (TimeSpanType timeSpanType : proxyChoiceMatchingTimeSpans) {
      final String begin = ofNullable(timeSpanType.getBegin()).map(Begin::getString).orElse(null);
      final String end = ofNullable(timeSpanType.getEnd()).map(End::getString).orElse(null);
      // If either 'begin' or 'end' is null, set it to the value of the other
      final String finalBegin = ofNullable(begin).orElse(end);
      final String finalEnd = ofNullable(end).orElse(begin);
      // We only need to check if finalBegin is no-null since if finalBegin is non-null then finalEnd will certainly be non-null
      if (finalBegin != null) {
        document.addField(edmLabelDate.toString(), String.format("[%sTO%s]", finalBegin, finalEnd));

        final LocalDate localDateFinalBegin = LocalDate.parse(finalBegin);
        final LocalDate localDateFinalEnd = LocalDate.parse(finalEnd);

        earliestBegin = earliestBegin.map(earliest -> localDateFinalBegin.isBefore(earliest) ? localDateFinalBegin : earliest)
                                     .or(() -> of(localDateFinalBegin));
        latestEnd = latestEnd.map(latest -> localDateFinalEnd.isAfter(latest) ? localDateFinalEnd : latest)
                             .or(() -> of(localDateFinalEnd));
      }
    }
    earliestBegin.ifPresent(date -> document.addField(edmLabelDateBegin.toString(), date.toString()));
    latestEnd.ifPresent(date -> document.addField(edmLabelDateEnd.toString(), date.toString()));
  }

  private List<AggregationImpl> getDataProviderAggregations(FullBeanImpl fullBean) {
    List<String> proxyInResult = fullBean.getProxies().stream()
                                         .filter(not(ProxyImpl::isEuropeanaProxy))
                                         .filter(proxy -> ArrayUtils.isEmpty(proxy.getLineage())).map(ProxyImpl::getProxyIn)
                                         .map(Arrays::asList).flatMap(List::stream).toList();

    return fullBean.getAggregations().stream().filter(x -> proxyInResult.contains(x.getAbout())).toList();
  }
}
