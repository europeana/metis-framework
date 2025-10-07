package eu.europeana.indexing.redirect.v2;

import com.mongodb.client.MongoClient;
import eu.europeana.indexing.common.contract.QueryableSearchPersistence;
import eu.europeana.indexing.common.contract.RedirectPersistence;
import eu.europeana.indexing.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.exception.RecordRelatedIndexingException;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.common.persistence.solr.v2.SolrV2Field;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.mongo.connection.MongoClientProvider;
import eu.europeana.metis.mongo.dao.RecordRedirectDao;
import eu.europeana.metis.mongo.model.RecordRedirect;
import eu.europeana.metis.schema.jibx.Description;
import eu.europeana.metis.schema.jibx.Identifier;
import eu.europeana.metis.schema.jibx.IsShownBy;
import eu.europeana.metis.schema.jibx.Title;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * This class implements record redirect persistence using the redirect MongoDB V2.
 */
public final class RedirectPersistenceV2 implements RedirectPersistence {

  private static final Logger LOGGER = LoggerFactory.getLogger(RedirectPersistenceV2.class);

  private static final String REDIRECT_PUBLISH_ERROR = "Could not publish the redirection changes.";

  private final MongoClient mongoClientToClose;
  private final RecordRedirectDao recordRedirectDao;

  private final QueryableSearchPersistence<SolrDocument, ?> searchPersistence;

  /**
   * Constructor.
   *
   * @param mongoClientProvider Provider for record persistence. Clients that are provided from this
   *                            object will be closed when this instance's {@link #close()} method
   *                            is called.
   * @param mongoRedirectDBName The specific Mongo database to connect to.
   * @param searchPersistence   Search persistence access. Note: this instance will not take
   *                            responsibility for closing this persistence object.
   * @throws SetupRelatedIndexingException In the case of setup issues.
   */
  public RedirectPersistenceV2(
      MongoClientProvider<SetupRelatedIndexingException> mongoClientProvider,
      String mongoRedirectDBName, QueryableSearchPersistence<SolrDocument, ?> searchPersistence)
      throws SetupRelatedIndexingException {
    this.mongoClientToClose = mongoClientProvider.createMongoClient();
    this.recordRedirectDao = new RecordRedirectDao(this.mongoClientToClose, mongoRedirectDBName);
    this.searchPersistence = searchPersistence;
  }

  /**
   * Constructor.
   *
   * @param recordRedirectDao DAO object for record redirects. Note: this instance will not take
   *                          responsibility for closing this client.
   * @param searchPersistence Search persistence access. Note: this instance will not take
   *                          responsibility for closing this persistence object.
   */
  public RedirectPersistenceV2(RecordRedirectDao recordRedirectDao,
      QueryableSearchPersistence<SolrDocument, ?> searchPersistence) {
    this.mongoClientToClose = null;
    this.recordRedirectDao = recordRedirectDao;
    this.searchPersistence = searchPersistence;
  }

  @Override
  public Date performRedirection(RdfWrapper rdf, Date redirectDate,
      List<String> datasetIdsToRedirectFrom) throws IndexingException {
    try {
      return performRedirectionInternal(rdf, redirectDate, datasetIdsToRedirectFrom);
    } catch (RuntimeException e) {
      throw new RecordRelatedIndexingException(REDIRECT_PUBLISH_ERROR, e);
    }
  }

  private Date performRedirectionInternal(RdfWrapper rdf, Date redirectDate,
      List<String> datasetIdsToRedirectFrom) throws IndexingException {

    // Search Solr to find matching record for redirection
    final List<Pair<String, Date>> recordsForRedirection = searchMatchingRecordForRedirection(rdf,
        datasetIdsToRedirectFrom);

    // Create redirection
    for (Pair<String, Date> recordForRedirection : recordsForRedirection) {
      introduceRedirection(rdf.getAbout(), recordForRedirection.getLeft(), redirectDate);
    }

    // Done.
    return recordsForRedirection.stream().map(Pair::getValue).min(Comparator.naturalOrder())
        .orElse(null);
  }

