package eu.europeana.indexing.tiers.metadata;

import eu.europeana.metis.schema.jibx.AboutType;
import eu.europeana.metis.schema.jibx.AgentType;
import eu.europeana.metis.schema.jibx.Concept;
import eu.europeana.metis.schema.jibx.PlaceType;
import eu.europeana.metis.schema.jibx.ProxyType;
import eu.europeana.metis.schema.jibx.TimeSpanType;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This enum contains the enabling elements, including functionality for determining if a {@link
 * ProxyType} contains one such element and which {@link EnablingElementGroup}(s) the element in
 * question belongs to. Note that an element doesn't necessarily belong to exactly one group, and it
 * may depend on the contextual classes present in the record.
 */
public enum EnablingElement {

  // Temporal properties
  DCTERMS_CREATED(ResourceLinkFromProxy.CREATED, EnablingElementGroup.TEMPORAL),
  DCTERMS_ISSUED(ResourceLinkFromProxy.ISSUED, EnablingElementGroup.TEMPORAL),
  DCTERMS_TEMPORAL(ResourceLinkFromProxy.TEMPORAL, EnablingElementGroup.TEMPORAL),

  // Conceptual properties
  DC_FORMAT(ResourceLinkFromProxy.FORMAT, EnablingElementGroup.CONCEPTUAL),
  DC_TYPE(ResourceLinkFromProxy.TYPE, EnablingElementGroup.CONCEPTUAL),
  DCTERMS_MEDIUM(ResourceLinkFromProxy.MEDIUM, EnablingElementGroup.CONCEPTUAL),

  // Personal properties
  DC_CREATOR(ResourceLinkFromProxy.CREATOR, EnablingElementGroup.PERSONAL),
  DC_CONTRIBUTOR(ResourceLinkFromProxy.CONTRIBUTOR, EnablingElementGroup.PERSONAL),
  DC_PUBLISHER(ResourceLinkFromProxy.PUBLISHER, EnablingElementGroup.PERSONAL),

  // Geographical properties
  DCTERMS_SPATIAL(ResourceLinkFromProxy.SPATIAL, EnablingElementGroup.GEOGRAPHICAL),
  EDM_CURRENT_LOCATION(ResourceLinkFromProxy.CURRENT_LOCATION, EnablingElementGroup.GEOGRAPHICAL),

  // General properties
  EDM_HAS_MET(ResourceLinkFromProxy.HAS_MET,
      EnumSet.of(EnablingElementGroup.TEMPORAL, EnablingElementGroup.PERSONAL)),
  DC_SUBJECT(ResourceLinkFromProxy.SUBJECT, EnumSet.of(EnablingElementGroup.CONCEPTUAL,
      EnablingElementGroup.PERSONAL, EnablingElementGroup.GEOGRAPHICAL));

  /**
   * This enum contains all the groups of enabling elements, including the contextual class with
   * which this group is associated.
   */
  public enum EnablingElementGroup {

    TEMPORAL(TimeSpanType.class),
    CONCEPTUAL(Concept.class),
    PERSONAL(AgentType.class),
    GEOGRAPHICAL(PlaceType.class);

    private final Class<? extends AboutType> contextualClass;

    EnablingElementGroup(Class<? extends AboutType> contextualClass) {
      this.contextualClass = contextualClass;
    }

    Class<? extends AboutType> getContextualClass() {
      return contextualClass;
    }
  }

  private final ElementGroupDetector elementGroupDetector;

  /**
   * This constructor is for predicates with a given constant group: an occurrence of this element
   * (either a link or a value) always triggers this group.
   *
   * @param property The property.
   * @param fixedGroup The group to assign an occurrence to.
   */
  EnablingElement(ResourceLinkFromProxy property, EnablingElementGroup fixedGroup) {
    final Predicate<ProxyType> hasLiteralOrLink = proxy -> Stream
        .concat(property.getLinkAndValueGetter().getValues(proxy),
            property.getLinkAndValueGetter().getLinks(proxy)).findAny().isPresent();
    this.elementGroupDetector = (proxies, objectTypesByUri) ->
        proxies.stream().anyMatch(hasLiteralOrLink) ? Collections.singleton(fixedGroup)
            : Collections.emptySet();
  }

  /**
   * This constructor is for predicates with a resource value and a list of group candidates: an
   * occurrence of this element triggers one of the candidate groups only if the element links to a
   * contextual class belonging to this candidate group.
   *
   * @param property The property.
   * @param candidateGroups The candidate groups.
   */
  EnablingElement(ResourceLinkFromProxy property, Set<EnablingElementGroup> candidateGroups) {
    this.elementGroupDetector = (proxies, objectTypesByUri) -> {
      final BiPredicate<String, EnablingElementGroup> urlHasGroup = (url, group) -> Optional
          .ofNullable(objectTypesByUri.apply(url)).map(Set::stream).orElseGet(Stream::empty)
          .anyMatch(group.contextualClass::isAssignableFrom);
      return proxies.stream().flatMap(proxy -> property.getLinkAndValueGetter().getLinks(proxy))
          .flatMap(url -> candidateGroups.stream().filter(group -> urlHasGroup.test(url, group)))
          .collect(Collectors.toSet());
    };
  }

  /**
   * This method analyzes the proxy for the presence of this enabling element.
   *
   * @param proxies The proxies to analyze.
   * @param contextualObjectTypes The URLs of contextual objects that are present in the record,
   * mapped to the contextual classes they represent.
   * @return If this enabling element is present, the groups with which this element is present. If
   * this enabling element is not present, this method returns the empty set.
   */
  public Set<EnablingElementGroup> analyze(Collection<ProxyType> proxies,
      Map<String, Set<Class<? extends AboutType>>> contextualObjectTypes) {
    return this.elementGroupDetector.apply(proxies, contextualObjectTypes::get);
  }

  /**
   * This interface represents the functionality of detecting an element group given a collection of
   * proxies as well as functionality to evaluate a link to a contextual class.
   */
  @FunctionalInterface
  private interface ElementGroupDetector extends
      BiFunction<Collection<ProxyType>, Function<String, Set<Class<? extends AboutType>>>, Set<EnablingElementGroup>> {

    /**
     * Detect an element group.
     *
     * @param proxies The proxy objects in which to look.
     * @param stringSetFunction A mechanism to match a resource link to a contextual resource type.
     * @return The element groups detected in the proxy, or the empty set if none are found.
     */
    @Override
    Set<EnablingElementGroup> apply(Collection<ProxyType> proxies,
        Function<String, Set<Class<? extends AboutType>>> stringSetFunction);
  }
}
