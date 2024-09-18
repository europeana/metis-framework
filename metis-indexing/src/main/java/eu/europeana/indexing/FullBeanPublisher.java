package eu.europeana.indexing;

import static eu.europeana.metis.network.ExternalRequestUtil.retryableExternalRequestForNetworkExceptions;

import com.mongodb.MongoClientException;
import com.mongodb.MongoConfigurationException;
import com.mongodb.MongoIncompatibleDriverException;
import com.mongodb.MongoInternalException;
import com.mongodb.MongoInterruptedException;
import com.mongodb.MongoSecurityException;
import com.mongodb.MongoSocketException;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.beans.IdBean;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.exception.PublishToSolrIndexingException;
import eu.europeana.indexing.exception.RecordRelatedIndexingException;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.fullbean.RdfToFullBeanConverter;
import eu.europeana.indexing.mongo.FullBeanUpdater;
import eu.europeana.indexing.solr.EdmLabel;
import eu.europeana.indexing.solr.SolrDocumentPopulator;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.TriConsumer;
import eu.europeana.metis.mongo.dao.RecordDao;
import eu.europeana.metis.mongo.dao.RecordRedirectDao;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.MapSolrParams;

/**
 * Publisher for Full Beans (instances of {@link FullBeanImpl}) that makes them accessible and searchable for external agents.
 *
 * @author jochen
 */
public class FullBeanPublisher {

  private static final String REDIRECT_PUBLISH_ERROR = "Could not publish the redirection changes.";

  private static final String MONGO_SERVER_PUBLISH_ERROR = "Could not publish to Mongo server.";

  private static final String SOLR_SERVER_PUBLISH_ERROR = "Could not publish to Solr server.";
  private static final String SOLR_SERVER_PUBLISH_RETRY_ERROR = "Could not publish to Solr server after retry.";
  private static final String SOLR_SERVER_SEARCH_ERROR = "Could not search Solr server.";

  private static final TriConsumer<FullBeanImpl, FullBeanImpl, Pair<Date, Date>> EMPTY_PREPROCESSOR = (created, updated, recordDateAndCreationDate) -> {
  };

  private final Supplier<RdfToFullBeanConverter> fullBeanConverterSupplier;

  private final RecordDao recordDao;
  private final RecordDao tombstoneRecordDao;
  private final SolrClient solrServer;
  private final boolean preserveUpdateAndCreateTimesFromRdf;
  private final RecordRedirectDao recordRedirectDao;

  /**
   * Constructor.
   *
   * @param recordDao The Mongo persistence.
   * @param tombstoneRecordDao The mongo tombstone persistence.
   * @param recordRedirectDao The record redirect dao
   * @param solrServer The searchable persistence.
   * @param preserveUpdateAndCreateTimesFromRdf This determines whether this publisher will use the updated and created times from
   * the incoming RDFs, or whether it computes its own.
   */
  FullBeanPublisher(RecordDao recordDao, RecordDao tombstoneRecordDao, RecordRedirectDao recordRedirectDao,
      SolrClient solrServer, boolean preserveUpdateAndCreateTimesFromRdf) {
    this(recordDao, tombstoneRecordDao, recordRedirectDao, solrServer, preserveUpdateAndCreateTimesFromRdf,
        RdfToFullBeanConverter::new);
  }

