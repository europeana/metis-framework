package eu.europeana.metis.mediaprocessing.model;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * This class implements {@link Resource}.
 */
public class ResourceImpl extends AbstractTemporaryFile implements Resource {

  private static final String DEFAULT_MIME_TYPE = "application/octet-stream";
  private static final Long DEFAULT_FILE_SIZE = Long.valueOf(0L);
  private final String providedMimeType;
  private final long providedFileSize;
  private final Set<UrlType> urlTypes;
  private final URI actualLocation;

  /**
   * Constructor.
   *
   * @param rdfResourceEntry The resource entry for which this file contains the content.
   * @param providedMimeType The mime type of this content, as provided by the source. Can be null
   * if the source didn't specify a mime type.
   * @param providedFileSize The file size of this content, as provided by the source. Can be null
   * if the source didn't specify a file size.
   * @param actualLocation The actual location where the resource was obtained (as opposed from the
   * resource URL given by {@link ResourceImpl#getResourceUrl()}).
   */
  public ResourceImpl(RdfResourceEntry rdfResourceEntry, String providedMimeType,
      Long providedFileSize, URI actualLocation) {
    super(rdfResourceEntry.getResourceUrl(), "media_resource_", null);
    this.providedMimeType = Optional.ofNullable(providedMimeType).orElse(DEFAULT_MIME_TYPE);
    this.providedFileSize = Optional.ofNullable(providedFileSize).orElse(DEFAULT_FILE_SIZE);
    this.urlTypes = new HashSet<>(rdfResourceEntry.getUrlTypes());
    this.actualLocation = actualLocation;
  }

  @Override
  public Path getContentPath() {
    return super.getContentPath();
  }

  @Override
  public Set<UrlType> getUrlTypes() {
    return Collections.unmodifiableSet(urlTypes);
  }

  @Override
  public String getProvidedMimeType() {
    return providedMimeType;
  }

  @Override
  public long getProvidedFileSize() {
    return providedFileSize;
  }

  @Override
  public URI getActualLocation() {
    return actualLocation;
  }

  @Override
  public File getContentFile() {
    return Optional.ofNullable(getContentPath()).map(Path::toFile).orElse(null);
  }
}
