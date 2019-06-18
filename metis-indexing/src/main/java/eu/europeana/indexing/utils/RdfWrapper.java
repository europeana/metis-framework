package eu.europeana.indexing.utils;

import eu.europeana.corelib.definitions.jibx.AboutType;
import eu.europeana.corelib.definitions.jibx.AgentType;
import eu.europeana.corelib.definitions.jibx.Aggregation;
import eu.europeana.corelib.definitions.jibx.CollectionName;
import eu.europeana.corelib.definitions.jibx.Concept;
import eu.europeana.corelib.definitions.jibx.DatasetName;
import eu.europeana.corelib.definitions.jibx.EdmType;
import eu.europeana.corelib.definitions.jibx.EuropeanaAggregationType;
import eu.europeana.corelib.definitions.jibx.EuropeanaProxy;
import eu.europeana.corelib.definitions.jibx.License;
import eu.europeana.corelib.definitions.jibx.PlaceType;
import eu.europeana.corelib.definitions.jibx.ProvidedCHOType;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.QualityAnnotation;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.ResourceType;
import eu.europeana.corelib.definitions.jibx.Service;
import eu.europeana.corelib.definitions.jibx.TimeSpanType;
import eu.europeana.corelib.definitions.jibx.Type2;
import eu.europeana.corelib.definitions.jibx.WebResourceType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

