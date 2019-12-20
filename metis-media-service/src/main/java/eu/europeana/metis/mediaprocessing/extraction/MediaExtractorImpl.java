package eu.europeana.metis.mediaprocessing.extraction;

import eu.europeana.metis.mediaprocessing.MediaExtractor;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.http.MimeTypeDetectHttpClient;
import eu.europeana.metis.mediaprocessing.http.ResourceDownloadClient;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import eu.europeana.metis.utils.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts technical metadata and generates thumbnails for web resources.
 */
public class MediaExtractorImpl implements MediaExtractor {

  private static final Logger LOGGER = LoggerFactory.getLogger(MediaExtractorImpl.class);

  enum ProcessingMode {FULL, REDUCED, NONE}

  private static final Set<UrlType> URL_TYPES_FOR_FULL_PROCESSING = Collections
      .unmodifiableSet(EnumSet.of(UrlType.IS_SHOWN_BY, UrlType.HAS_VIEW, UrlType.OBJECT));

  private static final Set<UrlType> URL_TYPES_FOR_REDUCED_PROCESSING = Collections
      .singleton(UrlType.IS_SHOWN_AT);

  private final ResourceDownloadClient forcedDownloadClient;
  private final ResourceDownloadClient fullProcessingDownloadClient;
  private final ResourceDownloadClient reducedProcessingDownloadClient;
  private final MimeTypeDetectHttpClient mimeTypeDetectHttpClient;
  private final Tika tika;

  private final ImageProcessor imageProcessor;
  private final AudioVideoProcessor audioVideoProcessor;
  private final TextProcessor textProcessor;

  /**
   * Constructor meant for testing purposes.
   *
   * @param forcedDownloadClient The download client for resources.
   * @param fullProcessingDownloadClient The download client for resources.
   * @param reducedProcessingDownloadClient The download client for resources.
   * @param mimeTypeDetectHttpClient The mime type detector for URLs.
   * @param tika A tika instance.
   * @param imageProcessor An image processor.
   * @param audioVideoProcessor An audio/video processor.
   * @param textProcessor A text processor.
   */
  MediaExtractorImpl(ResourceDownloadClient forcedDownloadClient,
      ResourceDownloadClient fullProcessingDownloadClient,
      ResourceDownloadClient reducedProcessingDownloadClient,
      MimeTypeDetectHttpClient mimeTypeDetectHttpClient, Tika tika, ImageProcessor imageProcessor,
      AudioVideoProcessor audioVideoProcessor, TextProcessor textProcessor) {
    this.forcedDownloadClient = forcedDownloadClient;
    this.fullProcessingDownloadClient = fullProcessingDownloadClient;
    this.reducedProcessingDownloadClient = reducedProcessingDownloadClient;
    this.mimeTypeDetectHttpClient = mimeTypeDetectHttpClient;
    this.tika = tika;
    this.imageProcessor = imageProcessor;
    this.audioVideoProcessor = audioVideoProcessor;
    this.textProcessor = textProcessor;
  }

  /**
   * Constructor for non-testing purposes.
   *
   * @param redirectCount The maximum number of times we will follow a redirect.
   * @param thumbnailGenerateTimeout The maximum amount of time, in seconds, a thumbnail generation
   * command is allowed to take before it is forcibly destroyed (i.e. cancelled).
   * @param audioVideoProbeTimeout The maximum amount of time, in seconds, a audio/video probe
   * command is allowed to take before it is forcibly destroyed (i.e. cancelled).
   * @param connectTimeout The connection timeout in milliseconds for downloading resources.
   * @param socketTimeout The socket timeout in milliseconds for downloading resources.
   * @param downloadTimeout The download timeout in milliseconds for downloading resources.
   * @throws MediaProcessorException In case something went wrong while initializing the extractor.
   */
  public MediaExtractorImpl(int redirectCount, int thumbnailGenerateTimeout,
      int audioVideoProbeTimeout, int connectTimeout, int socketTimeout, int downloadTimeout)
      throws MediaProcessorException {
    final ThumbnailGenerator thumbnailGenerator = new ThumbnailGenerator(
        new CommandExecutor(thumbnailGenerateTimeout));
    this.forcedDownloadClient = new ResourceDownloadClient(redirectCount, mimeType -> true,
        connectTimeout, socketTimeout, downloadTimeout);
    this.fullProcessingDownloadClient = new ResourceDownloadClient(redirectCount,
        this::shouldDownloadForFullProcessing, connectTimeout, socketTimeout, downloadTimeout);
    this.reducedProcessingDownloadClient = new ResourceDownloadClient(redirectCount,
        mimeType -> false, connectTimeout, socketTimeout, downloadTimeout);
    this.mimeTypeDetectHttpClient = new MimeTypeDetectHttpClient(connectTimeout, socketTimeout,
        downloadTimeout);
    this.tika = new Tika();
    this.imageProcessor = new ImageProcessor(thumbnailGenerator);
    this.audioVideoProcessor = new AudioVideoProcessor(new CommandExecutor(audioVideoProbeTimeout));
    this.textProcessor = new TextProcessor(thumbnailGenerator);
  }

