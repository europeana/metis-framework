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
import eu.europeana.corelib.definitions.jibx.Identifier;
import eu.europeana.corelib.definitions.jibx.IsShownAt;
import eu.europeana.corelib.definitions.jibx.IsShownBy;
import eu.europeana.corelib.definitions.jibx.Title;
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
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.TriConsumer;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
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
      List<String> datasetIdsToRedirectFrom)
      throws IndexingException {
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
      boolean performRedirects)
      throws IndexingException {

    // Convert RDF to Full Bean.
    final RdfToFullBeanConverter fullBeanConverter = fullBeanConverterSupplier.get();
    final FullBeanImpl fullBean = fullBeanConverter.convertRdfToFullBean(rdf);

    // Provide the preprocessor: this will set the created and updated timestamps as needed.
    final TriConsumer<FullBeanImpl, FullBeanImpl, Date> fullBeanPreprocessor =
        preserveUpdateAndCreateTimesFromRdf ? EMPTY_PREPROCESSOR
            : (FullBeanPublisher::setUpdateAndCreateTime);

    //The list can be empty if redirects are not applied if no redirect were found
    List<String> recordsForRedirection = checkAndApplyRedirects(rdf, recordDate,
        datasetIdsToRedirectFrom, performRedirects);

    // Publish to Mongo
    final FullBeanImpl savedFullBean;
    try {
      savedFullBean = new FullBeanUpdater(fullBeanPreprocessor)
          .update(fullBean, recordDate, recordsForRedirection.stream().findFirst().orElse(null),
              edmMongoClient);
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

  private List<String> checkAndApplyRedirects(RdfWrapper rdf, Date recordDate,
      List<String> datasetIdsToRedirectFrom, boolean performRedirects)
      throws IndexerRelatedIndexingException, RecordRelatedIndexingException {
    List<String> recordsForRedirection = new ArrayList<>();
    if (performRedirects) {
      //Search Solr to find matching record for redirection
      recordsForRedirection = searchMatchingRecordForRedirection(rdf,
          datasetIdsToRedirectFrom);

      //Create redirection
      if (!CollectionUtils.isEmpty(recordsForRedirection)) {
        createRedirections(rdf.getAbout(), recordsForRedirection, recordDate);
      }
    }
    return recordsForRedirection;
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

  private List<String> searchMatchingRecordForRedirection(RdfWrapper rdfWrapper,
      List<String> datasetIdsToRedirectFrom)
      throws IndexerRelatedIndexingException, RecordRelatedIndexingException {
    //The incoming structure of the identifier is /datasetId/recordId
    final String[] splitRecordIdentifier = rdfWrapper.getAbout().split("/");
    String datasetId = splitRecordIdentifier[1];
    String recordId = splitRecordIdentifier[2];

    //Create combinations of all rules into one query
    final String queryForDatasetIds = generateQueryForDatasetIds(datasetIdsToRedirectFrom,
        recordId);
    final HashMap<String, List<String>> subQueryFields = new HashMap<>();
    final String queryForMatchingFields = generateQueryForMatchingFields(rdfWrapper,
        subQueryFields);
    final String combinedQueryOr = Stream
        .of(queryForDatasetIds, queryForMatchingFields)
        .filter(StringUtils::isNotBlank).collect(Collectors.joining(" OR "));

    //Create query to restrict search on specific datasetId subsets
    final List<String> datasetIds = new ArrayList<>();
    datasetIds.add(datasetId);
    if (!CollectionUtils.isEmpty(datasetIdsToRedirectFrom)) {
      datasetIdsToRedirectFrom.stream().filter(StringUtils::isNotBlank).forEach(datasetIds::add);
    }
    final String datasetIdSubsets = generateQueryInDatasetSubsets(datasetIds);
    final String finalQuery = String.format("(%s) AND (%s)", datasetIdSubsets, combinedQueryOr);

    //Apply solr query and execute
    final Map<String, String> queryParamMap = new HashMap<>();
    queryParamMap.put("q", finalQuery);

    //Prepare subquery
    Predicate<SolrDocument> predicate = document -> {
      boolean flag = false;
      for (Entry<String, List<String>> entry : subQueryFields.entrySet()) {
        flag = document.getFieldValues(entry.getKey()).stream().map(String.class::cast)
            .anyMatch(entry.getValue()::contains);
      }
      return flag;
    };
    //Preprocess subquery and replace documents based on the result
    SolrDocumentList solrDocuments = getSolrDocuments(queryParamMap);
    final SolrDocumentList subQueryResults = solrDocuments.stream().filter(predicate)
        .collect(Collectors.toCollection(SolrDocumentList::new));
    if (!CollectionUtils.isEmpty(subQueryResults)) {
      solrDocuments = subQueryResults;
    }
    //Return all identifiers found
    if (!CollectionUtils.isEmpty(solrDocuments)) {
      return solrDocuments.stream()
          .map(document -> (String) document.getFieldValue(EdmLabel.EUROPEANA_ID.toString()))
          .collect(Collectors.toList());
    }
    return new ArrayList<>();
  }

  private String generateQueryForMatchingFields(RdfWrapper rdfWrapper,
      Map<String, List<String>> subQueryFields) {
    //Collect all required information for heuristics
    final List<String> identifiers = rdfWrapper.getProviderProxyIdentifiers().stream()
        .map(Identifier::getString).collect(Collectors.toList());
    final List<String> titles = rdfWrapper.getProviderProxyTitles().stream().map(Title::getString)
        .collect(Collectors.toList());
    final List<String> descriptions = rdfWrapper.getProviderProxyDescriptions().stream()
        .map(description -> {
          if (StringUtils.isNotBlank(description.getString())) {
            return description.getString();
          } else if (description.getResource() != null && StringUtils
              .isNotBlank(description.getResource().getResource())) {
            return description.getResource().getResource();
          }
          return null;
        }).collect(Collectors.toList());
    final List<String> isShownByList = rdfWrapper.getIsShownByList().stream()
        .map(IsShownBy::getResource)
        .collect(Collectors.toList());
    final List<String> isShownAtList = rdfWrapper.getIsShownAtList().stream()
        .map(IsShownAt::getResource)
        .collect(Collectors.toList());

    //Create all lists that need to be combined
    final Map<String, List<String>> firstMapOfLists = createFirstCombinationGroup(identifiers,
        titles, descriptions);
    subQueryFields = firstMapOfLists;
    final Map<String, List<String>> secondMapOfLists = createSecondCombinationGroup(isShownByList,
        titles, descriptions);
    final Map<String, List<String>> thirdMapOfLists = createThirdCombinationGroup(isShownByList,
        identifiers);
    final Map<String, List<String>> isShownAtAndByMapOfLists = createIsShowByAtCombinationGroup(
        isShownByList, isShownAtList);

    //Combine different permutation groups into an OR joined string
    final String firstGroupOr = generateQueryForFields(firstMapOfLists);
    final String secondGroupOr = generateQueryForFields(secondMapOfLists);
    final String thirdGroupOr = generateQueryForFields(thirdMapOfLists);
    final String isShownAtAndByGroupOr = generateQueryForFields(isShownAtAndByMapOfLists);

    return Stream.of(firstGroupOr, secondGroupOr, thirdGroupOr, isShownAtAndByGroupOr)
        .filter(StringUtils::isNotBlank).collect(Collectors.joining(" OR "));
  }

  private HashMap<String, List<String>> createFirstCombinationGroup(List<String> identifiers,
      List<String> titles, List<String> descriptions) {
    final HashMap<String, List<String>> listsToCombineMaps = new HashMap<>();
    if (!CollectionUtils.isEmpty(identifiers) && !CollectionUtils.isEmpty(titles)) {
      listsToCombineMaps.put(EdmLabel.PROXY_DC_IDENTIFIER.toString(), identifiers);
      listsToCombineMaps.put(EdmLabel.PROXY_DC_TITLE.toString(), titles);
    } else if (!CollectionUtils.isEmpty(identifiers) && !CollectionUtils.isEmpty(descriptions)) {
      listsToCombineMaps.put(EdmLabel.PROXY_DC_IDENTIFIER.toString(), identifiers);
      listsToCombineMaps.put(EdmLabel.PROXY_DC_DESCRIPTION.toString(), descriptions);
    }
    return listsToCombineMaps;
  }

  private HashMap<String, List<String>> createSecondCombinationGroup(List<String> isShownByList,
      List<String> titles, List<String> descriptions) {
    HashMap<String, List<String>> listsToCombineMaps = new HashMap<>();
    if (!CollectionUtils.isEmpty(isShownByList)) {
      if (!CollectionUtils.isEmpty(titles)) {
        listsToCombineMaps
            .put(EdmLabel.PROVIDER_AGGREGATION_EDM_IS_SHOWN_BY.toString(), isShownByList);
        listsToCombineMaps.put(EdmLabel.PROXY_DC_TITLE.toString(), titles);
      } else if (!CollectionUtils.isEmpty(descriptions)) {
        listsToCombineMaps
            .put(EdmLabel.PROVIDER_AGGREGATION_EDM_IS_SHOWN_BY.toString(), isShownByList);
        listsToCombineMaps.put(EdmLabel.PROXY_DC_DESCRIPTION.toString(), descriptions);
      }
    }
    return listsToCombineMaps;
  }

  private HashMap<String, List<String>> createThirdCombinationGroup(List<String> isShownByList,
      List<String> identifiers) {
    HashMap<String, List<String>> listsToCombineMaps = new HashMap<>();
    if (!CollectionUtils.isEmpty(isShownByList) && !CollectionUtils.isEmpty(identifiers)) {
      listsToCombineMaps
          .put(EdmLabel.PROVIDER_AGGREGATION_EDM_IS_SHOWN_BY.toString(), isShownByList);
      listsToCombineMaps.put(EdmLabel.PROXY_DC_IDENTIFIER.toString(), identifiers);
    }
    return listsToCombineMaps;
  }

  private HashMap<String, List<String>> createIsShowByAtCombinationGroup(List<String> isShownByList,
      List<String> isShownAtList) {
    final HashMap<String, List<String>> listsToCombineMapsIsShownAtAndBy = new HashMap<>();
    if (!CollectionUtils.isEmpty(isShownAtList) && !CollectionUtils.isEmpty(isShownByList)) {
      listsToCombineMapsIsShownAtAndBy
          .put(EdmLabel.PROVIDER_AGGREGATION_EDM_IS_SHOWN_BY.toString(), isShownByList);
      listsToCombineMapsIsShownAtAndBy
          .put(EdmLabel.PROVIDER_AGGREGATION_EDM_IS_SHOWN_AT.toString(), isShownAtList);
    }
    return listsToCombineMapsIsShownAtAndBy;
  }

  private String generateQueryForFields(Map<String, List<String>> listsToCombine) {
    final String combinationResult = listsToCombine.entrySet().stream()
        .map(entry -> generateOrOperationFromList(entry.getKey(), entry.getValue()))
        .collect(Collectors.joining(" AND "));
    return String.format("(%s)", combinationResult);
  }

  private String generateOrOperationFromList(String queryFieldName, List<String> items) {
    final String orCombinedItems = items.stream()
        .map(item -> String.format("%s:%s", queryFieldName, item))
        .collect(Collectors.joining(" OR "));
    return String.format("(%s)", orCombinedItems);
  }

  private String generateQueryInDatasetSubsets(List<String> datasetIds) {
    return datasetIds.stream().map(datasetId -> String
        .format("%s:%s_*", EdmLabel.EDM_DATASETNAME, ClientUtils.escapeQueryChars(datasetId)))
        .collect(Collectors.joining(" OR "));
  }

  private String generateQueryForDatasetIds(List<String> datasetIdsToRedirectFrom,
      String recordId) {
    String combinedQueryForRedirectedDatasetIds = null;
    //Check matches with older dataset identifiers
    if (!CollectionUtils.isEmpty(datasetIdsToRedirectFrom)) {
      combinedQueryForRedirectedDatasetIds = datasetIdsToRedirectFrom.stream()
          .map(datasetIdForRedirection -> String
              .format("%s:/%s/%s", EdmLabel.EUROPEANA_ID.toString(), datasetIdForRedirection,
                  recordId)).collect(Collectors.joining(" OR "));
    }
    return combinedQueryForRedirectedDatasetIds;
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

  private void createRedirections(String newIdentifier, List<String> recordsForRedirection,
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
