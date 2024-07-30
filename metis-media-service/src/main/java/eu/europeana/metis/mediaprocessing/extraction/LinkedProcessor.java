package eu.europeana.metis.mediaprocessing.extraction;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkedProcessor implements MediaProcessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(LinkedProcessor.class);

  private List<MediaProcessor> mediaProcessors;

  public LinkedProcessor(List<MediaProcessor> mediaProcessorList) {
    this.mediaProcessors = mediaProcessorList;
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
    LOGGER.info("Extracting metadata for resource {}", resource);
    Iterator<MediaProcessor> iterator = mediaProcessors.iterator();
    while (iterator.hasNext()) {
      MediaProcessor mediaProcessor = iterator.next();

    }
    return null;
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
    LOGGER.info("Copying metadata for resource {}", resource);
    return null;
  }

  /**
   * @return Whether the processor needs the downloaded resource for full processing.
   */
  @Override
  public boolean downloadResourceForFullProcessing() {

    return true;
  }

}
