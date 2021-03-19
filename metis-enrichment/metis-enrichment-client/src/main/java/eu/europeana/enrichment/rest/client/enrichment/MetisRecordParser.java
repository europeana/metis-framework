package eu.europeana.enrichment.rest.client.enrichment;

import eu.europeana.enrichment.api.internal.AggregationFieldType;
import eu.europeana.enrichment.api.internal.FieldType;
import eu.europeana.enrichment.api.internal.FieldValue;
import eu.europeana.enrichment.api.internal.ProxyFieldType;
import eu.europeana.enrichment.api.internal.RecordParser;
import eu.europeana.enrichment.api.internal.ReferenceTermContext;
import eu.europeana.enrichment.api.internal.SearchTermContext;
import eu.europeana.enrichment.utils.RdfEntityUtils;
import eu.europeana.metis.schema.jibx.AboutType;
import eu.europeana.metis.schema.jibx.AgentType;
import eu.europeana.metis.schema.jibx.Concept;
import eu.europeana.metis.schema.jibx.PlaceType;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.schema.jibx.ResourceType;
import eu.europeana.metis.schema.jibx.TimeSpanType;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the Metis implementation of record parsing. When extracting references from an RDF file,
 * it returns both the references in the provider proxy as well as any declared equivalency (sameAs
 * or exactMatch) references in the referenced contextual classes.
 */
public class MetisRecordParser implements RecordParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetisRecordParser.class);

  @Override
  public Set<SearchTermContext> parseSearchTerms(RDF rdf) {
    //Proxy search terms
    final List<AboutType> providerProxies = RdfEntityUtils.getProviderProxies(rdf).stream()
        .map(AboutType.class::cast).collect(Collectors.toList());
    final Set<SearchTermContext> resultSearchTermsSet = getFieldValueSet(ProxyFieldType.values(),
        providerProxies);
    final List<AboutType> aggregations = rdf.getAggregationList().stream()
        .map(AboutType.class::cast).collect(Collectors.toList());
    resultSearchTermsSet.addAll(getFieldValueSet(AggregationFieldType.values(), aggregations));
    return resultSearchTermsSet;
  }

  private Set<SearchTermContext> getFieldValueSet(FieldType[] fieldTypes,
      List<AboutType> aboutTypes) {
    final Map<FieldValue, Set<FieldType>> fieldValueFieldTypesMap = new HashMap<>();
    for (FieldType fieldType : fieldTypes) {
      aboutTypes.stream().map(fieldType::extractFieldValuesForEnrichment)
          .flatMap(Collection::stream).forEach(
          value -> fieldValueFieldTypesMap.computeIfAbsent(value, key -> new HashSet<>())
              .add(fieldType));
    }
    return fieldValueFieldTypesMap.entrySet().stream().map(
        entry -> new SearchTermContext(entry.getKey().getValue(), entry.getKey().getLanguage(),
            entry.getValue())).collect(Collectors.toSet());
  }

  @Override
  public Set<ReferenceTermContext> parseReferences(RDF rdf) {

    // Get all direct references (also look in Europeana proxy as it may have been dereferenced - we
    // use this below to follow sameAs links).
    final List<AboutType> proxies = Optional.ofNullable(rdf.getProxyList()).stream()
        .flatMap(Collection::stream).filter(Objects::nonNull).collect(Collectors.toList());
    final Map<String, Set<FieldType>> directReferences = new HashMap<>();
    for (FieldType field : ProxyFieldType.values()) {
      final Set<String> directLinks = proxies.stream().map(field::extractFieldLinksForEnrichment)
          .flatMap(Set::stream).collect(Collectors.toSet());
      for (String directLink : directLinks) {
        directReferences.computeIfAbsent(directLink, key -> new HashSet<>())
            .add(field);
      }
    }

    // Get all sameAs links from the directly referenced contextual entities.
    final Map<String, Set<FieldType>> indirectReferences = new HashMap<>();
    final Consumer<AboutType> contextualTypeProcessor = contextualClass -> {
      final Set<FieldType> linkTypes = directReferences.get(contextualClass.getAbout());
      if (linkTypes != null) {
        for (String sameAsLink : getSameAsLinks(contextualClass)) {
          indirectReferences.computeIfAbsent(sameAsLink, key -> new HashSet<>())
              .addAll(linkTypes);
        }
      }
    };
    Optional.ofNullable(rdf.getAgentList()).orElseGet(Collections::emptyList)
        .forEach(contextualTypeProcessor);
    Optional.ofNullable(rdf.getConceptList()).orElseGet(Collections::emptyList)
        .forEach(contextualTypeProcessor);
    Optional.ofNullable(rdf.getPlaceList()).orElseGet(Collections::emptyList)
        .forEach(contextualTypeProcessor);
    Optional.ofNullable(rdf.getTimeSpanList()).orElseGet(Collections::emptyList)
        .forEach(contextualTypeProcessor);

    // Merge the two maps.
    final Map<String, Set<FieldType>> resultMap = mergeMapInto(directReferences,
        indirectReferences);

    // Clean up the result: no null values. But objects we already have need to
    // stay: maybe they are matched using a sameAs link.
    resultMap.remove(null);

    // Convert and done
    final Set<ReferenceTermContext> result = new HashSet<>();
    for (Map.Entry<String, Set<FieldType>> entry : resultMap.entrySet()) {
      ReferenceTermContext value;
      try {
        value = new ReferenceTermContext(new URL(entry.getKey()), entry.getValue());
        result.add(value);
      } catch (MalformedURLException e) {
        LOGGER.debug("Invalid enrichment reference found: {}", entry.getKey());
      }
    }
    return result;
  }

  private static Set<String> getSameAsLinks(AboutType contextualClass) {
    final List<? extends ResourceType> result;
    if (contextualClass instanceof AgentType) {
      result = ((AgentType) contextualClass).getSameAList();
    } else if (contextualClass instanceof Concept) {
      result = Optional.ofNullable(((Concept) contextualClass).getChoiceList()).stream()
          .flatMap(Collection::stream).filter(Objects::nonNull).filter(Concept.Choice::ifExactMatch)
          .map(Concept.Choice::getExactMatch).filter(Objects::nonNull).collect(Collectors.toList());
    } else if (contextualClass instanceof PlaceType) {
      result = ((PlaceType) contextualClass).getSameAList();
    } else if (contextualClass instanceof TimeSpanType) {
      result = ((TimeSpanType) contextualClass).getSameAList();
    } else {
      result = null;
    }
    return Optional.ofNullable(result).orElseGet(Collections::emptyList).stream()
        .filter(Objects::nonNull).map(ResourceType::getResource).filter(StringUtils::isNotBlank)
        .collect(Collectors.toSet());
  }

  private static <T, S> Map<T, Set<S>> mergeMapInto(Map<T, Set<S>> map1, Map<T, Set<S>> map2) {
    // Merge the second map into the first one.
    map2.forEach((key, values) -> map1.merge(key, values, (values1, values2) -> {
      values1.addAll(values2);
      return values1;
    }));
    return map1;
  }
}
