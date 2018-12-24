package eu.europeana.metis.mediaprocessing.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.Resource;

/**
 * An {@link HttpClient} that obtains the actual content of a resource link.
 */
public class ResourceDownloadClient extends HttpClient<Resource> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceDownloadClient.class);

  private final Predicate<String> shouldDownloadMimetype;

  /**
   * 
   * @param maxRedirectCount The maximum number of times we follow a redirect status (status 3xx).
   * @param shouldDownloadMimetype A predicate that, based on the mime type, can decide whether or
   *        not to proceed with the download.
   */
  public ResourceDownloadClient(int maxRedirectCount, Predicate<String> shouldDownloadMimetype) {
    super(maxRedirectCount, 10000, 20000);
    this.shouldDownloadMimetype = shouldDownloadMimetype;
  }

  @Override
  protected Resource createResult(RdfResourceEntry resourceEntry, URI actualUri, String mimeType,
      ContentRetriever contentRetriever) throws IOException {

    // Create resource
    final Resource resource = new Resource(resourceEntry, mimeType, actualUri);

    // In case we are expecting a file, we download it.
    try {
      if (shouldDownloadMimetype.test(mimeType)) {
        LOGGER.debug("Starting download of resource: {}", resourceEntry.getResourceUrl());
        downloadResource(resourceEntry.getResourceUrl(), resource, contentRetriever);
        LOGGER.debug("Finished download of resource: {}", resourceEntry.getResourceUrl());
      }
    } catch (IOException | RuntimeException e) {
      // Close the resource if a problem occurs.
      resource.close();
      throw e;
    }

    // Done: return the resource.
    return resource;
  }

  private static void downloadResource(String resourceUrl, Resource resource,
      ContentRetriever contentRetriever) throws IOException {
    try (final InputStream inputStream = contentRetriever.getContent()) {
      Files.copy(inputStream, resource.getContentPath(), StandardCopyOption.REPLACE_EXISTING);
    }
    if (resource.getContentSize() == 0) {
      throw new IOException("Download failed of resource " + resourceUrl + ": no content found.");
    }
  }
}
