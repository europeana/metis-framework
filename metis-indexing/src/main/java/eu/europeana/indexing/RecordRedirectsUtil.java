package eu.europeana.indexing;

import eu.europeana.corelib.definitions.jibx.Identifier;
import eu.europeana.corelib.definitions.jibx.IsShownBy;
import eu.europeana.corelib.definitions.jibx.Title;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.solr.EdmLabel;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.mongo.RecordRedirect;
import eu.europeana.metis.mongo.RecordRedirectDao;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.util.CollectionUtils;

/**
 * Utilities class to assist record redirects logic.
 * <p>Not to be instantiated</p>
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2020-02-11
 */
public final class RecordRedirectsUtil {

  private RecordRedirectsUtil() {
  }

  static List<Pair<String, Date>> checkAndApplyRedirects(
      RecordRedirectDao recordRedirectDao, RdfWrapper rdf, Date recordDate,
      List<String> datasetIdsToRedirectFrom, boolean performRedirects,
      ThrowingFunction<Map<String, String>, SolrDocumentList, IndexingException> solrDocumentRetriever)
      throws IndexingException {
    List<Pair<String, Date>> recordsForRedirection = new ArrayList<>();
    if (performRedirects) {
      //Search Solr to find matching record for redirection
      recordsForRedirection = searchMatchingRecordForRedirection(rdf, datasetIdsToRedirectFrom,
          solrDocumentRetriever);

      //Create redirection
      if (!CollectionUtils.isEmpty(recordsForRedirection)) {
        createRedirections(recordRedirectDao, rdf.getAbout(),
            recordsForRedirection, recordDate);
      }
    }
    return recordsForRedirection;
  }

  private static List<Pair<String, Date>> searchMatchingRecordForRedirection(RdfWrapper rdfWrapper,
      List<String> datasetIdsToRedirectFrom,
      ThrowingFunction<Map<String, String>, SolrDocumentList, IndexingException> solrDocumentRetriever)
      throws IndexingException {
    //The incoming structure of the identifier is /datasetId/recordId
    final String[] splitRecordIdentifier = rdfWrapper.getAbout().split("/");
    String datasetId = splitRecordIdentifier[1];
    String recordId = splitRecordIdentifier[2];

    //Create combinations of all rules into one query
    final Pair<String, List<String>> queryForDatasetIdsAndConcatenatedIds = generateQueryForDatasetIds(
        datasetIdsToRedirectFrom, recordId);
    final Pair<String, Map<String, List<String>>> queryMatchingFieldsAndFirstQueryGroup = generateQueryForMatchingFields(
        rdfWrapper);
    final String queryForMatchingFields = queryMatchingFieldsAndFirstQueryGroup.getLeft();
    final String combinedQueryOr = Stream
        .of(queryForDatasetIdsAndConcatenatedIds.getLeft(), queryForMatchingFields)
        .filter(StringUtils::isNotBlank).collect(Collectors.joining(" OR ", "(", ")"));

    //Create query to restrict search on specific datasetId subsets
    final List<String> datasetIds = new ArrayList<>();
    datasetIds.add(datasetId);
    if (!CollectionUtils.isEmpty(datasetIdsToRedirectFrom)) {
      datasetIdsToRedirectFrom.stream().filter(StringUtils::isNotBlank).forEach(datasetIds::add);
    }
    final String datasetIdSubsets = generateQueryInDatasetSubsets(datasetIds);
    final String finalQuery = String.format("%s AND %s", datasetIdSubsets, combinedQueryOr);

    //Apply solr query and execute
    final Map<String, String> queryParamMap = new HashMap<>();
    queryParamMap.put("q", finalQuery);

    //Prepare sub-query
    Predicate<SolrDocument> predicate = getSolrDocumentPredicate(
        queryForDatasetIdsAndConcatenatedIds, queryMatchingFieldsAndFirstQueryGroup);
    //Preprocess sub-query and replace documents based on the result
    SolrDocumentList solrDocuments = solrDocumentRetriever.apply(queryParamMap);
    final SolrDocumentList subQueryResults = solrDocuments.stream().filter(predicate)
        .collect(Collectors.toCollection(SolrDocumentList::new));
    if (!subQueryResults.isEmpty()) {
      solrDocuments = subQueryResults;
    }
    //Return all identifiers found and their creationDates
    return solrDocuments.stream()
        .map(document -> ImmutablePair
            .of((String) document.getFieldValue(EdmLabel.EUROPEANA_ID.toString()),
                (Date) document.getFieldValue(EdmLabel.TIMESTAMP_CREATED.toString())))
        .collect(Collectors.toList());
  }

