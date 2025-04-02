package eu.europeana.enrichment.utils;

import static eu.europeana.enrichment.utils.RdfEntityUtils.appendLinkToEuropeanaProxy;
import static eu.europeana.enrichment.utils.RdfEntityUtils.replaceResourceWithLinkInAggregation;
import static eu.europeana.enrichment.utils.RdfEntityUtils.replaceValueWithLinkInAggregation;

import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.Concept;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.Organization;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.TimeSpan;
import eu.europeana.enrichment.api.internal.AggregationFieldType;
import eu.europeana.enrichment.api.internal.FieldType;
import eu.europeana.enrichment.api.internal.ProxyFieldType;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.ReferenceTermContext;
import eu.europeana.enrichment.api.internal.SearchTermContext;
import eu.europeana.enrichment.rest.client.dereference.DereferencedEntities;
import eu.europeana.metis.schema.jibx.AboutType;
import eu.europeana.metis.schema.jibx.Aggregation;
import eu.europeana.metis.schema.jibx.RDF;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;



/**
 * Class that contains logic for converting class entity types and/or merging entities to {@link RDF}
 */
public class EntityMergeEngine {

  private static <I extends EnrichmentBase, T extends AboutType> T convertAndAddEntity(
      I inputEntity, Function<I, T> converter, Supplier<List<T>> listGetter,
      Consumer<List<T>> listSetter) {

    // Check if Entity already exists in the list. If so, return it. We don't overwrite.
    final T existingEntity = Optional.ofNullable(listGetter.get()).stream()
                                     .flatMap(Collection::stream)
                                     .filter(candidate -> inputEntity.getAbout().equals(candidate.getAbout())).findAny()
                                     .orElse(null);
    if (existingEntity != null) {
      return existingEntity;
    }

    // Convert and add the new entity.
    final T convertedEntity = converter.apply(inputEntity);
    if (listGetter.get() == null) {
      listSetter.accept(new ArrayList<>());
    }
    listGetter.get().add(convertedEntity);
    return convertedEntity;
  }

  /**
   * Converts a given enrichment entity to jibx entity and adds it to the provided rdf.
   * @param rdf the rdf
   * @param enrichmentBase the enrichment entity
   * @return the converted entity
   */
  public static AboutType convertAndAddEntity(RDF rdf, EnrichmentBase enrichmentBase) {
    return switch (enrichmentBase) {
      case Place place -> convertAndAddEntity(place, EntityConverterUtils::convertPlace, rdf::getPlaceList, rdf::setPlaceList);
      case Agent agent -> convertAndAddEntity(agent, EntityConverterUtils::convertAgent, rdf::getAgentList, rdf::setAgentList);
      case Concept concept ->
          convertAndAddEntity(concept, EntityConverterUtils::convertConcept, rdf::getConceptList, rdf::setConceptList);
      case TimeSpan timeSpan ->
          convertAndAddEntity(timeSpan, EntityConverterUtils::convertTimeSpan, rdf::getTimeSpanList, rdf::setTimeSpanList);
      case Organization organization -> convertAndAddEntity(organization,
          EntityConverterUtils::convertOrganization, rdf::getOrganizationList,
          rdf::setOrganizationList);
      case null, default -> throw new IllegalArgumentException("Unknown entity type: " + enrichmentBase.getClass());
    };
  }

  /**
   * Merge entities in a record.
   *
   * @param rdf The RDF to enrich
   * @param enrichmentBaseList The information to append
   * @param searchTermContext the search term context
   */
  public void mergeSearchEntities(RDF rdf, List<EnrichmentBase> enrichmentBaseList,
      SearchTermContext searchTermContext) {
    for (EnrichmentBase base : enrichmentBaseList) {
      final AboutType aboutType = convertAndAddEntity(rdf, base);
      if (isProxyFieldType(searchTermContext.getFieldTypes())) {
        appendLinkToEuropeanaProxy(rdf, aboutType.getAbout(),
            searchTermContext.getFieldTypes().stream().map(ProxyFieldType.class::cast)
                             .collect(Collectors.toSet()));
      } else {
        //Replace matching values in Aggregation
        replaceValueWithLinkInAggregation(rdf, aboutType.getAbout(), searchTermContext);
      }
    }
  }

  /**
   * Merge entities in a record.
   * <p>This method is when enrichment is performed where we <b>do</b> want to add the generated
   * links to the europeana proxy</p>
   *
   * @param rdf The RDF to enrich
   * @param enrichmentBaseList The information to append
   * @param referenceTermContext the reference term context
   */
  public void mergeReferenceEntities(RDF rdf, List<EnrichmentBase> enrichmentBaseList,
      ReferenceTermContext referenceTermContext) {
    for (EnrichmentBase base : enrichmentBaseList) {
      final AboutType aboutType = convertAndAddEntity(rdf, base);
      if (referenceTermContext != null) {
        final Set<ProxyFieldType> proxyFieldTypes = referenceTermContext.getFieldTypes().stream()
            .filter(field -> field instanceof ProxyFieldType)
            .map(ProxyFieldType.class::cast)
            .collect(Collectors.toSet());
        appendLinkToEuropeanaProxy(rdf, aboutType.getAbout(), proxyFieldTypes);
        // TODO merge to aggregation
      }
    }
  }

  /**
   * Merge entities in a record.
   * <p>This method is when enrichment is performed where we <b>do</b> want to add the generated
   * links to the europeana proxy</p>
   *
   * @param rdf The RDF to enrich
   * @param dereferencedEntitiesList The information to append
   */
  public void mergeReferenceEntitiesFromDereferencedEntities(RDF rdf, List<DereferencedEntities> dereferencedEntitiesList) {
    for (DereferencedEntities dereferencedEntities : dereferencedEntitiesList) {
      for (Map.Entry<ReferenceTerm, List<EnrichmentBase>> entry : dereferencedEntities.getReferenceTermListMap().entrySet()) {
        List<AboutType> aboutTypeList = entry.getValue()
                                             .stream()
                                             .map(base -> convertAndAddEntity(rdf, base))
                                             .toList();
        if (dereferencedEntities.getClassType().equals(Aggregation.class)) {
          replaceResourceWithLinkInAggregation(rdf, aboutTypeList, entry.getKey());
        }
      }
    }
  }

  private static <T extends FieldType<? extends AboutType>> boolean isProxyFieldType(Set<T> set) {
    //This shouldn't happen normally
    if (set == null || set.isEmpty()) {
      throw new IllegalArgumentException("Set cannot be empty");
    }
    final boolean allProxyFieldTypes = set.stream().allMatch(ProxyFieldType.class::isInstance);
    final boolean allAggregationFieldTypes = set.stream()
                                                .allMatch(AggregationFieldType.class::isInstance);

    if (!allProxyFieldTypes && !allAggregationFieldTypes) {
      throw new IllegalArgumentException("Invalid set");
    }

    return allProxyFieldTypes;
  }
}
