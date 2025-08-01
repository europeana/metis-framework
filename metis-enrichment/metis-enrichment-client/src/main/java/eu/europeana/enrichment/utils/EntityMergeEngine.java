package eu.europeana.enrichment.utils;

import static eu.europeana.enrichment.utils.RdfEntityUtils.appendLinkToEuropeanaProxy;
import static eu.europeana.enrichment.utils.RdfEntityUtils.replaceValueOfTermInAggregation;

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
import eu.europeana.enrichment.api.internal.TermContext;
import eu.europeana.enrichment.rest.client.dereference.DereferencedEntities;
import eu.europeana.metis.schema.jibx.AboutType;
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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;


/**
 * Class that contains logic for converting class entity types and/or merging entities to {@link RDF}
 */
public class EntityMergeEngine {

  private static final Map<EntityType, Class<? extends EnrichmentBase>> ENTITY_TYPE_MAP = Map.of(
      EntityType.AGENT, Agent.class,
      EntityType.CONCEPT, Concept.class,
      EntityType.PLACE, Place.class,
      EntityType.TIMESPAN, TimeSpan.class,
      EntityType.ORGANIZATION, Organization.class
  );

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
      case null -> throw new IllegalArgumentException("Unknown entity type: null.");
      default -> throw new IllegalArgumentException("Unknown entity type: " + enrichmentBase.getClass());
    };
  }

  /**
   * Filters the list of fields to include only those whose entity type matches that of the given
   * entity. The idea is that links should not be created to entities of the wrong type for the
   * field. This method also splits the fields into proxy fields and aggregation fields.
   *
   * @param entity The entity to link
   * @param fields The fields to link from.
   * @return The fields appropriate for linking to this entity.
   */
  private static Pair<Set<ProxyFieldType>, Set<AggregationFieldType>> filterFieldTypesForLinking(
      EnrichmentBase entity, Set<FieldType<?>> fields) {
    final List<EntityType> entityTypes = ENTITY_TYPE_MAP.entrySet().stream()
        .filter(entry -> entry.getValue().isAssignableFrom(entity.getClass()))
        .map(Map.Entry::getKey).distinct().toList();
    if (entityTypes.size() != 1) {
      throw new IllegalArgumentException("Unknown entity type: " + entity.getClass());
    }
    final Set<FieldType<?>> result = fields.stream()
        .filter(field -> field.getEntityType() == entityTypes.getFirst())
        .collect(Collectors.toSet());
    return ImmutablePair.of(result.stream().filter(ProxyFieldType.class::isInstance)
            .map(ProxyFieldType.class::cast).collect(Collectors.toSet()),
        result.stream().filter(AggregationFieldType.class::isInstance)
            .map(AggregationFieldType.class::cast).collect(Collectors.toSet()));
  }

  /**
   * Merge entities in a record.
   * <p>This method is when enrichment is performed where we <b>do</b> want to add the generated
   * links to the europeana proxy</p>
   *
   * @param rdf The RDF to enrich
   * @param enrichmentBaseList The information to append
   * @param termContext the reference/search term context
   */
  public void mergeEntities(RDF rdf, List<EnrichmentBase> enrichmentBaseList,
      TermContext termContext) {
    for (EnrichmentBase base : enrichmentBaseList) {

      // Filter the fields into those suitable for the type of the entity to add.
      final Pair<Set<ProxyFieldType>, Set<AggregationFieldType>> fields =
          filterFieldTypesForLinking(base, termContext.getFieldTypes());
      if (fields.getLeft().isEmpty() && fields.getRight().isEmpty()) {
        continue;
      }

      // Convert and add the entity to the record.
      final AboutType entity = convertAndAddEntity(rdf, base);

      // Append or update the references to the entity in the record.
      if (!fields.getLeft().isEmpty()) {
        appendLinkToEuropeanaProxy(rdf, entity.getAbout(), fields.getLeft());
      }
      if (!fields.getRight().isEmpty()) {
        replaceValueOfTermInAggregation(rdf, entity.getAbout(), fields.getRight(),
            termContext::valueEquals);
      }
    }
  }

  /**
   * Merge entities in a record without creating any links to the entities.
   *
   * @param rdf The RDF to enrich
   * @param dereferencedEntities The information to append
   */
  public void convertAndAddAllEntities(RDF rdf, DereferencedEntities dereferencedEntities) {
    for (Map.Entry<ReferenceTerm, List<EnrichmentBase>> entry : dereferencedEntities.getReferenceTermListMap().entrySet()) {
      entry.getValue().forEach(base -> convertAndAddEntity(rdf, base));
    }
  }
}
