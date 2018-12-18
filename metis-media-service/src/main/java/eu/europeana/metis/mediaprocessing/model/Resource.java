package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.metis.mediaprocessing.temp.DownloadedResource;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class represents the binary content of a resource. Please see {@link ResourceFile} for more
 * information.
 */
public class Resource extends TemporaryFile implements DownloadedResource {

  // TODO should be final.
  private String mimeType;
  private final Set<UrlType> urlTypes;

  /**
   * Constructor.
   *
   * @param rdfResourceEntry The resource entry for which this file contains the content.
   * @param mimeType The mime type of this content.
   * @throws IOException In case the temporary file could not be created.
   */
  public Resource(RdfResourceEntry rdfResourceEntry, String mimeType) throws IOException {
    super(rdfResourceEntry.getResourceUrl(), "media_resource_", null);
    this.mimeType = mimeType;
    this.urlTypes = new HashSet<>(rdfResourceEntry.getUrlTypes());
  }

  public Set<UrlType> getUrlTypes() {
    return Collections.unmodifiableSet(urlTypes);
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }
}
