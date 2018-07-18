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
import java.util.List;
import java.util.Locale;
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

    // Get the provider and europeana proxies
    final ProxyType providerProxy = rdf.getProxyList().stream()
        .filter(proxy -> !isEuropeanaProxy(proxy)).findAny().orElse(null);
    final ProxyType europeanaProxy = rdf.getProxyList().stream()
        .filter(EnrichmentUtils::isEuropeanaProxy).findAny().orElse(null);
    if (providerProxy == null || europeanaProxy == null) {
      return;
    }

    // Obtain the date strings from the various proxy fields.
    final List<String> dateStrings =
        providerProxy.getChoiceList().stream().map(EnrichmentUtils::getDateFromChoice)
            .filter(Objects::nonNull).collect(Collectors.toList());

    // Parse them and set them in the europeana proxy.
    final List<Year> yearList = new YearParser().parse(dateStrings).stream()
        .map(EnrichmentUtils::createYear).collect(Collectors.toList());
    europeanaProxy.setYearList(yearList);

    EuropeanaAggregationType europeanaAggregation = rdf.getEuropeanaAggregationList().stream()
        .findAny().orElse(null);
    if (europeanaAggregation != null) {
      Completeness completeness = new Completeness();
      completeness.setString(Integer
          .toString(computeEuropeanaCompleteness(providerProxy, rdf.getAggregationList().get(0))));
      europeanaAggregation.setCompleteness(completeness);
    }
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

  private static int computeEuropeanaCompleteness(final ProxyType providerProxy,
      final Aggregation aggregation) {
    List<String> tags = new ArrayList<>();
    List<String> descriptions = new ArrayList<>();
    List<String> titles = new ArrayList<>();
    List<Choice> europeanaTypeList = providerProxy.getChoiceList();
    if (europeanaTypeList != null) {
      addResourceOrLiteralTypeFromChoicesToList(europeanaTypeList, tags, descriptions);
      addLiteralTypeFromChoicesToList(europeanaTypeList, tags, titles);
    }

    String thumbnailUrl = Optional.ofNullable(aggregation.getObject())
        .map(ResourceType::getResource)
        .orElse(null);

    return completenessCalculation(thumbnailUrl, titles, descriptions, tags);
  }

  private static void addResourceOrLiteralTypeFromChoicesToList(List<Choice> europeanaTypeList,
      List<String> tags, List<String> descriptions) {
    ResourceOrLiteralType resourceOrLiteralType = null;
    for (Choice europeanaType : europeanaTypeList) {
      if (europeanaType.ifCoverage()) {
        resourceOrLiteralType = europeanaType.getCoverage();
      } else if (europeanaType.ifContributor()) {
        resourceOrLiteralType = europeanaType.getContributor();
      } else if (europeanaType.ifCreator()) {
        resourceOrLiteralType = europeanaType.getCreator();
      } else if (europeanaType.ifDate()) {
        resourceOrLiteralType = europeanaType.getDate();
      } else if (europeanaType.ifFormat()) {
        resourceOrLiteralType = europeanaType.getFormat();
      } else if (europeanaType.ifPublisher()) {
        resourceOrLiteralType = europeanaType.getPublisher();
      } else if (europeanaType.ifRelation()) {
        resourceOrLiteralType = europeanaType.getRelation();
      } else if (europeanaType.ifRights()) {
        resourceOrLiteralType = europeanaType.getRights();
      } else if (europeanaType.ifSource()) {
        resourceOrLiteralType = europeanaType.getSource();
      } else if (europeanaType.ifSubject()) {
        resourceOrLiteralType = europeanaType.getSubject();
      } else if (europeanaType.ifCreated()) {
        resourceOrLiteralType = europeanaType.getCreated();
      } else if (europeanaType.ifConformsTo()) {
        resourceOrLiteralType = europeanaType.getConformsTo();
      } else if (europeanaType.ifExtent()) {
        resourceOrLiteralType = europeanaType.getExtent();
      } else if (europeanaType.ifHasFormat()) {
        resourceOrLiteralType = europeanaType.getHasFormat();
      } else if (europeanaType.ifHasPart()) {
        resourceOrLiteralType = europeanaType.getHasPart();
      } else if (europeanaType.ifHasVersion()) {
        resourceOrLiteralType = europeanaType.getHasVersion();
      } else if (europeanaType.ifIsFormatOf()) {
        resourceOrLiteralType = europeanaType.getIsFormatOf();
      } else if (europeanaType.ifIsPartOf()) {
        resourceOrLiteralType = europeanaType.getIsPartOf();
      } else if (europeanaType.ifIsReferencedBy()) {
        resourceOrLiteralType = europeanaType.getIsReferencedBy();
      } else if (europeanaType.ifIsReplacedBy()) {
        resourceOrLiteralType = europeanaType.getIsReplacedBy();
      } else if (europeanaType.ifIsRequiredBy()) {
        resourceOrLiteralType = europeanaType.getIsRequiredBy();
      } else if (europeanaType.ifIssued()) {
        resourceOrLiteralType = europeanaType.getIssued();
      } else if (europeanaType.ifIsVersionOf()) {
        resourceOrLiteralType = europeanaType.getIsVersionOf();
      } else if (europeanaType.ifMedium()) {
        resourceOrLiteralType = europeanaType.getMedium();
      } else if (europeanaType.ifProvenance()) {
        resourceOrLiteralType = europeanaType.getProvenance();
      } else if (europeanaType.ifReferences()) {
        resourceOrLiteralType = europeanaType.getReferences();
      } else if (europeanaType.ifReplaces()) {
        resourceOrLiteralType = europeanaType.getReplaces();
      } else if (europeanaType.ifRequires()) {
        resourceOrLiteralType = europeanaType.getRequires();
      } else if (europeanaType.ifSpatial()) {
        resourceOrLiteralType = europeanaType.getSpatial();
      } else if (europeanaType.ifTableOfContents()) {
        resourceOrLiteralType = europeanaType.getTableOfContents();
      } else if (europeanaType.ifTemporal()) {
        resourceOrLiteralType = europeanaType.getTemporal();
      } else if (europeanaType.ifDescription()) {
        resourceOrLiteralType = europeanaType.getDescription();
      }
      String literalOrResourceValue = getLiteralOrResourceValue(resourceOrLiteralType);
      if (literalOrResourceValue != null) {
        if (europeanaType.ifDescription()) {
          descriptions.add(literalOrResourceValue);
        } else {
          tags.add(literalOrResourceValue);
        }
      }
    }
  }

  private static void addLiteralTypeFromChoicesToList(List<Choice> europeanaTypeList,
      List<String> tags, List<String> titles) {

    LiteralType literalType = null;
    for (Choice europeanaType : europeanaTypeList) {
      if (europeanaType.ifAlternative()) {
        literalType = europeanaType.getAlternative();
      } else if (europeanaType.ifTitle()) {
        literalType = europeanaType.getTitle();
      } else if (europeanaType.ifIdentifier()) {
        literalType = europeanaType.getIdentifier();
      } else if (europeanaType.ifLanguage()) {
        literalType = europeanaType.getLanguage();
      }

      String literalValue = getLiteralValue(literalType);
      if (literalValue != null) {
        if (europeanaType.ifTitle()) {
          titles.add(literalValue);
        } else {
          tags.add(literalValue);
        }
      }
    }
  }

  private static <T extends ResourceOrLiteralType> String getLiteralOrResourceValue(
      T resourceOrLiteralType) {
    if (resourceOrLiteralType != null) {
      return Optional.ofNullable(resourceOrLiteralType.getString()).orElseGet(
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

    Optional<String> firstTitleOptional = titles.stream().findFirst();
    String title = null;
    if (firstTitleOptional.isPresent()) {
      title = firstTitleOptional.get();
    }

    if ((StringUtils.isEmpty(thumbnailUrl) || StringUtils.isEmpty(title))
        && isListFullOfEmptyValues(
        descriptions)) {
      return 0;
    }

    List<String> text = new ArrayList<>(descriptions);
    text.add(title);

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
