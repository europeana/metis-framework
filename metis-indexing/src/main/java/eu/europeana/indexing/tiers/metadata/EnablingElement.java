package eu.europeana.indexing.tiers.metadata;

import eu.europeana.corelib.definitions.jibx.AboutType;
import eu.europeana.corelib.definitions.jibx.AgentType;
import eu.europeana.corelib.definitions.jibx.Concept;
import eu.europeana.corelib.definitions.jibx.PlaceType;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.TimeSpanType;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
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
      Stream.of(EnablingElementGroup.TEMPORAL, EnablingElementGroup.PERSONAL)
          .collect(Collectors.toSet())),
  DC_SUBJECT(ResourceLinkFromProxy.SUBJECT, Stream
      .of(EnablingElementGroup.CONCEPTUAL, EnablingElementGroup.PERSONAL,
          EnablingElementGroup.GEOGRAPHICAL).collect(Collectors.toSet()));

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
  }

  private final ResourceLinkFromProxy property;
  private final EnablingElementGroup fixedGroup;
  private final Set<EnablingElementGroup> candidateGroups = EnumSet
      .noneOf(EnablingElementGroup.class);

  /*  This constructor is for predicates with a given constant group. */
  EnablingElement(ResourceLinkFromProxy property, EnablingElementGroup fixedGroup) {
    this.property = property;
    this.fixedGroup = fixedGroup;
  }

  /*  This constructor is for predicates with a resource value and a list of group candidates. */
  EnablingElement(ResourceLinkFromProxy property, Set<EnablingElementGroup> candidateGroups) {
    this.property = property;
    this.fixedGroup = null;
    this.candidateGroups.addAll(candidateGroups);
  }

  /**
   * This method analyzes the proxy for the presence of this enabling element.
   *
   * @param proxies The proxies to analyze.
   * @param contextualObjects The URLs of contextual objects that are present in the record, mapped
   * to the contextual classes they represent.
   * @return If this enabling element is present, the groups with which this element is present. If
   * this enabling element is not present, this method returns the empty set.
   */
  public Set<EnablingElementGroup> analyze(Collection<ProxyType> proxies,
      Map<String, Set<Class<? extends AboutType>>> contextualObjects) {
    final Set<EnablingElementGroup> result;
    if (fixedGroup != null) {
      // Fixed group: check that the property has a link or value, and award the group.
      Predicate<ProxyType> hasLiteralOrLink = proxy -> Stream
          .concat(property.getLinkAndValueGetter().getValues(proxy),
              property.getLinkAndValueGetter().getLinks(proxy)).findAny().isPresent();
      result = proxies.stream().anyMatch(hasLiteralOrLink) ? Collections.singleton(fixedGroup)
          : Collections.emptySet();
    } else {
      // Multiple candidate groups: for each link and each candidate group determine whether the map
      // of contextual objects contain the url with a class associated with the given group.
      final BiPredicate<String, EnablingElementGroup> contextualObjectChecker = (url, group) -> Optional
          .of(contextualObjects).map(map -> map.get(url)).map(Set::stream).orElseGet(Stream::empty)
          .anyMatch(group.contextualClass::isAssignableFrom);
      result = proxies.stream().flatMap(proxy -> property.getLinkAndValueGetter().getLinks(proxy))
          .flatMap(url -> candidateGroups.stream()
              .filter(group -> contextualObjectChecker.test(url, group)))
          .collect(Collectors.toSet());
    }
    return result;
  }
}