  private List<Pair<String, Date>> searchMatchingRecordForRedirection(RdfWrapper rdfWrapper,
      List<String> datasetIdsToRedirectFrom) throws IndexingException {

    //The incoming structure of the identifier is /datasetId/recordId
    final String[] splitRecordIdentifier = rdfWrapper.getAbout().split("/");
    String datasetId = splitRecordIdentifier[1];
    String recordId = splitRecordIdentifier[2];

    //Create combinations of all rules into one query
    final Pair<String, List<String>> queryForDatasetIdsAndConcatenatedIds = generateQueryForDatasetIds(
        datasetIdsToRedirectFrom, recordId);
    final Map<String, List<String>> firstMapOfLists = new HashMap<>();
    final Map<String, List<String>> secondMapOfLists = new HashMap<>();
    final Map<String, List<String>> thirdMapOfLists = new HashMap<>();
    final String queryMatchingFields = generateQueryForMatchingFields(rdfWrapper, firstMapOfLists,
        secondMapOfLists, thirdMapOfLists);
    final List<String> queriesToCombine = Arrays
        .asList(queryForDatasetIdsAndConcatenatedIds.getLeft(), queryMatchingFields);
    final String combinedQueryOr = computeJoiningQuery(getFilteredItems(queriesToCombine),
        UnaryOperator.identity(), Collectors.joining(" OR ", "(", ")"));

    //If combined query or is empty then we do not have a reason to search for a redirection match
    if (StringUtils.isNotBlank(combinedQueryOr)) {
      //Create query to restrict search on specific datasetId subsets
      final List<String> datasetIds = new ArrayList<>();
      datasetIds.add(datasetId);
      if (!CollectionUtils.isEmpty(datasetIdsToRedirectFrom)) {
        datasetIdsToRedirectFrom.stream().filter(StringUtils::isNotBlank).forEach(datasetIds::add);
      }
      final String datasetIdSubsets = generateQueryInDatasetSubsets(datasetIds);

      // Query avoiding self-redirection. If the dataset already exists in the Solr it is likely that
      // our query so far would return the very record we're indexing, which should be prevented.
      final String queryPreventingFindingSameRecord = String
          .format("-%s:%s", SolrV2Field.EUROPEANA_ID,
              ClientUtils.escapeQueryChars(rdfWrapper.getAbout()));

      // Assemble final query.
      final List<String> finalQueryParts = Arrays
          .asList(datasetIdSubsets, combinedQueryOr, queryPreventingFindingSameRecord);
      final String finalQuery = computeJoiningQuery(getFilteredItems(finalQueryParts),
          UnaryOperator.identity(), Collectors.joining(" AND "));

      //Apply solr query and execute
      final Map<String, String> queryParamMap = new HashMap<>();
      queryParamMap.put("q", finalQuery);
      queryParamMap.put("fl",
          String.format("%s,%s,%s,%s,%s,%s", SolrV2Field.EUROPEANA_ID, SolrV2Field.TIMESTAMP_CREATED,
              SolrV2Field.PROXY_DC_IDENTIFIER, SolrV2Field.PROXY_DC_TITLE, SolrV2Field.PROXY_DC_DESCRIPTION,
              SolrV2Field.PROVIDER_AGGREGATION_EDM_IS_SHOWN_BY));

      //Preprocess sub-query and replace documents based on the result
      Collection<SolrDocument> solrDocuments = searchPersistence.search(queryParamMap);

      //Check exact ids match first
      modifyDocumentListIfMatchesFound(solrDocuments,
          queryForDatasetIdsAndConcatenatedIds.getRight(), firstMapOfLists, secondMapOfLists,
          thirdMapOfLists);

      //Return all identifiers found and their creationDates
      return solrDocuments.stream().map(document -> ImmutablePair
                              .of((String) document.getFieldValue(SolrV2Field.EUROPEANA_ID.toString()),
                                  (Date) document.getFieldValue(SolrV2Field.TIMESTAMP_CREATED.toString())))
                          .collect(Collectors.toList());
    }
    return new ArrayList<>();
  }