  @Override
  public ResourceExtractionResult performMediaExtraction(RdfResourceEntry resourceEntry)
      throws MediaExtractionException {

    // Decide how to process it.
    final ProcessingMode mode = getMode(resourceEntry);
    if (mode == ProcessingMode.NONE) {
      return null;
    }

    // Determine the http client to use (full download vs. quick ping)
    final ResourceDownloadClient httpClient = mode == ProcessingMode.FULL ?
        fullProcessingDownloadClient : reducedProcessingDownloadClient;

    // Download resource and then perform media extraction on it.
    try (Resource resource = httpClient.download(resourceEntry)) {
      return performProcessing(resource, mode);
    } catch (IOException | RuntimeException e) {
      throw new MediaExtractionException(
          "Problem while processing " + resourceEntry.getResourceUrl(), e);
    }
  }

  ProcessingMode getMode(RdfResourceEntry resourceEntry) {
    final ProcessingMode result;
    if (URL_TYPES_FOR_FULL_PROCESSING.stream().anyMatch(resourceEntry.getUrlTypes()::contains)) {
      result = ProcessingMode.FULL;
    } else if (URL_TYPES_FOR_REDUCED_PROCESSING.stream()
        .anyMatch(resourceEntry.getUrlTypes()::contains)) {
      result = ProcessingMode.REDUCED;
    } else {
      result = ProcessingMode.NONE;
    }
    return result;
  }

  String detectAndVerifyMimeType(Resource resource, ProcessingMode mode)
      throws MediaExtractionException {

    // Sanity check - shouldn't be called for this mode.
    if (mode == ProcessingMode.NONE) {
      throw new IllegalStateException();
    }

    // Detect the mime type.
    final String providedMimeType = resource.getProvidedMimeType();
    final String detectedMimeType;
    final boolean hasContent;
    try {
      hasContent = resource.hasContent();
      detectedMimeType = hasContent ? detectType(resource.getContentPath(), providedMimeType)
          : mimeTypeDetectHttpClient.download(resource.getActualLocation().toURL());
    } catch (IOException e) {
      throw new MediaExtractionException("Mime type checking error", e);
    }

    // Log if the detected mime type is different from the provided one. If application/xhtml+xml is
    // detected from tika, and text/html is provided, we don't give a warning.
    if (providedMimeType != null) {
      final boolean xhtmlHtmlEquivalenceOccurs = "application/xhtml+xml".equals(detectedMimeType)
          && providedMimeType.startsWith("text/html");
      if (!xhtmlHtmlEquivalenceOccurs && !detectedMimeType.equals(providedMimeType)) {
        LOGGER.info("Invalid mime type provided (should be {}, was {}): {}", detectedMimeType,
            providedMimeType, resource.getResourceUrl());
      }
    }

    // Done
    return detectedMimeType;
  }

  String detectType(Path path, String providedMimeType) throws IOException {
    final Metadata metadata = new Metadata();
    if (providedMimeType != null) {
      final int separatorIndex = providedMimeType.indexOf(';');
      final String adjustedMimeType =
              separatorIndex < 0 ? providedMimeType : providedMimeType.substring(0, separatorIndex);
      metadata.set(Metadata.CONTENT_TYPE, adjustedMimeType);
    }
    try (final InputStream stream = TikaInputStream.get(path, metadata)) {
      return tika.detect(stream, metadata);
    }
  }

  MediaProcessor chooseMediaProcessor(MediaType mediaType) {
    final MediaProcessor processor;
    switch (mediaType) {
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
    return processor;
  }
  
  void verifyAndCorrectContentAvailability(Resource resource, ProcessingMode mode,
      String detectedMimeType) throws MediaExtractionException {
    try {
      if (mode == ProcessingMode.FULL && shouldDownloadForFullProcessing(detectedMimeType)
          && !resource.hasContent()) {
        final RdfResourceEntry downloadInput =  new RdfResourceEntry(
            resource.getResourceUrl(), new ArrayList<>(resource.getUrlTypes()));
        try (
            final Resource resourceWithContent = this.forcedDownloadClient.download(downloadInput);
            final InputStream inputStream = resourceWithContent.getContentStream()) {
          resource.markAsWithContent(inputStream);
        }
      }
    } catch (IOException e) {
      throw new MediaExtractionException("Content availability verification error.", e);
    }
  }

  ResourceExtractionResult performProcessing(Resource resource, ProcessingMode mode)
      throws MediaExtractionException {

    // Sanity check - shouldn't be called for this mode.
    if (mode == ProcessingMode.NONE) {
      throw new IllegalStateException();
    }

    // Detect and verify the mime type.
    final String detectedMimeType = detectAndVerifyMimeType(resource, mode);
    
    // Verify that we have content when we need to. This can happen if the resource doesn't come
    // with the correct mime type. We correct this here.
    verifyAndCorrectContentAvailability(resource, mode, detectedMimeType);

    // Choose the right media processor.
    final MediaProcessor processor = chooseMediaProcessor(MediaType.getMediaType(detectedMimeType));

    // Process the resource depending on the mode.
    final ResourceExtractionResult result;
    if (processor == null) {
      result = null;
    } else if (mode == ProcessingMode.FULL) {
      result = processor.extractMetadata(resource, detectedMimeType);
    } else {
      result = processor.copyMetadata(resource, detectedMimeType);
    }

    // Done
    return result;
  }

  @Override
  public void close() throws IOException {
    try {
      fullProcessingDownloadClient.close();
    } finally {
      reducedProcessingDownloadClient.close();
    }
  }

  /**
   * @return true if and only if resources of the given type need to be downloaded before performing
   * full processing.
   */
  boolean shouldDownloadForFullProcessing(String mimeType) {
    return Optional.of(MediaType.getMediaType(mimeType)).map(this::chooseMediaProcessor)
        .map(MediaProcessor::downloadResourceForFullProcessing).orElse(Boolean.FALSE);
  }
}
