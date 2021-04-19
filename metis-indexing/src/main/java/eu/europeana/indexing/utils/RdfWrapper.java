package eu.europeana.indexing.utils;

import eu.europeana.metis.schema.jibx.AboutType;
import eu.europeana.metis.schema.jibx.AgentType;
import eu.europeana.metis.schema.jibx.Aggregation;
import eu.europeana.metis.schema.jibx.CollectionName;
import eu.europeana.metis.schema.jibx.Concept;
import eu.europeana.metis.schema.jibx.DatasetName;
import eu.europeana.metis.schema.jibx.Description;
import eu.europeana.metis.schema.jibx.EdmType;
import eu.europeana.metis.schema.jibx.EuropeanaAggregationType;
import eu.europeana.metis.schema.jibx.EuropeanaProxy;
import eu.europeana.metis.schema.jibx.EuropeanaType;
import eu.europeana.metis.schema.jibx.EuropeanaType.Choice;
import eu.europeana.metis.schema.jibx.Identifier;
import eu.europeana.metis.schema.jibx.IsShownBy;
import eu.europeana.metis.schema.jibx.License;
import eu.europeana.metis.schema.jibx.PlaceType;
import eu.europeana.metis.schema.jibx.ProvidedCHOType;
import eu.europeana.metis.schema.jibx.ProxyIn;
import eu.europeana.metis.schema.jibx.ProxyType;
import eu.europeana.metis.schema.jibx.QualityAnnotation;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.schema.jibx.ResourceType;
import eu.europeana.metis.schema.jibx.Service;
import eu.europeana.metis.schema.jibx.TimeSpanType;
import eu.europeana.metis.schema.jibx.Title;
import eu.europeana.metis.schema.jibx.Type2;
import eu.europeana.metis.schema.jibx.WebResourceType;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * This class is a wrapper around instances of type {@link RDF}. Its responsibility is to hide the
 * RDF structure and objects needed when extracting information from the records.
 *
 * @author jochen
 */
public class RdfWrapper {

  private final RDF record;

  /**
   * Constructor.
   *
   * @param record The record to wrap.
   */
  public RdfWrapper(RDF record) {
    this.record = record;
  }

  /**
   * This method extracts the rdf:about from the RDF object.
   *
   * @return The record's rdf:about (obtained from a providedCHO). Or null if none could be found.
   */
  public String getAbout() {
    return getFilteredPropertyStream(record.getProvidedCHOList()).map(ProvidedCHOType::getAbout)
        .findFirst().orElse(null);
  }

  /**
   * This method extracts the dataset name or, if that doesn't exist, the collection name from the
   * RDF object.
   *
   * @return The dataset name, or the empty string if no dataset name or collection name is known.
   */
  public String getDatasetName() {
    final Optional<EuropeanaAggregationType> aggregation = getEuropeanaAggregation();
    final Optional<String> datasetName = aggregation.map(EuropeanaAggregationType::getDatasetName)
        .map(DatasetName::getString).filter(StringUtils::isNotBlank);
    final Optional<String> collectionName = aggregation
        .map(EuropeanaAggregationType::getCollectionName).map(CollectionName::getString)
        .filter(StringUtils::isNotBlank);
    return datasetName.orElseGet(() -> collectionName.orElse(StringUtils.EMPTY));
  }

  /**
   * This method extracts the Europeana aggregation from the RDF object.
   *
   * @return The Europeana aggregation in a non-null {@link Optional}.
   */
  public Optional<EuropeanaAggregationType> getEuropeanaAggregation() {
    return getPropertyStream(record.getEuropeanaAggregationList()).findFirst();
  }

  public List<Identifier> getProviderProxyIdentifiers() {
    final List<Choice> choiceList = getProviderProxiesChoices();
    return choiceList.stream().filter(Choice::ifIdentifier).map(Choice::getIdentifier)
        .filter(Objects::nonNull).collect(Collectors.toList());
  }

  public List<Title> getProviderProxyTitles() {
    final List<Choice> choiceList = getProviderProxiesChoices();
    return choiceList.stream().filter(Choice::ifTitle).map(Choice::getTitle)
        .filter(Objects::nonNull).collect(Collectors.toList());
  }

  public List<Description> getProviderProxyDescriptions() {
    final List<Choice> choiceList = getProviderProxiesChoices();
    return choiceList.stream().filter(Choice::ifDescription).map(Choice::getDescription)
        .filter(Objects::nonNull).collect(Collectors.toList());
  }

