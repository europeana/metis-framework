package eu.europeana.metis.mediaprocessing.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This object represents a resource entry in an RDF file. It contains the resource URL,
 * and a list of reference types: the way this resource is referenced from within the RDF.
 */
public class RdfResourceEntry implements Serializable {

  @Serial
  private static final long serialVersionUID = -5873067668837140080L;

  private String resourceUrl;
  private Set<UrlType> urlTypes;
  private RdfResourceKind resourceKind;
  private String serviceReference;

  /**
   * Instantiates a new Rdf resource entry.
   *
   * @param resourceUrl      The URL of the resource.
   * @param urlTypes         The resource URL types with which this resource is referenced.
   * @param rdfResourceKind  The rdf resource kind.
   * @param serviceReference The resource's reference to a svcs:Service object, if any such
   *                         reference was present in the record. Is null otherwise.
   */
  public RdfResourceEntry(String resourceUrl, Collection<UrlType> urlTypes,
      RdfResourceKind rdfResourceKind, String serviceReference) {
    this.resourceUrl = resourceUrl;
    this.urlTypes = new HashSet<>(urlTypes);
    this.resourceKind = rdfResourceKind;
    this.serviceReference = serviceReference;
  }

  /**
   * Constructor. Don't use this: it's required for deserialization.
   */
  RdfResourceEntry() {
  }

  /**
   * Gets resource url.
   *
   * @return the resource url
   */
  public String getResourceUrl() {
    return resourceUrl;
  }

  /**
   * Gets url types.
   *
   * @return the url types
   */
  public Set<UrlType> getUrlTypes() {
    return Collections.unmodifiableSet(urlTypes);
  }

  /**
   * Gets resource kind.
   *
   * @return the resource kind
   */
  public RdfResourceKind getResourceKind() {
    return resourceKind;
  }

  /**
   * Gets service reference.
   *
   * @return the service reference
   */
  public String getServiceReference() {
    return serviceReference;
  }

  @Override
  public String toString() {
    return String.format("%s{resourceUrl=%s, urlTypes=%s, resourceKind=%s, serviceReference=%s}",
        RdfResourceEntry.class.getSimpleName(), resourceUrl, urlTypes, resourceKind, serviceReference);
  }
}
