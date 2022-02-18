package eu.europeana.indexing.tiers.metadata;

import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.model.TierClassifierBreakdown;
import eu.europeana.indexing.tiers.view.ContextualClassesBreakdown;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.jibx.AboutType;
import eu.europeana.metis.schema.jibx.AgentType;
import eu.europeana.metis.schema.jibx.Concept;
import eu.europeana.metis.schema.jibx.Concept.Choice;
import eu.europeana.metis.schema.jibx.Lat;
import eu.europeana.metis.schema.jibx.LiteralType;
import eu.europeana.metis.schema.jibx.PlaceType;
import eu.europeana.metis.schema.jibx.ProxyType;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType.Resource;
import eu.europeana.metis.schema.jibx.ResourceType;
import eu.europeana.metis.schema.jibx.TimeSpanType;
import eu.europeana.metis.schema.jibx._Long;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

/**
 * Classifier for contextual class completeness.
 */
public class ContextualClassesClassifier implements TierClassifierBreakdown<ContextualClassesBreakdown> {

  private static final RdfConversionUtils rdfConversionUtils = new RdfConversionUtils();

  private static Set<String> getResourceLinks(ProxyType proxy) {
    return Stream.of(ResourceLinkFromProxy.values())
                 .map(ResourceLinkFromProxy::getLinkAndValueGetter)
                 .flatMap(link -> link.getLinks(proxy)).collect(Collectors.toSet());
  }

  private static <T extends AboutType> boolean hasQualifiedEntities(List<T> entities,
      Set<String> referencedEntities, Predicate<T> entityQualifier) {
    return entities.stream().filter(entity -> referencedEntities.contains(entity.getAbout()))
                   .anyMatch(entityQualifier);
  }

  @Override
  public ContextualClassesBreakdown classifyBreakdown(RdfWrapper entity) {

    // Count the contextual class type occurrences of qualifying contextual classes.
    final ContextualClassesStatistics contextualClassesStatistics = countQualifyingContextualClassTypes(entity);

    // perform classification
    final MetadataTier metadataTier;
    if (contextualClassesStatistics.getCompleteContextualResources() > 1) {
      metadataTier = MetadataTier.TC;
    } else if (contextualClassesStatistics.getCompleteContextualResources() == 1) {
      metadataTier = MetadataTier.TB;
    } else {
      metadataTier = MetadataTier.TA;
    }

    final Set<String> uniqueContextualClasses = contextualClassesStatistics.getDistinctClassesSet().stream()
                                                                           .map(ContextualClassGroup::getContextualClass)
                                                                           .map(
                                                                               rdfConversionUtils::getQualifiedElementNameForClass)
                                                                           .collect(Collectors.toSet());

    return new ContextualClassesBreakdown(contextualClassesStatistics.getCompleteContextualResources(), uniqueContextualClasses,
        metadataTier);
  }

  static class ContextualClassesStatistics {

    private final int completeContextualResources;
    private final Set<ContextualClassGroup> distinctClassesSet;

    ContextualClassesStatistics(int completeContextualResources, Set<ContextualClassGroup> distinctClassesSet) {
      this.completeContextualResources = completeContextualResources;
      this.distinctClassesSet = distinctClassesSet == null ? new HashSet<>() : new HashSet<>(distinctClassesSet);
    }

    public int getCompleteContextualResources() {
      return completeContextualResources;
    }

    public Set<ContextualClassGroup> getDistinctClassesSet() {
      return new HashSet<>(distinctClassesSet);
    }
  }

  ContextualClassesStatistics countQualifyingContextualClassTypes(RdfWrapper entity) {

    // Get all the entity references from the provider proxies.
    final Set<String> referencedEntities = entity.getProviderProxies().stream()
                                                 .map(ContextualClassesClassifier::getResourceLinks).flatMap(Set::stream)
                                                 .collect(Collectors.toSet());

    // Count the entity types that are referenced and that qualify according to the criteria.
    int contextualClassCount = 0;
    final Set<ContextualClassGroup> uniqueContextualClasses = EnumSet.noneOf(ContextualClassGroup.class);
    if (hasQualifiedEntities(entity.getAgents(), referencedEntities, this::entityQualifies)) {
      contextualClassCount++;
      uniqueContextualClasses.add(ContextualClassGroup.PERSONAL);
    }
    if (hasQualifiedEntities(entity.getConcepts(), referencedEntities, this::entityQualifies)) {
      contextualClassCount++;
      uniqueContextualClasses.add(ContextualClassGroup.CONCEPTUAL);
    }
    if (hasQualifiedEntities(entity.getPlaces(), referencedEntities, this::entityQualifies)) {
      contextualClassCount++;
      uniqueContextualClasses.add(ContextualClassGroup.GEOGRAPHICAL);
    }
    if (hasQualifiedEntities(entity.getTimeSpans(), referencedEntities, this::entityQualifies)) {
      contextualClassCount++;
      uniqueContextualClasses.add(ContextualClassGroup.TEMPORAL);
    }

    return new ContextualClassesStatistics(contextualClassCount, uniqueContextualClasses);
  }