  /**
   * Constructor for testing purposes.
   *
   * @param recordDao The Mongo persistence.
   * @param tombstoneRecordDao The Mongo persistence.
   * @param recordRedirectDao The record redirect dao
   * @param solrServer The searchable persistence.
   * @param preserveUpdateAndCreateTimesFromRdf This determines whether this publisher will use the updated and created times from
   * the incoming RDFs, or whether it computes its own.
   * @param fullBeanConverterSupplier Supplies an instance of {@link RdfToFullBeanConverter} used to parse strings to instances of
   * {@link FullBeanImpl}. Will be called once during every publish.
   */
  FullBeanPublisher(RecordDao recordDao, RecordDao tombstoneRecordDao, RecordRedirectDao recordRedirectDao,
      SolrClient solrServer, boolean preserveUpdateAndCreateTimesFromRdf,
      Supplier<RdfToFullBeanConverter> fullBeanConverterSupplier) {
    this.recordDao = recordDao;
    this.tombstoneRecordDao = tombstoneRecordDao;
    this.recordRedirectDao = recordRedirectDao;
    this.solrServer = solrServer;
    this.fullBeanConverterSupplier = fullBeanConverterSupplier;
    this.preserveUpdateAndCreateTimesFromRdf = preserveUpdateAndCreateTimesFromRdf;
  }

  private static void setUpdateAndCreateTime(IdBean current, FullBean updated,
      Pair<Date, Date> recordDateAndCreationDate) {
    final Date updatedDate = recordDateAndCreationDate.getLeft() == null ? new Date()
        : recordDateAndCreationDate.getLeft();
    final Date createdDate;
    if (recordDateAndCreationDate.getRight() == null) {
      createdDate = current == null ? updatedDate : current.getTimestampCreated();
    } else {
      createdDate = recordDateAndCreationDate.getRight();
    }
    updated.setTimestampCreated(createdDate);
    updated.setTimestampUpdated(updatedDate);
  }

  /**
   * Publishes an RDF.
   *
   * @param rdf RDF to publish.
   * @param recordDate The date that would represent the created/updated date of a record
   * @param datasetIdsToRedirectFrom The dataset ids that their records need to be redirected
   * @throws IndexingException which can be one of:
   * <ul>
   * <li>{@link IndexerRelatedIndexingException} In case an error occurred during publication.</li>
   * <li>{@link SetupRelatedIndexingException} in case an error occurred during indexing setup</li>
   * <li>{@link RecordRelatedIndexingException} in case an error occurred related to record
   * contents</li>
   * </ul>
   */
  public void publishWithRedirects(RdfWrapper rdf, Date recordDate,
      List<String> datasetIdsToRedirectFrom) throws IndexingException {
    publish(rdf, recordDate, datasetIdsToRedirectFrom, true);
  }

  /**
   * Publishes an RDF.
   *
   * @param rdf RDF to publish.
   * @param recordDate The date that would represent the created/updated date of a record
   * @param datasetIdsToRedirectFrom The dataset ids that their records need to be redirected
   * @throws IndexingException which can be one of:
   * <ul>
   * <li>{@link IndexerRelatedIndexingException} In case an error occurred during publication.</li>
   * <li>{@link SetupRelatedIndexingException} in case an error occurred during indexing setup</li>
   * <li>{@link RecordRelatedIndexingException} in case an error occurred related to record
   * contents</li>
   * </ul>
   */
  public void publish(RdfWrapper rdf, Date recordDate, List<String> datasetIdsToRedirectFrom)
      throws IndexingException {
    publish(rdf, recordDate, datasetIdsToRedirectFrom, false);
  }

  /**
   * Publishes an RDF.
   *
   * @param rdf RDF to publish.
   * @param recordDate The date that would represent the created/updated date of a record
   * @param datasetIdsToRedirectFrom The dataset ids that their records need to be redirected
   * @param performRedirects flag that indicates if redirect should be performed
   * @throws IndexingException which can be one of:
   * <ul>
   * <li>{@link IndexerRelatedIndexingException} In case an error occurred during publication.</li>
   * <li>{@link SetupRelatedIndexingException} in case an error occurred during indexing setup</li>
   * <li>{@link RecordRelatedIndexingException} in case an error occurred related to record
   * contents</li>
   * </ul>
   */
  private void publish(RdfWrapper rdf, Date recordDate, List<String> datasetIdsToRedirectFrom,
      boolean performRedirects) throws IndexingException {

    final FullBeanImpl fullBean = convertRDFToFullBean(rdf);

    final TriConsumer<FullBeanImpl, FullBeanImpl, Pair<Date, Date>> fullBeanPreprocessor = providePreprocessor();

    final List<Pair<String, Date>> recordsForRedirection = performRedirection(rdf,
        recordDate, datasetIdsToRedirectFrom, performRedirects);

    final FullBeanImpl savedFullBean = publishToRecordMongo(recordDate, fullBean, fullBeanPreprocessor,
        recordsForRedirection);

    publishToSolrFinal(rdf, savedFullBean);
  }

