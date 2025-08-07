package eu.europeana.indexing.common.contract;

import eu.europeana.indexing.common.exception.IndexingException;
import eu.europeana.indexing.utils.RdfWrapper;
import java.util.Date;
import java.util.List;

/**
 * Implementations of this interface find and persist redirections between EDM records.
 */
public interface RedirectPersistence extends Persistence {

  /**
   * Perform redirection.
   * @param rdf The record to perform redirection for.
   * @param redirectDate The date of any new redirect record added.
   * @param datasetIdsToRedirectFrom The list of dataset IDs we will search in addition to the
   *                                 record's own dataset.
   * @return The created date of the earliest record match that was found.
   * @throws IndexingException In case of issues.
   */
  Date performRedirection(RdfWrapper rdf, Date redirectDate, List<String> datasetIdsToRedirectFrom)
      throws IndexingException;
}
