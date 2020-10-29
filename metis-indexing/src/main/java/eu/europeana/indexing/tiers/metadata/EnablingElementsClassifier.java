package eu.europeana.indexing.tiers.metadata;

import eu.europeana.metis.schema.jibx.AboutType;
import eu.europeana.metis.schema.jibx.ProxyType;
import eu.europeana.indexing.tiers.metadata.EnablingElement.EnablingElementGroup;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.model.TierClassifier;
import eu.europeana.indexing.utils.RdfWrapper;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Classifier for counting enabling elements.
 */
public class EnablingElementsClassifier implements TierClassifier<MetadataTier> {


  private static final int MIN_ELEMENTS_TIER_A = 1;
  private static final int MIN_ELEMENTS_TIER_B = 3;
  private static final int MIN_ELEMENTS_TIER_C = 4;
  private static final int MIN_GROUPS_TIER_A = 1;
  private static final int MIN_GROUPS_TIER_B = 2;
  private static final int MIN_GROUPS_TIER_C = 2;

  @Override
  public MetadataTier classify(RdfWrapper entity) {

    // Perform the element inventory
    final EnablingElementInventory inventory = performEnablingElementInventory(entity);
    final int elementTypeCount = inventory.getElementTypeCount();
    final int groupTypeCount = inventory.getGroupTypeCount();

    // Compute the tier.
    final MetadataTier tier;
    if (groupTypeCount >= MIN_GROUPS_TIER_C && elementTypeCount >= MIN_ELEMENTS_TIER_C) {
      tier = MetadataTier.TC;
    } else if (groupTypeCount >= MIN_GROUPS_TIER_B && elementTypeCount >= MIN_ELEMENTS_TIER_B) {
      tier = MetadataTier.TB;
    } else if (groupTypeCount >= MIN_GROUPS_TIER_A && elementTypeCount >= MIN_ELEMENTS_TIER_A) {
      tier = MetadataTier.TA;
    } else {
      tier = MetadataTier.T0;
    }

    // Done
    return tier;
  }

  EnablingElementInventory performEnablingElementInventory(RdfWrapper entity) {

    // Gather the contextual objects in one map.
    final Map<String, Set<Class<? extends AboutType>>> contextualObjectMap = createContextualObjectMap(
        entity);

    // Go by all the enabling elements and match them.
    final Set<EnablingElement> elements = EnumSet.noneOf(EnablingElement.class);
    final Set<EnablingElementGroup> groups = EnumSet.noneOf(EnablingElementGroup.class);
    for (EnablingElement element : EnablingElement.values()) {
      final Set<EnablingElementGroup> groupsToAdd = analyzeForElement(element,
          entity.getProviderProxies(), contextualObjectMap);
      if (!groupsToAdd.isEmpty()) {
        elements.add(element);
        groups.addAll(groupsToAdd);
      }
    }

    // Done
    return new EnablingElementInventory(elements.size(), groups.size());
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

  Set<EnablingElementGroup> analyzeForElement(EnablingElement element, List<ProxyType> proxies,
      Map<String, Set<Class<? extends AboutType>>> contextualObjectMap) {
    return element.analyze(proxies, contextualObjectMap);
  }

  static class EnablingElementInventory {

    private final int elementTypeCount;
    private final int groupTypeCount;

    EnablingElementInventory(int elementTypeCount, int groupTypeCount) {
      this.elementTypeCount = elementTypeCount;
      this.groupTypeCount = groupTypeCount;
    }

    int getElementTypeCount() {
      return elementTypeCount;
    }

    int getGroupTypeCount() {
      return groupTypeCount;
    }
  }
}
