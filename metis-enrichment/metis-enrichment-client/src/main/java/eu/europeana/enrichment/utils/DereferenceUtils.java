package eu.europeana.enrichment.utils;

import eu.europeana.enrichment.api.internal.AggregationFieldType;
import eu.europeana.metis.schema.jibx.*;
import eu.europeana.metis.schema.jibx.EuropeanaType.Choice;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by gmamakis on 9-3-17.
 */
public final class DereferenceUtils {

  private static final Function<ResourceType, String> RESOURCE_EXTRACTOR = DereferenceUtils::extractFromResource;
  private static final Function<ResourceOrLiteralType, String> RESOURCE_OR_LITERAL_EXTRACTOR = DereferenceUtils::extractFromResourceOrLiteral;

  private DereferenceUtils() {
  }

  /**
   * Extract references from RDF document
   *
   * @param rdf input document
   * @return non-null set of values for dereferencing, not containing null.
   */
  public static Map<Class<? extends AboutType>, Set<String>> extractReferencesForDereferencing(RDF rdf) {

    // Get all the links we are interested in.
    final Map<Class<? extends AboutType>, Set<String>> result = new HashMap<>();
    extractValues(rdf.getAgentList(), item -> dereferenceAgent(item, result));
    extractValues(rdf.getConceptList(), item -> dereferenceConcept(item, result));
    extractValues(rdf.getPlaceList(), item -> dereferencePlace(item, result));
    extractValues(rdf.getTimeSpanList(), item -> dereferenceTimespan(item, result));
    extractValues(rdf.getWebResourceList(), item -> dereferenceWebResource(item, result));
    extractValues(rdf.getAggregationList(), item -> dereferenceAggregation(item, result));
    extractValues(RdfEntityUtils.getProviderProxies(rdf), item -> dereferenceProxy(item, result));

    // Clean up the result: no null values and no objects that we already have.
    result.remove(null);
    final Consumer<List<? extends AboutType>> cleaner = list -> Optional.ofNullable(list)
                                                                        .map(List::stream).orElseGet(Stream::empty)
                                                                        .map(AboutType::getAbout)
                                                                        .forEach(result::remove);
    cleaner.accept(rdf.getAgentList());
    cleaner.accept(rdf.getConceptList());
    cleaner.accept(rdf.getPlaceList());
    cleaner.accept(rdf.getTimeSpanList());

    // Done.
    return result;
  }

  private static <S> void extractValues(List<S> source, Consumer<S> extractor) {
    if (source != null) {
      for (S sourceItem : source) {
        extractor.accept(sourceItem);
      }
    }
  }

  private static <T> void convertValues(List<? extends T> source,
      Function<T, String> conversion, Map<Class<? extends AboutType>, Set<String>> result,
      Class<? extends AboutType> classType) {
    if (source != null) {
      for (T sourceItem : source) {
        convertValue(sourceItem, conversion, result, classType);
      }
    }
  }

  private static <T> void convertValue(T source, Function<T, String> conversion,
      Map<Class<? extends AboutType>, Set<String>> resultMap,
      Class<? extends AboutType> classType) {
    Set<String> result = new HashSet<>();
    if (source != null) {
      final String target = conversion.apply(source);
      if (StringUtils.isNotBlank(target)) {
        result.add(target);
        //If Map already contains key with 'classType', then just add the new values into existing Set
        resultMap.computeIfPresent(classType, (key, value) -> {
          value.addAll(result);
          return value;
        });
        resultMap.putIfAbsent(classType, result);
      }
    }

  }

  private static <T> void convertValue(boolean proceed, T source,
      Function<T, String> conversion, Map<Class<? extends AboutType>, Set<String>> result,
      Class<? extends AboutType> classType) {
    if (proceed) {
      convertValue(source, conversion, result, classType);
    }
  }

