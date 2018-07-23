package eu.europeana.enrichment.utils;

import eu.europeana.corelib.definitions.jibx.Aggregation;
import eu.europeana.corelib.definitions.jibx.Completeness;
import eu.europeana.corelib.definitions.jibx.EuropeanaAggregationType;
import eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice;
import eu.europeana.corelib.definitions.jibx.LiteralType;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
import eu.europeana.corelib.definitions.jibx.ResourceType;
import eu.europeana.corelib.definitions.jibx.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;

/**
 * Utilities for enrichment and dereferencing Created by gmamakis on 8-3-17.
 */
public final class EnrichmentUtils {

  private static final int MIN_WORD_LENGTH = 3;
  private static final int POINT_DIVIDER_FOR_TAGS = 1;
  private static final int POINT_DIVIDER_FOR_TEXTS = 5;
  private static final int POINTS_UPPER_CAP = 5;

  private EnrichmentUtils() {
  }

  /**
   * Extract the fields to enrich from an RDF file
   *
   * @param rdf The RDF to extract from.
   * @return List<InputValue> The extracted fields that need to be enriched.
   */
  public static List<InputValue> extractFieldsForEnrichmentFromRDF(RDF rdf) {
    ProxyType providerProxy = RdfProxyUtils.getProviderProxy(rdf);
    List<InputValue> valuesForEnrichment = new ArrayList<>();
    for (EnrichmentFields field : EnrichmentFields.values()) {
      List<InputValue> values = field.extractFieldValuesForEnrichment(providerProxy);
      valuesForEnrichment.addAll(values);
    }
    return valuesForEnrichment;
  }

  /**
   * <p>
   * This method is executed at the end of the enrichment process and sets additional values/fields
   * in the RDF, based on all the data that has been collected.
   * </p>
   * <p>
   * Currently sets the edm:year fields (obtained from the provider proxy) in the europeana proxy.
   * If no europeana or provider proxy is present, this method has no effect.
   * </p>
   *
   * @param rdf The RDF in which to set additional field values.
   */
  public static void setAdditionalData(RDF rdf) {

    // Get the provider and europeana proxy
    final ProxyType providerProxy = rdf.getProxyList().stream()
        .filter(proxy -> !isEuropeanaProxy(proxy)).findAny().orElse(null);
    final ProxyType europeanaProxy = rdf.getProxyList().stream()
        .filter(EnrichmentUtils::isEuropeanaProxy).findAny().orElse(null);
    if (providerProxy == null || europeanaProxy == null) {
      return;
    }

    // Calculate completeness first
    EuropeanaAggregationType europeanaAggregation = rdf.getEuropeanaAggregationList().stream()
        .findAny().orElse(null);
    if (europeanaAggregation != null) {
      Completeness completeness = new Completeness();
      completeness.setString(Integer
          .toString(computeEuropeanaCompleteness(providerProxy, rdf.getAggregationList().get(0))));
      europeanaAggregation.setCompleteness(completeness);
    }

    // Obtain the date strings from the various proxy fields.
    final List<String> dateStrings =
        providerProxy.getChoiceList().stream().map(EnrichmentUtils::getDateFromChoice)
            .filter(Objects::nonNull).collect(Collectors.toList());

    // Parse them and set them in the europeana proxy.
    final List<Year> yearList = new YearParser().parse(dateStrings).stream()
        .map(EnrichmentUtils::createYear).collect(Collectors.toList());
    europeanaProxy.setYearList(yearList);
  }

  private static Year createYear(Integer year) {
    final Year result = new Year();
    result.setString(year.toString());
    return result;
  }

  private static String getDateFromChoice(Choice choice) {
    final ResourceOrLiteralType result;
    if (choice.ifDate()) {
      result = choice.getDate();
    } else if (choice.ifTemporal()) {
      result = choice.getTemporal();
    } else if (choice.ifCreated()) {
      result = choice.getCreated();
    } else if (choice.ifIssued()) {
      result = choice.getIssued();
    } else {
      result = null;
    }
    return result == null ? null : result.getString();
  }

