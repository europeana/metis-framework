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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
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
 * Utilities class to assist record redirects logic.
 * <p>Not to be instantiated</p>
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2020-02-11
 */
public final class RecordRedirectsUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(RecordRedirectsUtil.class);

  private RecordRedirectsUtil() {
  }

  static List<Pair<String, Date>> checkAndApplyRedirects(
      RecordRedirectDao recordRedirectDao, RdfWrapper rdf, Date recordDate,
      List<String> datasetIdsToRedirectFrom, boolean performRedirects,
      ThrowingFunction<Map<String, String>, SolrDocumentList, IndexingException> solrDocumentRetriever)
      throws IndexingException {

    // If no redirects are to be performed, we're done.
    if (!performRedirects) {
      return Collections.emptyList();
    }

    // Search Solr to find matching record for redirection
    final List<Pair<String, Date>> recordsForRedirection = searchMatchingRecordForRedirection(rdf,
        datasetIdsToRedirectFrom, solrDocumentRetriever);

    // Create redirection
    for (Pair<String, Date> recordForRedirection : recordsForRedirection) {
      introduceRedirection(recordRedirectDao, rdf.getAbout(), recordForRedirection.getLeft(),
          recordDate);
    }

    // Done.
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
    final List<String> queriesToCombine = Arrays
        .asList(queryForDatasetIdsAndConcatenatedIds.getLeft(), queryForMatchingFields);
    final String combinedQueryOr = computeJoiningQuery(getFilteredItems(queriesToCombine),
        UnaryOperator.identity(), Collectors.joining(" OR ", "(", ")"));

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
        .format("-%s:%s", EdmLabel.EUROPEANA_ID.toString(),
            ClientUtils.escapeQueryChars(rdfWrapper.getAbout()));

    // Assemble final query.
    final List<String> finalQueryParts = Arrays
        .asList(datasetIdSubsets, combinedQueryOr, queryPreventingFindingSameRecord);
    final String finalQuery = computeJoiningQuery(getFilteredItems(finalQueryParts),
        UnaryOperator.identity(), Collectors.joining(" AND "));

    //Apply solr query and execute
    final Map<String, String> queryParamMap = new HashMap<>();
    queryParamMap.put("q", finalQuery);

    //Preprocess sub-query and replace documents based on the result
    SolrDocumentList solrDocuments = solrDocumentRetriever.apply(queryParamMap);

    //Check exact ids match first
    final SolrDocumentList exactIdsMatchResults = solrDocuments.stream().filter(checkExactIdsMatch(
        queryForDatasetIdsAndConcatenatedIds.getRight()))
        .collect(Collectors.toCollection(SolrDocumentList::new));
    if (!exactIdsMatchResults.isEmpty()) {
      solrDocuments = exactIdsMatchResults;
    } else {
      //Check for first group if and only if the exact ids match do not have any results
      final SolrDocumentList firstGroupMatchResults = solrDocuments.stream()
          .filter(checkFirstGroupMatch(queryMatchingFieldsAndFirstQueryGroup.getRight()))
          .collect(Collectors.toCollection(SolrDocumentList::new));
      if (!firstGroupMatchResults.isEmpty()) {
        solrDocuments = firstGroupMatchResults;
      }
    }

    //Return all identifiers found and their creationDates
    return solrDocuments.stream().map(document -> ImmutablePair
        .of((String) document.getFieldValue(EdmLabel.EUROPEANA_ID.toString()),
            (Date) document.getFieldValue(EdmLabel.TIMESTAMP_CREATED.toString())))
        .collect(Collectors.toList());
  }

  private static Predicate<SolrDocument> checkExactIdsMatch(List<String> concatenatedIds) {
    return document -> concatenatedIds.stream()
        .anyMatch(((String) document.getFieldValue(EdmLabel.EUROPEANA_ID.toString()))::equals);
  }

  private static Predicate<SolrDocument> checkFirstGroupMatch(
      Map<String, List<String>> firstQueryGroup) {
    return document -> {
      int counter = 0;
      for (Entry<String, List<String>> entry : firstQueryGroup.entrySet()) {
        if (document.getFieldValues(entry.getKey()).stream().map(String.class::cast)
            .anyMatch(entry.getValue()::contains)) {
          counter++;
        }
      }
      return firstQueryGroup.size() == counter;
    };
  }

  private static Pair<String, Map<String, List<String>>> generateQueryForMatchingFields(
      RdfWrapper rdfWrapper) {
    //Collect all required information for heuristics
    final List<String> identifiers = rdfWrapper.getProviderProxyIdentifiers().stream()
        .map(Identifier::getString).filter(StringUtils::isNotBlank).collect(Collectors.toList());
    final List<String> titles = rdfWrapper.getProviderProxyTitles().stream().map(Title::getString)
        .filter(StringUtils::isNotBlank).collect(Collectors.toList());
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
        .map(IsShownBy::getResource).filter(StringUtils::isNotBlank)
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
      List<String> isShownByList, List<String> identifiers) {
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
    return computeJoiningQuery(items, UnaryOperator.identity(),
        Collectors.joining(" AND ", "(", ")"));
  }

  private static String generateOrOperationFromList(String queryFieldName, List<String> items) {
    final List<String> filteredItems = getFilteredItems(items);
    return computeJoiningQuery(filteredItems, ClientUtils::escapeQueryChars,
        Collectors.joining(" OR ", String.format("%s:(", queryFieldName), ")"));
  }

  private static String generateQueryInDatasetSubsets(List<String> datasetIds) {
    final List<String> filteredItems = getFilteredItems(datasetIds);
    return computeJoiningQuery(filteredItems,
        datasetId -> String.format("%s_*", ClientUtils.escapeQueryChars(datasetId)),
        Collectors.joining(" OR ", String.format("%s:(", EdmLabel.EDM_DATASETNAME), ")"));
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
    return items.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
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
          UnaryOperator.identity(),
          Collectors.joining(" OR ", String.format("%s:(", EdmLabel.EUROPEANA_ID), ")"));
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
   * @param recordRedirectDao The DAO object to manage redirects.
   * @param newIdentifier The new identifier (value Y).
   * @param oldIdentifier The old identifier (value X).
   * @param recordRedirectDate The date (timestamp) for any new redirects.
   */
  private static void introduceRedirection(RecordRedirectDao recordRedirectDao,
      String newIdentifier, String oldIdentifier, Date recordRedirectDate) {

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
        .map(RecordRedirect::getNewId).anyMatch(newIdentifier::equals);
    if (!mappingAlreadyExists) {
      recordRedirectDao
          .createUpdate(new RecordRedirect(newIdentifier, oldIdentifier, recordRedirectDate));
    }

    // Update the redirects ? -> X to point to Y instead, becoming ? -> Y.
    recordRedirectDao.getRecordRedirectsByNewId(oldIdentifier).forEach(redirect -> {
      redirect.setNewId(newIdentifier);
      recordRedirectDao.createUpdate(redirect);
    });
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