  boolean entityQualifies(AgentType agent) {
    boolean hasPrefLabel = hasLiteralProperty(agent.getPrefLabelList());
    boolean hasBeginInfo = hasProperty(agent.getBegin()) || hasProperty(agent.getDateOfBirth())
        || hasResourceOrLiteralProperty(agent.getPlaceOfBirthList());
    boolean hasEndInfo = hasProperty(agent.getEnd()) || hasProperty(agent.getDateOfDeath())
        || hasResourceOrLiteralProperty(agent.getPlaceOfDeathList());
    boolean hasProfessionOrOccupation = hasResourceOrLiteralProperty(
        agent.getProfessionOrOccupationList());
    return hasPrefLabel && (hasBeginInfo || hasEndInfo || hasProfessionOrOccupation);
  }

  boolean entityQualifies(Concept concept) {
    if (concept.getChoiceList() == null) {
      return false;
    }
    final boolean hasPrefLabel = concept.getChoiceList().stream().filter(Choice::ifPrefLabel)
                                        .map(Choice::getPrefLabel).anyMatch(this::hasProperty);
    final boolean hasRelationOrNote = concept.getChoiceList().stream()
                                             .anyMatch(this::hasRelationOrNote);
    return hasPrefLabel && hasRelationOrNote;
  }

  private boolean hasRelationOrNote(Choice choice) {
    final boolean hasNote = choice.ifNote() && hasProperty(choice.getNote());
    final boolean hasHierarchicalMatch = (choice.ifBroader() && hasProperty(choice.getBroader())) ||
        (choice.ifNarrower() && hasProperty(choice.getNarrower()));
    final boolean hasParityMatch = (choice.ifExactMatch() && hasProperty(choice.getExactMatch())) ||
        (choice.ifCloseMatch() && hasProperty(choice.getCloseMatch()));
    final boolean hasRelation = choice.ifRelated() && hasProperty(choice.getRelated());
    return hasNote || hasHierarchicalMatch || hasParityMatch || hasRelation;
  }

  boolean entityQualifies(PlaceType place) {
    boolean hasPrefLabel = hasLiteralProperty(place.getPrefLabelList());
    boolean hasLat = Optional.ofNullable(place.getLat()).map(Lat::getLat).isPresent();
    boolean hasLong = Optional.ofNullable(place.getLong()).map(_Long::getLong).isPresent();
    return (hasPrefLabel && hasLat && hasLong);
  }

  boolean entityQualifies(TimeSpanType timeSpan) {
    boolean hasBegin = hasProperty(timeSpan.getBegin());
    boolean hasEnd = hasProperty(timeSpan.getEnd());
    return (hasBegin && hasEnd);
  }

  boolean hasProperty(LiteralType literal) {
    return literal != null && StringUtils.isNotBlank(literal.getString());
  }

  boolean hasProperty(ResourceOrLiteralType resourceOrLiteral) {
    final Optional<ResourceOrLiteralType> property = Optional.ofNullable(resourceOrLiteral);
    final boolean hasLiteral = property.map(ResourceOrLiteralType::getString)
                                       .filter(StringUtils::isNotBlank).isPresent();
    final boolean hasResource = property.map(ResourceOrLiteralType::getResource)
                                        .map(Resource::getResource).filter(StringUtils::isNotBlank).isPresent();
    return hasLiteral || hasResource;
  }

  boolean hasProperty(ResourceType resource) {
    return resource != null && StringUtils.isNotBlank(resource.getResource());
  }

  boolean hasLiteralProperty(List<? extends LiteralType> literals) {
    return literals != null && literals.stream().anyMatch(this::hasProperty);
  }

  boolean hasResourceOrLiteralProperty(
      List<? extends ResourceOrLiteralType> objects) {
    return objects != null && objects.stream().anyMatch(this::hasProperty);
  }
}
