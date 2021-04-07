package eu.europeana.indexing.fullbean;

import eu.europeana.corelib.definitions.edm.entity.Aggregation;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.WebResourceImpl;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.jibx.Created;
import eu.europeana.metis.schema.jibx.EuropeanaAggregationType;
import eu.europeana.metis.schema.jibx.LiteralType;
import eu.europeana.metis.schema.jibx.Modified;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.schema.jibx.WebResourceType;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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

  /**
   * Converts an RDF to Full Bean.
   *
   * @param record The RDF record to convert.
   * @return The Full Bean.
   */
  public FullBeanImpl convertRdfToFullBean(RdfWrapper record) {

    // Create full bean and set about value.
    final FullBeanImpl fullBean = new FullBeanImpl();
    fullBean.setAbout(record.getAbout());

    // Set list properties.
    fullBean
        .setProvidedCHOs(convertList(record.getProvidedCHOs(), new ProvidedCHOFieldInput(), false));
    fullBean.setProxies(convertList(record.getProxies(), new ProxyFieldInput(), false));
    fullBean.setAggregations(convertAggregations(record));
    fullBean.setConcepts(convertList(record.getConcepts(), new ConceptFieldInput(), false));
    fullBean.setPlaces(convertList(record.getPlaces(), new PlaceFieldInput(), false));
    fullBean.setTimespans(convertList(record.getTimeSpans(), new TimespanFieldInput(), false));
    fullBean.setAgents(convertList(record.getAgents(), new AgentFieldInput(), false));
    fullBean.setLicenses(convertList(record.getLicenses(), new LicenseFieldInput(), false));
    fullBean.setServices(convertList(record.getServices(), new ServiceFieldInput(), false));
    fullBean.setQualityAnnotations(
        convertList(record.getQualityAnnotations(), new QualityAnnotationFieldInput(), false));

    // Set properties related to the Europeana aggregation
    fullBean.setEuropeanaCollectionName(new String[]{record.getDatasetName()});
    final Optional<EuropeanaAggregationType> europeanaAggregation = record
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

    // Done.
    return fullBean;
  }

  private List<Aggregation> convertAggregations(RdfWrapper record) {
    //The record web resources is reduced every time one of it's web resources gets referenced
    final List<WebResourceImpl> recordWebResources = new WebResourcesExtractor(record).get();
    //The reference list is being extended every time a new web resource is referenced from an aggregator
    final List<WebResourceImpl> referencedWebResources = new ArrayList<>(recordWebResources.size());
    //Convert the provider aggregations
    final List<AggregationImpl> providerAggregations = convertList(record.getProviderAggregations(),
        new AggregationFieldInput(recordWebResources, referencedWebResources), false);

    //Convert the aggregator aggregations
    final List<AggregationImpl> aggregatorAggregations = convertList(
        record.getAggregatorAggregations(),
        new AggregationFieldInput(recordWebResources, referencedWebResources), false);

    //We choose to add leftovers on the first provider aggregation
    final AggregationImpl firstProviderAggregation = Optional.ofNullable(providerAggregations)
        .stream().flatMap(List::stream).findFirst().orElse(null);
    if (firstProviderAggregation != null) {
      final List<WebResourceImpl> providerAggregationWebResources = firstProviderAggregation
          .getWebResources().stream().map(WebResourceImpl.class::cast).collect(Collectors.toList());
      providerAggregationWebResources.addAll(recordWebResources);
      firstProviderAggregation.setWebResources(providerAggregationWebResources);
    }

    //Combine aggregation lists
    return Stream.of(providerAggregations, aggregatorAggregations).map(Aggregation.class::cast)
        .collect(Collectors.toList());
  }

  private static Date convertToDate(String dateString) {
    return Date.from(Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(dateString)));
  }

  private static <S, T> List<T> convertList(List<S> sourceList, Function<S, T> converter,
      boolean returnNullIfEmpty) {
    final List<T> result = sourceList.stream().map(converter).collect(Collectors.toList());
    if (result.isEmpty() && returnNullIfEmpty) {
      return null;
    }
    return result;
  }

  private static class WebResourcesExtractor implements Supplier<List<WebResourceImpl>> {

    private final RdfWrapper record;
    private List<WebResourceImpl> webResources;

    public WebResourcesExtractor(RdfWrapper record) {
      this.record = record;
    }

    @Override
    public List<WebResourceImpl> get() {
      if (webResources == null) {
        final Collection<WebResourceType> webResourcesBeforeConversion = record.getWebResources()
            .stream().collect(Collectors.toMap(WebResourceType::getAbout, UnaryOperator.identity(),
                (first, second) -> first)).values();
        if (webResourcesBeforeConversion.isEmpty()) {
          webResources = Collections.emptyList();
        } else {
          webResources = webResourcesBeforeConversion.stream().map(new WebResourceFieldInput())
              .collect(Collectors.toList());
        }
      }
      return Collections.unmodifiableList(webResources);
    }
  }
}
