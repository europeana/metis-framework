package eu.europeana.indexing;

import com.mongodb.MongoClientException;
import com.mongodb.MongoConfigurationException;
import com.mongodb.MongoIncompatibleDriverException;
import com.mongodb.MongoInternalException;
import com.mongodb.MongoInterruptedException;
import com.mongodb.MongoSecurityException;
import com.mongodb.MongoSocketException;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.beans.IdBean;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.exception.RecordRelatedIndexingException;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.fullbean.RdfToFullBeanConverter;
import eu.europeana.indexing.mongo.FullBeanUpdater;
import eu.europeana.indexing.solr.EdmLabel;
import eu.europeana.indexing.solr.SolrDocumentPopulator;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.mongo.RecordRedirect;
import eu.europeana.metis.mongo.RecordRedirectDao;
import eu.europeana.metis.utils.ExternalRequestUtil;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.logging.log4j.util.TriConsumer;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.MapSolrParams;
import org.springframework.util.CollectionUtils;

/**
 * Publisher for Full Beans (instances of {@link FullBeanImpl}) that makes them accessible and
 * searchable for external agents.
 *
 * @author jochen
 */
class FullBeanPublisher {

  private static final String MONGO_SERVER_PUBLISH_ERROR = "Could not publish to Mongo server.";

  private static final String SOLR_SERVER_PUBLISH_ERROR = "Could not publish to Solr server.";
  private static final String SOLR_SERVER_SEARCH_ERROR = "Could not search Solr server.";

  private static final TriConsumer<FullBeanImpl, FullBeanImpl, Date> EMPTY_PREPROCESSOR = (created, updated, recordDate) -> {
  };
  private static final int PUBLISH_MAX_RETRIES = 30;
  private static final int PERIOD_BETWEEN_RETRIES_IN_MILLIS = 1000;

  private final Supplier<RdfToFullBeanConverter> fullBeanConverterSupplier;

  private final EdmMongoServer edmMongoClient;
  private final SolrClient solrServer;
  private final boolean preserveUpdateAndCreateTimesFromRdf;
  private final RecordRedirectDao recordRedirectDao;

  /**
   * Constructor.
   *
   * @param edmMongoClient The Mongo persistence.
   * @param recordRedirectDao The record redirect dao
   * @param solrServer The searchable persistence.
   * @param preserveUpdateAndCreateTimesFromRdf This determines whether this publisher will use the
   * updated and created times from the incoming RDFs, or whether it computes its own.
   */
  FullBeanPublisher(EdmMongoServer edmMongoClient, RecordRedirectDao recordRedirectDao,
      SolrClient solrServer, boolean preserveUpdateAndCreateTimesFromRdf) {
    this(edmMongoClient, recordRedirectDao, solrServer, preserveUpdateAndCreateTimesFromRdf,
        RdfToFullBeanConverter::new);
  }

  /**
   * Constructor for testing purposes.
   *
   * @param edmMongoClient The Mongo persistence.
   * @param recordRedirectDao The record redirect dao
   * @param solrServer The searchable persistence.
   * @param preserveUpdateAndCreateTimesFromRdf This determines whether this publisher will use the
   * updated and created times from the incoming RDFs, or whether it computes its own.
   * @param fullBeanConverterSupplier Supplies an instance of {@link RdfToFullBeanConverter} used to
   * parse strings to instances of {@link FullBeanImpl}. Will be called once during every publish.
   */
  FullBeanPublisher(EdmMongoServer edmMongoClient,
      RecordRedirectDao recordRedirectDao, SolrClient solrServer,
      boolean preserveUpdateAndCreateTimesFromRdf,
      Supplier<RdfToFullBeanConverter> fullBeanConverterSupplier) {
    this.edmMongoClient = edmMongoClient;
    this.solrServer = solrServer;
    this.fullBeanConverterSupplier = fullBeanConverterSupplier;
    this.preserveUpdateAndCreateTimesFromRdf = preserveUpdateAndCreateTimesFromRdf;
    this.recordRedirectDao = recordRedirectDao;
  }

  private static void setUpdateAndCreateTime(IdBean current, FullBean updated, Date recordDate) {
    final Date updatedDate = recordDate == null ? new Date() : recordDate;
    final Date createdDate = current == null ? updatedDate : current.getTimestampCreated();
    updated.setTimestampCreated(createdDate);
    updated.setTimestampUpdated(updatedDate);
  }

