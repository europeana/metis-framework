package eu.europeana.indexing.utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import eu.europeana.corelib.definitions.jibx.AboutType;
import eu.europeana.corelib.definitions.jibx.AgentType;
import eu.europeana.corelib.definitions.jibx.Concept;
import eu.europeana.corelib.definitions.jibx.License;
import eu.europeana.corelib.definitions.jibx.PlaceType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.Service;
import eu.europeana.corelib.definitions.jibx.TimeSpanType;
import eu.europeana.corelib.definitions.jibx.WebResourceType;

/**
 * This class contains utililty methods regarding RDF records.
 */
public final class RdfUtils {

  private RdfUtils() {}

  /**
   * Obtains the list of web resources from an RDF record. This will filter objects: it only
   * returns those that need to be indexed.
   *
   * @param record The record from which to obtain the web resources.
   * @return The web resources that need to be indexed.
   */
  public static Stream<WebResourceType> getWebResourcesWithNonemptyAbout(RDF record) {
    return getFilteredPropertyStream(record.getWebResourceList());
  }
  
  /**
   * Obtains the list of agents from an RDF record. This will filter the objects: it only
   * returns those that need to be indexed.
   *
   * @param record The record from which to obtain the agents.
   * @return The agents that need to be indexed.
   */
  public static List<AgentType> getAgentsWithNonemptyAbout(RDF record) {
    return getFilteredPropertyList(record.getAgentList());
  }

  /**
   * Obtains the list of concepts from an RDF record. This will filter the objects: it only
   * returns those that need to be indexed.
   *
   * @param record The record from which to obtain the concepts.
   * @return The concepts that need to be indexed.
   */
  public static List<Concept> getConceptsWithNonemptyAbout(RDF record) {
    return getFilteredPropertyList(record.getConceptList());
  }

  /**
   * Obtains the list of licenses from an RDF record. This will filter the objects: it only
   * returns those that need to be indexed.
   *
   * @param record The record from which to obtain the licenses.
   * @return The licenses that need to be indexed.
   */
  public static List<License> getLicensesWithNonemptyAbout(RDF record) {
    return getFilteredPropertyList(record.getLicenseList());
  }

  /**
   * Obtains the list of places from an RDF record. This will filter the objects: it only
   * returns those that need to be indexed.
   *
   * @param record The record from which to obtain the places.
   * @return The places that need to be indexed.
   */
  public static List<PlaceType> getPlacesWithNonemptyAbout(RDF record) {
    return getFilteredPropertyList(record.getPlaceList());
  }

  /**
   * Obtains the list of time spans from an RDF record. This will filter the objects: it only
   * returns those that need to be indexed.
   *
   * @param record The record from which to obtain the time spans.
   * @return The time spans that need to be indexed.
   */
  public static List<TimeSpanType> getTimeSpansWithNonemptyAbout(RDF record) {
    return getFilteredPropertyList(record.getTimeSpanList());
  }

  /**
   * Obtains the list of services from an RDF record. This will filter the objects: it only
   * returns those that need to be indexed.
   *
   * @param record The record from which to obtain the services.
   * @return The services that need to be indexed.
   */
  public static List<Service> getServicesWithNonemptyAbout(RDF record) {
    return getFilteredPropertyList(record.getServiceList());
  }

  private static <T extends AboutType> List<T> getFilteredPropertyList(List<T> propertyList) {
    return getFilteredPropertyStream(propertyList).collect(Collectors.toList());
  }

  private static <T extends AboutType> Stream<T> getFilteredPropertyStream(List<T> propertyList) {
    if (propertyList == null) {
      return Stream.empty();
    }
    return propertyList.stream().filter(resource -> StringUtils.isNotBlank(resource.getAbout()));
  }
}
