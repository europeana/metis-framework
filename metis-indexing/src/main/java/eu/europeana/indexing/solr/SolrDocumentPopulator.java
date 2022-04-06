package eu.europeana.indexing.solr;

import static eu.europeana.indexing.solr.EdmLabel.COVERAGE_LOCATION_WGS;
import static eu.europeana.indexing.solr.EdmLabel.CURRENT_LOCATION_WGS;
import static eu.europeana.indexing.solr.EdmLabel.LOCATION_WGS;
import static java.lang.String.format;
import static java.util.function.Predicate.not;

import eu.europeana.corelib.definitions.edm.entity.QualityAnnotation;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.LicenseImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.indexing.solr.facet.FacetEncoder;
import eu.europeana.indexing.solr.property.AgentSolrCreator;
import eu.europeana.indexing.solr.property.AggregationSolrCreator;
import eu.europeana.indexing.solr.property.ConceptSolrCreator;
import eu.europeana.indexing.solr.property.EuropeanaAggregationSolrCreator;
import eu.europeana.indexing.solr.property.LicenseSolrCreator;
import eu.europeana.indexing.solr.property.PlaceSolrCreator;
import eu.europeana.indexing.solr.property.ProvidedChoSolrCreator;
import eu.europeana.indexing.solr.property.ProxySolrCreator;
import eu.europeana.indexing.solr.property.ServiceSolrCreator;
import eu.europeana.indexing.solr.property.SolrPropertyUtils;
import eu.europeana.indexing.solr.property.TimespanSolrCreator;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.WebResourceLinkType;
import eu.europeana.indexing.utils.WebResourceWrapper;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.schema.model.MediaType;
import eu.europeana.metis.utils.GeoUriParser;
import eu.europeana.metis.utils.GeoUriParser.GeoCoordinates;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides functionality to populate Solr documents. Both methods in this class should be called to fill the Solr
 * document. The method {@link #populateWithProperties(SolrInputDocument, FullBeanImpl)} copies properties from the source to the
 * Solr document. The method {@link #populateWithFacets(SolrInputDocument, RdfWrapper)} on the other hand performs some analysis
 * and sets technical metadata.
 *
 * @author jochen
 */
public class SolrDocumentPopulator {

  private static final Logger LOGGER = LoggerFactory.getLogger(SolrDocumentPopulator.class);

  /**
   * Populates a Solr document with the properties of the full bean. Please note: this method should only be called once on a
   * given document, otherwise the behavior is not defined.
   *
   * @param document The Solr document to populate.
   * @param fullBean The FullBean to populate from.
   */
  public void populateWithProperties(SolrInputDocument document, FullBeanImpl fullBean) {

    // Get the type: filter duplicates
    final String[] types = Optional.ofNullable(fullBean.getProxies()).stream().flatMap(List::stream)
                                   .filter(Objects::nonNull).map(ProxyImpl::getEdmType).filter(Objects::nonNull).distinct()
                                   .toArray(String[]::new);
    SolrPropertyUtils.addValues(document, EdmLabel.PROVIDER_EDM_TYPE, types);

    // Gather the licenses.
    final List<LicenseImpl> licenses = Optional.ofNullable(fullBean.getLicenses()).stream()
                                               .flatMap(List::stream).filter(Objects::nonNull).collect(Collectors.toList());

    // Gather the quality annotations.
    final Set<String> acceptableTargets = Optional.ofNullable(fullBean.getAggregations()).stream()
                                                  .flatMap(Collection::stream).filter(Objects::nonNull)
                                                  .map(AggregationImpl::getAbout)
                                                  .filter(Objects::nonNull).collect(Collectors.toSet());
    final Predicate<QualityAnnotation> hasAcceptableTarget = annotation -> Optional
        .ofNullable(annotation.getTarget()).stream().flatMap(Arrays::stream)
        .anyMatch(acceptableTargets::contains);
    final Map<String, QualityAnnotation> qualityAnnotations = Optional
        .ofNullable(fullBean.getQualityAnnotations()).map(List::stream).orElseGet(Stream::empty)
        .filter(Objects::nonNull)
        .filter(annotation -> StringUtils.isNotBlank(annotation.getAbout()))
        .filter(hasAcceptableTarget).collect(
            Collectors.toMap(QualityAnnotation::getAbout, Function.identity(), (v1, v2) -> v1));

    // Add the containing objects.
    new ProvidedChoSolrCreator().addToDocument(document, fullBean.getProvidedCHOs().get(0));
    new AggregationSolrCreator(licenses, fullBean.getOrganizations())
        .addToDocument(document, getDataProviderAggregations(fullBean).get(0));
    new EuropeanaAggregationSolrCreator(licenses, qualityAnnotations::get)
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

    // Add geo spatial fields
    setGeospatialFields(document, fullBean);

    // Add the top-level properties.
    document
        .addField(EdmLabel.EUROPEANA_COMPLETENESS.toString(), fullBean.getEuropeanaCompleteness());
    document.addField(EdmLabel.EUROPEANA_COLLECTIONNAME.toString(),
        fullBean.getEuropeanaCollectionName()[0]);
    document.addField(EdmLabel.TIMESTAMP_CREATED.toString(), fullBean.getTimestampCreated());
    document.addField(EdmLabel.TIMESTAMP_UPDATED.toString(), fullBean.getTimestampUpdated());
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

  private List<AggregationImpl> getDataProviderAggregations(FullBeanImpl fullBean) {
    List<String> proxyInResult = fullBean.getProxies().stream()
                                         .filter(not(ProxyImpl::isEuropeanaProxy))
                                         .filter(proxy -> ArrayUtils.isEmpty(proxy.getLineage())).map(ProxyImpl::getProxyIn)
                                         .map(Arrays::asList).flatMap(List::stream).collect(Collectors.toList());

    return fullBean.getAggregations().stream().filter(x -> proxyInResult.contains(x.getAbout()))
                   .collect(Collectors.toList());
  }

  private void setGeospatialFields(SolrInputDocument document, FullBeanImpl fullBean) {
    final List<ProxyImpl> proxies = fullBean.getProxies();
    final Map<String, PlaceImpl> placesAboutMap = fullBean.getPlaces().stream()
                                                          .collect(Collectors.toMap(PlaceImpl::getAbout, Function.identity(),
                                                              (place1, place2) -> place1));
    final Set<String> currentLocationStrings = new HashSet<>();
    final Set<String> coverageLocationStrings = new HashSet<>();
    for (ProxyImpl proxy : proxies) {
      currentLocationStrings.addAll(getCurrentLocationStrings(proxy));
      coverageLocationStrings.addAll(getCoverageLocationStrings(proxy));
    }
    final Set<LocationPoint> currentLocationPoints = new HashSet<>(
        getReferencedPlacesLocationPoints(placesAboutMap, currentLocationStrings));
    currentLocationPoints.addAll(getWGS84LocationPoints(currentLocationStrings));

    final Set<LocationPoint> coverageLocationPoints = new HashSet<>(
        getReferencedPlacesLocationPoints(placesAboutMap, coverageLocationStrings));
    coverageLocationPoints.addAll(getWGS84LocationPoints(coverageLocationStrings));

    SolrPropertyUtils.addValues(document, CURRENT_LOCATION_WGS,
        currentLocationPoints.stream().map(Object::toString).toArray(String[]::new));

    SolrPropertyUtils.addValues(document, COVERAGE_LOCATION_WGS,
        coverageLocationPoints.stream().map(Object::toString).toArray(String[]::new));

    Set<LocationPoint> locationPointsCombined = new HashSet<>();
    locationPointsCombined.addAll(currentLocationPoints);
    locationPointsCombined.addAll(coverageLocationPoints);
    SolrPropertyUtils.addValues(document, LOCATION_WGS,
        locationPointsCombined.stream().map(Object::toString).toArray(String[]::new));
  }

  private Set<LocationPoint> getReferencedPlacesLocationPoints(Map<String, PlaceImpl> placesAboutMap,
      Set<String> locationStrings) {
    return locationStrings.stream().map(placesAboutMap::get).filter(Objects::nonNull)
                          .map(this::getPlaceLocationPoint).filter(Objects::nonNull).collect(Collectors.toSet());
  }

  private Set<LocationPoint> getWGS84LocationPoints(Set<String> locationStrings) {
    return locationStrings.stream().map(this::getValidGeoCoordinates).filter(Objects::nonNull)
                          .map(LocationPoint::new).collect(Collectors.toSet());
  }

  private Set<String> getCurrentLocationStrings(ProxyImpl proxy) {
    final Set<String> currentLocations = new HashSet<>();
    Optional.ofNullable(proxy.getEdmCurrentLocation()).map(Map::values).stream().flatMap(Collection::stream)
            .flatMap(Collection::stream)
            .filter(StringUtils::isNotBlank)
            .forEach(currentLocations::add);
    return currentLocations;
  }

  private Set<String> getCoverageLocationStrings(ProxyImpl proxy) {
    final Set<String> coverageLocations = new HashSet<>();
    Optional.ofNullable(proxy.getDctermsSpatial()).map(Map::values).stream().flatMap(Collection::stream)
            .flatMap(Collection::stream)
            .filter(StringUtils::isNotBlank)
            .forEach(coverageLocations::add);
    Optional.ofNullable(proxy.getDcCoverage()).map(Map::values).stream().flatMap(Collection::stream)
            .flatMap(Collection::stream)
            .filter(StringUtils::isNotBlank)
            .forEach(coverageLocations::add);
    return coverageLocations;
  }

  private LocationPoint getPlaceLocationPoint(PlaceImpl place) {
    if (place.getLatitude() != null && place.getLongitude() != null) {
      return new LocationPoint(place.getLatitude().doubleValue(), place.getLongitude().doubleValue());
    }
    return null;
  }

  private GeoCoordinates getValidGeoCoordinates(String s) {
    try {
      return GeoUriParser.parse(s);
    } catch (BadContentException e) {
      LOGGER.debug(format("Geo parsing failed %s", s), e);
    }
    return null;
  }


  private static class LocationPoint {

    private final Double latitude;
    private final Double longitude;

    public LocationPoint(Double latitude, Double longitude) {
      this.latitude = latitude;
      this.longitude = longitude;
    }

    public LocationPoint(GeoCoordinates geoCoordinates) {
      this.latitude = geoCoordinates.getLatitude();
      this.longitude = geoCoordinates.getLongitude();
    }

    @Override
    public String toString() {
      return format(Locale.US, "%f,%f", latitude, longitude);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      LocationPoint that = (LocationPoint) o;
      return this.toString().equals(that.toString());
    }

    @Override
    public int hashCode() {
      return Objects.hash(latitude, longitude);
    }
  }
}