/**
 * This class is a wrapper around instances of type {@link RDF}. Its responsibility is to hide the
 * RDF structure and objects needed when extracting information from the records.
 * 
 * @author jochen
 *
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
    final Optional<String> collectionName =
        aggregation.map(EuropeanaAggregationType::getCollectionName).map(CollectionName::getString)
            .filter(StringUtils::isNotBlank);
    return datasetName.orElse(collectionName.orElse(""));
  }

  /**
   * This method extracts the Europeana aggregation from the RDF object.
   * 
   * @return The Europeana aggregation in a non-null {@link Optional}.
   */
  public Optional<EuropeanaAggregationType> getEuropeanaAggregation() {
    return getPropertyStream(record.getEuropeanaAggregationList()).findFirst();
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
        .map(EuropeanaProxy::isEuropeanaProxy).orElse(false);
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
    return (types.size() != 1) ? null : types.iterator().next();
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
    return types.stream().map(type -> type.getUrlsOfType(this)).flatMap(Set::stream).collect(
        Collectors.toSet());
  }

  /**
   * This method extracts all web resources from the RDF object. This will filter the objects: it
   * only returns those that need to be indexed and that have at least one of the given types.
   *
   * @param types The types to which we limit our search. We only return web resources that have at
   * least one of the given types. Cannot be null.
   * @return The list of processed web resources. Is not null, but could be empty.
   */
  public List<WebResourceWrapper> getWebResourceWrappers(Set<WebResourceLinkType> types) {
    final Map<String, Set<WebResourceLinkType>> webResourceUrlsWithTypes = WebResourceLinkType
        .getAllLinksForTypes(this, types);
    return getFilteredPropertyStream(record.getWebResourceList())
        .filter(webResource -> webResourceUrlsWithTypes.containsKey(webResource.getAbout()))
        .map(webResource -> new WebResourceWrapper(webResource,
            webResourceUrlsWithTypes.get(webResource.getAbout()))).collect(Collectors.toList());
  }

  /**
   * This method extracts all web resources from the RDF object. This will filter the objects: it
   * only returns those that need to be indexed. But contrary to {@link
   * #getWebResourceWrappers(Set)} it also returns all web resources that have none of the supported
   * types.
   *
   * @return The list of processed web resources. Is not null, but could be empty.
   */
  public List<WebResourceWrapper> getWebResourceWrappers() {
    final Map<String, Set<WebResourceLinkType>> webResourceUrlsWithTypes = WebResourceLinkType
        .getAllLinksForTypes(this,
            Stream.of(WebResourceLinkType.values()).collect(Collectors.toSet()));
    return getFilteredPropertyStream(record.getWebResourceList()).map(
        webResource -> new WebResourceWrapper(webResource,
            webResourceUrlsWithTypes.get(webResource.getAbout()))).collect(Collectors.toList());
  }

  /**
   * This method extracts all web resources from the RDF object. This will filter the objects: it
   * only returns those that need to be indexed.
   * 
   * @return The list of web resources. Is not null, but could be empty.
   */
  public List<WebResourceType> getWebResources() {
    return getFilteredPropertyList(record.getWebResourceList());
  }

  /**
   * This method extracts all web resources from the RDF object. This will filter the objects: it
   * only returns those that need to be indexed and that have at least one of the given types.
   *
   * @param types The types to which we limit our search. We only return web resources that have at
   * least one of the given types. Cannot be null.
   * @return The list of processed web resources. Is not null, but could be empty.
   */
  public List<WebResourceType> getWebResources(Set<WebResourceLinkType> types) {
    final Map<String, Set<WebResourceLinkType>> webResourceUrlsWithTypes = WebResourceLinkType
        .getAllLinksForTypes(this, types);
    return getFilteredPropertyStream(record.getWebResourceList())
        .filter(webResource -> webResourceUrlsWithTypes.containsKey(webResource.getAbout()))
        .collect(Collectors.toList());
  }

  /**
   * An entity is considered to have thumbnails if if and only if edm:EuropeanaAggregation/edm:preview
   * is filled and the associated edm:webResource exists with technical metadata (i.e.
   * ebucore:hasMimetype is set)
   *
   * @return Whether the entity has thumbnails.
   */
  public boolean hasThumbnails() {
    final String previewUri = getEuropeanaAggregation().map(EuropeanaAggregationType::getPreview)
        .map(ResourceType::getResource).filter(StringUtils::isNotBlank).orElse(null);
    return previewUri != null && getFilteredPropertyStream(record.getWebResourceList())
        .filter(resource -> previewUri.equals(resource.getAbout()))
        .map(WebResourceWrapper::getMimeType).anyMatch(Objects::nonNull);
  }


  /**
   * Obtains the list of agents from an RDF record. This will filter the objects: it only returns
   * those that need to be indexed.
   *
   * @return The agents that need to be indexed.
   */
  public List<AgentType> getAgents() {
    return getFilteredPropertyList(record.getAgentList());
  }

  /**
   * Obtains the list of concepts from an RDF record. This will filter the objects: it only returns
   * those that need to be indexed.
   *
   * @return The concepts that need to be indexed.
   */
  public List<Concept> getConcepts() {
    return getFilteredPropertyList(record.getConceptList());
  }

  /**
   * Obtains the list of licenses from an RDF record. This will filter the objects: it only returns
   * those that need to be indexed.
   *
   * @return The licenses that need to be indexed.
   */
  public List<License> getLicenses() {
    return getFilteredPropertyList(record.getLicenseList());
  }

  /**
   * Obtains the list of places from an RDF record. This will filter the objects: it only returns
   * those that need to be indexed.
   *
   * @return The places that need to be indexed.
   */
  public List<PlaceType> getPlaces() {
    return getFilteredPropertyList(record.getPlaceList());
  }

  /**
   * Obtains the list of time spans from an RDF record. This will filter the objects: it only
   * returns those that need to be indexed.
   *
   * @return The time spans that need to be indexed.
   */
  public List<TimeSpanType> getTimeSpans() {
    return getFilteredPropertyList(record.getTimeSpanList());
  }

  /**
   * Obtains the list of services from an RDF record. This will filter the objects: it only returns
   * those that need to be indexed.
   *
   * @return The services that need to be indexed.
   */
  public List<Service> getServices() {
    return getFilteredPropertyList(record.getServiceList());
  }

  public List<QualityAnnotation> getQualityAnnotations() {
    return getFilteredPropertyList(record.getQualityAnnotationList());
  }

  private static <T extends AboutType> List<T> getFilteredPropertyList(List<T> propertyList) {
    return getFilteredPropertyStream(propertyList).collect(Collectors.toList());
  }

  private static <T extends AboutType> Stream<T> getFilteredPropertyStream(List<T> propertyList) {
    return getPropertyStream(propertyList)
        .filter(resource -> StringUtils.isNotBlank(resource.getAbout()));
  }

  private static <T extends AboutType> List<T> getPropertyList(List<T> propertyList) {
    return propertyList == null ? Collections.emptyList() : propertyList;
  }

  private static <T extends AboutType> Stream<T> getPropertyStream(List<T> propertyList) {
    return propertyList == null ? Stream.empty() : propertyList.stream();
  }
}
