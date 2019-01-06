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

  private static final Logger LOGGER = LoggerFactory.getLogger(MediaExtractorImpl.class);

  private final ResourceDownloadClient resourceDownloadClient;
  private final CommandExecutor commandExecutor;
  private final Tika tika;

  private final ImageProcessor imageProcessor;
  private final AudioVideoProcessor audioVideoProcessor;
  private final TextProcessor textProcessor;

  MediaExtractorImpl(ResourceDownloadClient resourceDownloadClient, CommandExecutor commandExecutor,
      Tika tika, ThumbnailGenerator thumbnailGenerator, AudioVideoProcessor audioVideoProcessor) {
    this.resourceDownloadClient = resourceDownloadClient;
    this.commandExecutor = commandExecutor;
    this.tika = tika;
    this.imageProcessor = new ImageProcessor(thumbnailGenerator);
    this.audioVideoProcessor = audioVideoProcessor;
    this.textProcessor = new TextProcessor(thumbnailGenerator);
  }

  private MediaExtractorImpl(ResourceDownloadClient resourceDownloadClient,
      CommandExecutor commandExecutor, Tika tika) throws MediaProcessorException {
    this(resourceDownloadClient, commandExecutor, tika, new ThumbnailGenerator(commandExecutor),
        new AudioVideoProcessor(commandExecutor));
  }

  public MediaExtractorImpl(int redirectCount, int commandThreadPoolSize)
      throws MediaProcessorException {
    this(new ResourceDownloadClient(redirectCount, ResourceType::shouldDownloadMimetype),
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
  // TODO only not private because of unit tests: should fix tests.
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
    if (contents == null && ResourceType.shouldDownloadMimetype(mimeType)) {
      throw new MediaExtractionException(
          "File content is not downloaded and mimeType does not support processing without a downloaded file.");
    }

    // Process the resource.
    final ResourceExtractionResult result;
    switch (ResourceType.getResourceType(mimeType)) {
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

  @Override
  public void close() throws IOException {
    try {
      commandExecutor.shutdown();
    } finally {
      resourceDownloadClient.close();
    }
  }
}
