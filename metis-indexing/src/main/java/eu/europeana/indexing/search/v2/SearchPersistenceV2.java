package eu.europeana.indexing.search.v2;

import static eu.europeana.metis.network.ExternalRequestUtil.retryableExternalRequestForNetworkExceptions;
import static eu.europeana.metis.network.ExternalRequestUtil.retryableExternalRequestForNetworkExceptionsThrowing;

import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.common.contract.SearchPersistence;
import eu.europeana.indexing.common.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.common.exception.IndexingException;
import eu.europeana.indexing.common.exception.PublishToSolrIndexingException;
import eu.europeana.indexing.common.exception.RecordRelatedIndexingException;
import eu.europeana.indexing.common.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.common.fullbean.RdfToFullBeanConverter;
import eu.europeana.indexing.common.persistence.solr.v2.SolrV2Field;
import eu.europeana.indexing.utils.RDFDeserializer;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.RecordDateUtils;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.solr.client.CompoundSolrClient;
import eu.europeana.metis.solr.connection.SolrClientProvider;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.MapSolrParams;

/**
 * This class implements search persistence using the record Solr V2.
 */
public class SearchPersistenceV2 implements SearchPersistence<SolrDocument, SolrDocumentList> {

  private static final String SOLR_SERVER_PUBLISH_ERROR = "Could not publish to Solr server.";
  private static final String SOLR_SERVER_PUBLISH_RETRY_ERROR = "Could not publish to Solr server after retry.";
  private static final String SOLR_SERVER_SEARCH_ERROR = "Could not search Solr server.";

  private static final String NULL_RECORD_MESSAGE = "record is null";

  private final CompoundSolrClient solrClientToClose;
  private final SolrClient solrClient;
  private final RDFDeserializer rdfDeserializer = new RDFDeserializer();

  /**
   * Constructor.
   *
   * @param solrClientProvider Provider for searchable persistence. Clients that are provided from
   *                           this object will be closed when this instance's {@link #close()}
   *                           method is called.
   * @throws SetupRelatedIndexingException In the case of setup issues.
   */
  public SearchPersistenceV2(SolrClientProvider<SetupRelatedIndexingException> solrClientProvider)
      throws SetupRelatedIndexingException {
    this.solrClientToClose = solrClientProvider.createSolrClient();
    this.solrClient = solrClientToClose.getSolrClient();
  }

  /**
   * Constructor.
   *
   * @param solrClient The searchable persistence. Note: this instance will not take responsibility
   *                   for closing this client.
   */
  public SearchPersistenceV2(SolrClient solrClient) {
    this.solrClientToClose = null;
    this.solrClient = solrClient;
  }

  @Override
  public SolrDocumentList search(Map<String, String> queryParamMap)
      throws IndexerRelatedIndexingException {
    final MapSolrParams queryParams = new MapSolrParams(queryParamMap);
    final QueryResponse response;
    try {
      response = retryableExternalRequestForNetworkExceptionsThrowing(() -> solrClient.query(queryParams));
    } catch (Exception e) {
      throw new IndexerRelatedIndexingException(SOLR_SERVER_SEARCH_ERROR, e);
    }
    return response.getResults();
  }

  @Override
  public void indexForSearch(String rdfRecord) throws IndexingException {
    Objects.requireNonNull(rdfRecord, NULL_RECORD_MESSAGE);
    indexForSearch(rdfDeserializer.convertToRdf(rdfRecord));
  }

  @Override
  public void indexForSearch(RDF rdfRecord) throws IndexingException {
    Objects.requireNonNull(rdfRecord, NULL_RECORD_MESSAGE);
    indexForSearch(new RdfWrapper(rdfRecord), false, null);
  }

  @Override
  public void indexForSearch(RdfWrapper rdfRecord, boolean preserveUpdateAndCreateTimesFromRdf,
      Date updatedDate) throws IndexingException {
    Objects.requireNonNull(rdfRecord, NULL_RECORD_MESSAGE);
    final FullBeanImpl fullBean = convertRDFToFullBean(rdfRecord);
    if (!preserveUpdateAndCreateTimesFromRdf) {
      Date createdDate;
      if (rdfRecord.getAbout() == null) {
        createdDate = updatedDate;
      } else {
        final String solrQuery = String.format("%s:\"%s\"", SolrV2Field.EUROPEANA_ID,
            ClientUtils.escapeQueryChars(rdfRecord.getAbout()));
        final Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("q", solrQuery);
        queryParamMap.put("fl", SolrV2Field.TIMESTAMP_CREATED + "," + SolrV2Field.EUROPEANA_ID);
        SolrDocumentList solrDocuments = search(queryParamMap);
        createdDate = (Date) solrDocuments.stream()
            .map(document -> document.getFieldValue(SolrV2Field.TIMESTAMP_CREATED.toString()))
            .toList().stream().findFirst().orElse(updatedDate);
      }
      RecordDateUtils.setUpdateAndCreateTime(fullBean, updatedDate, createdDate);
    }
    indexForSearch(rdfRecord, fullBean);
  }

  @Override
  public void indexForSearch(RdfWrapper rdfWrapper, Date updatedDate, Date createdDate)
      throws IndexingException {
    Objects.requireNonNull(rdfWrapper, "rdfWrapper is null");
    final FullBeanImpl fullBean = convertRDFToFullBean(rdfWrapper);
    RecordDateUtils.setUpdateAndCreateTime(fullBean, updatedDate, createdDate);
    indexForSearch(rdfWrapper, fullBean);
  }

  private void indexForSearch(RdfWrapper rdf, FullBeanImpl savedFullBean)
      throws RecordRelatedIndexingException {
    try {
      retryableExternalRequestForNetworkExceptions(() -> {
        try {
          publishToSolr(rdf, savedFullBean);
        } catch (IndexingException e) {
          throw new PublishToSolrIndexingException(SOLR_SERVER_PUBLISH_ERROR, e);
        }
        return null;
      });
    } catch (RuntimeException e) {
      throw new RecordRelatedIndexingException(SOLR_SERVER_PUBLISH_RETRY_ERROR, e);
    }
  }

  private FullBeanImpl convertRDFToFullBean(RdfWrapper rdf) {
    final RdfToFullBeanConverter fullBeanConverter = new RdfToFullBeanConverter();
    return fullBeanConverter.convertRdfToFullBean(rdf);
  }

  private void publishToSolr(RdfWrapper rdfWrapper, FullBeanImpl fullBean) throws IndexingException {

    // Create Solr document.
    final SolrDocumentPopulator documentPopulator = new SolrDocumentPopulator();
    final SolrInputDocument document = new SolrInputDocument();
    documentPopulator.populateWithProperties(document, fullBean);
    documentPopulator.populateWithFacets(document, rdfWrapper);
    documentPopulator.populateWithDateRanges(document, rdfWrapper);

    // Save Solr document.
    try {
      solrClient.add(document);
    } catch (IOException e) {
      throw new IndexerRelatedIndexingException(SOLR_SERVER_PUBLISH_ERROR, e);
    } catch (SolrServerException | RuntimeException e) {
      throw new RecordRelatedIndexingException(SOLR_SERVER_PUBLISH_ERROR, e);
    }
  }

  @Override
  public void close() throws IOException {
    if (this.solrClientToClose != null) {
      this.solrClientToClose.close();
    }
  }
}

