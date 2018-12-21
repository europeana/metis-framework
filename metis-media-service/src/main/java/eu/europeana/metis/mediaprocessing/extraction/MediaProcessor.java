package eu.europeana.metis.mediaprocessing.extraction;

import eu.europeana.metis.mediaprocessing.model.UrlType;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Set;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts technical metadata and generates thumbnails for web resources.
 */
public class MediaProcessor implements Closeable {

  enum ResourceType {AUDIO, VIDEO, TEXT, IMAGE, UNKNOWN}

  private static final Logger LOGGER = LoggerFactory.getLogger(MediaProcessor.class);

  private static Tika tika = new Tika();

  private final CommandExecutor commandExecutor;
  private final ImageProcessor imageProcessor;
  private final AudioVideoProcessor audioVideoProcessor;
  private final TextProcessor textProcessor;

  MediaProcessor(CommandExecutor commandExecutor) throws MediaProcessorException {
    this.commandExecutor = commandExecutor;
    final ThumbnailGenerator thumbnailGenerator = new ThumbnailGenerator(commandExecutor);
    this.imageProcessor = new ImageProcessor(thumbnailGenerator);
    this.audioVideoProcessor = new AudioVideoProcessor(commandExecutor);
    this.textProcessor = new TextProcessor(thumbnailGenerator);
  }

  public MediaProcessor(int commandThreadPoolSize) throws MediaProcessorException {
    this(new CommandExecutor(commandThreadPoolSize));
  }

  /**
   * @param contents downloaded file, can be {@code null} for mime types accepted by {@link
   * #supportsLinkProcessing(String)}
   */
  public ResourceExtractionResult processResource(String url, Set<UrlType> urlTypes,
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
      LOGGER
          .info("Invalid mime type provided (should be {}, was {}): {}", mimeType, providedMimeType,
              url);
    }
    if (contents == null && !supportsLinkProcessing(mimeType)) {
      throw new MediaExtractionException("File content is null and mimeType does not support link processing");
    }

    // Process the resource.
    final ResourceExtractionResult result;
    switch (getResourceType(mimeType)) {
      case TEXT:
        result = textProcessor.processText(url, urlTypes, mimeType, contents);
        break;
      case AUDIO:
      case VIDEO:
        result = audioVideoProcessor
            .processAudioVideo(url, urlTypes, mimeType, contents);
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
  public void close() {
    commandExecutor.shutdown();
  }

  /**
   * @return if true, resources of given type don't need to be downloaded before processing.
   */
  public static boolean supportsLinkProcessing(String mimeType) {
    // TODO also when type is UNKNOWN! So that we don't download something that is not processable.
    final ResourceType resourceType = getResourceType(mimeType);
    return ResourceType.AUDIO == resourceType || ResourceType.VIDEO == resourceType;
  }

  static void setTika(Tika tika) {
    MediaProcessor.tika = tika;
  }
}
