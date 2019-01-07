package eu.europeana.metis.mediaprocessing.extraction;

import java.io.Closeable;
import java.io.IOException;
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

  /**
   * Constructor meant for testing purposes.
   * 
   * @param resourceDownloadClient The download client for resources.
   * @param commandExecutor A command executor.
   * @param tika A tika instance.
   * @param thumbnailGenerator A thumbnail generator.
   * @param audioVideoProcessor An audio/video processor.
   */
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

  /**
   * Constructor for non-testing purposes.
   * 
   * @param redirectCount The maximum number of times we will follow a redirect.
   * @param commandThreadPoolSize The maximum number of processes that can do command-line IO.
   * @throws MediaProcessorException In case something went wrong while initializing the extractor.
   */
  public MediaExtractorImpl(int redirectCount, int commandThreadPoolSize)
      throws MediaProcessorException {
    this(new ResourceDownloadClient(redirectCount, ResourceType::shouldDownloadMimetype),
        new CommandExecutor(commandThreadPoolSize), new Tika());
  }

  @Override
  public ResourceExtractionResult performMediaExtraction(RdfResourceEntry resourceEntry)
      throws MediaExtractionException {

    // Download resource and then perform media extraction on it.
    try (Resource resource = resourceDownloadClient.download(resourceEntry)) {
      return processResource(resource);
    } catch (IOException | RuntimeException e) {
      throw new MediaExtractionException(
          "Problem while processing " + resourceEntry.getResourceUrl(), e);
    }
  }

  private ResourceExtractionResult processResource(Resource resource)
      throws MediaExtractionException {

    // Obtain the mime type. If no content, check against the URL. Note: we use the actual location
    // instead of the resource URL (because Tika doesn't seem to do forwarding properly).
    final String detectedMimeType;
    try {
      detectedMimeType = resource.hasContent() ? tika.detect(resource.getContentPath())
          : tika.detect(resource.getActualLocation().toURL());
    } catch (IOException e) {
      throw new MediaExtractionException("Mime type checking error", e);
    }

    // Verify the mime type. Permit the application/xhtml+xml detected from tika to be virtually
    // equal to the text/html detected from the providedMimeType.
    final String providedMimeType = resource.getMimeType();
    if (!("application/xhtml+xml".equals(detectedMimeType) && "text/html".equals(providedMimeType))
        && !detectedMimeType.equals(providedMimeType)) {
      LOGGER.info("Invalid mime type provided (should be {}, was {}): {}", detectedMimeType,
          providedMimeType, resource.getResourceUrl());
    }
    if (!resource.hasContent() && ResourceType.shouldDownloadMimetype(providedMimeType)) {
      throw new MediaExtractionException(
          "File content is not downloaded and mimeType does not support processing without a downloaded file.");
    }

    // Choose the right media processor.
    final MediaProcessor processor;
    switch (ResourceType.getResourceType(detectedMimeType)) {
      case TEXT:
        processor = textProcessor;
        break;
      case AUDIO:
      case VIDEO:
        processor = audioVideoProcessor;
        break;
      case IMAGE:
        processor = imageProcessor;
        break;
      default:
        processor = null;
        break;
    }

    // Process the resource.
    return processor != null ? processor.process(resource) : null;
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
