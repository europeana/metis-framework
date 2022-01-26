package eu.europeana.indexing.tiers.metadata;

import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.model.TierClassifierBreakdown;
import eu.europeana.indexing.tiers.view.EnablingElementsBreakdown;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.jibx.AboutType;
import eu.europeana.metis.schema.jibx.ProxyType;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * Classifier for counting enabling elements.
 */
public class EnablingElementsClassifier implements TierClassifierBreakdown<EnablingElementsBreakdown> {

  private static final int MIN_ELEMENTS_TIER_A = 1;
  private static final int MIN_ELEMENTS_TIER_B = 3;
  private static final int MIN_ELEMENTS_TIER_C = 4;
  private static final int MIN_GROUPS_TIER_A = 1;
  private static final int MIN_GROUPS_TIER_B = 2;
  private static final int MIN_GROUPS_TIER_C = 2;

  @Override
  public EnablingElementsBreakdown classifyBreakdown(RdfWrapper entity) {

    // Perform the element inventory
    final EnablingElementInventory inventory = performEnablingElementInventory(entity);

    final MetadataTier metadataTier = calculateMetadataTier(inventory);
    final Set<String> distinctEnablingElementsList = inventory.getElements().stream().map(EnablingElement::getTypedClass)
                                                              .map(RdfConversionUtils::getQualifiedElementNameForClass)
                                                              .collect(Collectors.toSet());
    final Set<String> metadataGroupsList = inventory.getGroups().stream().map(ContextualClassGroup::getContextualClass)
                                                    .map(RdfConversionUtils::getQualifiedElementNameForClass)
                                                    .collect(Collectors.toSet());

    return new EnablingElementsBreakdown(distinctEnablingElementsList, metadataGroupsList, metadataTier);
  }

  @NotNull
  private MetadataTier calculateMetadataTier(EnablingElementInventory inventory) {
    final int elementTypeCount = inventory.getElements().size();
    final int groupTypeCount = inventory.getGroups().size();
    final MetadataTier metadataTier;
    if (groupTypeCount >= MIN_GROUPS_TIER_C && elementTypeCount >= MIN_ELEMENTS_TIER_C) {
      metadataTier = MetadataTier.TC;
    } else if (groupTypeCount >= MIN_GROUPS_TIER_B && elementTypeCount >= MIN_ELEMENTS_TIER_B) {
      metadataTier = MetadataTier.TB;
    } else if (groupTypeCount >= MIN_GROUPS_TIER_A && elementTypeCount >= MIN_ELEMENTS_TIER_A) {
      metadataTier = MetadataTier.TA;
    } else {
      metadataTier = MetadataTier.T0;
    }
    return metadataTier;
  }

  static class EnablingElementInventory {

    private final Set<EnablingElement> elements;
    private final Set<ContextualClassGroup> groups;

    EnablingElementInventory(Set<EnablingElement> elements, Set<ContextualClassGroup> groups) {
      this.elements = elements == null ? new HashSet<>() : new HashSet<>(elements);
      this.groups = groups == null ? new HashSet<>() : new HashSet<>(groups);
    }

    public Set<EnablingElement> getElements() {
      return new HashSet<>(elements);
    }

    public Set<ContextualClassGroup> getGroups() {
      return new HashSet<>(groups);
    }
  }

  EnablingElementInventory performEnablingElementInventory(RdfWrapper entity) {

    // Gather the contextual objects in one map.
    final Map<String, Set<Class<? extends AboutType>>> contextualObjectMap = createContextualObjectMap(
        entity);

    // Go by all the enabling elements and match them.
    final Set<EnablingElement> elements = EnumSet.noneOf(EnablingElement.class);
    final Set<ContextualClassGroup> groups = EnumSet.noneOf(ContextualClassGroup.class);

    for (EnablingElement element : EnablingElement.values()) {
      final Set<ContextualClassGroup> groupsToAdd = analyzeForElement(element,
          entity.getProviderProxies(), contextualObjectMap);
      if (!groupsToAdd.isEmpty()) {
        elements.add(element);
        groups.addAll(groupsToAdd);
      }
    }

    return new EnablingElementInventory(elements, groups);
  }

  Set<ContextualClassGroup> analyzeForElement(EnablingElement element, List<ProxyType> proxies,
      Map<String, Set<Class<? extends AboutType>>> contextualObjectMap) {
    return element.analyze(proxies, contextualObjectMap);
  }

  Map<String, Set<Class<? extends AboutType>>> createContextualObjectMap(RdfWrapper entity) {

    // Collect the objects we are interested in.
    final List<AboutType> contextualObjects = new ArrayList<>(entity.getAgents());
    contextualObjects.addAll(entity.getConcepts());
    contextualObjects.addAll(entity.getPlaces());
    contextualObjects.addAll(entity.getTimeSpans());

    // Group them into a map by about value.
    // Note: no need to check for lists or objects being null or about values being blank.
    return contextualObjects.stream().collect(Collectors.groupingBy(AboutType::getAbout,
        Collectors.mapping(AboutType::getClass, Collectors.toSet())));
  }
}
