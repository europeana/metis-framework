package eu.europeana.metis.mediaprocessing.extraction;

import static eu.europeana.metis.utils.SonarqubeNullcheckAvoidanceUtils.performThrowingAction;
import static org.apache.tika.metadata.HttpHeaders.CONTENT_TYPE;

import eu.europeana.metis.mediaprocessing.MediaExtractor;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.http.MimeTypeDetectHttpClient;
import eu.europeana.metis.mediaprocessing.http.ResourceDownloadClient;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import eu.europeana.metis.mediaprocessing.wrappers.TikaWrapper;
import eu.europeana.metis.schema.model.MediaType;
import eu.europeana.metis.utils.SonarqubeNullcheckAvoidanceUtils.ThrowingConsumer;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

  private final ResourceDownloadClient resourceDownloadClientOembed;
  private final ResourceDownloadClient resourceDownloadClientNonOembed;
  private final MimeTypeDetectHttpClient mimeTypeDetectHttpClient;
  private final TikaWrapper tika;

  private final ImageProcessor imageProcessor;
  private final AudioVideoProcessor audioVideoProcessor;
  private final TextProcessor textProcessor;
  private final Media3dProcessor media3dProcessor;
  private final OEmbedProcessor oEmbedProcessor;

  /**
   * Constructor meant for testing purposes.
   *
   * @param resourceDownloadClient The download client for resources.
   * @param mimeTypeDetectHttpClient The mime type detector for URLs.
   * @param tika A tika instance.
   * @param mediaProcessorList the media processor list
   */
  MediaExtractorImpl(ResourceDownloadClient resourceDownloadClient,
      MimeTypeDetectHttpClient mimeTypeDetectHttpClient, TikaWrapper tika,
      List<MediaProcessor> mediaProcessorList) {
    this.resourceDownloadClientNonOembed = resourceDownloadClient;
    this.resourceDownloadClientOembed = resourceDownloadClient;
    this.mimeTypeDetectHttpClient = mimeTypeDetectHttpClient;
    this.tika = tika;
    this.imageProcessor = (ImageProcessor) getMediaProcessor(mediaProcessorList, ImageProcessor.class);
    this.audioVideoProcessor = (AudioVideoProcessor) getMediaProcessor(mediaProcessorList, AudioVideoProcessor.class);
    this.textProcessor = (TextProcessor) getMediaProcessor(mediaProcessorList, TextProcessor.class);
    this.media3dProcessor = (Media3dProcessor) getMediaProcessor(mediaProcessorList, Media3dProcessor.class);
    this.oEmbedProcessor = (OEmbedProcessor) getMediaProcessor(mediaProcessorList, OEmbedProcessor.class);
  }

  /**
   * Constructor for non-testing purposes.
   *
   * @param redirectCount The maximum number of times we will follow a redirect.
   * @param thumbnailGenerateTimeout The maximum amount of time, in seconds, a thumbnail generation command is allowed to take
   * before it is forcibly destroyed (i.e. cancelled).
   * @param audioVideoProbeTimeout The maximum amount of time, in seconds, a audio/video probe command is allowed to take before
   * it is forcibly destroyed (i.e. cancelled).
   * @param connectTimeout The connection timeout in milliseconds for downloading resources.
   * @param responseTimeout The response timeout in milliseconds for downloading resources.
   * @param downloadTimeout The download timeout in milliseconds for downloading resources.
   * @throws MediaProcessorException In case something went wrong while initializing the extractor.
   */
  public MediaExtractorImpl(int redirectCount, int thumbnailGenerateTimeout,
      int audioVideoProbeTimeout, int connectTimeout, int responseTimeout, int downloadTimeout)
      throws MediaProcessorException {
    final ThumbnailGenerator thumbnailGenerator = new ThumbnailGenerator(
        new CommandExecutor(thumbnailGenerateTimeout));
    this.resourceDownloadClientOembed = new ResourceDownloadClient(redirectCount,
        type -> this.shouldDownloadForFullProcessing(type, true),
        connectTimeout, responseTimeout, downloadTimeout);
    this.resourceDownloadClientNonOembed = new ResourceDownloadClient(redirectCount,
        type -> this.shouldDownloadForFullProcessing(type, false),
        connectTimeout, responseTimeout, downloadTimeout);
    this.mimeTypeDetectHttpClient = new MimeTypeDetectHttpClient(connectTimeout, responseTimeout,
        downloadTimeout);
    this.tika = new TikaWrapper();
    this.imageProcessor = new ImageProcessor(thumbnailGenerator);
    this.audioVideoProcessor = new AudioVideoProcessor(new CommandExecutor(audioVideoProbeTimeout));
    this.textProcessor = new TextProcessor(thumbnailGenerator,
        new PdfToImageConverter(new CommandExecutor(thumbnailGenerateTimeout)));
    this.media3dProcessor = new Media3dProcessor();
    this.oEmbedProcessor = new OEmbedProcessor();
  }

  private <T> Object getMediaProcessor(List<?> mediaProcessorList, Class<T> type) {
    for (Object mediaProcessor : mediaProcessorList) {
      if (type.isInstance(mediaProcessor)) {
        return type.cast(mediaProcessor);
      }
    }
    return null;
  }

  @Override
  public ResourceExtractionResult performMediaExtraction(RdfResourceEntry resourceEntry,
      boolean mainThumbnailAvailable) throws MediaExtractionException {

    // Decide how to process it.
    final ProcessingMode mode = getMode(resourceEntry);
    if (mode == ProcessingMode.NONE) {
      return null;
    }

    // Download resource and then perform media extraction on it.
    try (Resource resource = downloadBasedOnProcessingMode(resourceEntry, mode,
        resourceEntry.isResourceConfiguredForOembed())) {
      return performProcessing(resource, mode, mainThumbnailAvailable,
          resourceEntry.isResourceConfiguredForOembed());
    } catch (IOException | RuntimeException e) {
      throw new MediaExtractionException(
          String.format("Problem while processing %s", resourceEntry.getResourceUrl()), e);
    }
  }

  private ResourceDownloadClient getResourceDownloadClient(boolean potentialOembedResource) {
    return potentialOembedResource ? this.resourceDownloadClientOembed
        : this.resourceDownloadClientNonOembed;
  }

  private Resource downloadBasedOnProcessingMode(RdfResourceEntry resourceEntry,
      ProcessingMode mode, boolean potentialOembedResource) throws IOException {

    // Determine the download method to use (full download vs. quick ping)
    final ResourceDownloadClient client = getResourceDownloadClient(potentialOembedResource);
    return (mode == ProcessingMode.FULL)
        ? client.downloadBasedOnMimeType(resourceEntry)
        : client.downloadWithoutContent(resourceEntry);
  }

  ProcessingMode getMode(RdfResourceEntry resourceEntry) {
    final ProcessingMode result;
    if (resourceEntry.isResourceConfiguredForOembed()) {
      result = ProcessingMode.FULL;
    } else if (URL_TYPES_FOR_FULL_PROCESSING.stream().anyMatch(resourceEntry.getUrlTypes()::contains)) {
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
    } catch (IOException | IllegalArgumentException e) {
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
      metadata.set(CONTENT_TYPE, adjustedMimeType);
    }
    try (final InputStream stream = TikaInputStream.get(path, metadata)) {
      return tika.detect(stream, metadata);
    }
  }

  List<MediaProcessor> chooseMediaProcessor(MediaType mediaType, String detectedMimeType,
      boolean potentialOembedResource) {
    return switch (mediaType) {
      case TEXT, OTHER -> chooseMediaProcessorTextAndOther(mediaType, detectedMimeType,
          potentialOembedResource);
      case AUDIO, VIDEO -> List.of(audioVideoProcessor);
      case IMAGE ->  List.of(imageProcessor);
      case THREE_D -> List.of(media3dProcessor);
    };
  }

  private List<MediaProcessor> chooseMediaProcessorTextAndOther(MediaType mediaType,
      String detectedMimeType, boolean potentialOembedResource) {
    if (detectedMimeType == null) {
      return null;
    } else if (potentialOembedResource && (detectedMimeType.startsWith("text/xml")
        || detectedMimeType.startsWith("application/xml") || detectedMimeType.startsWith("application/json"))) {
      return List.of(oEmbedProcessor, textProcessor);
    } else if (mediaType == MediaType.TEXT) {
      return List.of(textProcessor);
    } else {
      return Collections.emptyList();
    }
  }

  void verifyAndCorrectContentAvailability(Resource resource, ProcessingMode mode,
      String detectedMimeType, boolean potentialOembedResource)
      throws MediaExtractionException, IOException {

    // If the mime type changed and we need the content after all, we download it.
    if (mode == ProcessingMode.FULL && shouldDownloadForFullProcessing(detectedMimeType, potentialOembedResource)
        && !shouldDownloadForFullProcessing(resource.getProvidedMimeType(), potentialOembedResource)) {
      final RdfResourceEntry downloadInput = new RdfResourceEntry(resource.getResourceUrl(),
          new ArrayList<>(resource.getUrlTypes()), potentialOembedResource);

      ThrowingConsumer<Resource, IOException> action = resourceWithContent -> {
        if (resourceWithContent.hasContent()) {
          try (final InputStream inputStream = resourceWithContent.getContentStream()) {
            resource.markAsWithContent(inputStream);
          }
        }
      };
      try (final Resource resourceWithContent = getResourceDownloadClient(potentialOembedResource)
          .downloadWithContent(downloadInput)) {
        performThrowingAction(resourceWithContent, action);
      }
    }

    // Verify that we have content when we need to.
    if (mode == ProcessingMode.FULL && shouldDownloadForFullProcessing(detectedMimeType, potentialOembedResource)
        && !resource.hasContent()) {
      throw new MediaExtractionException(
          "File content is not downloaded and mimeType does not support processing without a downloaded file.");
    }
  }

  ResourceExtractionResult performProcessing(Resource resource, ProcessingMode mode,
      boolean mainThumbnailAvailable, boolean potentialOembedResource) throws MediaExtractionException {

    // Sanity check - shouldn't be called for this mode.
    if (mode == ProcessingMode.NONE) {
      throw new IllegalStateException();
    }

    // Detect and verify the mime type.
    final String detectedMimeType = detectAndVerifyMimeType(resource, mode);

    // Verify that we have content when we need to. This can happen if the resource doesn't come
    // with the correct mime type. We correct this here.
    try {
      verifyAndCorrectContentAvailability(resource, mode, detectedMimeType, potentialOembedResource);
    } catch (IOException e) {
      throw new MediaExtractionException("Content availability verification error.", e);
    }

    // Choose the right media processor.
    final List<MediaProcessor> processors = chooseMediaProcessor(
        MediaType.getMediaType(detectedMimeType), detectedMimeType, potentialOembedResource);

    // Go in order, the first result we get, we accept.
    for (MediaProcessor processor: processors) {
      final ResourceExtractionResult result = getResourceExtractionResult(resource, mode,
          mainThumbnailAvailable, processor, detectedMimeType);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  private static ResourceExtractionResult getResourceExtractionResult(Resource resource,
      ProcessingMode mode, boolean mainThumbnailAvailable, MediaProcessor processor,
      String detectedMimeType) throws MediaExtractionException {
    final ResourceExtractionResult result;
    // Process the resource depending on the mode.
    if (mode == ProcessingMode.FULL) {
      result = processor.extractMetadata(resource, detectedMimeType, mainThumbnailAvailable);
    } else {
      result = processor.copyMetadata(resource, detectedMimeType);
    }
    return result;
  }

  @Override
  public void close() throws IOException {
    resourceDownloadClientOembed.close();
    resourceDownloadClientNonOembed.close();
    mimeTypeDetectHttpClient.close();
  }

  /**
   * @return true if and only if resources of the given type need to be downloaded before performing full processing.
   */
  boolean shouldDownloadForFullProcessing(String mimeType, boolean potentialOembedResource) {
    return Optional.of(MediaType.getMediaType(mimeType))
        .map(mediaType -> chooseMediaProcessor(mediaType, mimeType, potentialOembedResource))
        .stream().flatMap(Collection::stream)
        .anyMatch(MediaProcessor::downloadResourceForFullProcessing);
  }
}
