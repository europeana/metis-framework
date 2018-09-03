package eu.europeana.indexing.fullbean;

import eu.europeana.indexing.utils.RdfUtils;
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
import org.apache.commons.lang3.StringUtils;
import eu.europeana.corelib.definitions.jibx.CollectionName;
import eu.europeana.corelib.definitions.jibx.Created;
import eu.europeana.corelib.definitions.jibx.DatasetName;
import eu.europeana.corelib.definitions.jibx.EuropeanaAggregationType;
import eu.europeana.corelib.definitions.jibx.LiteralType;
import eu.europeana.corelib.definitions.jibx.Modified;
import eu.europeana.corelib.definitions.jibx.ProvidedCHOType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.WebResourceType;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.WebResourceImpl;

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
  public FullBeanImpl convertRdfToFullBean(RDF record) {

    final FullBeanImpl fullBean = new FullBeanImpl();
    fullBean.setAbout(record.getProvidedCHOList().stream().map(ProvidedCHOType::getAbout)
        .findFirst().orElse(null));

    fullBean.setProvidedCHOs(
        convertList(record.getProvidedCHOList(), new ProvidedCHOFieldInput(), false));
    fullBean.setProxies(convertList(record.getProxyList(), new ProxyFieldInput(), true));
    fullBean.setAggregations(convertList(record.getAggregationList(),
        new AggregationFieldInput(new WebResourcesExtractor(record)), false));
    fullBean.setConcepts(convertList(RdfUtils.getConceptsWithNonemptyAbout(record), new ConceptFieldInput(), true));
    fullBean.setPlaces(convertList(RdfUtils.getPlacesWithNonemptyAbout(record), new PlaceFieldInput(), true));
    fullBean.setTimespans(convertList(RdfUtils.getTimeSpansWithNonemptyAbout(record), new TimespanFieldInput(), true));
    fullBean.setAgents(convertList(RdfUtils.getAgentsWithNonemptyAbout(record), new AgentFieldInput(), true));
    fullBean.setLicenses(convertList(RdfUtils.getLicensesWithNonemptyAbout(record), new LicenseFieldInput(), true));
    fullBean.setServices(convertList(RdfUtils.getServicesWithNonemptyAbout(record), new ServiceFieldInput(), false));

    fullBean.setEuropeanaCollectionName(new String[]{getDatasetNameFromRdf(record)});

    final Optional<EuropeanaAggregationType> europeanaAggregation;
    if (record.getEuropeanaAggregationList() != null) {
      europeanaAggregation = record.getEuropeanaAggregationList().stream().findFirst();
    } else {
      europeanaAggregation = Optional.empty();
    }

    fullBean.setEuropeanaAggregation(
        europeanaAggregation.map(new EuropeanaAggregationFieldInput()).orElse(null));
    europeanaAggregation.map(EuropeanaAggregationType::getCompleteness).map(LiteralType::getString)
        .map(Integer::parseInt).ifPresent(fullBean::setEuropeanaCompleteness);

    fullBean.setTimestampCreated(europeanaAggregation.map(EuropeanaAggregationType::getCreated)
        .map(Created::getString).map(RdfToFullBeanConverter::convertToDate).orElse(null));
    fullBean.setTimestampUpdated(europeanaAggregation.map(EuropeanaAggregationType::getModified)
        .map(Modified::getString).map(RdfToFullBeanConverter::convertToDate).orElse(null));

    return fullBean;
  }

  private static Date convertToDate(String dateString) {
    return Date.from(Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(dateString)));
  }

  private static <S, T> List<T> convertList(List<S> sourceList, Function<S, T> converter,
      boolean returnNullIfEmpty) {
    final List<T> result;
    if (sourceList != null) {
      result = sourceList.stream().map(converter).collect(Collectors.toList());
    } else {
      result = new ArrayList<>();
    }
    if (result.isEmpty() && returnNullIfEmpty) {
      return null;
    }
    return result;
  }

  private static String getDatasetNameFromRdf(RDF rdf) {

    // Try to find the europeana aggregation
    final EuropeanaAggregationType aggregation;
    if (rdf.getEuropeanaAggregationList() == null || rdf.getEuropeanaAggregationList().isEmpty()) {
      aggregation = null;
    } else {
      aggregation = rdf.getEuropeanaAggregationList().get(0);
    }
    if (aggregation == null) {
      return "";
    }

    // Try the dataset name property from the aggregation.
    final DatasetName datasetNameObject = aggregation.getDatasetName();
    final String datasetName = datasetNameObject == null ? null : datasetNameObject.getString();
    if (StringUtils.isNotBlank(datasetName)) {
      return datasetName;
    }

    // If that fails, try the collection name property from the aggregation.
    final CollectionName collectionNameObject = aggregation.getCollectionName();
    final String collectionName =
        collectionNameObject == null ? null : collectionNameObject.getString();
    return StringUtils.isNotBlank(collectionName) ? collectionName : "";
  }

  private static class WebResourcesExtractor implements Supplier<List<WebResourceImpl>> {

    private final RDF record;
    private List<WebResourceImpl> webResources = null;

    public WebResourcesExtractor(RDF record) {
      this.record = record;
    }

    @Override
    public List<WebResourceImpl> get() {
      if (webResources == null) {
        final Collection<WebResourceType> webResourcesBeforeConversion = RdfUtils
            .getWebResourcesWithNonemptyAbout(record).collect(Collectors
                .toMap(WebResourceType::getAbout, UnaryOperator.identity(),
                    (first, second) -> first)).values();
        if (!webResourcesBeforeConversion.isEmpty()) {
          webResources = webResourcesBeforeConversion.stream().map(new WebResourceFieldInput())
              .collect(Collectors.toList());
        } else {
          webResources = Collections.emptyList();
        }
      }
      return Collections.unmodifiableList(webResources);
    }
  }
}