  /**
   * Publishes an RDF.
   *
   * @param rdf RDF to publish.
   * @param recordDate The date that would represent the created/updated date of a record
   * @param datasetIdsForRedirection The dataset ids that their records need to be redirected
   * @throws IndexingException which can be one of:
   * <ul>
   * <li>{@link IndexerRelatedIndexingException} In case an error occurred during publication.</li>
   * <li>{@link SetupRelatedIndexingException} in case an error occurred during indexing setup</li>
   * <li>{@link RecordRelatedIndexingException} in case an error occurred related to record
   * contents</li>
   * </ul>
   */
  public void publish(RdfWrapper rdf, Date recordDate, List<String> datasetIdsForRedirection)
      throws IndexingException {

    // Convert RDF to Full Bean.
    final RdfToFullBeanConverter fullBeanConverter = fullBeanConverterSupplier.get();
    final FullBeanImpl fullBean = fullBeanConverter.convertRdfToFullBean(rdf);

    // Provide the preprocessor: this will set the created and updated timestamps as needed.
    final TriConsumer<FullBeanImpl, FullBeanImpl, Date> fullBeanPreprocessor =
        preserveUpdateAndCreateTimesFromRdf ? EMPTY_PREPROCESSOR
            : (FullBeanPublisher::setUpdateAndCreateTime);

    //Search Solr to find matching record for redirection
    final List<String> recordsForRedirection = searchMatchingRecordForRedirection(rdf.getAbout(),
        datasetIdsForRedirection);

    //Create redirection
    if (!CollectionUtils.isEmpty(recordsForRedirection)) {
      createRedirection(rdf.getAbout(), recordsForRedirection, recordDate);
    }

    // Publish to Mongo
    final FullBeanImpl savedFullBean;
    try {
      savedFullBean = new FullBeanUpdater(fullBeanPreprocessor)
          .update(fullBean, recordDate, edmMongoClient);
    } catch (MongoIncompatibleDriverException | MongoConfigurationException | MongoSecurityException e) {
      throw new SetupRelatedIndexingException(MONGO_SERVER_PUBLISH_ERROR, e);
    } catch (MongoSocketException | MongoClientException | MongoInternalException | MongoInterruptedException e) {
      throw new IndexerRelatedIndexingException(MONGO_SERVER_PUBLISH_ERROR, e);
    } catch (RuntimeException e) {
      throw new RecordRelatedIndexingException(MONGO_SERVER_PUBLISH_ERROR, e);
    }

    // Publish to Solr
    try {
      ExternalRequestUtil.retryableExternalRequest(() -> {
            publishToSolr(rdf, savedFullBean);
            return null;
          }, Collections.singletonMap(UnknownHostException.class, ""), PUBLISH_MAX_RETRIES,
          PERIOD_BETWEEN_RETRIES_IN_MILLIS);
    } catch (IndexingException e) {
      throw e;
    } catch (Exception e) {
      throw new RecordRelatedIndexingException(SOLR_SERVER_PUBLISH_ERROR, e);
    }
  }

  private void publishToSolr(RdfWrapper rdf, FullBeanImpl fullBean) throws IndexingException {

    // Create Solr document.
    final SolrDocumentPopulator documentPopulator = new SolrDocumentPopulator();
    final SolrInputDocument document = new SolrInputDocument();
    documentPopulator.populateWithProperties(document, fullBean);
    documentPopulator.populateWithFacets(document, rdf);

    // Save Solr document.
    try {
      solrServer.add(document);
    } catch (IOException e) {
      throw new IndexerRelatedIndexingException(SOLR_SERVER_PUBLISH_ERROR, e);
    } catch (SolrServerException | RuntimeException e) {
      throw new RecordRelatedIndexingException(SOLR_SERVER_PUBLISH_ERROR, e);
    }
  }

  private List<String> searchMatchingRecordForRedirection(String recordIdIncludingDatasetId,
      List<String> datasetIdsForRedirection)
      throws IndexerRelatedIndexingException, RecordRelatedIndexingException {
    //Check first if record with same record identifier exists
    final Map<String, String> queryParamMap = new HashMap<>();
    queryParamMap.put("q",
        String.format("%s:%s", EdmLabel.EUROPEANA_ID.toString(), recordIdIncludingDatasetId));
    queryParamMap.put("fl", EdmLabel.EUROPEANA_ID.toString());

    //If a record already exist in the database, we assume that the redirection already happened
    //during a previous indexing operation
    SolrDocumentList documents = getSolrDocuments(queryParamMap);
    if (!CollectionUtils.isEmpty(documents)) {
      return new ArrayList<>();
    }

    //Check matches with older dataset identifiers
    if (!CollectionUtils.isEmpty(datasetIdsForRedirection)) {
      //The incoming structure of the identifier is /datasetId/recordId
      final String[] splitRecordIdentifier = recordIdIncludingDatasetId.split("/");
      String recordId = splitRecordIdentifier[2];

      final String combinedQueryForRedirectedDatasetIds = datasetIdsForRedirection.stream()
          .map(datasetIdForRedirection -> String
              .format("%s:/%s/%s", EdmLabel.EUROPEANA_ID.toString(), datasetIdForRedirection,
                  recordId)).collect(Collectors.joining(" OR "));
      queryParamMap.put("q", combinedQueryForRedirectedDatasetIds);
    }

    documents = getSolrDocuments(queryParamMap);
    if (!CollectionUtils.isEmpty(documents)) {
      //Get identifier of the first document found
      return documents.stream()
          .map(document -> (String) document.getFieldValue(EdmLabel.EUROPEANA_ID.toString()))
          .collect(Collectors.toList());
    }
    return new ArrayList<>();
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

  private void createRedirection(String newIdentifier, List<String> recordsForRedirection,
      Date recordRedirectDate) {
    for (String recordForRedirection : recordsForRedirection) {
      final RecordRedirect recordRedirect = new RecordRedirect(newIdentifier, recordForRedirection,
          recordRedirectDate);
      recordRedirectDao.createUpdate(recordRedirect);
      //Update the previous redirect item in db that has newId == oldIdentifier
      final RecordRedirect recordRedirectByNewId = recordRedirectDao
          .getRecordRedirectByNewId(recordForRedirection);
      if (recordRedirectByNewId != null) {
        recordRedirectByNewId.setNewId(newIdentifier);
        recordRedirectDao.createUpdate(recordRedirectByNewId);
      }
    }
  }
}
