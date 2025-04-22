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
import eu.europeana.metis.schema.jibx.Aggregation;
import eu.europeana.metis.schema.jibx.Concept;
import eu.europeana.metis.schema.jibx.PlaceType;
import eu.europeana.metis.schema.jibx.ProxyType;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.schema.jibx.ResourceType;
import eu.europeana.metis.schema.jibx.TimeSpanType;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the Metis implementation of record parsing. When extracting references from an RDF file, it returns both the references
 * in the provider proxy as well as any declared equivalency (sameAs or exactMatch) references in the referenced contextual
 * classes.
 */
public class MetisRecordParser implements RecordParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetisRecordParser.class);

  @Override
  public Set<SearchTermContext> parseSearchTerms(RDF rdf) {
    // Proxy search terms.
    final Set<SearchTermContext> resultSearchTermsSet = getFieldValueSet(ProxyFieldType.values(),
        RdfEntityUtils.getProviderProxies(rdf));
    // Aggregation search terms.
    resultSearchTermsSet.addAll(getFieldValueSet(AggregationFieldType.values(), rdf.getAggregationList()));
    return resultSearchTermsSet;
  }

  private <T extends AboutType> Set<SearchTermContext> getFieldValueSet(FieldType<T>[] fieldTypes,
      List<T> aboutTypes) {
    final Map<FieldValue, Set<FieldType<T>>> fieldValueFieldTypesMap = new HashMap<>();
    for (FieldType<T> fieldType : fieldTypes) {
      aboutTypes.stream().map(fieldType::extractFieldValuesForEnrichment)
                .flatMap(Collection::stream).forEach(
                    value -> fieldValueFieldTypesMap.computeIfAbsent(value, key -> new HashSet<>())
                                                    .add(fieldType));
    }
    return fieldValueFieldTypesMap.entrySet().stream().map(
        entry -> new SearchTermContext(entry.getKey().value(), entry.getKey().language(),
            entry.getValue())).collect(Collectors.toSet());
  }

  @Override
  public Set<ReferenceTermContext> parseReferences(RDF rdf) {

    // Get all direct references from proxies and aggregations. Also look in Europeana proxy as it
    // may have been dereferenced - we use this below to follow sameAs links. We don't have to look
    // in the Europeana aggregation as for aggregations dereferencing happens in place (in the aggregation).
    final Map<String, Set<FieldType<?>>> directReferences = new HashMap<>();
    final BiConsumer<Set<String>, FieldType<?>> directReferenceCollector = (links, field) -> links.stream()
        .map(link -> directReferences.computeIfAbsent(link, key -> new HashSet<>()))
        .forEach(fieldTypes -> fieldTypes.add(field));
    final List<ProxyType> proxies = Optional.ofNullable(rdf.getProxyList()).stream()
        .flatMap(Collection::stream).filter(Objects::nonNull).toList();
    for (ProxyFieldType field : ProxyFieldType.values()) {
      proxies.stream().map(field::extractFieldLinksForEnrichment)
          .forEach(links -> directReferenceCollector.accept(links, field));
    }
    final List<Aggregation> aggregations = Optional.ofNullable(rdf.getAggregationList()).stream()
        .flatMap(Collection::stream).filter(Objects::nonNull).toList();
    for (AggregationFieldType field : AggregationFieldType.values()) {
      aggregations.stream().map(field::extractFieldLinksForEnrichment)
          .forEach(links -> directReferenceCollector.accept(links, field));
    }

    // Get all sameAs links from the directly referenced contextual entities. Only for
    // proxy-referenced entities.
    final Map<String, Set<FieldType<?>>> indirectReferences = new HashMap<>();
    final Consumer<AboutType> contextualTypeCollector = contextualClass -> {
      final Set<FieldType<?>> linkTypes = Optional
          .ofNullable(directReferences.get(contextualClass.getAbout()))
          .orElseGet(Collections::emptySet);
      if (!linkTypes.isEmpty()) {
        for (String sameAsLink : getSameAsLinks(contextualClass)) {
          indirectReferences.computeIfAbsent(sameAsLink, key -> new HashSet<>()).addAll(linkTypes);
        }
      }
    };
    Optional.ofNullable(rdf.getAgentList()).orElseGet(Collections::emptyList)
            .forEach(contextualTypeCollector);
    Optional.ofNullable(rdf.getConceptList()).orElseGet(Collections::emptyList)
            .forEach(contextualTypeCollector);
    Optional.ofNullable(rdf.getPlaceList()).orElseGet(Collections::emptyList)
            .forEach(contextualTypeCollector);
    Optional.ofNullable(rdf.getTimeSpanList()).orElseGet(Collections::emptyList)
            .forEach(contextualTypeCollector);

    // Merge the two maps.
    final Map<String, Set<FieldType<?>>> resultMap = mergeMapInto(directReferences,
        indirectReferences);

    // Clean up the result: no null values. But entities that are already present in the record
    // need to stay. It is possible that a different field (type) refers to an alias (sameAs) for
    // the entity, in which case a new field needs to be created in the Europeana proxy.
    // We can, however, remove known organisations as we will not add new fields for them.
    resultMap.remove(null);
    Optional.of(rdf.getOrganizationList()).stream().flatMap(Collection::stream)
        .filter(Objects::nonNull).map(AboutType::getAbout).filter(Objects::nonNull)
        .forEach(resultMap::remove);

    // Convert and done
    return resultMap.entrySet().stream()
        .map(entry -> ReferenceTermContext.createFromString(entry.getKey(), entry.getValue()))
        .filter(Objects::nonNull).collect(Collectors.toSet());
  }

  private static Set<String> getSameAsLinks(AboutType contextualClass) {
    final List<? extends ResourceType> result;
    result = switch (contextualClass) {
      case AgentType agentType -> agentType.getSameAList();
      case Concept concept -> Optional.ofNullable(concept.getChoiceList()).stream()
                                      .flatMap(Collection::stream).filter(Objects::nonNull).filter(Concept.Choice::ifExactMatch)
                                      .map(Concept.Choice::getExactMatch).filter(Objects::nonNull).toList();
      case PlaceType placeType -> placeType.getSameAList();
      case TimeSpanType timeSpanType -> timeSpanType.getSameAList();
      default -> null;
    };

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
