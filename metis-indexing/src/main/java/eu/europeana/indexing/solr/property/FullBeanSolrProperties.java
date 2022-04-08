package eu.europeana.indexing.solr.property;

import static eu.europeana.indexing.solr.EdmLabel.COVERAGE_LOCATION_WGS;
import static eu.europeana.indexing.solr.EdmLabel.CURRENT_LOCATION_WGS;
import static eu.europeana.indexing.solr.EdmLabel.LOCATION_WGS;
import static java.lang.String.format;

import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.indexing.solr.EdmLabel;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.utils.GeoUriWGS84Parser;
import eu.europeana.metis.utils.GeoUriWGS84Parser.GeoCoordinates;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that creates Solr properties related to the FullBean and properties that need to be retrieved and computed from multiple
 * sub-elements.
 */
public class FullBeanSolrProperties {

  private static final Logger LOGGER = LoggerFactory.getLogger(FullBeanSolrProperties.class);

  /**
   * Computes and creates all properties relevant to fullbean as a whole.
   *
   * @param document the solr document
   * @param fullBean the fullbean to analyze
   */
  public void setProperties(SolrInputDocument document, FullBeanImpl fullBean) {
    // Get the type: filter duplicates
    final String[] types = Optional.ofNullable(fullBean.getProxies()).stream().flatMap(List::stream)
                                   .filter(Objects::nonNull).map(ProxyImpl::getEdmType).filter(Objects::nonNull).distinct()
                                   .toArray(String[]::new);
    SolrPropertyUtils.addValues(document, EdmLabel.PROVIDER_EDM_TYPE, types);

    setGeospatialFields(document, fullBean);

    document.addField(EdmLabel.EUROPEANA_COMPLETENESS.toString(), fullBean.getEuropeanaCompleteness());
    document.addField(EdmLabel.EUROPEANA_COLLECTIONNAME.toString(), fullBean.getEuropeanaCollectionName()[0]);
    document.addField(EdmLabel.TIMESTAMP_CREATED.toString(), fullBean.getTimestampCreated());
    document.addField(EdmLabel.TIMESTAMP_UPDATED.toString(), fullBean.getTimestampUpdated());
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
      return GeoUriWGS84Parser.parse(s);
    } catch (BadContentException e) {
      LOGGER.debug(format("Geo parsing failed %s", s), e);
    }
    return null;
  }


  private static class LocationPoint {

    //We allow 7 decimal points
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.#######", new DecimalFormatSymbols(Locale.US));
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
      return format(Locale.US, "%s,%s", decimalFormat.format(latitude), decimalFormat.format(longitude));
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
