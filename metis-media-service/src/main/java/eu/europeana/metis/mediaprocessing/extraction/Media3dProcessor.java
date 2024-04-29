package eu.europeana.metis.mediaprocessing.extraction;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.model.Media3dResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResultImpl;

class Media3dProcessor implements MediaProcessor {

  @Override
  public ResourceExtractionResult extractMetadata(Resource resource, String detectedMimeType, boolean mainThumbnailAvailable)
      throws MediaExtractionException {
    // Set the metadata in the web resource.
    final Media3dResourceMetadata resourceMetadata;
    resourceMetadata = new Media3dResourceMetadata(detectedMimeType, resource.getResourceUrl(), resource.getProvidedFileSize());

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
    return false;
  }
}
