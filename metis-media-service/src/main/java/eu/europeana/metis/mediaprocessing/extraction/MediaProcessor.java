package eu.europeana.metis.mediaprocessing.extraction;

import java.io.File;
import java.util.Set;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import eu.europeana.metis.mediaprocessing.model.UrlType;

/**
 * Implementations of this interface are capable of processing a particular {@link ResourceType}.
 */
interface MediaProcessor {

  /**
   * Process a resource.
   * 
   * @param url The resource URL.
   * @param urlTypes The link types that link to the resource.
   * @param mimeType The mime type of the resource.
   * @param content The (downloaded) content of the resource: can be null (see
   *        {@link ResourceType#shouldDownloadMimetype(String)}).
   * @return The result of the extraction.
   * @throws MediaExtractionException In case something went wrong during the extraction.
   */
  ResourceExtractionResult process(String url, Set<UrlType> urlTypes, String mimeType, File content)
      throws MediaExtractionException;

}
