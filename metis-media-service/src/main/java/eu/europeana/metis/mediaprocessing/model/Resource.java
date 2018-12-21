package eu.europeana.metis.mediaprocessing.model;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class represents the binary content of a resource. Please see {@link ResourceFile} for more
 * information.
 */
public class Resource extends TemporaryFile {

  private final String mimeType;
  private final Set<UrlType> urlTypes;
  private final URI actualLocation;

  /**
   * Constructor.
   *
   * @param rdfResourceEntry The resource entry for which this file contains the content.
   * @param mimeType The mime type of this content.
   * @param actualLocation The actual location where the resource was obtained (as opposed from the
   * resource URL given by {@link Resource#getResourceUrl()}).
   * @throws IOException In case the temporary file could not be created.
   */
  public Resource(RdfResourceEntry rdfResourceEntry, String mimeType, URI actualLocation)
      throws IOException {
    super(rdfResourceEntry.getResourceUrl(), "media_resource_", null);
    this.mimeType = mimeType;
    this.urlTypes = new HashSet<>(rdfResourceEntry.getUrlTypes());
    this.actualLocation=actualLocation;
  }

  public Set<UrlType> getUrlTypes() {
    return Collections.unmodifiableSet(urlTypes);
  }

  public String getMimeType() {
    return mimeType;
  }

  public URI getActualLocation() {
    return actualLocation;
  }
}
