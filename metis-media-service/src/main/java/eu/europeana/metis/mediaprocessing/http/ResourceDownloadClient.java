package eu.europeana.metis.mediaprocessing.http;

import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceImpl;
import eu.europeana.metis.network.AbstractHttpClient;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.function.Predicate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link AbstractHttpClient} that obtains the actual content of a resource link.
 */
public class ResourceDownloadClient extends
    AbstractHttpClient<Pair<RdfResourceEntry, ResourceDownloadClient.DownloadMode>, Resource> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceDownloadClient.class);

  /**
   * The mode for taking the decision whether or not to download the full resource.
   */
  public enum DownloadMode {
    /**
     * Signifies that the resource is always to be downloaded.
     */
    ALWAYS,

    /**
     * Signifies that the resource is never to be downloaded.
     */
    NEVER,

    /**
     * Signifies that the decision on whether to download the resource is taken based on the
     * detected mime type.
     */
    MIME_TYPE
  }

  private final Predicate<String> shouldDownloadMimetype;

  /**
   * Constructor.
   *
   * @param maxRedirectCount The maximum number of times we follow a redirect status (status 3xx).
   * @param shouldDownloadMimetype A predicate that, based on the mime type, can decide whether or
   * not to proceed with the download. This will be used for a download with {@link DownloadMode#MIME_TYPE}. 
   * @param connectTimeout The connection timeout in milliseconds.
   * @param responseTimeout The response timeout in milliseconds.
   * @param downloadTimeout The time after which the download will be aborted (if it hasn't finished
   * by then). In milliseconds.
   */
  public ResourceDownloadClient(int maxRedirectCount, Predicate<String> shouldDownloadMimetype,
      int connectTimeout, int responseTimeout, int downloadTimeout) {
    super(maxRedirectCount, connectTimeout, responseTimeout, downloadTimeout);
    this.shouldDownloadMimetype = shouldDownloadMimetype;
  }

  /**
   * Convenience method for triggering a download with {@link DownloadMode#ALWAYS}, forcing the
   * download of the content as well.
   * 
   * @param resourceEntry The resource entry.
   * @return The resulting/downloaded object.
   * @throws IOException In case a connection or other IO problem occurred (including an HTTP status
   *         other than 2xx).
   */
  public Resource downloadWithContent(RdfResourceEntry resourceEntry) throws IOException {
    return download(new ImmutablePair<>(resourceEntry, DownloadMode.ALWAYS));
  }

  /**
   * Convenience method for triggering a download with {@link DownloadMode#NEVER}, preventing the
   * download of the content.
   * 
   * @param resourceEntry The resource entry.
   * @return The resulting/downloaded object.
   * @throws IOException In case a connection or other IO problem occurred (including an HTTP status
   *         other than 2xx).
   */
  public Resource downloadWithoutContent(RdfResourceEntry resourceEntry) throws IOException {
    return download(new ImmutablePair<>(resourceEntry, DownloadMode.NEVER));
  }

  /**
   * Convenience method for triggering a download with {@link DownloadMode#MIME_TYPE}, taking the
   * decision on whether to download the content as well based on the provided mime type.
   * 
   * @param resourceEntry The resource entry.
   * @return The resulting/downloaded object.
   * @throws IOException In case a connection or other IO problem occurred (including an HTTP status
   *         other than 2xx).
   */
  public Resource downloadBasedOnMimeType(RdfResourceEntry resourceEntry) throws IOException {
    return download(new ImmutablePair<>(resourceEntry, DownloadMode.MIME_TYPE));
  }

  @Override
  protected String getResourceUrl(Pair<RdfResourceEntry, DownloadMode> resourceEntry) {
    return resourceEntry.getLeft().getResourceUrl();
  }

  @Override
  protected Resource createResult(Pair<RdfResourceEntry, DownloadMode> providedLink, URI actualUri,
      String mimeType, Long fileSize, ContentRetriever contentRetriever) throws IOException {

    // Create resource
    final RdfResourceEntry resourceEntry = providedLink.getLeft();
    final Resource resource = new ResourceImpl(resourceEntry, mimeType, fileSize, actualUri);

    // In case we are expecting a file, we download it.
    final boolean fullDownload = providedLink.getRight() == DownloadMode.ALWAYS
        || (providedLink.getRight() == DownloadMode.MIME_TYPE && shouldDownloadMimetype.test(mimeType));
    try {
      if (fullDownload) {
        LOGGER.debug("Starting download of resource: {}", resourceEntry.getResourceUrl());
        downloadResource(resourceEntry.getResourceUrl(), resource, contentRetriever);
        LOGGER.debug("Finished download of resource: {}", resourceEntry.getResourceUrl());
      } else {
        LOGGER.debug("Download mode {} and media type {} - choosing not to download resource: {}",
            providedLink.getRight(), mimeType, resourceEntry.getResourceUrl());
        resource.markAsNoContent();
      }
    } catch (IOException | RuntimeException e) {
      resource.close();
      throw e;
    }

    return resource;
  }

  private static void downloadResource(String resourceUrl, Resource resource,
      ContentRetriever contentRetriever) throws IOException {
    try (final InputStream inputStream = contentRetriever.getContent()) {
      resource.markAsWithContent(inputStream);
    }
    if (resource.getContentSize() == 0) {
      throw new IOException("Download failed of resource " + resourceUrl + ": no content found.");
    }
  }
}