  private static boolean isEuropeanaProxy(ProxyType proxy) {
    return proxy.getEuropeanaProxy() != null && proxy.getEuropeanaProxy().isEuropeanaProxy();
  }

  /**
   * Calculates the Europeana Completeness.
   *
   * It gives a rank from 0 to maximum 10 for a record. That ranking is divided in two parts.
   * <p>Up to 5 points for tags and up to 5 points for free-text fields(title, description).
   * Tags are populated from the first value encountered from each corresponding field. Free-text
   * fields Title and Description are all taken into account.
   * </p>
   * <p>Records with thumbnail or both (title and description) missing get rank 0.<p/>
   * <p>Points are calculated in the following manner:</p>
   * <ul>
   * <li>For tags the number of words inside all fields is calculated and divided by 1. Max value
   * of points is 5</li>
   * <li>For free-text the number of words inside all fields is calculated and divided by 5. Max
   * value of points is 5</li>
   * </ul>
   *
   * @param providerProxy the provider proxy
   * @param aggregation the provider aggregation
   * @return the points awarded to the record
   */
  private static int computeEuropeanaCompleteness(final ProxyType providerProxy,
      final Aggregation aggregation) {
    List<String> tags = new ArrayList<>();
    List<String> descriptions = new ArrayList<>();
    List<String> titles = new ArrayList<>();
    List<Choice> choices = providerProxy.getChoiceList();
    if (choices != null) {
      Map<Class<?>, String> uniqueResourceOrLiteralTypeClassesMap = createCollectionsForResourceOrLiteralType(
          choices, descriptions, titles);
      addResourceOrLiteralTypeFromMapsToList(uniqueResourceOrLiteralTypeClassesMap, tags);
    }

    String thumbnailUrl = Optional.ofNullable(aggregation.getObject())
        .map(ResourceType::getResource)
        .orElse(null);

    return completenessCalculation(thumbnailUrl, titles, descriptions, tags);
  }

