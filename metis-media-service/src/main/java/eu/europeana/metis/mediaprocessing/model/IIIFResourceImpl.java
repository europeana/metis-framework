package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.metis.mediaprocessing.extraction.iiif.IIIFInfoJson;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.Set;

/**
 * The type Resource with iiif info.json. Wraps a regular resource and adds functionality.
 */
public class IIIFResourceImpl implements IIIFResource {

  private final Resource resource;
  private final IIIFInfoJson iiifInfoJson;

  /**
   * Constructor.
   *
   * @param resource the resource to wrap.
   * @param iiifInfoJson the iiif info json of the iiif resource
   */
  public IIIFResourceImpl(Resource resource, IIIFInfoJson iiifInfoJson) {
    this.resource = resource;
    this.iiifInfoJson = iiifInfoJson;
  }

  @Override
  public Set<UrlType> getUrlTypes() {
    return resource.getUrlTypes();
  }

  @Override
  public String getProvidedMimeType() {
    return resource.getProvidedMimeType();
  }

  @Override
  public Long getProvidedFileSize() {
    return resource.getProvidedFileSize();
  }

  @Override
  public URI getActualLocation() {
    return resource.getActualLocation();
  }

  @Override
  public Path getContentPath() {
    return resource.getContentPath();
  }

  @Override
  public File getContentFile() {
    return resource.getContentFile();
  }

  @Override
  public String getResourceUrl() {
    return resource.getResourceUrl();
  }

  @Override
  public boolean hasContent() throws IOException {
    return resource.hasContent();
  }

  @Override
  public InputStream getContentStream() throws IOException {
    return resource.getContentStream();
  }

  @Override
  public Long getContentSize() throws IOException {
    return resource.getContentSize();
  }

  @Override
  public void markAsNoContent() throws IOException {
    resource.markAsNoContent();
  }

  @Override
  public void markAsWithContent(InputStream content) throws IOException {
    resource.markAsWithContent(content);
  }

  @Override
  public void close() throws IOException {
    resource.close();
  }

  public IIIFInfoJson getIIIFInfoJson() {
    return iiifInfoJson;
  }
}