  private static void dereferenceProxy(ProxyType proxyType, Map<Class<? extends AboutType>, Set<String>> values) {
    convertValues(proxyType.getHasMetList(), RESOURCE_EXTRACTOR, values, ProxyType.class);
    convertValues(proxyType.getHasTypeList(), RESOURCE_OR_LITERAL_EXTRACTOR, values, ProxyType.class);
    convertValues(proxyType.getIncorporateList(), RESOURCE_EXTRACTOR, values, ProxyType.class);
    convertValues(proxyType.getIsDerivativeOfList(), RESOURCE_EXTRACTOR, values, ProxyType.class);
    convertValues(proxyType.getIsRelatedToList(), RESOURCE_OR_LITERAL_EXTRACTOR, values, ProxyType.class);
    convertValues(proxyType.getIsSimilarToList(), RESOURCE_EXTRACTOR, values, ProxyType.class);
    convertValues(proxyType.getIsSuccessorOfList(), RESOURCE_EXTRACTOR, values, ProxyType.class);
    convertValues(proxyType.getRealizeList(), RESOURCE_EXTRACTOR, values, ProxyType.class);
    convertValue(proxyType.getCurrentLocation(), RESOURCE_OR_LITERAL_EXTRACTOR, values, ProxyType.class);
    extractValues(proxyType.getChoiceList(), item -> dereferenceChoice(item, values));
  }

  private static void dereferenceChoice(Choice choice, Map<Class<? extends AboutType>, Set<String>> values) {
    convertValue(choice.ifContributor(), choice.getContributor(), RESOURCE_OR_LITERAL_EXTRACTOR, values, ProxyType.class);
    convertValue(choice.ifCoverage(), choice.getCoverage(), RESOURCE_OR_LITERAL_EXTRACTOR, values, ProxyType.class);
    convertValue(choice.ifCreator(), choice.getCreator(), RESOURCE_OR_LITERAL_EXTRACTOR, values, ProxyType.class);
    convertValue(choice.ifCreated(), choice.getCreated(), RESOURCE_OR_LITERAL_EXTRACTOR, values, ProxyType.class);
    convertValue(choice.ifDate(), choice.getDate(), RESOURCE_OR_LITERAL_EXTRACTOR, values, ProxyType.class);
    convertValue(choice.ifExtent(), choice.getExtent(), RESOURCE_OR_LITERAL_EXTRACTOR, values, ProxyType.class);
    convertValue(choice.ifFormat(), choice.getFormat(), RESOURCE_OR_LITERAL_EXTRACTOR, values, ProxyType.class);
    convertValue(choice.ifHasFormat(), choice.getHasFormat(), RESOURCE_OR_LITERAL_EXTRACTOR, values, ProxyType.class);
    convertValue(choice.ifHasVersion(), choice.getHasVersion(), RESOURCE_OR_LITERAL_EXTRACTOR,
        values, ProxyType.class);
    convertValue(choice.ifIsFormatOf(), choice.getIsFormatOf(), RESOURCE_OR_LITERAL_EXTRACTOR,
        values, ProxyType.class);
    convertValue(choice.ifIsReferencedBy(), choice.getIsReferencedBy(),
        RESOURCE_OR_LITERAL_EXTRACTOR, values, ProxyType.class);
    convertValue(choice.ifIsReplacedBy(), choice.getIsReplacedBy(), RESOURCE_OR_LITERAL_EXTRACTOR,
        values, ProxyType.class);
    convertValue(choice.ifIsRequiredBy(), choice.getIsRequiredBy(), RESOURCE_OR_LITERAL_EXTRACTOR,
        values, ProxyType.class);
    convertValue(choice.ifIsVersionOf(), choice.getIsVersionOf(), RESOURCE_OR_LITERAL_EXTRACTOR,
        values, ProxyType.class);
    convertValue(choice.ifMedium(), choice.getMedium(), RESOURCE_OR_LITERAL_EXTRACTOR,
        values, ProxyType.class);
    convertValue(choice.ifPublisher(), choice.getPublisher(), RESOURCE_OR_LITERAL_EXTRACTOR,
        values, ProxyType.class);
    convertValue(choice.ifReferences(), choice.getReferences(), RESOURCE_OR_LITERAL_EXTRACTOR,
        values, ProxyType.class);
    convertValue(choice.ifRelation(), choice.getRelation(), RESOURCE_OR_LITERAL_EXTRACTOR, values, ProxyType.class);
    convertValue(choice.ifReplaces(), choice.getReplaces(), RESOURCE_OR_LITERAL_EXTRACTOR, values, ProxyType.class);
    convertValue(choice.ifSource(), choice.getSource(), RESOURCE_OR_LITERAL_EXTRACTOR, values, ProxyType.class);
    convertValue(choice.ifSpatial(), choice.getSpatial(), RESOURCE_OR_LITERAL_EXTRACTOR, values, ProxyType.class);
    convertValue(choice.ifSubject(), choice.getSubject(), RESOURCE_OR_LITERAL_EXTRACTOR, values, ProxyType.class);
    convertValue(choice.ifTemporal(), choice.getTemporal(), RESOURCE_OR_LITERAL_EXTRACTOR, values, ProxyType.class);
    convertValue(choice.ifIssued(), choice.getIssued(), RESOURCE_OR_LITERAL_EXTRACTOR, values, ProxyType.class);
    convertValue(choice.ifType(), choice.getType(), RESOURCE_OR_LITERAL_EXTRACTOR, values, ProxyType.class);
  }