  private static Map<Class<?>, String> createCollectionsForResourceOrLiteralType(
      List<Choice> choices, List<String> descriptions, List<String> titles) {
    Map<Class<?>, String> hashMap = new HashMap<>();
    for (Choice choice : choices) {
      //Collect only the first occurrence per class type
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifAlternative, choice::getAlternative, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifIdentifier, choice::getIdentifier, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifLanguage, choice::getLanguage, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifCoverage, choice::getCoverage, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifContributor, choice::getContributor, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifCreator, choice::getCreator, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifDate, choice::getDate, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifFormat, choice::getFormat, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifPublisher, choice::getPublisher, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifRelation, choice::getRelation, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifRights, choice::getRights, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifSource, choice::getSource, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifCreated, choice::getCreated, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifConformsTo, choice::getConformsTo, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifExtent, choice::getExtent, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifHasFormat, choice::getHasFormat, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifHasPart, choice::getHasPart, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifHasVersion, choice::getHasVersion, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifIsFormatOf, choice::getIsFormatOf, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifIsPartOf, choice::getIsPartOf, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifIsReferencedBy, choice::getIsReferencedBy,
          hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifIsReplacedBy, choice::getIsReplacedBy, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifIsRequiredBy, choice::getIsRequiredBy, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifIssued, choice::getIssued, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifIsVersionOf, choice::getIsVersionOf, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifMedium, choice::getMedium, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifProvenance, choice::getProvenance, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifReferences, choice::getReferences, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifReplaces, choice::getReplaces, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifRequires, choice::getRequires, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifSpatial, choice::getSpatial, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifTableOfContents, choice::getTableOfContents,
          hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifTemporal, choice::getTemporal, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifSubject, choice::getSubject, hashMap);
      addFirstTypeOccurrenceOfChoiceToMap(choice::ifSpatial, choice::getSpatial, hashMap);

      //Collect all values of these fields
      addChoiceToList(choice::ifTitle, choice::getTitle, titles);
      addChoiceToList(choice::ifDescription, choice::getDescription, descriptions);
    }
    return hashMap;
  }

  private static <R> void addFirstTypeOccurrenceOfChoiceToMap(BooleanSupplier booleanSupplier,
      Supplier<R> supplier,
      Map<Class<?>, String> map) {
    if (booleanSupplier.getAsBoolean()) {
      R result = supplier.get();
      String value;
      if (result instanceof LiteralType) {
        value = getLiteralValue((LiteralType) result);
      } else {
        value = getResourceorLiteralValue((ResourceOrLiteralType) result);
      }
      map.putIfAbsent(result.getClass(), value);
    }
  }

  private static <R> void addChoiceToList(BooleanSupplier booleanSupplier, Supplier<R> supplier,
      List<String> list) {
    if (booleanSupplier.getAsBoolean()) {
      R result = supplier.get();
      String value;
      if (result instanceof LiteralType) {
        value = getLiteralValue((LiteralType) result);
      } else {
        value = getResourceorLiteralValue((ResourceOrLiteralType) result);
      }
      list.add(value);
    }
  }

  private static void addResourceOrLiteralTypeFromMapsToList(
      final Map<Class<?>, String> uniqueResourceOrLiteralTypeClassesMap,
      List<String> tags) {
    uniqueResourceOrLiteralTypeClassesMap.values().stream().filter(StringUtils::isNotBlank)
        .forEach(tags::add);
  }

  //Priority is the literal value and if it doesn't exist then the resource value
  private static <T extends ResourceOrLiteralType> String getResourceorLiteralValue(
      T resourceOrLiteralType) {
    return Optional.ofNullable(resourceOrLiteralType).map(ResourceOrLiteralType::getString)
        .map(StringUtils::trimToNull)
        .orElseGet(() -> Optional.ofNullable(resourceOrLiteralType.getResource())
            .map(ResourceOrLiteralType.Resource::getResource).orElse(null));
  }

  private static <T extends LiteralType> String getLiteralValue(
      T literalType) {
    return Optional.ofNullable(literalType).map(LiteralType::getString)
        .map(StringUtils::trimToNull)
        .orElse(null);
  }

  private static int completenessCalculation(String thumbnailUrl, List<String> titles,
      List<String> descriptions, List<String> tags) {

    if (StringUtils.isEmpty(thumbnailUrl) || (isListFullOfEmptyValues(titles)
        && isListFullOfEmptyValues(descriptions))) {
      return 0;
    }

    List<String> text = new ArrayList<>(descriptions);
    text.addAll(titles);

    int pointsForTags = computePoints(tags, POINT_DIVIDER_FOR_TAGS);
    int pointsForText = computePoints(text, POINT_DIVIDER_FOR_TEXTS);

    return pointsForText + pointsForTags;
  }

  private static int computePoints(Collection<String> fields, int wordsPerPoint) {
    Collection<String> words = extractWordsFromFields(fields);
    int points = words.size() / wordsPerPoint;
    return Math.min(POINTS_UPPER_CAP, points);
  }

  private static Collection<String> extractWordsFromFields(Collection<String> fields) {
    return fields.stream().filter(StringUtils::isNotEmpty)
        .map(EnrichmentUtils::extractWordsFromField)
        .flatMap(Collection::stream).collect(Collectors.toList());
  }

  private static List<String> extractWordsFromField(String field) {
    return Arrays.stream(field.split("\\W"))
        .filter(word -> StringUtils.isNotEmpty(word) && word.length() >= MIN_WORD_LENGTH)
        .map(word -> word.toLowerCase(Locale.US)).collect(Collectors.toList());
  }

  private static boolean isListFullOfEmptyValues(List<String> descriptions) {
    return descriptions.stream().allMatch(StringUtils::isBlank);
  }
}
