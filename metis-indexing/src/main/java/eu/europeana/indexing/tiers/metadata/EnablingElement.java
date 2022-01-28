package eu.europeana.indexing.tiers.metadata;

import eu.europeana.metis.schema.jibx.AboutType;
import eu.europeana.metis.schema.jibx.Contributor;
import eu.europeana.metis.schema.jibx.Created;
import eu.europeana.metis.schema.jibx.Creator;
import eu.europeana.metis.schema.jibx.CurrentLocation;
import eu.europeana.metis.schema.jibx.Format;
import eu.europeana.metis.schema.jibx.HasMet;
import eu.europeana.metis.schema.jibx.Issued;
import eu.europeana.metis.schema.jibx.Medium;
import eu.europeana.metis.schema.jibx.ProxyType;
import eu.europeana.metis.schema.jibx.Publisher;
import eu.europeana.metis.schema.jibx.Spatial;
import eu.europeana.metis.schema.jibx.Subject;
import eu.europeana.metis.schema.jibx.Temporal;
import eu.europeana.metis.schema.jibx.Type;
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
 * This enum contains the enabling elements, including functionality for determining if a {@link ProxyType} contains one such
 * element and which {@link ContextualClassGroup}(s) the element in question belongs to. Note that an element doesn't necessarily
 * belong to exactly one group, and it may depend on the contextual classes present in the record.
 */
public enum EnablingElement {

  // Temporal properties
  DCTERMS_CREATED(Created.class, ResourceLinkFromProxy.CREATED, ContextualClassGroup.TEMPORAL),
  DCTERMS_ISSUED(Issued.class, ResourceLinkFromProxy.ISSUED, ContextualClassGroup.TEMPORAL),
  DCTERMS_TEMPORAL(Temporal.class, ResourceLinkFromProxy.TEMPORAL, ContextualClassGroup.TEMPORAL),

  // Conceptual properties
  DC_FORMAT(Format.class, ResourceLinkFromProxy.FORMAT, ContextualClassGroup.CONCEPTUAL),
  DC_TYPE(Type.class, ResourceLinkFromProxy.TYPE, ContextualClassGroup.CONCEPTUAL),
  DCTERMS_MEDIUM(Medium.class, ResourceLinkFromProxy.MEDIUM, ContextualClassGroup.CONCEPTUAL),

  // Personal properties
  DC_CREATOR(Creator.class, ResourceLinkFromProxy.CREATOR, ContextualClassGroup.PERSONAL),
  DC_CONTRIBUTOR(Contributor.class, ResourceLinkFromProxy.CONTRIBUTOR, ContextualClassGroup.PERSONAL),
  DC_PUBLISHER(Publisher.class, ResourceLinkFromProxy.PUBLISHER, ContextualClassGroup.PERSONAL),

  // Geographical properties
  DCTERMS_SPATIAL(Spatial.class, ResourceLinkFromProxy.SPATIAL, ContextualClassGroup.GEOGRAPHICAL),
  EDM_CURRENT_LOCATION(CurrentLocation.class, ResourceLinkFromProxy.CURRENT_LOCATION, ContextualClassGroup.GEOGRAPHICAL),

  // General properties
  EDM_HAS_MET(HasMet.class, ResourceLinkFromProxy.HAS_MET,
      EnumSet.of(ContextualClassGroup.TEMPORAL, ContextualClassGroup.PERSONAL)),
  DC_SUBJECT(Subject.class, ResourceLinkFromProxy.SUBJECT, EnumSet.of(ContextualClassGroup.CONCEPTUAL,
      ContextualClassGroup.PERSONAL, ContextualClassGroup.GEOGRAPHICAL));

  private final Class<?> typedClass;

  private final ElementGroupDetector elementGroupDetector;

  /**
   * This constructor is for predicates with a given constant group: an occurrence of this element (either a link or a value)
   * always triggers this group.
   *
   * @param property The property.
   * @param fixedGroup The group to assign an occurrence to.
   */
  EnablingElement(Class<?> typedClass, ResourceLinkFromProxy property, ContextualClassGroup fixedGroup) {
    this.typedClass = typedClass;
    final Predicate<ProxyType> hasLiteralOrLink = proxy -> Stream
        .concat(property.getLinkAndValueGetter().getValues(proxy),
            property.getLinkAndValueGetter().getLinks(proxy)).findAny().isPresent();

    this.elementGroupDetector = (proxies, objectTypesByUri) ->
        proxies.stream().anyMatch(hasLiteralOrLink) ? Collections.singleton(fixedGroup)
            : Collections.emptySet();
  }

  /**
   * This constructor is for predicates with a resource value and a list of group candidates: an occurrence of this element
   * triggers one of the candidate groups only if the element links to a contextual class belonging to this candidate group.
   *
   * @param property The property.
   * @param candidateGroups The candidate groups.
   */
  EnablingElement(Class<?> typedClass, ResourceLinkFromProxy property, Set<ContextualClassGroup> candidateGroups) {
    this.typedClass = typedClass;
    this.elementGroupDetector = (proxies, objectTypesByUri) -> {
      final BiPredicate<String, ContextualClassGroup> urlHasGroup = (url, group) -> Optional
          .ofNullable(objectTypesByUri.apply(url)).stream().flatMap(Collection::stream)
          .anyMatch(group.getContextualClass()::isAssignableFrom);

      return proxies.stream().flatMap(proxy -> property.getLinkAndValueGetter().getLinks(proxy))
                    .flatMap(url -> candidateGroups.stream().filter(group -> urlHasGroup.test(url, group)))
                    .collect(Collectors.toSet());
    };
  }

  public Class<?> getTypedClass() {
    return typedClass;
  }

  /**
   * This method analyzes the proxy for the presence of this enabling element.
   *
   * @param proxies The proxies to analyze.
   * @param contextualObjectTypes The URLs of contextual objects that are present in the record, mapped to the contextual classes
   * they represent.
   * @return If this enabling element is present, the groups with which this element is present. If this enabling element is not
   * present, this method returns the empty set.
   */
  public Set<ContextualClassGroup> analyze(Collection<ProxyType> proxies,
      Map<String, Set<Class<? extends AboutType>>> contextualObjectTypes) {
    return this.elementGroupDetector.apply(proxies, contextualObjectTypes::get);
  }

  /**
   * This interface represents the functionality of detecting an element group given a collection of proxies as well as
   * functionality to evaluate a link to a contextual class.
   */
  @FunctionalInterface
  private interface ElementGroupDetector extends
      BiFunction<Collection<ProxyType>, Function<String, Set<Class<? extends AboutType>>>, Set<ContextualClassGroup>> {

    /**
     * Detect an element group.
     *
     * @param proxies The proxy objects in which to look.
     * @param stringSetFunction A mechanism to match a resource link to a contextual resource type.
     * @return The element groups detected in the proxy, or the empty set if none are found.
     */
    @Override
    Set<ContextualClassGroup> apply(Collection<ProxyType> proxies,
        Function<String, Set<Class<? extends AboutType>>> stringSetFunction);
  }
}
