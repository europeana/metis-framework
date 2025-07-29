package eu.europeana.indexing.search.v2;

import static eu.europeana.metis.network.ExternalRequestUtil.retryableExternalRequestForNetworkExceptions;

import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.common.contract.IndexerForSearching;
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
import eu.europeana.indexing.utils.SolrUtils;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.solr.client.CompoundSolrClient;
import eu.europeana.metis.solr.connection.SolrClientProvider;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
public class IndexerForSearchingV2 implements IndexerForSearching {

  private static final String SOLR_SERVER_PUBLISH_ERROR = "Could not publish to Solr server.";
  private static final String SOLR_SERVER_PUBLISH_RETRY_ERROR = "Could not publish to Solr server after retry.";

  private final Supplier<RdfToFullBeanConverter> fullBeanConverterSupplier;
  private final CompoundSolrClient solrClientToClose;
  private final SolrClient solrClient;
  private final RDFDeserializer rdfDeserializer = new RDFDeserializer();

  /**
   * Constructor.
   *
   * @param solrClientProvider The searchable persistence. Clients that are provided from this
   *                           object will be closed when this instance's {@link #close()}
   *                           method is called.
   * @throws SetupRelatedIndexingException In the case of setup issues.
   */
  public IndexerForSearchingV2(SolrClientProvider<SetupRelatedIndexingException> solrClientProvider)
      throws SetupRelatedIndexingException {
    this.solrClientToClose = solrClientProvider.createSolrClient();
    this.solrClient = solrClientToClose.getSolrClient();
    this.fullBeanConverterSupplier = RdfToFullBeanConverter::new;
  }

  /**
   * Constructor for testing purposes.
   *
   * @param solrClient The searchable persistence. This instance will not take responsibility for
   *                   closing this client.
   * @param fullBeanConverterSupplier Supplies an instance of {@link RdfToFullBeanConverter} used to
   * parse strings to instances of {@link FullBeanImpl}.
   */
  public IndexerForSearchingV2(SolrClient solrClient,
      Supplier<RdfToFullBeanConverter> fullBeanConverterSupplier) {
    this.solrClientToClose = null;
    this.solrClient = solrClient;
    this.fullBeanConverterSupplier = fullBeanConverterSupplier;
  }

  @Override
  public void indexForSearching(String rdfRecord) throws IndexingException {
    Objects.requireNonNull(rdfRecord, "record is null");
    indexForSearching(rdfDeserializer.convertToRdf(rdfRecord));
  }

  @Override
  public void indexForSearching(RDF rdfRecord) throws IndexingException {
    Objects.requireNonNull(rdfRecord, "record is null");
    indexForSearching(new RdfWrapper(rdfRecord), false, null);
  }

  @Override
  public void indexForSearching(RdfWrapper rdfWrapper, boolean preserveUpdateAndCreateTimesFromRdf,
      Date updatedDate) throws IndexingException {
    Objects.requireNonNull(rdfWrapper, "rdfWrapper is null");
    final FullBeanImpl fullBean = convertRDFToFullBean(rdfWrapper);
    if (!preserveUpdateAndCreateTimesFromRdf) {
      Date createdDate;
      if (rdfWrapper.getAbout() == null) {
        createdDate = updatedDate;
      } else {
        final String solrQuery = String.format("%s:\"%s\"", SolrV2Field.EUROPEANA_ID,
            ClientUtils.escapeQueryChars(rdfWrapper.getAbout()));
        final Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("q", solrQuery);
        queryParamMap.put("fl", SolrV2Field.TIMESTAMP_CREATED + "," + SolrV2Field.EUROPEANA_ID);
        SolrDocumentList solrDocuments = SolrUtils.getSolrDocuments(solrClient, queryParamMap);
        createdDate = (Date) solrDocuments.stream()
            .map(document -> document.getFieldValue(SolrV2Field.TIMESTAMP_CREATED.toString()))
            .toList().stream().findFirst().orElse(updatedDate);
      }
      RecordDateUtils.setUpdateAndCreateTime(fullBean, updatedDate, createdDate);
    }
    indexForSearching(rdfWrapper, fullBean);
  }

  @Override
  public void indexForSearching(RdfWrapper rdfWrapper, Date updatedDate, Date createdDate)
      throws IndexingException {
    Objects.requireNonNull(rdfWrapper, "rdfWrapper is null");
    final FullBeanImpl fullBean = convertRDFToFullBean(rdfWrapper);
    RecordDateUtils.setUpdateAndCreateTime(fullBean, updatedDate, createdDate);
    indexForSearching(rdfWrapper, fullBean);
  }

  private void indexForSearching(RdfWrapper rdf, FullBeanImpl savedFullBean)
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

  @Override
  public void close() throws IOException {
    if (this.solrClientToClose != null) {
      this.solrClientToClose.close();
    }
  }
}

