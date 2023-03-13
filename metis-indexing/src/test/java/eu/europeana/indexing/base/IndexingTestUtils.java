package eu.europeana.indexing.base;

import eu.europeana.indexing.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.exception.RecordRelatedIndexingException;
import java.io.IOException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

public class IndexingTestUtils {

  private static final String SOLR_SERVER_SEARCH_ERROR = "Could not search Solr server.";

  public static String getResourceFileContent(String fileName) {
    try {
      return new String(IndexingTestUtils.class.getClassLoader().getResourceAsStream(fileName).readAllBytes());
    } catch (IOException ioException) {
      return "";
    }
  }

  public static SolrDocumentList getSolrDocuments(SolrClient solrServer, String query)
      throws IndexerRelatedIndexingException, RecordRelatedIndexingException {
    SolrQuery solrQuery = new SolrQuery();
    solrQuery.set("q", query);
    QueryResponse response;
    try {
      response = solrServer.query(solrQuery);
    } catch (SolrServerException e) {
      throw new IndexerRelatedIndexingException(SOLR_SERVER_SEARCH_ERROR, e);
    } catch (IOException e) {
      throw new RecordRelatedIndexingException(SOLR_SERVER_SEARCH_ERROR, e);
    }
    return response.getResults();
  }
}