  private static void modifyDocumentListIfMatchesFound(Collection<SolrDocument> solrDocuments,
      List<String> concatenatedIds, Map<String, List<String>> firstMap,
      Map<String, List<String>> secondMap, Map<String, List<String>> thirdMap) {
    //We check in the following order only if the previous result was empty. Exact Ids, first group, second group
    SolrDocumentList matchedResults = getMatchingSolrDocuments(solrDocuments,
        doExactIdsMatch(concatenatedIds));
    if (matchedResults.isEmpty()) {
      matchedResults = getMatchingSolrDocuments(solrDocuments, doesGroupMatch(firstMap));
    }
    if (matchedResults.isEmpty()) {
      matchedResults = getMatchingSolrDocuments(solrDocuments, doesGroupMatch(secondMap));
    }
    if (matchedResults.isEmpty()) {
      matchedResults = getMatchingSolrDocuments(solrDocuments, doesGroupMatch(thirdMap));
    }
    //Replace with matches, if no matches then the solr documents returned were false positive because of the text tokenized fields
    solrDocuments.clear();
    solrDocuments.addAll(matchedResults);
  }

  private static SolrDocumentList getMatchingSolrDocuments(Collection<SolrDocument> solrDocuments,
      Predicate<SolrDocument> solrDocumentPredicate) {
    return solrDocuments.stream().filter(solrDocumentPredicate)
                        .collect(Collectors.toCollection(SolrDocumentList::new));
  }

  private static Predicate<SolrDocument> doExactIdsMatch(List<String> concatenatedIds) {
    return document -> concatenatedIds.stream()
                                      .anyMatch(((String) document.getFieldValue(SolrV2Field.EUROPEANA_ID.toString()))::equals);
  }

  private static Predicate<SolrDocument> doesGroupMatch(Map<String, List<String>> queryGroup) {
    return document -> {
      int counter = 0;
      for (Entry<String, List<String>> entry : queryGroup.entrySet()) {

        if (Optional.ofNullable(document.getFieldValues(entry.getKey())).stream().flatMap(Collection::stream)
                    .map(String.class::cast).anyMatch(entry.getValue()::contains)) {
          counter++;
        }
      }
      return !queryGroup.isEmpty() && queryGroup.size() == counter;
    };
  }

  private static String generateQueryForMatchingFields(RdfWrapper rdfWrapper,
      Map<String, List<String>> firstMapOfLists, Map<String, List<String>> secondMapOfLists,
      Map<String, List<String>> thirdMapOfLists) {
    //Collect all required information for heuristics
    final Function<Description, String> descriptionToString = description -> {
      if (StringUtils.isNotBlank(description.getString())) {
        return description.getString();
      } else if (description.getResource() != null && StringUtils
          .isNotBlank(description.getResource().getResource())) {
        return description.getResource().getResource();
      }
      return null;
    };
    final List<String> identifiers = rdfWrapper.getProviderProxyIdentifiers().stream()
        .map(Identifier::getString).filter(StringUtils::isNotBlank).toList();
    final List<String> titles = rdfWrapper.getProviderProxyTitles().stream()
        .map(Title::getString).filter(StringUtils::isNotBlank).toList();
    final List<String> descriptions = rdfWrapper.getProviderProxyDescriptions().stream()
        .map(descriptionToString).filter(Objects::nonNull).toList();
    final List<String> isShownByList = rdfWrapper.getIsShownByList().stream()
        .map(IsShownBy::getResource).filter(StringUtils::isNotBlank).toList();

    //Create all lists that need to be combined
    firstMapOfLists.putAll(createFirstCombinationGroup(identifiers, titles, descriptions));
    secondMapOfLists.putAll(createSecondCombinationGroup(isShownByList, titles, descriptions));
    thirdMapOfLists.putAll(createThirdCombinationGroup(isShownByList, identifiers));

    //Combine different list of fields in groups
    final String firstQueryGroup = generateQueryForFields(firstMapOfLists);
    final String secondQueryGroup = generateQueryForFields(secondMapOfLists);
    final String thirdQueryGroup = generateQueryForFields(thirdMapOfLists);

    //Join all groups
    final String combinedQuery = Stream.of(firstQueryGroup, secondQueryGroup, thirdQueryGroup)
                                       .filter(StringUtils::isNotBlank).collect(Collectors.joining(" OR "));
    return StringUtils.isNotBlank(combinedQuery) ? combinedQuery : null;
  }

