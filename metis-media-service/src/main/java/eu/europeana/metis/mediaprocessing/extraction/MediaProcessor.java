package eu.europeana.metis.mediaprocessing.extraction;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;

/**
 * Implementations of this interface are capable of processing a particular {@link
 * eu.europeana.metis.utils.MediaType}.
 */
interface MediaProcessor {

  /**
   * Process a resource by extracting the metadata from the content.
   *
   * @param resource The resource to process. Note that the resource may not have content (see
   * {@link MediaExtractorImpl#shouldDownloadForFullProcessing(String)}).
   * @param detectedMimeType The mime type that was detected for this resource (may deviate from the
   * mime type that was provided by the server and which is stored in {@link
   * Resource#getProvidedMimeType()}).
   * @return The result of the processing.
   * @throws MediaExtractionException In case something went wrong during the extraction.
   */
  ResourceExtractionResult extractMetadata(Resource resource, String detectedMimeType)
      throws MediaExtractionException;

  /**
   * Process a resource by copying the metadata from the input without performing any extraction.
   *
   * @param resource The resource to process. The resource is not expected to have content.
   * @param detectedMimeType The mime type that was detected for this resource (may deviate from the
   * mime type that was provided by the server and which is stored in {@link
   * Resource#getProvidedMimeType()}).
   * @return The result of the processing.
   * @throws MediaExtractionException In case something went wrong during the extraction.
   */
  ResourceExtractionResult copyMetadata(Resource resource, String detectedMimeType)
      throws MediaExtractionException;

  /**
   * @return Whether the processor needs the downloaded resource for full processing.
   */
  boolean downloadResourceForFullProcessing();
}
