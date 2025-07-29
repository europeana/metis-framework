package eu.europeana.indexing.utils;

import static eu.europeana.metis.network.ExternalRequestUtil.retryableExternalRequestForNetworkExceptionsThrowing;

import eu.europeana.indexing.common.exception.IndexerRelatedIndexingException;
import java.util.Map;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.MapSolrParams;

/**
 * Utilities for Solr usage.
 */
public final class SolrUtils {

  private static final String SOLR_SERVER_SEARCH_ERROR = "Could not search Solr server.";

  private SolrUtils() {
  }

  /**
   * Query for Solr documents using the given query parameters.
   *
   * @param solrClient    The Solr client.
   * @param queryParamMap The key-value map containing the query.
   * @return A list of matching documents.
   * @throws IndexerRelatedIndexingException In case of issues.
   */
  public static SolrDocumentList getSolrDocuments(SolrClient solrClient,
      Map<String, String> queryParamMap) throws IndexerRelatedIndexingException {
    final MapSolrParams queryParams = new MapSolrParams(queryParamMap);
    final QueryResponse response;
    try {
      response = retryableExternalRequestForNetworkExceptionsThrowing(() -> solrClient.query(queryParams));
    } catch (Exception e) {
      throw new IndexerRelatedIndexingException(SOLR_SERVER_SEARCH_ERROR, e);
    }
    return response.getResults();
  }
}
