package eu.europeana.metis.mediaprocessing.extraction;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Set;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.metis.mediaprocessing.MediaExtractor;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.http.ResourceDownloadClient;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import eu.europeana.metis.mediaprocessing.model.UrlType;

/**
 * Extracts technical metadata and generates thumbnails for web resources.
 */
public class MediaExtractorImpl implements MediaExtractor, Closeable {

  enum ResourceType {
    AUDIO, VIDEO, TEXT, IMAGE, UNKNOWN
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(MediaExtractorImpl.class);

  private final ResourceDownloadClient resourceDownloadClient;
  private final CommandExecutor commandExecutor;
  private final Tika tika;

  private final ImageProcessor imageProcessor;
  private final AudioVideoProcessor audioVideoProcessor;
  private final TextProcessor textProcessor;

  MediaExtractorImpl(ResourceDownloadClient resourceDownloadClient, CommandExecutor commandExecutor,
      Tika tika) throws MediaProcessorException {
    this.resourceDownloadClient = resourceDownloadClient;
    this.commandExecutor = commandExecutor;
    this.tika = tika;
    final ThumbnailGenerator thumbnailGenerator = new ThumbnailGenerator(commandExecutor);
    this.imageProcessor = new ImageProcessor(thumbnailGenerator);
    this.audioVideoProcessor = new AudioVideoProcessor(commandExecutor);
    this.textProcessor = new TextProcessor(thumbnailGenerator);
  }

  public MediaExtractorImpl(int redirectCount, int commandThreadPoolSize)
      throws MediaProcessorException {
    this(new ResourceDownloadClient(redirectCount, MediaExtractorImpl::shouldDownloadMimetype),
        new CommandExecutor(commandThreadPoolSize), new Tika());
  }

  @Override
  public ResourceExtractionResult performMediaExtraction(RdfResourceEntry resourceEntry)
      throws MediaExtractionException {

    // Download resource and then perform media extraction on it.
    final ResourceExtractionResult result;
    try (Resource resource = resourceDownloadClient.download(resourceEntry)) {
      final File file =
          resource.getContentPath() == null ? null : resource.getContentPath().toFile();
      result = processResource(resource.getResourceUrl(), resource.getUrlTypes(),
          resource.getMimeType(), file);
    } catch (IOException | RuntimeException e) {
      throw new MediaExtractionException(
          "Problem while processing " + resourceEntry.getResourceUrl(), e);
    }

    // Return result.
    if (result == null) {
      // TODO unknown type... should that result in an exception? Or should we just return null.
      throw new IllegalStateException("Unexpected result size!");
    }
    return result;
  }

  /**
   * @param contents downloaded file, can be {@code null} for mime types accepted by
   *        {@link #supportsLinkProcessing(String)}
   */
  ResourceExtractionResult processResource(String url, Set<UrlType> urlTypes,
      String providedMimeType, File contents) throws MediaExtractionException {

    // Obtain the mime type
    final String mimeType;
    try {
      mimeType = contents == null ? tika.detect(URI.create(url).toURL()) : tika.detect(contents);
    } catch (IOException e) {
      throw new MediaExtractionException("Mime type checking error", e);
    }

    // Verify the mime type. Permit the application/xhtml+xml detected from tika to be virtually
    // equal to the text/html detected from the providedMimeType.
    if (!("application/xhtml+xml".equals(mimeType) && "text/html".equals(providedMimeType))
        && !mimeType.equals(providedMimeType)) {
      LOGGER.info("Invalid mime type provided (should be {}, was {}): {}", mimeType,
          providedMimeType, url);
    }
    if (contents == null && shouldDownloadMimetype(mimeType)) {
      throw new MediaExtractionException(
          "File content is null and mimeType does not support link processing");
    }

    // Process the resource.
    final ResourceExtractionResult result;
    switch (getResourceType(mimeType)) {
      case TEXT:
        result = textProcessor.processText(url, urlTypes, mimeType, contents);
        break;
      case AUDIO:
      case VIDEO:
        result = audioVideoProcessor.processAudioVideo(url, urlTypes, mimeType, contents);
        break;
      case IMAGE:
        result = imageProcessor.processImage(url, urlTypes, mimeType, contents);
        break;
      default:
        result = null;
        break;
    }

    // Done
    return result;
  }

  private static ResourceType getResourceType(String mimeType) {
    final ResourceType result;
    if (mimeType.startsWith("image/")) {
      result = ResourceType.IMAGE;
    } else if (mimeType.startsWith("audio/")) {
      result = ResourceType.AUDIO;
    } else if (mimeType.startsWith("video/")) {
      result = ResourceType.VIDEO;
    } else if (isText(mimeType)) {
      result = ResourceType.TEXT;
    } else {
      result = ResourceType.UNKNOWN;
    }
    return result;
  }

  private static boolean isText(String mimeType) {
    switch (mimeType) {
      case "application/xml":
      case "application/rtf":
      case "application/epub":
      case "application/pdf":
      case "application/xhtml+xml":
        return true;
      default:
        return mimeType.startsWith("text/");
    }
  }

  @Override
  public void close() throws IOException {
    try {
      commandExecutor.shutdown();
    } finally {
      resourceDownloadClient.close();
    }
  }

  /**
   * @return true if and only if resources of the given type need to be downloaded before
   *         processing.
   */
  // TODO where should this method be?? In ResourceType?
  private static boolean shouldDownloadMimetype(String mimeType) {
    // TODO also when type is UNKNOWN! So that we don't download something that is not processable.
    final ResourceType resourceType = getResourceType(mimeType);
    return ResourceType.AUDIO != resourceType && ResourceType.VIDEO != resourceType;
  }
}
