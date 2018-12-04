package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.metis.mediaservice.EdmObject;
import eu.europeana.metis.mediaservice.WebResource;

public abstract class ResourceMetadata {

  private final String mimeType;

  private final String resourceUrl;

  private final long contentSize;

  protected ResourceMetadata(String mimeType, String resourceUrl, long contentSize) {
    this.mimeType = mimeType;
    this.resourceUrl = resourceUrl;
    this.contentSize = contentSize;
  }

  public String getResourceUrl() {
    return resourceUrl;
  }

  public String getMimeType() {
    return mimeType;
  }

  public final void updateRdf(RDF rdf) {
    updateRdf(new EdmObject(rdf));
  }

  public final void updateRdf(EdmObject rdf) {
    final WebResource resource = rdf.getWebResource(resourceUrl);
    resource.setMimeType(mimeType);
    resource.setFileSize(contentSize);
    updateResource(resource);
  }

  protected abstract void updateResource(WebResource resource);
}
