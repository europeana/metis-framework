package eu.europeana.metis.mediaprocessing.extraction;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.extraction.iiif.IIIFInfoJson;
import eu.europeana.metis.mediaprocessing.model.IIIFResource;
import eu.europeana.metis.mediaprocessing.model.ImageResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.RdfResourceKind;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
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
   * Resource iiif post processing.
   *
   * @param resultToPostProcess The result to post process.
   * @param rdfResourceEntry The RDF resource entry.
   * @return The post processed result.
   * @throws MediaExtractionException In case something went wrong during the post processing.
   */
  public static ResourceExtractionResult resourcePostProcessing(ResourceExtractionResult resultToPostProcess,
      RdfResourceEntry rdfResourceEntry) throws MediaExtractionException {
    if (resultToPostProcess != null && rdfResourceEntry.getResourceKind().equals(RdfResourceKind.IIIF)) {
      ImageResourceMetadata thumbnailMetadata = (ImageResourceMetadata) ((ResourceExtractionResultImpl) resultToPostProcess).getOriginalMetadata();
      ImageResourceMetadata imageResourceMetadata = new ImageResourceMetadata(thumbnailMetadata.getMimeType(),
          rdfResourceEntry.getResourceUrl(),
          thumbnailMetadata.getContentSize(),
          thumbnailMetadata.getWidth(),
          thumbnailMetadata.getHeight(),
          thumbnailMetadata.getColorSpace(),
          thumbnailMetadata.getDominantColors()
                           .stream()
                           .map(colorName -> colorName.replace("#", ""))
                           .toList(),
          thumbnailMetadata.getThumbnailTargetNames()
                           .stream()
                           .map(thumbnailName ->
                               new ThumbnailImpl(thumbnailMetadata.getResourceUrl(),
                                   thumbnailMetadata.getMimeType(),
                                   thumbnailName))
                           .toList());
      return new ResourceExtractionResultImpl(imageResourceMetadata);
    } else {
      return resultToPostProcess;
    }
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

    // Check: if this is not a IIIF resource, we can't process it, otherwise yes.
    if (resource instanceof IIIFResource iiifResource && iiifResource.getIIIFInfoJson() != null) {
      final IIIFInfoJson infoJson = iiifResource.getIIIFInfoJson();
      // Process the smaller thumbnail resource as if it were the real image.
      ResourceExtractionResultImpl extractionResult = super.extractMetadata(resource, detectedMimeType, mainThumbnailAvailable);
      ImageResourceMetadata metadata = (ImageResourceMetadata) extractionResult.getOriginalMetadata();
      List<Thumbnail> thumbnailList =
          metadata.getThumbnailTargetNames()
                  .stream()
                  .map(name -> (Thumbnail) new ThumbnailImpl(resource.getResourceUrl(), detectedMimeType, name))
                  .toList();

      // Then adjust with the information from the info.json.
      ImageResourceMetadata imageMetadata = new ImageResourceMetadata(metadata.getMimeType(),
          resource.getResourceUrl(),
          metadata.getContentSize(),
          infoJson.getWidth(),
          infoJson.getHeight(),
          metadata.getColorSpace(),
          metadata.getDominantColors().stream().map(item -> item.replace("#", "")).toList(),
          thumbnailList);
      return new ResourceExtractionResultImpl(imageMetadata, thumbnailList);
    } else {
      return null;
    }
  }
}
