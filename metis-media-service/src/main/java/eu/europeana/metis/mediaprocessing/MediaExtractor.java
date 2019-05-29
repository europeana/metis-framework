package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;

/**
 * Implementations of this interface provide the media extraction functionality. This object can be
 * reused multiple times, as the construction of it incurs overhead. Please note that this object is
 * not guaranteed to be thread-safe. Access to this object should be from one thread only, or
 * synchronized/locked.
 */
public interface MediaExtractor extends
    PoolableProcessor<RdfResourceEntry, ResourceExtractionResult, MediaExtractionException> {

  /**
   * Perform media extraction on the given resource link.
   *
   * @param resourceEntry The resource entry (obtained from an RDF)
   * @return A model object containing the result of the extraction and the generated thumbnails.
   * Note that this object can be null in case there is nothing to extract.
   * @throws MediaExtractionException In case of issues occurring during media extraction.
   */
  ResourceExtractionResult performMediaExtraction(RdfResourceEntry resourceEntry)
      throws MediaExtractionException;

  @Override
  default ResourceExtractionResult processTask(RdfResourceEntry input)
      throws MediaExtractionException {
    return performMediaExtraction(input);
  }
}
