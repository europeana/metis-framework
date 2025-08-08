package eu.europeana.indexing.common.contract;

import eu.europeana.indexing.common.exception.IndexerRelatedIndexingException;
import java.util.Collection;
import java.util.Map;

/**
 * Implementations of this interface access, index and query EDM records in a search database.
 *
 * @param <S> A search match object as returned by querying using this persistence access.
 * @param <L> A collection of search match objects as returned by querying using this persistence
 *           access.
 */
public interface QueryableSearchPersistence<S, L extends Collection<S>> extends SearchPersistence {

  /**
   * Perform a search using the given query parameters.
   *
   * @param queryParamMap The key-value map containing the query.
   * @return A search result.
   * @throws IndexerRelatedIndexingException In case of issues.
   */
  L search(Map<String, String> queryParamMap) throws IndexerRelatedIndexingException;

}
