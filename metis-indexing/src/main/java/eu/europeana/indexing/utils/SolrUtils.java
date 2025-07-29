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

  public static SolrDocumentList getSolrDocuments(SolrClient solrClient,
      Map<String, String> queryParamMap) throws IndexerRelatedIndexingException {
    MapSolrParams queryParams = new MapSolrParams(queryParamMap);
    QueryResponse response;
    try {
      response = retryableExternalRequestForNetworkExceptionsThrowing(() -> solrClient.query(queryParams));
    } catch (Exception e) {
      throw new IndexerRelatedIndexingException(SOLR_SERVER_SEARCH_ERROR, e);
    }
    return response.getResults();
  }
}
