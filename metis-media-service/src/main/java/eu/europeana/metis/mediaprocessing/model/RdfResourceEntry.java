package eu.europeana.metis.mediaprocessing.model;

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

  /**
   * Implements {@link java.io.Serializable}
   **/
  private static final long serialVersionUID = -5873067668837140080L;

  private String resourceUrl;
  private Set<UrlType> urlTypes;
  private RdfResourceKind resourceKind;

  /**
   * Instantiates a new Rdf resource entry.
   *
   * @param resourceUrl The URL of the resource.
   * @param urlTypes The resource URL types with which this resource is referenced.
   * @param rdfResourceKind the rdf resource kind If the resource is configured in the record
   * as if it were a regular resource, an oEmbed resource or IIIF resource.
   */
  public RdfResourceEntry(String resourceUrl,
      Collection<UrlType> urlTypes,
      RdfResourceKind rdfResourceKind) {
    this.resourceUrl = resourceUrl;
    this.urlTypes = new HashSet<>(urlTypes);
    this.resourceKind = rdfResourceKind;
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

  @Override
  public String toString() {
    return String.format("%s{resourceUrl=%s, urlTypes=%s, resourceKind=%s}",
        RdfResourceEntry.class.getSimpleName(), resourceUrl, urlTypes, resourceKind);
  }
}