  private static Predicate<SolrDocument> getSolrDocumentPredicate(
      Pair<String, List<String>> queryForDatasetIdsAndConcatenatedIds,
      Pair<String, Map<String, List<String>>> queryMatchingFieldsAndFirstQueryGroup) {
    return document -> {
      boolean doesDocumentMatchWithQuery;
      //Check matching of europeana ids first
      doesDocumentMatchWithQuery = queryForDatasetIdsAndConcatenatedIds.getRight().stream()
          .anyMatch(((String) document.getFieldValue(EdmLabel.EUROPEANA_ID.toString()))::equals);

      //Check for first group matching if no europeana id matches found
      if (!doesDocumentMatchWithQuery) {
        for (Entry<String, List<String>> entry : queryMatchingFieldsAndFirstQueryGroup.getRight()
            .entrySet()) {
          doesDocumentMatchWithQuery = document.getFieldValues(entry.getKey()).stream()
              .map(String.class::cast)
              .anyMatch(entry.getValue()::contains);
        }
      }
      return doesDocumentMatchWithQuery;
    };
  }

  private static Pair<String, Map<String, List<String>>> generateQueryForMatchingFields(
      RdfWrapper rdfWrapper) {
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
        }).filter(Objects::nonNull).collect(Collectors.toList());
    final List<String> isShownByList = rdfWrapper.getIsShownByList().stream()
        .map(IsShownBy::getResource)
        .collect(Collectors.toList());

    //Create all lists that need to be combined
    final Map<String, List<String>> firstMapOfLists = createFirstCombinationGroup(identifiers,
        titles, descriptions);
    final Map<String, List<String>> secondMapOfLists = createSecondCombinationGroup(isShownByList,
        titles, descriptions);
    final Map<String, List<String>> thirdMapOfLists = createThirdCombinationGroup(isShownByList,
        identifiers);

    //Combine different list of fields in groups
    final String firstQueryGroup = generateQueryForFields(firstMapOfLists);
    final String secondQueryGroup = generateQueryForFields(secondMapOfLists);
    final String thirdQueryGroup = generateQueryForFields(thirdMapOfLists);

    //Join all groups
    final String combinedQuery = Stream.of(firstQueryGroup, secondQueryGroup, thirdQueryGroup)
        .filter(StringUtils::isNotBlank).collect(Collectors.joining(" OR "));
    return new ImmutablePair<>(combinedQuery, firstMapOfLists);
  }

  private static HashMap<String, List<String>> createFirstCombinationGroup(List<String> identifiers,
      List<String> titles, List<String> descriptions) {
    final HashMap<String, List<String>> listsToCombineMaps = new HashMap<>();
    if (!CollectionUtils.isEmpty(identifiers)) {
      if (!CollectionUtils.isEmpty(titles)) {
        listsToCombineMaps.put(EdmLabel.PROXY_DC_IDENTIFIER.toString(), identifiers);
        listsToCombineMaps.put(EdmLabel.PROXY_DC_TITLE.toString(), titles);
      } else if (!CollectionUtils.isEmpty(descriptions)) {
        listsToCombineMaps.put(EdmLabel.PROXY_DC_IDENTIFIER.toString(), identifiers);
        listsToCombineMaps.put(EdmLabel.PROXY_DC_DESCRIPTION.toString(), descriptions);
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

  private static HashMap<String, List<String>> createThirdCombinationGroup(
      List<String> isShownByList,
      List<String> identifiers) {
    HashMap<String, List<String>> listsToCombineMaps = new HashMap<>();
    if (!CollectionUtils.isEmpty(isShownByList) && !CollectionUtils.isEmpty(identifiers)) {
      listsToCombineMaps
          .put(EdmLabel.PROVIDER_AGGREGATION_EDM_IS_SHOWN_BY.toString(), isShownByList);
      listsToCombineMaps.put(EdmLabel.PROXY_DC_IDENTIFIER.toString(), identifiers);
    }
    return listsToCombineMaps;
  }

  private static String generateQueryForFields(Map<String, List<String>> listsToCombine) {
    final List<String> items = listsToCombine.entrySet().stream()
        .map(entry -> generateOrOperationFromList(entry.getKey(), entry.getValue()))
        .filter(StringUtils::isNotBlank).collect(Collectors.toList());
    return computeJoiningQuery(items, Function.identity(),
        Collectors.joining(" AND ", "(", ")"));
  }

  private static String generateOrOperationFromList(String queryFieldName, List<String> items) {
    final List<String> filteredItems = getFilteredItems(items);
    return computeJoiningQuery(filteredItems, Function.identity(),
        Collectors.joining(" OR ", String.format("%s:(", queryFieldName), ")"));
  }

  private static String generateQueryInDatasetSubsets(List<String> datasetIds) {
    final List<String> filteredItems = getFilteredItems(datasetIds);
    return computeJoiningQuery(filteredItems,
        datasetId -> String.format("%s_*", ClientUtils.escapeQueryChars(datasetId)),
        Collectors.joining(" OR ", String.format("%s:(", EdmLabel.EDM_DATASETNAME), ")"));
  }

  private static String computeJoiningQuery(List<String> filteredItems,
      Function<String, String> preprocessor, Collector<CharSequence, ?, String> joining) {
    String result = null;
    if (!CollectionUtils.isEmpty(filteredItems)) {
      result = filteredItems.stream().map(preprocessor).collect(joining);
    }
    return result;
  }

  private static List<String> getFilteredItems(List<String> datasetIds) {
    return datasetIds.stream().filter(StringUtils::isNotBlank)
        .collect(Collectors.toList());
  }

  private static Pair<String, List<String>> generateQueryForDatasetIds(
      List<String> datasetIdsToRedirectFrom, String recordId) {
    String combinedQueryForRedirectedDatasetIds = null;
    List<String> concatenatedDatasetRecordIds = new ArrayList<>();
    //Check matches with older dataset identifiers
    if (!CollectionUtils.isEmpty(datasetIdsToRedirectFrom)) {
      final List<String> filteredItems = getFilteredItems(datasetIdsToRedirectFrom);

      concatenatedDatasetRecordIds = filteredItems.stream().map(
          datasetIdForRedirection -> String.format("/%s/%s", datasetIdForRedirection, recordId))
          .collect(Collectors.toList());

      combinedQueryForRedirectedDatasetIds = computeJoiningQuery(concatenatedDatasetRecordIds,
          Function.identity(),
          Collectors.joining(" OR ", String.format("%s:(", EdmLabel.EUROPEANA_ID), ")"));
    }
    return ImmutablePair.of(combinedQueryForRedirectedDatasetIds, concatenatedDatasetRecordIds);
  }

  private static void createRedirections(RecordRedirectDao recordRedirectDao,
      String newIdentifier, List<Pair<String, Date>> recordsForRedirection,
      Date recordRedirectDate) {
    for (Pair<String, Date> recordForRedirection : recordsForRedirection) {
      final RecordRedirect recordRedirect = new RecordRedirect(newIdentifier,
          recordForRedirection.getKey(),
          recordRedirectDate);
      recordRedirectDao.createUpdate(recordRedirect);
      //Update the previous redirect item in db that has newId == oldIdentifier
      final RecordRedirect recordRedirectByNewId = recordRedirectDao
          .getRecordRedirectByNewId(recordRedirect.getOldId());
      if (recordRedirectByNewId != null) {
        recordRedirectByNewId.setNewId(newIdentifier);
        recordRedirectDao.createUpdate(recordRedirectByNewId);
      }
    }
  }

  /**
   * Represents a function that accepts one argument and produces a result with the possibility of
   * an {@link IndexingException} thrown.
   *
   * @param <T> the type of the input to the function
   * @param <R> the type of the result of the function
   * @param <E> the type of the possible exception thrown
   */
  @FunctionalInterface
  public interface ThrowingFunction<T, R, E extends IndexingException> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     * @throws E the function exception
     */
    R apply(T t) throws E;
  }
}
