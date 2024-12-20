package eu.europeana.metis.mediaprocessing.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This object represents a resource entry in an RDF file. It contains the resource URL, and a list of reference types: the way
 * this resource is referenced from within the RDF.
 */
public class RdfResourceEntry implements Serializable {

  /**
   * Implements {@link java.io.Serializable}
   **/
  private static final long serialVersionUID = -5873067668837140080L;

  private String resourceUrl;
  private Set<UrlType> urlTypes;

  private boolean resourceConfiguredForOembed;

  /**
   * Constructor.
   *
   * @param resourceUrl The URL of the resource.
   * @param urlTypes The resource URL types with which this resource is referenced.
   * @param resourceConfiguredForOembed If the resource is configured in the record as if it were
   *                                    an oEmbed resource.
   */
  public RdfResourceEntry(String resourceUrl, Collection<UrlType> urlTypes,
      boolean resourceConfiguredForOembed) {
    this.resourceUrl = resourceUrl;
    this.urlTypes = new HashSet<>(urlTypes);
    this.resourceConfiguredForOembed = resourceConfiguredForOembed;
  }

  /**
   * Constructor. Don't use this: it's required for deserialization.
   */
  RdfResourceEntry() {
  }

  public String getResourceUrl() {
    return resourceUrl;
  }

  public Set<UrlType> getUrlTypes() {
    return Collections.unmodifiableSet(urlTypes);
  }

  public boolean isResourceConfiguredForOembed() {
    return resourceConfiguredForOembed;
  }

  @Override
  public String toString() {
    return String.format("%s{resourceUrl=%s, urlTypes=%s, oembed=%s}",
        RdfResourceEntry.class.getSimpleName(), resourceUrl, urlTypes, resourceConfiguredForOembed);
  }
}
