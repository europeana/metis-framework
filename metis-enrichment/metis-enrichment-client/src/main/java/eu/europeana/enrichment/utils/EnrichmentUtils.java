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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
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
   * Tags are populated from the first value encountered from each corresponding field.
   * Free-text fields Title and Description are all taken into account.
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
      Map<Class<? extends ResourceOrLiteralType>, String> uniqueResourceOrLiteralTypeClassesMap = createCollectionsForResourceOrLiteralType(
          choices, descriptions);
      Map<Class<? extends LiteralType>, String> uniqueLiteralTypeClassesMap = createCollectionsForLiteralType(
          choices, titles);
      addResourceOrLiteralTypeFromMapsToList(uniqueResourceOrLiteralTypeClassesMap,
          uniqueLiteralTypeClassesMap, tags);
    }

    String thumbnailUrl = Optional.ofNullable(aggregation.getObject())
        .map(ResourceType::getResource)
        .orElse(null);

    return completenessCalculation(thumbnailUrl, titles, descriptions, tags);
  }

  private static Map<Class<? extends ResourceOrLiteralType>, String> createCollectionsForResourceOrLiteralType(
      List<Choice> choices, List<String> descriptions) {
    Map<Class<? extends ResourceOrLiteralType>, String> hashMap = new HashMap<>();
    for (Choice choice : choices) {
      if (choice.ifCoverage()) {
        hashMap.putIfAbsent(choice.getCoverage().getClass(),
            getLiteralOrResourceValue(choice.getCoverage()));
      } else if (choice.ifContributor()) {
        hashMap.putIfAbsent(choice.getContributor().getClass(),
            getLiteralOrResourceValue(choice.getContributor()));
      } else if (choice.ifDate()) {
        hashMap.putIfAbsent(choice.getDate().getClass(),
            getLiteralOrResourceValue(choice.getDate()));
      } else if (choice.ifFormat()) {
        hashMap.putIfAbsent(choice.getFormat().getClass(),
            getLiteralOrResourceValue(choice.getFormat()));
      } else if (choice.ifPublisher()) {
        hashMap.putIfAbsent(choice.getPublisher().getClass(),
            getLiteralOrResourceValue(choice.getPublisher()));
      } else if (choice.ifRelation()) {
        hashMap.putIfAbsent(choice.getRelation().getClass(),
            getLiteralOrResourceValue(choice.getRelation()));
      } else if (choice.ifRights()) {
        hashMap.putIfAbsent(choice.getRights().getClass(),
            getLiteralOrResourceValue(choice.getRights()));
      } else if (choice.ifSource()) {
        hashMap.putIfAbsent(choice.getSource().getClass(),
            getLiteralOrResourceValue(choice.getSource()));
      } else if (choice.ifCreated()) {
        hashMap.putIfAbsent(choice.getCreated().getClass(),
            getLiteralOrResourceValue(choice.getCreated()));
      } else if (choice.ifConformsTo()) {
        hashMap.putIfAbsent(choice.getConformsTo().getClass(),
            getLiteralOrResourceValue(choice.getConformsTo()));
      } else if (choice.ifExtent()) {
        hashMap.putIfAbsent(choice.getExtent().getClass(),
            getLiteralOrResourceValue(choice.getExtent()));
      } else if (choice.ifHasFormat()) {
        hashMap.putIfAbsent(choice.getHasFormat().getClass(),
            getLiteralOrResourceValue(choice.getHasFormat()));
      } else if (choice.ifHasPart()) {
        hashMap.putIfAbsent(choice.getHasPart().getClass(),
            getLiteralOrResourceValue(choice.getHasPart()));
      } else if (choice.ifHasVersion()) {
        hashMap.putIfAbsent(choice.getHasVersion().getClass(),
            getLiteralOrResourceValue(choice.getHasVersion()));
      } else if (choice.ifIsFormatOf()) {
        hashMap.putIfAbsent(choice.getIsFormatOf().getClass(),
            getLiteralOrResourceValue(choice.getIsFormatOf()));
      } else if (choice.ifIsPartOf()) {
        hashMap.putIfAbsent(choice.getIsPartOf().getClass(),
            getLiteralOrResourceValue(choice.getIsPartOf()));
      } else if (choice.ifIsReferencedBy()) {
        hashMap.putIfAbsent(choice.getIsReferencedBy().getClass(),
            getLiteralOrResourceValue(choice.getIsReferencedBy()));
      } else if (choice.ifIsReplacedBy()) {
        hashMap.putIfAbsent(choice.getIsReplacedBy().getClass(),
            getLiteralOrResourceValue(choice.getIsReplacedBy()));
      } else if (choice.ifIsRequiredBy()) {
        hashMap.putIfAbsent(choice.getIsRequiredBy().getClass(),
            getLiteralOrResourceValue(choice.getIsRequiredBy()));
      } else if (choice.ifIssued()) {
        hashMap.putIfAbsent(choice.getIssued().getClass(),
            getLiteralOrResourceValue(choice.getIssued()));
      } else if (choice.ifIsVersionOf()) {
        hashMap.putIfAbsent(choice.getIsVersionOf().getClass(),
            getLiteralOrResourceValue(choice.getIsVersionOf()));
      } else if (choice.ifMedium()) {
        hashMap.putIfAbsent(choice.getMedium().getClass(),
            getLiteralOrResourceValue(choice.getMedium()));
      } else if (choice.ifProvenance()) {
        hashMap.putIfAbsent(choice.getProvenance().getClass(),
            getLiteralOrResourceValue(choice.getProvenance()));
      } else if (choice.ifReferences()) {
        hashMap.putIfAbsent(choice.getReferences().getClass(),
            getLiteralOrResourceValue(choice.getReferences()));
      } else if (choice.ifReplaces()) {
        hashMap.putIfAbsent(choice.getReplaces().getClass(),
            getLiteralOrResourceValue(choice.getReplaces()));
      } else if (choice.ifRequires()) {
        hashMap.putIfAbsent(choice.getRequires().getClass(),
            getLiteralOrResourceValue(choice.getRequires()));
      } else if (choice.ifSpatial()) {
        hashMap.putIfAbsent(choice.getSpatial().getClass(),
            getLiteralOrResourceValue(choice.getSpatial()));
      } else if (choice.ifTableOfContents()) {
        hashMap.putIfAbsent(choice.getTableOfContents().getClass(),
            getLiteralOrResourceValue(choice.getTableOfContents()));
      } else if (choice.ifTemporal()) {
        hashMap.putIfAbsent(choice.getTemporal().getClass(),
            getLiteralOrResourceValue(choice.getTemporal()));
      } else if (choice.ifSubject()) {
        hashMap.putIfAbsent(choice.getSubject().getClass(),
            getLiteralOrResourceValue(choice.getSubject()));
      } else if (choice.ifDescription()) {
        descriptions.add(getLiteralOrResourceValue(choice.getDescription()));
      }
    }
    return hashMap;
  }

  private static Map<Class<? extends LiteralType>, String> createCollectionsForLiteralType(
      List<Choice> choices,
      List<String> titles) {
    Map<Class<? extends LiteralType>, String> hashMap = new HashMap<>();
    for (Choice choice : choices) {
      if (choice.ifAlternative()) {
        hashMap.putIfAbsent(choice.getAlternative().getClass(),
            getLiteralValue(choice.getAlternative()));
      } else if (choice.ifIdentifier()) {
        hashMap.putIfAbsent(choice.getIdentifier().getClass(),
            getLiteralValue(choice.getIdentifier()));
      } else if (choice.ifLanguage()) {
        hashMap.putIfAbsent(choice.getLanguage().getClass(),
            getLiteralValue(choice.getLanguage()));
      } else if (choice.ifTitle()) {
        titles.add(getLiteralValue(choice.getTitle()));
      }
    }
    return hashMap;
  }

  private static void addResourceOrLiteralTypeFromMapsToList(
      final Map<Class<? extends ResourceOrLiteralType>, String> uniqueResourceOrLiteralTypeClassesMap,
      final Map<Class<? extends LiteralType>, String> uniqueLiteralTypeClassesMap,
      List<String> tags) {
    for (Entry<Class<? extends ResourceOrLiteralType>, String> entry : uniqueResourceOrLiteralTypeClassesMap
        .entrySet()) {
      String literalOrResourceValue = entry.getValue();
      if (StringUtils.isNotBlank(literalOrResourceValue)) {
        tags.add(literalOrResourceValue);
      }
    }
    for (Entry<Class<? extends LiteralType>, String> entry : uniqueLiteralTypeClassesMap
        .entrySet()) {
      String literalOrResourceValue = entry.getValue();
      if (StringUtils.isNotBlank(literalOrResourceValue)) {
        tags.add(literalOrResourceValue);
      }
    }
  }

  private static <T extends ResourceOrLiteralType> String getLiteralOrResourceValue(
      T resourceOrLiteralType) {
    if (resourceOrLiteralType != null) {
      return Optional.ofNullable(resourceOrLiteralType.getString()).map(StringUtils::trimToNull)
          .orElseGet(
              () -> Optional.ofNullable(resourceOrLiteralType.getResource())
                  .map(ResourceOrLiteralType.Resource::getResource).orElse(null));
    }
    return null;
  }

  private static <T extends LiteralType> String getLiteralValue(
      T literalType) {
    if (literalType != null) {
      return Optional.ofNullable(literalType.getString()).map(StringUtils::trimToNull)
          .orElse(null);
    }
    return null;
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
    return points > POINTS_UPPER_CAP ? POINTS_UPPER_CAP : points;
  }

  private static Collection<String> extractWordsFromFields(Collection<String> fields) {
    List<String> words = new ArrayList<>();
    for (String field : fields) {
      if (!StringUtils.isEmpty(field)) {
        words.addAll(extractWordsFromField(field));
      }
    }
    return words;
  }

  private static List<String> extractWordsFromField(String field) {
    List<String> words = new ArrayList<>();
    for (String word : field.split("\\W")) {
      if (!StringUtils.isEmpty(word) && word.length() >= MIN_WORD_LENGTH) {
        words.add(word.toLowerCase(Locale.US));
      }
    }
    return words;
  }

  private static boolean isListFullOfEmptyValues(List<String> descriptions) {
    for (String description : descriptions) {
      if (StringUtils.isNotBlank(description)) {
        return false;
      }
    }
    return true;
  }
}
