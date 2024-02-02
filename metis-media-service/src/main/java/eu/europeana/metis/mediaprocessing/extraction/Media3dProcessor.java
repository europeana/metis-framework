package eu.europeana.metis.mediaprocessing.extraction;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResultImpl;
import eu.europeana.metis.mediaprocessing.model.Media3dResourceMetadata;
import java.io.IOException;

class Media3dProcessor implements MediaProcessor{

  @Override
  public ResourceExtractionResult extractMetadata(Resource resource, String detectedMimeType, boolean mainThumbnailAvailable)
      throws MediaExtractionException {

    // Sanity check
    try {
      if (!resource.hasContent()) {
        throw new MediaExtractionException("File does not exist or does not have content.");
      }
    } catch (IOException e) {
      throw new MediaExtractionException("Could not determine whether resource has content.", e);
    }

    // Get the size of the resource
    final Long contentSize;
    try {
      contentSize = nullIfNegative(resource.getContentSize());
    } catch (IOException e) {
      throw new MediaExtractionException(
          "Could not determine the size of the resource " + resource.getResourceUrl(), e);
    }

    // Set the metadata in the web resource.
    final Media3dResourceMetadata resourceMetadata;
    resourceMetadata = new Media3dResourceMetadata(detectedMimeType, resource.getResourceUrl(), contentSize);

    // Done.
    return new ResourceExtractionResultImpl(resourceMetadata, null);
  }

  @Override
  public ResourceExtractionResult copyMetadata(Resource resource, String detectedMimeType) throws MediaExtractionException {
    return new ResourceExtractionResultImpl(new Media3dResourceMetadata(detectedMimeType,
        resource.getResourceUrl(), resource.getProvidedFileSize()));
  }

  @Override
  public boolean downloadResourceForFullProcessing() {
    return true;
  }
}
