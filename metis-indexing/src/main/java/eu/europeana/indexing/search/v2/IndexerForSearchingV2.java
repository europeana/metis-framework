package eu.europeana.indexing.search.v2;

import static eu.europeana.metis.network.ExternalRequestUtil.retryableExternalRequestForNetworkExceptions;

import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.common.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.common.exception.IndexingException;
import eu.europeana.indexing.common.exception.PublishToSolrIndexingException;
import eu.europeana.indexing.common.exception.RecordRelatedIndexingException;
import eu.europeana.indexing.common.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.common.fullbean.RdfToFullBeanConverter;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.RecordDateUtils;
import eu.europeana.indexing.utils.SolrUtils;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

/**
 * Publisher for Full Beans (instances of {@link FullBeanImpl}) that makes them accessible and searchable for external agents.
 *
 * @author jochen
 */
public class IndexerForSearchingV2 {

  private static final String SOLR_SERVER_PUBLISH_ERROR = "Could not publish to Solr server.";
  private static final String SOLR_SERVER_PUBLISH_RETRY_ERROR = "Could not publish to Solr server after retry.";

  private final Supplier<RdfToFullBeanConverter> fullBeanConverterSupplier;

  private final SolrClient solrClient;
  private final boolean preserveUpdateAndCreateTimesFromRdf;

  /**
   * Constructor.
   *
   * @param solrClient The searchable persistence.
   * @param preserveUpdateAndCreateTimesFromRdf This determines whether this publisher will use the updated and created times from
   * the incoming RDFs, or whether it computes its own.
   */
  public IndexerForSearchingV2(SolrClient solrClient, boolean preserveUpdateAndCreateTimesFromRdf) {
    this(solrClient, preserveUpdateAndCreateTimesFromRdf, RdfToFullBeanConverter::new);
  }

  /**
   * Constructor for testing purposes.
   *
   * @param solrClient The searchable persistence.
   * @param preserveUpdateAndCreateTimesFromRdf This determines whether this publisher will use the updated and created times from
   * the incoming RDFs, or whether it computes its own.
   * @param fullBeanConverterSupplier Supplies an instance of {@link RdfToFullBeanConverter} used to parse strings to instances of
   * {@link FullBeanImpl}. Will be called once during every publish.
   */
  public IndexerForSearchingV2(SolrClient solrClient, boolean preserveUpdateAndCreateTimesFromRdf,
      Supplier<RdfToFullBeanConverter> fullBeanConverterSupplier) {
    this.solrClient = solrClient;
    this.fullBeanConverterSupplier = fullBeanConverterSupplier;
    this.preserveUpdateAndCreateTimesFromRdf = preserveUpdateAndCreateTimesFromRdf;
  }

  /**
   * Publishes an RDF to solr server
   *
   * @param rdfWrapper RDF to publish.
   * @param recordDate The date that would represent the created/updated date of a record
   * @throws IndexingException which can be one of:
   * <ul>
   * <li>{@link IndexerRelatedIndexingException} In case an error occurred during publication.</li>
   * <li>{@link SetupRelatedIndexingException} in case an error occurred during indexing setup</li>
   * <li>{@link RecordRelatedIndexingException} in case an error occurred related to record
   * contents</li>
   * </ul>
   */
  public void publishSolr(RdfWrapper rdfWrapper, Date recordDate) throws IndexingException {
    final FullBeanImpl fullBean = convertRDFToFullBean(rdfWrapper);
    if (!preserveUpdateAndCreateTimesFromRdf) {
      Date createdDate;
      if (rdfWrapper.getAbout() == null) {
        createdDate = recordDate;
      } else {
        final String solrQuery = String.format("%s:\"%s\"", EdmLabel.EUROPEANA_ID,
            ClientUtils.escapeQueryChars(rdfWrapper.getAbout()));
        final Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("q", solrQuery);
        queryParamMap.put("fl", EdmLabel.TIMESTAMP_CREATED + "," + EdmLabel.EUROPEANA_ID);
        SolrDocumentList solrDocuments = SolrUtils.getSolrDocuments(solrClient, queryParamMap);
        createdDate = (Date) solrDocuments.stream()
            .map(document -> document.getFieldValue(EdmLabel.TIMESTAMP_CREATED.toString()))
            .toList().stream().findFirst().orElse(recordDate);
      }
      RecordDateUtils.setUpdateAndCreateTime(null, fullBean, recordDate, createdDate);
    }
    publishToSolrFinal(rdfWrapper, fullBean);
  }

  public void publishToSolrFinal(RdfWrapper rdf, FullBeanImpl savedFullBean) throws RecordRelatedIndexingException {
    // Publish to Solr
    try {
      retryableExternalRequestForNetworkExceptions(() -> {
        try {
          publishToSolr(rdf, savedFullBean);
        } catch (IndexingException e) {
          throw new PublishToSolrIndexingException(SOLR_SERVER_PUBLISH_ERROR, e);
        }
        return null;
      });
    } catch (Exception e) {
      throw new RecordRelatedIndexingException(SOLR_SERVER_PUBLISH_RETRY_ERROR, e);
    }
  }

  private FullBeanImpl convertRDFToFullBean(RdfWrapper rdf) {
    // Convert RDF to Full Bean.
    final RdfToFullBeanConverter fullBeanConverter = fullBeanConverterSupplier.get();
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
}

