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
   * @param mainThumbnailAvailable Whether the main thumbnail for this record is available. This may
   * influence the decision on whether to generate a thumbnail for this resource.
   * @return The result of the processing.
   * @throws MediaExtractionException In case something went wrong during the extraction.
   */
  ResourceExtractionResult extractMetadata(Resource resource, String detectedMimeType,
          boolean mainThumbnailAvailable) throws MediaExtractionException;

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

  /**
   * Purges negative values.
   *
   * @param value The value to examine.
   * @return the passed value if the value is positive. Null if the value is strictly negative (or
   * null).
   */
  default Integer nullIfNegative(Integer value) {
    return (value == null || value < 0) ? null : value;
  }

  /**
   * Purges negative values.
   *
   * @param value The value to examine.
   * @return the passed value if the value is positive. Null if the value is strictly negative (or
   * null).
   */
  default Long nullIfNegative(Long value) {
    return (value == null || value < 0) ? null : value;
  }

  /**
   * Purges negative values.
   *
   * @param value The value to examine.
   * @return the passed value if the value is positive. Null if the value is strictly negative (or
   * null).
   */
  default Double nullIfNegative(Double value) {
    return (value == null || value < 0) ? null : value;
  }
}