  public List<Choice> getProviderProxiesChoices() {
    return getProviderProxies().stream().map(EuropeanaType::getChoiceList)
        .flatMap(Collection::stream).filter(Objects::nonNull).collect(Collectors.toList());
  }

  public List<IsShownBy> getIsShownByList() {
    return getAggregations().stream().map(Aggregation::getIsShownBy).filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  /**
   * This method extracts all provided CHOs from the RDF object.
   *
   * @return The list of provided CHOs. Is not null, but could be empty.
   */
  public List<ProvidedCHOType> getProvidedCHOs() {
    return getPropertyList(record.getProvidedCHOList());
  }

  /**
   * This method extracts all proxy objects from the RDF object.
   *
   * @return The list of proxies. Is not null, but could be empty.
   */
  public List<ProxyType> getProxies() {
    return getPropertyList(record.getProxyList());
  }

  /**
   * This method extracts all provider proxy objects from the RDF object.
   *
   * @return The list of proxies. Is not null, but could be empty.
   */
  public List<ProxyType> getProviderProxies() {
    return getProxies().stream().filter(proxy -> !isEuropeanaProxy(proxy))
        .collect(Collectors.toList());
  }

  private static boolean isEuropeanaProxy(ProxyType proxy) {
    return Optional.of(proxy).map(ProxyType::getEuropeanaProxy)
        .map(EuropeanaProxy::isEuropeanaProxy).orElse(Boolean.FALSE);
  }

  /**
   * This method extracts all aggregations from the RDF object.
   *
   * @return The list of aggregations. Is not null, but could be empty.
   */
  public List<Aggregation> getAggregations() {
    return getPropertyList(record.getAggregationList());
  }

  /**
   * Extract provider aggregations.
   * <p>To find provider aggregations we first find the {@link ProxyType}s of the record that has
   * an <b>EMPTY</b> {@link ProxyType#getLineageList()}. From those we return any aggregation, in
   * the record, that its about value matches any value in the {@link
   * ProxyType#getProxyInList()}.</p>
   *
   * @return the list of aggregations
   */
  public List<Aggregation> getProviderAggregations() {
    return getAggregations(proxyType -> CollectionUtils.isEmpty(proxyType.getLineageList()));
  }

  /**
   * Extract aggregator aggregations.
   * <p>To find aggregator aggregations we first find the {@link ProxyType}s of the record that
   * has a <b>NON EMPTY</b> {@link ProxyType#getLineageList()}. From those we return any
   * aggregation, in the record, that its about value matches any value in the {@link
   * ProxyType#getProxyInList()}.</p>
   *
   * @return the list of aggregations
   */
  public List<Aggregation> getAggregatorAggregations() {
    return getAggregations(proxyType -> CollectionUtils.isNotEmpty(proxyType.getLineageList()));
  }

  private List<Aggregation> getAggregations(Predicate<? super ProxyType> proxyTypePredicate) {
    final Set<String> proxyInList = getProviderProxies().stream().filter(proxyTypePredicate)
        .map(ProxyType::getProxyInList).flatMap(List::stream).map(ProxyIn::getResource)
        .collect(Collectors.toSet());

    return record.getAggregationList().stream()
        .filter(aggregation -> proxyInList.contains(aggregation.getAbout()))
        .collect(Collectors.toList());
  }

  /**
   * @return The license of this entity.
   */
  public LicenseType getLicenseType() {
    return getAggregations().stream().map(Aggregation::getRights).filter(Objects::nonNull)
        .map(LicenseType::getLicenseType).filter(Objects::nonNull).findAny().orElse(null);
  }

  /**
   * @return The edm:type of this entity. Or null if no type is given, or multiple types are.
   */
  public EdmType getEdmType() {
    final Set<EdmType> types = getProxies().stream().map(ProxyType::getType)
        .filter(Objects::nonNull).map(Type2::getType).filter(Objects::nonNull)
        .collect(Collectors.toSet());
    return (types.size() == 1) ? types.iterator().next() : null;
  }

  /**
   * Determines whether this entity has a landing page. An entity has a landing page if there is at
   * least one web resource of type 'isShownAt', representing technical metadata of some (non-empty)
   * mime type.
   *
   * @return Whether this entity has a landing page.
   */
  public boolean hasLandingPage() {
    return getWebResourceWrappers(EnumSet.of(WebResourceLinkType.IS_SHOWN_AT)).stream()
        .map(WebResourceWrapper::getMimeType).anyMatch(StringUtils::isNotBlank);
  }

  /**
   * This method extracts all web resources from the RDF object. This will filter the objects: it
   * only returns those with a non-blank about value.
   *
   * @return The list of web resources. Is not null, but could be empty.
   */
  public List<WebResourceType> getWebResources() {
    return getFilteredPropertyList(record.getWebResourceList());
  }

  /**
   * This method retrieves all URLs (as {@link String} objects) that the entity contains of the
   * provided link types. This method does not check whether there is a full web resource object for
   * the URLs.
   *
   * @param types The types to which we limit our search. We only return web resources that have at
   * least one of the given types. Cannot be null.
   * @return The URLs. They are not blank or null. The list is not null, but could be empty.
   */
  public Set<String> getUrlsOfTypes(Set<WebResourceLinkType> types) {
    return types.stream().map(this::getUrlsOfType).flatMap(Set::stream).collect(Collectors.toSet());
  }

  /**
   * This method extracts all web resources from the RDF object. This will filter the objects: it
   * only returns those with a non-blank about value and that have at least one of the given types.
   *
   * @param types The types to which we limit our search. We only return web resources that have at
   * least one of the given types. Cannot be null.
   * @return The list of processed web resources. Is not null, but could be empty.
   */
  public List<WebResourceWrapper> getWebResourceWrappers(Set<WebResourceLinkType> types) {
    final Map<String, Set<WebResourceLinkType>> webResourceUrlsWithTypes = getAllLinksForTypes(
        types);
    return getFilteredPropertyStream(record.getWebResourceList())
        .filter(webResource -> webResourceUrlsWithTypes.containsKey(webResource.getAbout())).map(
            webResource -> new WebResourceWrapper(webResource,
                webResourceUrlsWithTypes.get(webResource.getAbout()))).collect(Collectors.toList());
  }

  /**
   * This method extracts all web resources from the RDF object. This will filter the objects: it
   * only returns those with a non-blank about value. But contrary to {@link
   * #getWebResourceWrappers(Set)} it also returns all web resources that have none of the supported
   * types.
   *
   * @return The list of processed web resources. Is not null, but could be empty.
   */
  public List<WebResourceWrapper> getWebResourceWrappers() {
    final Map<String, Set<WebResourceLinkType>> webResourceUrlsWithTypes = getAllLinksForTypes(
        Stream.of(WebResourceLinkType.values()).collect(Collectors.toSet()));
    return getFilteredPropertyStream(record.getWebResourceList()).map(
        webResource -> new WebResourceWrapper(webResource,
            webResourceUrlsWithTypes.get(webResource.getAbout()))).collect(Collectors.toList());
  }

  /**
   * This method extracts all web resources from the RDF object. This will filter the objects: it
   * only returns those with a non-blank about value and that have at least one of the given types.
   *
   * @param types The types to which we limit our search. We only return web resources that have at
   * least one of the given types. Cannot be null.
   * @return The list of processed web resources. Is not null, but could be empty.
   */
  public List<WebResourceType> getWebResources(Set<WebResourceLinkType> types) {
    final Map<String, Set<WebResourceLinkType>> webResourceUrlsWithTypes = getAllLinksForTypes(
        types);
    return getFilteredPropertyStream(record.getWebResourceList())
        .filter(webResource -> webResourceUrlsWithTypes.containsKey(webResource.getAbout()))
        .collect(Collectors.toList());
  }

  /**
   * This method retrieves all URLs (as {@link String} objects) that this entity contains of the
   * given link type.
   *
   * @param type The link type for which to retrieve the urls.
   * @return The URLs. They are not blank or null. The list is not null, but could be empty.
   */
  private Set<String> getUrlsOfType(WebResourceLinkType type) {
    return getAggregations().stream().map(type::getResourcesOfType).filter(Objects::nonNull)
        .flatMap(List::stream).filter(Objects::nonNull).map(ResourceType::getResource)
        .filter(org.apache.commons.lang.StringUtils::isNotBlank).collect(Collectors.toSet());
  }

  /**
   * This method creates a map of all web resource URLs in this entity with the given link types.
   *
   * @param types The types to which we limit our search. We only return web resources that have at
   * least one of the given types.
   * @return The map of URLs to link types. The link types will include all types with which a given
   * URL occurs, not just those those that we searched for.
   */
  private Map<String, Set<WebResourceLinkType>> getAllLinksForTypes(
      Set<WebResourceLinkType> types) {

    // All types with the urls that have that type. This is the complete overview.
    final Map<WebResourceLinkType, Set<String>> urlsByType = Stream.of(WebResourceLinkType.values())
        .collect(Collectors.toMap(Function.identity(), this::getUrlsOfType));

    // The result map with empty type lists. Only contains the urls with one of the required types.
    final Map<String, Set<WebResourceLinkType>> result = types.stream().map(urlsByType::get)
        .flatMap(Set::stream).collect(
            Collectors.toMap(Function.identity(), url -> new HashSet<>(), (url1, url2) -> url1));

    // Add the right types to the urls.
    for (Entry<WebResourceLinkType, Set<String>> typeWithUrls : urlsByType.entrySet()) {
      for (String url : typeWithUrls.getValue()) {
        if (result.containsKey(url)) {
          result.get(url).add(typeWithUrls.getKey());
        }
      }
    }

    // Done.
    return result;
  }

  /**
   * An entity is considered to have thumbnails if if and only if edm:EuropeanaAggregation/edm:preview
   * is filled and the associated edm:webResource exists with technical metadata (i.e.
   * ebucore:hasMimetype is set to a non-empty value)
   *
   * @return Whether the entity has thumbnails.
   */
  public boolean hasThumbnails() {
    final String previewUri = getEuropeanaAggregation().map(EuropeanaAggregationType::getPreview)
        .map(ResourceType::getResource).filter(StringUtils::isNotBlank).orElse(null);
    return previewUri != null && getFilteredPropertyStream(record.getWebResourceList())
        .filter(resource -> previewUri.equals(resource.getAbout()))
        .map(WebResourceWrapper::getMimeType).anyMatch(StringUtils::isNotBlank);
  }

  /**
   * Obtains the list of agents from an RDF record. This will filter the objects: it only returns
   * those with a non-blank about value.
   *
   * @return The agents. Is not null, but could be empty.
   */
  public List<AgentType> getAgents() {
    return getFilteredPropertyList(record.getAgentList());
  }

  /**
   * Obtains the list of concepts from an RDF record. This will filter the objects: it only returns
   * those with a non-blank about value.
   *
   * @return The concepts. Is not null, but could be empty.
   */
  public List<Concept> getConcepts() {
    return getFilteredPropertyList(record.getConceptList());
  }

  /**
   * Obtains the list of licenses from an RDF record. This will filter the objects: it only returns
   * those with a non-blank about value.
   *
   * @return The licenses. Is not null, but could be empty.
   */
  public List<License> getLicenses() {
    return getFilteredPropertyList(record.getLicenseList());
  }

  /**
   * Obtains the list of places from an RDF record. This will filter the objects: it only returns
   * those with a non-blank about value.
   *
   * @return The places. Is not null, but could be empty.
   */
  public List<PlaceType> getPlaces() {
    return getFilteredPropertyList(record.getPlaceList());
  }

  /**
   * Obtains the list of time spans from an RDF record. This will filter the objects: it only
   * returns those with a non-blank about value.
   *
   * @return The time spans. Is not null, but could be empty.
   */
  public List<TimeSpanType> getTimeSpans() {
    return getFilteredPropertyList(record.getTimeSpanList());
  }

  /**
   * Obtains the list of services from an RDF record. This will filter the objects: it only returns
   * those with a non-blank about value.
   *
   * @return The services. Is not null, but could be empty.
   */
  public List<Service> getServices() {
    return getFilteredPropertyList(record.getServiceList());
  }

  /**
   * Obtain the list of quality annotations from an RDF record. This will filter the objects: it
   * only returns those with a non-blank about value.
   *
   * @return The quality annotations. Is not null, but could be empty.
   */
  public List<QualityAnnotation> getQualityAnnotations() {
    return getFilteredPropertyList(record.getQualityAnnotationList());
  }

  private static <T extends AboutType> List<T> getFilteredPropertyList(List<T> propertyList) {
    return getFilteredPropertyStream(propertyList).collect(Collectors.toList());
  }

  private static <T extends AboutType> Stream<T> getFilteredPropertyStream(List<T> propertyList) {
    return getPropertyStream(propertyList).filter(Objects::nonNull)
        .filter(resource -> StringUtils.isNotBlank(resource.getAbout()));
  }

  private static <T extends AboutType> List<T> getPropertyList(List<T> propertyList) {
    return propertyList == null ? Collections.emptyList() : propertyList;
  }

  private static <T extends AboutType> Stream<T> getPropertyStream(List<T> propertyList) {
    return propertyList == null ? Stream.empty() : propertyList.stream();
  }
}
