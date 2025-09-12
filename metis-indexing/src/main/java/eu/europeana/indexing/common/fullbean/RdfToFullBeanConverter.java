package eu.europeana.indexing.common.fullbean;

import eu.europeana.corelib.definitions.edm.entity.Aggregation;
import eu.europeana.corelib.definitions.edm.entity.PersistentIdentifier;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.WebResourceImpl;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.jibx.Created;
import eu.europeana.metis.schema.jibx.EuropeanaAggregationType;
import eu.europeana.metis.schema.jibx.HasQualityAnnotation;
import eu.europeana.metis.schema.jibx.LiteralType;
import eu.europeana.metis.schema.jibx.Modified;
import eu.europeana.metis.schema.jibx.QualityAnnotation;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.schema.jibx.WebResourceType;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class converts instances of {@link RDF} to instances of {@link FullBeanImpl}.
 *
 * @author jochen
 */
public class RdfToFullBeanConverter {

  private static Date convertToDate(String dateString) {
    return Date.from(Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(dateString)));
  }

  private static <S, T> List<T> convertList(List<S> sourceList, Function<S, T> converter,
      boolean returnNullIfEmpty) {
    final List<T> result = sourceList.stream().map(converter).toList();
    if (result.isEmpty() && returnNullIfEmpty) {
      return null;
    }
    return result;
  }

  private static List<QualityAnnotation> getQualityAnnotations(RdfWrapper rdfWrappedRecord) {
    return Stream.concat(rdfWrappedRecord.getEuropeanaAggregation()
                .stream()
                .flatMap(qa -> {
                  if (qa.getHasQualityAnnotationList() == null) {
                    return null;
                  } else {
                    return qa.getHasQualityAnnotationList().stream();
                  }
                })
                .filter(Objects::nonNull)
                .map(HasQualityAnnotation::getQualityAnnotation),
            rdfWrappedRecord.getAggregations()
                .stream()
                .flatMap(qa -> {
                  if (qa.getHasQualityAnnotationList() == null) {
                    return null;
                  } else {
                    return qa.getHasQualityAnnotationList().stream();
                  }
                })
                .filter(Objects::nonNull)
                .map(HasQualityAnnotation::getQualityAnnotation))
        .toList();
  }

  /**
   * Converts an RDF to Full Bean.
   *
   * @param rdfWrapper The RDF record to convert.
   * @return The Full Bean.
   */
  public FullBeanImpl convertRdfToFullBean(RdfWrapper rdfWrapper) {

    // Create full bean and set about value.
    final FullBeanImpl fullBean = new FullBeanImpl();
    fullBean.setAbout(rdfWrapper.getAbout());
    List<? extends PersistentIdentifier> persistentIdentifierList = convertList(rdfWrapper.getPersistentIdentifiers(), new PersistentIdentifierFieldInput(), false);
    // Set list properties.
    fullBean.setProvidedCHOs(convertList(rdfWrapper.getProvidedCHOs(), new ProvidedCHOFieldInput(), false));
    fullBean.setProxies(convertList(rdfWrapper.getProxies(), new ProxyFieldInput(persistentIdentifierList), false));
    fullBean.setAggregations(convertAggregations(rdfWrapper));
    fullBean.setConcepts(convertList(rdfWrapper.getConcepts(), new ConceptFieldInput(), false));
    fullBean.setPlaces(convertList(rdfWrapper.getPlaces(), new PlaceFieldInput(), false));
    fullBean.setTimespans(convertList(rdfWrapper.getTimeSpans(), new TimespanFieldInput(), false));
    fullBean.setAgents(convertList(rdfWrapper.getAgents(), new AgentFieldInput(), false));
    fullBean.setOrganizations(convertList(rdfWrapper.getOrganizations(), new OrganizationFieldInput(), false));
    fullBean.setLicenses(convertList(rdfWrapper.getLicenses(), new LicenseFieldInput(), false));
    fullBean.setServices(convertList(rdfWrapper.getServices(), new ServiceFieldInput(), false));
    var qualityAnnotationsList = convertList(getQualityAnnotations(rdfWrapper), new QualityAnnotationFieldInput(), false);
    fullBean.setQualityAnnotations(qualityAnnotationsList);

    // Set properties related to the Europeana aggregation
    fullBean.setEuropeanaCollectionName(new String[]{rdfWrapper.getDatasetName()});
    final Optional<EuropeanaAggregationType> europeanaAggregation = rdfWrapper
        .getEuropeanaAggregation();
    fullBean.setEuropeanaAggregation(
        europeanaAggregation.map(new EuropeanaAggregationFieldInput()).orElse(null));
    europeanaAggregation.map(EuropeanaAggregationType::getCompleteness).map(LiteralType::getString)
                        .map(Integer::parseInt).ifPresent(fullBean::setEuropeanaCompleteness);
    fullBean.setTimestampCreated(
        europeanaAggregation.map(EuropeanaAggregationType::getCreated).map(Created::getString)
                            .map(RdfToFullBeanConverter::convertToDate).orElse(null));
    fullBean.setTimestampUpdated(
        europeanaAggregation.map(EuropeanaAggregationType::getModified).map(Modified::getString)
                            .map(RdfToFullBeanConverter::convertToDate).orElse(null));

    //List<PersistentIdentifierImpl> persistentIdentifierList = convertList()
    // Done.
    return fullBean;
  }

  private List<Aggregation> convertAggregations(RdfWrapper rdfWrapper) {
    //The record web resources is reduced every time one of it's web resources gets referenced
    //We only keep the first web resource out of duplicate web resources with the same about value
    final Map<String, WebResourceImpl> recordWebResourcesMap = new WebResourcesExtractor(rdfWrapper)
        .get().stream().collect(Collectors.toMap(WebResourceImpl::getAbout, Function.identity(),
            (existing, replacement) -> existing));
    //The reference list is being extended every time a new web resource is referenced from an aggregator
    final Set<String> referencedWebResourceAbouts = HashSet.newHashSet(recordWebResourcesMap.size());
    //We first convert the provider aggregations because we want this aggregator to get first get matches of web resources
    final List<AggregationImpl> providerAggregations = convertList(rdfWrapper.getProviderAggregations(),
        new AggregationFieldInput(recordWebResourcesMap, referencedWebResourceAbouts), false);

    //Convert the aggregator aggregations
    final List<AggregationImpl> aggregatorAggregations = convertList(
        rdfWrapper.getAggregatorAggregations(),
        new AggregationFieldInput(recordWebResourcesMap, referencedWebResourceAbouts), false);

    //We choose to add leftovers on the first provider aggregation
    final AggregationImpl firstProviderAggregation = Optional.ofNullable(providerAggregations)
                                                             .stream().flatMap(List::stream).findFirst().orElse(null);
    if (firstProviderAggregation != null) {
      final List<WebResourceImpl> providerAggregationWebResources = firstProviderAggregation
          .getWebResources().stream().map(WebResourceImpl.class::cast).collect(Collectors.toList());
      providerAggregationWebResources.addAll(recordWebResourcesMap.values());
      firstProviderAggregation.setWebResources(providerAggregationWebResources);
    }

    //Combine aggregation lists
    return Stream.of(providerAggregations, aggregatorAggregations).filter(Objects::nonNull)
                 .flatMap(List::stream).map(Aggregation.class::cast).toList();
  }

  private static class WebResourcesExtractor implements Supplier<List<WebResourceImpl>> {

    private final RdfWrapper rdfWrapper;
    private List<WebResourceImpl> webResources;

    public WebResourcesExtractor(RdfWrapper rdfWrapper) {
      this.rdfWrapper = rdfWrapper;
    }

    @Override
    public List<WebResourceImpl> get() {
      if (webResources == null) {
        final Collection<WebResourceType> webResourcesBeforeConversion = rdfWrapper.getWebResources()
                                                                                   .stream().collect(
                Collectors.toMap(WebResourceType::getAbout, UnaryOperator.identity(),
                    (first, second) -> first)).values();
        if (webResourcesBeforeConversion.isEmpty()) {
          webResources = Collections.emptyList();
        } else {
          webResources = webResourcesBeforeConversion.stream().map(new WebResourceFieldInput()).toList();
        }
      }
      return Collections.unmodifiableList(webResources);
    }
  }
}
