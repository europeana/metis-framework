package eu.europeana.metis.mediaprocessing.extraction;

import static eu.europeana.metis.mediaprocessing.extraction.oembed.OEmbedValidation.checkValidWidthAndHeightDimensions;
import static eu.europeana.metis.mediaprocessing.extraction.oembed.OEmbedValidation.getDurationFromModel;
import static eu.europeana.metis.mediaprocessing.extraction.oembed.OEmbedValidation.getOEmbedModelFromJson;
import static eu.europeana.metis.mediaprocessing.extraction.oembed.OEmbedValidation.getOEmbedModelFromXml;
import static eu.europeana.metis.mediaprocessing.extraction.oembed.OEmbedValidation.hasValidVersion;
import static eu.europeana.metis.mediaprocessing.extraction.oembed.OEmbedValidation.isValidTypePhoto;
import static eu.europeana.metis.mediaprocessing.extraction.oembed.OEmbedValidation.isValidTypeVideo;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.extraction.oembed.OEmbedModel;
import eu.europeana.metis.mediaprocessing.model.GenericResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.ImageResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResultImpl;
import eu.europeana.metis.mediaprocessing.model.VideoResourceMetadata;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Oembed processor.
 */
public class OEmbedProcessor implements MediaProcessor {

  /**
   * The constant LOGGER.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(OEmbedProcessor.class);

  /**
   * Process a resource by extracting the metadata from the content.
   *
   * @param resource The resource to process. Note that the resource may not have content (see
   * {@link MediaExtractorImpl#shouldDownloadForFullProcessing(String, boolean)}).
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

    final ResourceExtractionResult resourceExtractionResult;
    // the content for this oembed needs to be downloaded to be examined
    if (resource.getContentPath() != null) {
      try {

        // Parse the model.
        final OEmbedModel embedModel;
        final String oEmbedMimetype;
        if (detectedMimeType.startsWith("application/json")) {
          embedModel = getOEmbedModelFromJson(Files.readAllBytes(Paths.get(resource.getContentPath().toString())));
          oEmbedMimetype = "application/json+oembed";
        } else if (detectedMimeType.startsWith("application/xml") || detectedMimeType.startsWith("text/xml")) {
          embedModel = getOEmbedModelFromXml(Files.readAllBytes(Paths.get(resource.getContentPath().toString())));
          oEmbedMimetype = "application/xml+oembed";
        } else {
          embedModel = null;
          oEmbedMimetype = null;
        }

        // Validate model and compile the extraction result.
        if (hasValidVersion(embedModel)) {
          resourceExtractionResult = getResourceExtractionResult(resource, oEmbedMimetype, embedModel);
        } else {
          LOGGER.info("No oembed model found.");
          resourceExtractionResult = null;
        }

      } catch (IOException e) {
        throw new MediaExtractionException("Unable to read OEmbedded resource", e);
      }
    } else {
      resourceExtractionResult = null;
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
    return null;
  }

  /**
   * @return Whether the processor needs the downloaded resource for full processing.
   */
  @Override
  public boolean downloadResourceForFullProcessing() {
    return true;
  }

  private ResourceExtractionResult getResourceExtractionResult(Resource resource, String oEmbedMimetype,
      OEmbedModel oEmbedModel) throws MediaExtractionException {
    ResourceExtractionResult resourceExtractionResult = null;
    if (oEmbedModel != null) {
      final String oEmbedType = oEmbedModel.getType().toLowerCase(Locale.US);
      if ("photo".equals(oEmbedType) && isValidTypePhoto(oEmbedModel)) {
        checkValidWidthAndHeightDimensions(oEmbedModel, resource.getResourceUrl());
        ImageResourceMetadata imageResourceMetadata = new ImageResourceMetadata(oEmbedMimetype,
            resource.getResourceUrl(), null,
            oEmbedModel.getWidth(), oEmbedModel.getHeight(), null, null, null);
        resourceExtractionResult = new ResourceExtractionResultImpl(imageResourceMetadata);
      } else if ("video".equals(oEmbedType) && isValidTypeVideo(oEmbedModel)) {
        checkValidWidthAndHeightDimensions(oEmbedModel, resource.getResourceUrl());
        Double duration = getDurationFromModel(oEmbedModel);
        VideoResourceMetadata videoResourceMetadata = new VideoResourceMetadata(oEmbedMimetype,
            resource.getResourceUrl(), null, duration, null,
            oEmbedModel.getWidth(), oEmbedModel.getHeight(), null, null);
        resourceExtractionResult = new ResourceExtractionResultImpl(videoResourceMetadata);
      } else {
        GenericResourceMetadata genericResourceMetadata = new GenericResourceMetadata(oEmbedMimetype,
            resource.getResourceUrl(), null);
        resourceExtractionResult = new ResourceExtractionResultImpl(genericResourceMetadata);
      }
    }
    return resourceExtractionResult;
  }
}
