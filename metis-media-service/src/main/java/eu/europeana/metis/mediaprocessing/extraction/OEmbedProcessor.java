package eu.europeana.metis.mediaprocessing.extraction;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.extraction.oembed.OEmbedModel;
import eu.europeana.metis.mediaprocessing.model.ImageResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResultImpl;
import eu.europeana.metis.mediaprocessing.model.VideoResourceMetadata;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Oembed processor.
 */
public class OEmbedProcessor implements MediaProcessor {

  /**
   * The constant LOGGER.
   */
  public static final Logger LOGGER = LoggerFactory.getLogger(OEmbedProcessor.class);

  private MediaProcessor nextProcessor;

  /**
   * Gets oembed model from json.
   *
   * @param jsonResource byte[]
   * @return the oembed model from json
   * @throws IOException the io exception
   */
  public static OEmbedModel getOEmbedModelfromJson(byte[] jsonResource) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return objectMapper.readValue(jsonResource, OEmbedModel.class);
  }

  /**
   * Gets oembed model from xml.
   *
   * @param xmlResource byte[]
   * @return the oembed model from xml
   * @throws IOException the io exception
   */
  public static OEmbedModel getOEmbedModelfromXml(byte[] xmlResource) throws IOException {
    XmlMapper xmlMapper = new XmlMapper();
    xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return xmlMapper.readValue(xmlResource, OEmbedModel.class);
  }

  /**
   * Is o embed boolean.
   *
   * @param oEmbedModel the o embed model
   * @return the boolean
   */
  public static boolean isOEmbed(OEmbedModel oEmbedModel) {
    return oEmbedModel != null
        && (oEmbedModel.getVersion() != null
        && oEmbedModel.getVersion().startsWith("1.0"))
        && (oEmbedModel.getType() != null
        && (oEmbedModel.getType().equalsIgnoreCase("photo")
        || oEmbedModel.getType().equalsIgnoreCase("video")))
        && (oEmbedModel.getWidth() > 0 && oEmbedModel.getHeight() > 0);
  }

  private static Double getDurationFromModel(OEmbedModel oEmbedModel) {
    Double duration;
    try {
      duration = Double.parseDouble(oEmbedModel.getDuration());
    } catch (NumberFormatException e) {
      duration = 0.0;
    }
    return duration;
  }

  /**
   * Process a resource by extracting the metadata from the content.
   *
   * @param resource The resource to process. Note that the resource may not have content (see
   * {@link MediaExtractorImpl#shouldDownloadForFullProcessing(String)}).
   * @param detectedMimeType The mime type that was detected for this resource (may deviate from the mime type that was provided
   * by the server and which is stored in {@link Resource#getProvidedMimeType()}).
   * @param mainThumbnailAvailable Whether the main thumbnail for this record is available. This may influence the decision on
   * whether to generate a thumbnail for this resource.
   * @return The result of the processing.
   * @throws MediaExtractionException In case something went wrong during the extraction.
   */
  @Override
  public ResourceExtractionResult extractMetadata(Resource resource, String detectedMimeType, boolean mainThumbnailAvailable)
      throws MediaExtractionException {

    ResourceExtractionResult resourceExtractionResult = null;
    if (resource.getContentPath() != null) {
      try {
        OEmbedModel embedModel = null;
        if (detectedMimeType.startsWith("application/json")) {
          embedModel = getOEmbedModelfromJson(Files.readAllBytes(Paths.get(resource.getContentPath().toString())));
        } else if (detectedMimeType.startsWith("application/xml")) {
          embedModel = getOEmbedModelfromXml(Files.readAllBytes(Paths.get(resource.getContentPath().toString())));
        }
        if (isOEmbed(embedModel)) {
          resourceExtractionResult = getResourceExtractionResult(resource, detectedMimeType, mainThumbnailAvailable,
              embedModel);
        } else {
          LOGGER.warn("No oembed model found");
          resourceExtractionResult = this.nextProcessor.extractMetadata(resource, detectedMimeType, mainThumbnailAvailable);
        }
      } catch (IOException e) {
        throw new MediaExtractionException("Unable to read OEmbedded resource", e);
      }
    }

    return resourceExtractionResult;
  }

  /**
   * Process a resource by copying the metadata from the input without performing any extraction.
   *
   * @param resource The resource to process. The resource is not expected to have content.
   * @param detectedMimeType The mime type that was detected for this resource (may deviate from the mime type that was provided
   * by the server and which is stored in {@link Resource#getProvidedMimeType()}).
   * @return The result of the processing.
   * @throws MediaExtractionException In case something went wrong during the extraction.
   */
  @Override
  public ResourceExtractionResult copyMetadata(Resource resource, String detectedMimeType) throws MediaExtractionException {
    if (detectedMimeType.startsWith("application/json+oembed") || detectedMimeType.startsWith("application/xml+oembed")) {
      return null;
    } else {
      return this.nextProcessor.copyMetadata(resource, detectedMimeType);
    }
  }

  /**
   * @return Whether the processor needs the downloaded resource for full processing.
   */
  @Override
  public boolean downloadResourceForFullProcessing() {
    return true;
  }

  /**
   * This creates structure to enable a chain of media processors
   *
   * @param nextProcessable next media processor in the chain
   */
  @Override
  public void setNextProcessor(MediaProcessor nextProcessable) {
    this.nextProcessor = nextProcessable;
  }

  private ResourceExtractionResult getResourceExtractionResult(Resource resource, String detectedMimeType,
      boolean mainThumbnailAvailable, OEmbedModel oEmbedModel) throws MediaExtractionException {
    ResourceExtractionResult resourceExtractionResult;
    switch (oEmbedModel.getType().toLowerCase()) {
      case "photo" -> {
        ImageResourceMetadata imageResourceMetadata = new ImageResourceMetadata(detectedMimeType,
            resource.getResourceUrl(),
            resource.getProvidedFileSize(), oEmbedModel.getWidth(), oEmbedModel.getHeight(), null, null, null);
        resourceExtractionResult = new ResourceExtractionResultImpl(imageResourceMetadata);
      }
      case "video" -> {
        Double duration = getDurationFromModel(oEmbedModel);
        VideoResourceMetadata videoResourceMetadata = new VideoResourceMetadata(detectedMimeType,
            resource.getResourceUrl(),
            resource.getProvidedFileSize(), duration, null, oEmbedModel.getWidth(), oEmbedModel.getHeight(), null, null);
        resourceExtractionResult = new ResourceExtractionResultImpl(videoResourceMetadata);
      }
      default ->
          resourceExtractionResult = this.nextProcessor.extractMetadata(resource, detectedMimeType, mainThumbnailAvailable);
    }
    return resourceExtractionResult;
  }
}
