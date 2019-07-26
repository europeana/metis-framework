package eu.europeana.metis.mediaprocessing.http;

import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceImpl;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link AbstractHttpClient} that obtains the actual content of a resource link.
 */
public class ResourceDownloadClient extends AbstractHttpClient<RdfResourceEntry, Resource> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceDownloadClient.class);

  private final Predicate<String> shouldDownloadMimetype;

  /**
   * Constructor.
   *
   * @param maxRedirectCount The maximum number of times we follow a redirect status (status 3xx).
   * @param shouldDownloadMimetype A predicate that, based on the mime type, can decide whether or
   * not to proceed with the download.
   * @param connectTimeout The connection timeout in milliseconds.
   * @param socketTimeout The socket timeout in milliseconds.
   */
  public ResourceDownloadClient(int maxRedirectCount, Predicate<String> shouldDownloadMimetype,
      int connectTimeout, int socketTimeout) {
    super(maxRedirectCount, connectTimeout, socketTimeout);
    this.shouldDownloadMimetype = shouldDownloadMimetype;
  }

  @Override
  protected String getResourceUrl(RdfResourceEntry resourceEntry) {
    return resourceEntry.getResourceUrl();
  }

  @Override
  protected Resource createResult(RdfResourceEntry resourceEntry, URI actualUri, String mimeType,
      Long fileSize, ContentRetriever contentRetriever) throws IOException {

    // Create resource
    final ResourceImpl resource = new ResourceImpl(resourceEntry, mimeType, fileSize, actualUri);

    // In case we are expecting a file, we download it.
    try {
      if (shouldDownloadMimetype.test(mimeType)) {
        LOGGER.debug("Starting download of resource: {}", resourceEntry.getResourceUrl());
        downloadResource(resourceEntry.getResourceUrl(), resource, contentRetriever);
        LOGGER.debug("Finished download of resource: {}", resourceEntry.getResourceUrl());
      } else {
        LOGGER.debug("Media type {} not supported - choosing not to download resource: {}",
            mimeType, resourceEntry.getResourceUrl());
        resource.markAsNoContent();
      }
    } catch (IOException | RuntimeException e) {
      // Close the resource if a problem occurs.
      resource.close();
      throw e;
    }

    // Done: return the resource.
    return resource;
  }

  private static void downloadResource(String resourceUrl, ResourceImpl resource,
      ContentRetriever contentRetriever) throws IOException {
    try (final InputStream inputStream = contentRetriever.getContent()) {
      resource.setContent(inputStream);
    }
    if (resource.getContentSize() == 0) {
      throw new IOException("Download failed of resource " + resourceUrl + ": no content found.");
    }
  }
}