  private static HashMap<String, List<String>> createFirstCombinationGroup(List<String> identifiers,
      List<String> titles, List<String> descriptions) {
    final HashMap<String, List<String>> listsToCombineMaps = new HashMap<>();
    if (!CollectionUtils.isEmpty(identifiers)) {
      if (!CollectionUtils.isEmpty(titles)) {
        listsToCombineMaps.put(SolrV2Field.PROXY_DC_IDENTIFIER.toString(), identifiers);
        listsToCombineMaps.put(SolrV2Field.PROXY_DC_TITLE.toString(), titles);
      } else if (!CollectionUtils.isEmpty(descriptions)) {
        listsToCombineMaps.put(SolrV2Field.PROXY_DC_IDENTIFIER.toString(), identifiers);
        listsToCombineMaps.put(SolrV2Field.PROXY_DC_DESCRIPTION.toString(), descriptions);
      }
    }
    return listsToCombineMaps;
  }

  private static HashMap<String, List<String>> createSecondCombinationGroup(
      List<String> isShownByList,
      List<String> titles, List<String> descriptions) {
    HashMap<String, List<String>> listsToCombineMaps = new HashMap<>();
    if (!CollectionUtils.isEmpty(isShownByList)) {
      if (!CollectionUtils.isEmpty(titles)) {
        listsToCombineMaps
            .put(SolrV2Field.PROVIDER_AGGREGATION_EDM_IS_SHOWN_BY.toString(), isShownByList);
        listsToCombineMaps.put(SolrV2Field.PROXY_DC_TITLE.toString(), titles);
      } else if (!CollectionUtils.isEmpty(descriptions)) {
        listsToCombineMaps
            .put(SolrV2Field.PROVIDER_AGGREGATION_EDM_IS_SHOWN_BY.toString(), isShownByList);
        listsToCombineMaps.put(SolrV2Field.PROXY_DC_DESCRIPTION.toString(), descriptions);
      }
    }
    return listsToCombineMaps;
  }

  private static HashMap<String, List<String>> createThirdCombinationGroup(
      List<String> isShownByList, List<String> identifiers) {
    HashMap<String, List<String>> listsToCombineMaps = new HashMap<>();
    if (!CollectionUtils.isEmpty(isShownByList) && !CollectionUtils.isEmpty(identifiers)) {
      listsToCombineMaps
          .put(SolrV2Field.PROVIDER_AGGREGATION_EDM_IS_SHOWN_BY.toString(), isShownByList);
      listsToCombineMaps.put(SolrV2Field.PROXY_DC_IDENTIFIER.toString(), identifiers);
    }
    return listsToCombineMaps;
  }

  private static String generateQueryForFields(Map<String, List<String>> listsToCombine) {
    final List<String> items = listsToCombine.entrySet().stream()
        .map(entry -> generateOrOperationFromList(entry.getKey(), entry.getValue()))
        .filter(StringUtils::isNotBlank).toList();
    return computeJoiningQuery(items, UnaryOperator.identity(),
        Collectors.joining(" AND ", "(", ")"));
  }

  private static String generateOrOperationFromList(String queryFieldName, List<String> items) {
    final List<String> filteredItems = getFilteredItems(items);
    return computeJoiningQuery(filteredItems,
        item -> String.format("\"%s\"", ClientUtils.escapeQueryChars(item)),
        Collectors.joining(" OR ", String.format("%s:(", queryFieldName), ")"));
  }

  private static String generateQueryInDatasetSubsets(List<String> datasetIds) {
    final List<String> filteredItems = getFilteredItems(datasetIds);
    return computeJoiningQuery(filteredItems,
        datasetId -> String.format("%s_*", ClientUtils.escapeQueryChars(datasetId)),
        Collectors.joining(" OR ", String.format("%s:(", SolrV2Field.EDM_DATASETNAME), ")"));
  }

  private static String computeJoiningQuery(List<String> filteredItems,
      UnaryOperator<String> preprocessor, Collector<CharSequence, ?, String> joining) {
    String result = null;
    if (!CollectionUtils.isEmpty(filteredItems)) {
      result = filteredItems.stream().map(preprocessor).collect(joining);
    }
    return result;
  }

  private static List<String> getFilteredItems(List<String> items) {
    return items.stream().filter(StringUtils::isNotBlank).toList();
  }

