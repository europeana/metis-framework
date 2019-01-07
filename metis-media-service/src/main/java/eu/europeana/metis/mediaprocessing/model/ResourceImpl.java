package eu.europeana.metis.mediaprocessing.model;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class implements {@link Resource}.
 */
public class ResourceImpl extends TemporaryFile implements Resource {

  private final String mimeType;
  private final Set<UrlType> urlTypes;
  private final URI actualLocation;

  /**
   * Constructor.
   *
   * @param rdfResourceEntry The resource entry for which this file contains the content.
   * @param mimeType The mime type of this content.
   * @param actualLocation The actual location where the resource was obtained (as opposed from the
   *        resource URL given by {@link ResourceImpl#getResourceUrl()}).
   * @throws IOException In case the temporary file could not be created.
   */
  public ResourceImpl(RdfResourceEntry rdfResourceEntry, String mimeType, URI actualLocation)
      throws IOException {
    super(rdfResourceEntry.getResourceUrl(), "media_resource_", null);
    this.mimeType = mimeType;
    this.urlTypes = new HashSet<>(rdfResourceEntry.getUrlTypes());
    this.actualLocation = actualLocation;
  }

  @Override
  public Set<UrlType> getUrlTypes() {
    return Collections.unmodifiableSet(urlTypes);
  }

  @Override
  public String getMimeType() {
    return mimeType;
  }

  @Override
  public URI getActualLocation() {
    return actualLocation;
  }
  
  @Override
  public boolean hasContent() {
    return super.hasContent();
  }
}
