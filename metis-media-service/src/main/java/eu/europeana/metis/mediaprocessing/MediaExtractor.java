package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.model.MediaExtractorInput;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;

/**
 * Implementations of this interface provide the media extraction functionality. This object can be
 * reused multiple times, as the construction of it incurs overhead. Please note that this object is
 * not guaranteed to be thread-safe. Access to this object should be from one thread only, or
 * synchronized/locked.
 */
public interface MediaExtractor extends
    PoolableProcessor<MediaExtractorInput, ResourceExtractionResult, MediaExtractionException> {

  /**
   * Perform media extraction on the given resource link.
   *
   * @param resourceEntry The resource entry (obtained from an RDF).
   * @param mainThumbnailAvailable Whether the main thumbnail for this record is available. This may
   * influence the decision on whether to generate a thumbnail for this resource.
   * @return A model object containing the result of the extraction and the generated thumbnails.
   * Note that this object can be null in case there is nothing to extract.
   * @throws MediaExtractionException In case of issues occurring during media extraction.
   */
  ResourceExtractionResult performMediaExtraction(RdfResourceEntry resourceEntry,
          boolean mainThumbnailAvailable) throws MediaExtractionException;

  @Override
  default ResourceExtractionResult processTask(MediaExtractorInput input)
      throws MediaExtractionException {
    return performMediaExtraction(input.getResourceEntry(), input.isMainThumbnailAvailable());
  }
}