  private static void dereferenceTimespan(TimeSpanType timespan, final Map<Class<? extends AboutType>, Set<String>> result) {
    convertValues(timespan.getIsPartOfList(), RESOURCE_OR_LITERAL_EXTRACTOR, result, TimeSpanType.class);
  }

  private static void dereferenceAgent(AgentType agent, final Map<Class<? extends AboutType>, Set<String>> result) {
    convertValues(agent.getProfessionOrOccupationList(), RESOURCE_OR_LITERAL_EXTRACTOR, result, AgentType.class);
    convertValues(agent.getIsPartOfList(), RESOURCE_OR_LITERAL_EXTRACTOR, result, AgentType.class);
  }

  private static void dereferenceConcept(Concept concept, final Map<Class<? extends AboutType>, Set<String>> result) {
    extractValues(concept.getChoiceList(), item -> dereferenceConceptChoice(item, result));
  }

  private static void dereferenceConceptChoice(Concept.Choice choice, final Map<Class<? extends AboutType>, Set<String>> result) {
    convertValue(choice.ifBroader(), choice.getBroader(), RESOURCE_EXTRACTOR, result, Concept.class);
  }

  private static void dereferencePlace(PlaceType place, Map<Class<? extends AboutType>, Set<String>> result) {
    convertValues(place.getIsPartOfList(), RESOURCE_OR_LITERAL_EXTRACTOR, result, PlaceType.class);
  }

  private static void dereferenceWebResource(WebResourceType wr, final Map<Class<? extends AboutType>, Set<String>> result) {
    convertValues(wr.getCreatedList(), RESOURCE_OR_LITERAL_EXTRACTOR, result, WebResourceType.class);
    convertValues(wr.getExtentList(), RESOURCE_OR_LITERAL_EXTRACTOR, result, WebResourceType.class);
    convertValues(wr.getFormatList(), RESOURCE_OR_LITERAL_EXTRACTOR, result, WebResourceType.class);
    convertValues(wr.getIsFormatOfList(), RESOURCE_OR_LITERAL_EXTRACTOR, result, WebResourceType.class);
    convertValues(wr.getIssuedList(), RESOURCE_OR_LITERAL_EXTRACTOR, result, WebResourceType.class);
  }

  private static void dereferenceAggregation(Aggregation aggregation, final Map<Class<? extends AboutType>, Set<String>> result) {
    AggregationFieldType.DATA_PROVIDER.extractFields(aggregation).forEach(item -> convertValue(item,
        RESOURCE_OR_LITERAL_EXTRACTOR, result, Aggregation.class));
    AggregationFieldType.PROVIDER.extractFields(aggregation).forEach(item -> convertValue(item,
        RESOURCE_OR_LITERAL_EXTRACTOR, result, Aggregation.class));
    AggregationFieldType.INTERMEDIATE_PROVIDER.extractFields(aggregation).forEach(item -> convertValue(item,
        RESOURCE_OR_LITERAL_EXTRACTOR, result, Aggregation.class));
  }

  private static String extractFromResourceOrLiteral(ResourceOrLiteralType type) {
    if (type.getResource() != null && StringUtils.isNotEmpty(type.getResource().getResource())) {
      return type.getResource().getResource();
    }
    return null;
  }

  private static String extractFromResource(ResourceType type) {
    if (StringUtils.isNotEmpty(type.getResource())) {
      return type.getResource();
    }
    return null;
  }
}
