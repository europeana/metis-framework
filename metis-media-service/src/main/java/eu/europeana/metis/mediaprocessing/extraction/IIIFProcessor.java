package eu.europeana.metis.mediaprocessing.extraction;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.extraction.iiif.IIIFInfoJson;
import eu.europeana.metis.mediaprocessing.extraction.iiif.IIIFValidation;
import eu.europeana.metis.mediaprocessing.model.ImageResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResultImpl;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import eu.europeana.metis.mediaprocessing.model.ThumbnailImpl;
import java.util.List;

/**
 * The type Iiif processor.
 */
public class IIIFProcessor extends ImageProcessor {

  /**
   * Constructor.
   *
   * @param thumbnailGenerator An object that can generate thumbnails.
   */
  IIIFProcessor(ThumbnailGenerator thumbnailGenerator) {
    super(thumbnailGenerator);
  }

  /**
   * Process a resource by extracting the metadata from the content.
   *
   * @param resource The resource to process. Note that the resource may not have content (see
   * {@link MediaExtractorImpl#shouldDownloadForFullProcessing(String, eu.europeana.metis.mediaprocessing.model.RdfResourceKind)}
   * (String)}).
   * @param detectedMimeType The mime type that was detected for this resource (may deviate from the mime type that was provided
   * by the server and which is stored in {@link Resource#getProvidedMimeType()}).
   * @param mainThumbnailAvailable Whether the main thumbnail for this record is available. This may influence the decision on
   * whether to generate a thumbnail for this resource.
   * @return The result of the processing.
   * @throws MediaExtractionException In case something went wrong during the extraction.
   */
  @Override
  public ResourceExtractionResultImpl extractMetadata(Resource resource, String detectedMimeType, boolean mainThumbnailAvailable)
      throws MediaExtractionException {

    ResourceExtractionResultImpl extractionResult = super.extractMetadata(resource, detectedMimeType, mainThumbnailAvailable);
    ImageResourceMetadata metadata = (ImageResourceMetadata) extractionResult.getOriginalMetadata();
    List<Thumbnail> thumbnailList =
        metadata.getThumbnailTargetNames()
                .stream()
                .map(name -> (Thumbnail) new ThumbnailImpl(resource.getResourceUrl(), detectedMimeType, name))
                .toList();

    IIIFInfoJson infoJson = IIIFValidation.fetchInfoJson(resource.getResourceUrl());

    ImageResourceMetadata imageMetadata = new ImageResourceMetadata(metadata.getMimeType(),
        resource.getResourceUrl(),
        metadata.getContentSize(),
        infoJson.getWidth(),
        infoJson.getHeight(),
        metadata.getColorSpace(),
        metadata.getDominantColors().stream().map(item -> item.replace("#", "")).toList(),
        thumbnailList);

    return new ResourceExtractionResultImpl(imageMetadata, thumbnailList);
  }

}