  /**
   * Publishes an RDF only to mongo server
   *
   * @param rdf RDF to publish.
   * @param recordDate The date that would represent the created/updated date of a record
   * @throws IndexingException which can be one of:
   * <ul>
   * <li>{@link IndexerRelatedIndexingException} In case an error occurred during publication.</li>
   * <li>{@link SetupRelatedIndexingException} in case an error occurred during indexing setup</li>
   * <li>{@link RecordRelatedIndexingException} in case an error occurred related to record
   * contents</li>
   * </ul>
   */
  public void publishMongo(RdfWrapper rdf, Date recordDate) throws IndexingException {
    final FullBeanImpl fullBean = convertRDFToFullBean(rdf);

    final TriConsumer<FullBeanImpl, FullBeanImpl, Pair<Date, Date>> fullBeanPreprocessor = providePreprocessor();

    publishToRecordMongo(recordDate, fullBean, fullBeanPreprocessor, Collections.emptyList());
  }

  /**
   * Publishes an RDF only to tombstone mongo.
   * @param fullBean Fullbean to publish.
   * @param recordDate the data that would represent the created/updated date of a record
   * @throws IndexingException which can be one of:
   * <ul>
   * <li>{@link IndexerRelatedIndexingException} In case an error occurred during publication.</li>
   * <li>{@link SetupRelatedIndexingException} in case an error occurred during indexing setup</li>
   * <li>{@link RecordRelatedIndexingException} in case an error occurred related to record
   * contents</li>
   * </ul>
   */
  public void publishTombstone(FullBeanImpl fullBean, Date recordDate) throws IndexingException {
    final TriConsumer<FullBeanImpl, FullBeanImpl, Pair<Date, Date>> fullBeanPreprocessor = providePreprocessor();
    publishToTombstoneMongo(recordDate, fullBean, fullBeanPreprocessor, Collections.emptyList());
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
        SolrDocumentList solrDocuments = getExistingDocuments(queryParamMap);
        createdDate = (Date) solrDocuments.stream()
                                          .map(document -> document.getFieldValue(EdmLabel.TIMESTAMP_CREATED.toString()))
                                          .toList().stream().findFirst().orElse(recordDate);
      }
      setUpdateAndCreateTime(null, fullBean, Pair.of(recordDate, createdDate));
    }
    publishToSolrFinal(rdfWrapper, fullBean);
  }

  private SolrDocumentList getExistingDocuments(Map<String, String> queryParamMap)
      throws IndexerRelatedIndexingException, RecordRelatedIndexingException {
    SolrDocumentList solrDocuments;
    try {
      // Found
      solrDocuments = getSolrDocuments(queryParamMap);
    } catch (RuntimeException e) {
      //Not found or an error use empty list of documents
      solrDocuments = new SolrDocumentList();
    }
    return solrDocuments;
  }

  private TriConsumer<FullBeanImpl, FullBeanImpl, Pair<Date, Date>> providePreprocessor() {
    // Provide the preprocessor: this will set the created and updated timestamps as needed.
    return preserveUpdateAndCreateTimesFromRdf ? EMPTY_PREPROCESSOR
        : (FullBeanPublisher::setUpdateAndCreateTime);
  }

  private void publishToSolrFinal(RdfWrapper rdf, FullBeanImpl savedFullBean) throws RecordRelatedIndexingException {
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

  private FullBeanImpl publishToRecordMongo(Date recordDate, FullBeanImpl fullBean,
      TriConsumer<FullBeanImpl, FullBeanImpl, Pair<Date, Date>> fullBeanPreprocessor,
      List<Pair<String, Date>> recordsForRedirection)
      throws SetupRelatedIndexingException, IndexerRelatedIndexingException, RecordRelatedIndexingException {
    return publishToMongo(recordDate, fullBean, fullBeanPreprocessor, recordsForRedirection, recordDao);
  }

  private FullBeanImpl publishToTombstoneMongo(Date recordDate, FullBeanImpl fullBean,
      TriConsumer<FullBeanImpl, FullBeanImpl, Pair<Date, Date>> fullBeanPreprocessor,
      List<Pair<String, Date>> recordsForRedirection)
      throws SetupRelatedIndexingException, IndexerRelatedIndexingException, RecordRelatedIndexingException {
    return publishToMongo(recordDate, fullBean, fullBeanPreprocessor, recordsForRedirection, tombstoneRecordDao);
  }

  private FullBeanImpl publishToMongo(Date recordDate, FullBeanImpl fullBean,
      TriConsumer<FullBeanImpl, FullBeanImpl, Pair<Date, Date>> fullBeanPreprocessor,
      List<Pair<String, Date>> recordsForRedirection, RecordDao tombstoneRecordDao)
      throws SetupRelatedIndexingException, IndexerRelatedIndexingException, RecordRelatedIndexingException {
    final FullBeanImpl savedFullBean;
    try {
      savedFullBean = new FullBeanUpdater(fullBeanPreprocessor).update(fullBean, recordDate,
          recordsForRedirection.stream().map(Pair::getValue).min(Comparator.naturalOrder())
                               .orElse(null), tombstoneRecordDao);
    } catch (MongoIncompatibleDriverException | MongoConfigurationException | MongoSecurityException e) {
      throw new SetupRelatedIndexingException(MONGO_SERVER_PUBLISH_ERROR, e);
    } catch (MongoSocketException | MongoClientException | MongoInternalException | MongoInterruptedException e) {
      throw new IndexerRelatedIndexingException(MONGO_SERVER_PUBLISH_ERROR, e);
    } catch (RuntimeException e) {
      throw new RecordRelatedIndexingException(MONGO_SERVER_PUBLISH_ERROR, e);
    }
    return savedFullBean;
  }

  private List<Pair<String, Date>> performRedirection(RdfWrapper rdf, Date recordDate, List<String> datasetIdsToRedirectFrom,
      boolean performRedirects) throws IndexingException {
    // Perform redirection
    final List<Pair<String, Date>> recordsForRedirection;
    try {
      recordsForRedirection = RecordRedirectsUtil
          .checkAndApplyRedirects(recordRedirectDao, rdf, recordDate, datasetIdsToRedirectFrom,
              performRedirects, this::getSolrDocuments);
    } catch (RuntimeException e) {
      throw new RecordRelatedIndexingException(REDIRECT_PUBLISH_ERROR, e);
    }
    return recordsForRedirection;
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
      solrServer.add(document);
    } catch (IOException e) {
      throw new IndexerRelatedIndexingException(SOLR_SERVER_PUBLISH_ERROR, e);
    } catch (SolrServerException | RuntimeException e) {
      throw new RecordRelatedIndexingException(SOLR_SERVER_PUBLISH_ERROR, e);
    }
  }

  private SolrDocumentList getSolrDocuments(Map<String, String> queryParamMap)
      throws IndexerRelatedIndexingException, RecordRelatedIndexingException {
    MapSolrParams queryParams = new MapSolrParams(queryParamMap);
    QueryResponse response;
    try {
      response = solrServer.query(queryParams);
    } catch (SolrServerException e) {
      throw new IndexerRelatedIndexingException(SOLR_SERVER_SEARCH_ERROR, e);
    } catch (IOException e) {
      throw new RecordRelatedIndexingException(SOLR_SERVER_SEARCH_ERROR, e);
    }
    return response.getResults();
  }
}
