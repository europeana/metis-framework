package eu.europeana.enrichment.utils;

import eu.europeana.enrichment.api.internal.AggregationFieldType;
import eu.europeana.enrichment.rest.client.dereference.Dereferencer.PermittedEntityType;
import eu.europeana.metis.schema.jibx.AboutType;
import eu.europeana.metis.schema.jibx.AgentType;
import eu.europeana.metis.schema.jibx.Aggregation;
import eu.europeana.metis.schema.jibx.Concept;
import eu.europeana.metis.schema.jibx.EuropeanaType.Choice;
import eu.europeana.metis.schema.jibx.PlaceType;
import eu.europeana.metis.schema.jibx.ProxyType;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType;
import eu.europeana.metis.schema.jibx.ResourceType;
import eu.europeana.metis.schema.jibx.TimeSpanType;
import eu.europeana.metis.schema.jibx.WebResourceType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
  public static Map<String, PermittedEntityType> extractReferencesForDereferencing(RDF rdf) {

    // Get all the links to be dereferenced with any entity (Europeana or external).
    final Set<String> anyTypeResult = new HashSet<>();
    extractValues(rdf.getAgentList(), item -> dereferenceAgent(item, anyTypeResult));
    extractValues(rdf.getConceptList(), item -> dereferenceConcept(item, anyTypeResult));
    extractValues(rdf.getPlaceList(), item -> dereferencePlace(item, anyTypeResult));
    extractValues(rdf.getTimeSpanList(), item -> dereferenceTimespan(item, anyTypeResult));
    extractValues(rdf.getWebResourceList(), item -> dereferenceWebResource(item, anyTypeResult));
    extractValues(RdfEntityUtils.getProviderProxies(rdf), item -> dereferenceProxy(item, anyTypeResult));

    // Get the links to be dereferenced with Europeana entities only.
    final Set<String> europeanaTypeResult = new HashSet<>();
    extractValues(rdf.getAggregationList(), item -> dereferenceAggregation(item, europeanaTypeResult));

    // Clean and compile the result. If a reference occurs in both lists, ensure it gets the most
    // permissive permitted entity type.
    cleanResultSet(rdf, anyTypeResult);
    cleanResultSet(rdf, europeanaTypeResult);
    final Map<String, PermittedEntityType> result = new HashMap<>();
    anyTypeResult.forEach(reference -> result.put(reference, PermittedEntityType.ANY_ENTITY));
    europeanaTypeResult.forEach(
        reference -> result.putIfAbsent(reference, PermittedEntityType.EUROPEANA_ENTITY));

    // Done.
    return result;
  }

  private static void cleanResultSet(RDF rdf, Set<String> result) {
    result.remove(null);
    final Consumer<List<? extends AboutType>> cleaner = list -> Optional.ofNullable(list)
        .map(List::stream).orElseGet(Stream::empty)
        .map(AboutType::getAbout)
        .forEach(result::remove);
    cleaner.accept(rdf.getAgentList());
    cleaner.accept(rdf.getConceptList());
    cleaner.accept(rdf.getOrganizationList());
    cleaner.accept(rdf.getPlaceList());
    cleaner.accept(rdf.getTimeSpanList());
  }

  private static <S> void extractValues(List<S> source, Consumer<S> extractor) {
    if (source != null) {
      for (S sourceItem : source) {
        extractor.accept(sourceItem);
      }
    }
  }

  private static <T> void convertValues(List<? extends T> source,
      Function<T, String> conversion, Set<String> result) {
    if (source != null) {
      for (T sourceItem : source) {
        convertValue(sourceItem, conversion, result);
      }
    }
  }

  private static <T> void convertValue(T source, Function<T, String> conversion, Set<String> result) {
    if (source != null) {
      final String target = conversion.apply(source);
      if (StringUtils.isNotBlank(target)) {
        result.add(target);
      }
    }
  }

  private static <T> void convertValue(boolean proceed, T source,
      Function<T, String> conversion, Set<String> result) {
    if (proceed) {
      convertValue(source, conversion, result);
    }
  }

  private static void dereferenceProxy(ProxyType proxyType, Set<String> values) {
    convertValues(proxyType.getHasMetList(), RESOURCE_EXTRACTOR, values);
    convertValues(proxyType.getHasTypeList(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValues(proxyType.getIncorporateList(), RESOURCE_EXTRACTOR, values);
    convertValues(proxyType.getIsDerivativeOfList(), RESOURCE_EXTRACTOR, values);
    convertValues(proxyType.getIsRelatedToList(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValues(proxyType.getIsSimilarToList(), RESOURCE_EXTRACTOR, values);
    convertValues(proxyType.getIsSuccessorOfList(), RESOURCE_EXTRACTOR, values);
    convertValues(proxyType.getRealizeList(), RESOURCE_EXTRACTOR, values);
    convertValue(proxyType.getCurrentLocation(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    extractValues(proxyType.getChoiceList(), item -> dereferenceChoice(item, values));
  }

  private static void dereferenceChoice(Choice choice, Set<String> values) {
    convertValue(choice.ifContributor(), choice.getContributor(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.ifCoverage(), choice.getCoverage(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.ifCreator(), choice.getCreator(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.ifCreated(), choice.getCreated(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.ifDate(), choice.getDate(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.ifExtent(), choice.getExtent(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.ifFormat(), choice.getFormat(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.ifHasFormat(), choice.getHasFormat(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.ifHasVersion(), choice.getHasVersion(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.ifIsFormatOf(), choice.getIsFormatOf(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.ifIsReferencedBy(), choice.getIsReferencedBy(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.ifIsReplacedBy(), choice.getIsReplacedBy(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.ifIsRequiredBy(), choice.getIsRequiredBy(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.ifIsVersionOf(), choice.getIsVersionOf(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.ifMedium(), choice.getMedium(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.ifPublisher(), choice.getPublisher(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.ifReferences(), choice.getReferences(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.ifRelation(), choice.getRelation(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.ifReplaces(), choice.getReplaces(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.ifSource(), choice.getSource(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.ifSpatial(), choice.getSpatial(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.ifSubject(), choice.getSubject(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.ifTemporal(), choice.getTemporal(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.ifIssued(), choice.getIssued(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
    convertValue(choice.ifType(), choice.getType(), RESOURCE_OR_LITERAL_EXTRACTOR, values);
  }

  private static void dereferenceTimespan(TimeSpanType timespan, final Set<String> result) {
    convertValues(timespan.getIsPartOfList(), RESOURCE_OR_LITERAL_EXTRACTOR, result);
  }

  private static void dereferenceAgent(AgentType agent, final Set<String> result) {
    convertValues(agent.getProfessionOrOccupationList(), RESOURCE_OR_LITERAL_EXTRACTOR, result);
    convertValues(agent.getIsPartOfList(), RESOURCE_OR_LITERAL_EXTRACTOR, result);
  }

  private static void dereferenceConcept(Concept concept, final Set<String> result) {
    extractValues(concept.getChoiceList(), item -> dereferenceConceptChoice(item, result));
  }

  private static void dereferenceConceptChoice(Concept.Choice choice, final Set<String> result) {
    convertValue(choice.ifBroader(), choice.getBroader(), RESOURCE_EXTRACTOR, result);
  }

  private static void dereferencePlace(PlaceType place, Set<String> result) {
    convertValues(place.getIsPartOfList(), RESOURCE_OR_LITERAL_EXTRACTOR, result);
  }

  private static void dereferenceWebResource(WebResourceType wr, final Set<String> result) {
    convertValues(wr.getCreatedList(), RESOURCE_OR_LITERAL_EXTRACTOR, result);
    convertValues(wr.getExtentList(), RESOURCE_OR_LITERAL_EXTRACTOR, result);
    convertValues(wr.getFormatList(), RESOURCE_OR_LITERAL_EXTRACTOR, result);
    convertValues(wr.getIsFormatOfList(), RESOURCE_OR_LITERAL_EXTRACTOR, result);
    convertValues(wr.getIssuedList(), RESOURCE_OR_LITERAL_EXTRACTOR, result);
  }

  private static void dereferenceAggregation(Aggregation aggregation, final Set<String> result) {
    AggregationFieldType.DATA_PROVIDER.extractFields(aggregation).forEach(item -> convertValue(item,
        RESOURCE_OR_LITERAL_EXTRACTOR, result));
    AggregationFieldType.PROVIDER.extractFields(aggregation).forEach(item -> convertValue(item,
        RESOURCE_OR_LITERAL_EXTRACTOR, result));
    AggregationFieldType.INTERMEDIATE_PROVIDER.extractFields(aggregation).forEach(item -> convertValue(item,
        RESOURCE_OR_LITERAL_EXTRACTOR, result));
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