  private static Pair<String, List<String>> generateQueryForDatasetIds(
      List<String> datasetIdsToRedirectFrom, String recordId) {
    String combinedQueryForRedirectedDatasetIds = null;
    List<String> concatenatedDatasetRecordIds = new ArrayList<>();
    //Check matches with older dataset identifiers
    if (!CollectionUtils.isEmpty(datasetIdsToRedirectFrom)) {
      final List<String> filteredItems = getFilteredItems(datasetIdsToRedirectFrom);

      concatenatedDatasetRecordIds = filteredItems.stream().map(
                                                      datasetIdForRedirection -> String.format("\"/%s/%s\"", datasetIdForRedirection, recordId))
                                                  .toList();

      combinedQueryForRedirectedDatasetIds = computeJoiningQuery(concatenatedDatasetRecordIds,
          UnaryOperator.identity(),
          Collectors.joining(" OR ", String.format("%s:(\"", SolrV2Field.EUROPEANA_ID), "\")"));
    }
    return ImmutablePair.of(combinedQueryForRedirectedDatasetIds, concatenatedDatasetRecordIds);
  }

  /**
   * Introduce a new redirect X -> Y. If X equals Y we do nothing. Otherwise, we do the following:
   * <ol>
   * <li>
   * We delete all mappings Y -> ?. We can do this because we are indexing record Y and it should
   * therefore not be redirected to any other link.
   * </li>
   * <li>
   * We delete all mappings X -> ? except X -> Y. We can do this because X will be redirected to
   * record Y only, and it should therefore not be redirected to any other record.
   * </li>
   * <li>
   * Introduce new redirect from X -> Y if it doesn't already exist. This new redirect will get the
   * record redirect date (passed to this method).
   * </li>
   * <li>
   * We update all existing redirects ? -> X to point to Y instead (so becoming ? -> Y).
   * </li>
   * </ol>
   * Note that after this method is called (and if X does not equal Y), X should only occur as
   * source of a redirect, and then only as part of redirect X -> Y, whereas Y should occur only as
   * destination of a redirect. Neither of them can therefore be part of a redirection cycle.
   *
   * @param newIdentifier The new identifier (value Y).
   * @param oldIdentifier The old identifier (value X).
   * @param redirectDate The date (timestamp) for any new redirects.
   */
  private void introduceRedirection(String newIdentifier, String oldIdentifier, Date redirectDate) {

    // Sanity check: if old and new identifier are equal, do nothing.
    if (oldIdentifier.equals(newIdentifier)) {
      LOGGER.info(
          "Encountered the request to create mappping from {} to itself. This will be ignored.",
          oldIdentifier);
      return;
    }

    // Remove any redirects Y -> ?.
    recordRedirectDao.getRecordRedirectsByOldId(newIdentifier).forEach(recordRedirectDao::delete);

    // Remove any redirects X -> ? except X -> Y.
    final List<RecordRedirect> existingRedirectsFromOldIdentifier = recordRedirectDao
        .getRecordRedirectsByOldId(oldIdentifier);
    existingRedirectsFromOldIdentifier.stream()
                                      .filter(redirect -> !redirect.getNewId().equals(newIdentifier))
                                      .forEach(recordRedirectDao::delete);

    // Create the new redirect X -> Y if one doesn't already exist.
    final boolean mappingAlreadyExists = existingRedirectsFromOldIdentifier.stream()
                                                                           .map(RecordRedirect::getNewId)
                                                                           .anyMatch(newIdentifier::equals);
    if (!mappingAlreadyExists) {
      recordRedirectDao
          .createUpdate(new RecordRedirect(newIdentifier, oldIdentifier, redirectDate));
    }

    // Update the redirects ? -> X to point to Y instead, becoming ? -> Y.
    recordRedirectDao.getRecordRedirectsByNewId(oldIdentifier).forEach(redirect -> {
      redirect.setNewId(newIdentifier);
      recordRedirectDao.createUpdate(redirect);
    });
  }

  @Override
  public void triggerFlushOfPendingChanges(boolean blockUntilComplete)
      throws IndexerRelatedIndexingException {
    // Nothing to do.
  }

  @Override
  public void close() throws IOException {
    if (mongoClientToClose != null) {
      this.mongoClientToClose.close();
    }
  }
}
