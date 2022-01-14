package eu.europeana.indexing.tiers.metadata;

import eu.europeana.indexing.tiers.metadata.EnablingElement.EnablingElementGroup;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.model.TierClassifier;
import eu.europeana.indexing.tiers.view.EnablingElements;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.jibx.AboutType;
import eu.europeana.metis.schema.jibx.ProxyType;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * Classifier for counting enabling elements.
 */
public class EnablingElementsClassifier implements TierClassifier<MetadataTier, EnablingElements> {

  private static final int MIN_ELEMENTS_TIER_A = 1;
  private static final int MIN_ELEMENTS_TIER_B = 3;
  private static final int MIN_ELEMENTS_TIER_C = 4;
  private static final int MIN_GROUPS_TIER_A = 1;
  private static final int MIN_GROUPS_TIER_B = 2;
  private static final int MIN_GROUPS_TIER_C = 2;

  @Override
  public TierClassification<MetadataTier, EnablingElements> classify(RdfWrapper entity) {

    // Perform the element inventory
    final EnablingElementInventory inventory = performEnablingElementInventory(entity);

    final MetadataTier metadataTier = calculateMetadataTier(inventory);
    final List<String> distinctEnablingElementsList = inventory.getElements().stream().map(EnablingElement::name)
                                                               .collect(Collectors.toList());
    final List<String> metadataGroupsList = inventory.getGroups().stream().map(EnablingElementGroup::name)
                                                     .collect(Collectors.toList());
    final EnablingElements enablingElements = new EnablingElements(distinctEnablingElementsList, metadataGroupsList,
        metadataTier);

    return new TierClassification<>(metadataTier, enablingElements);
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

    final Set<EnablingElement> elements;
    final Set<EnablingElementGroup> groups;

    EnablingElementInventory(Set<EnablingElement> elements, Set<EnablingElementGroup> groups) {
      this.elements = elements;
      this.groups = groups;
    }

    public Set<EnablingElement> getElements() {
      return elements;
    }

    public Set<EnablingElementGroup> getGroups() {
      return groups;
    }
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

    return new EnablingElementInventory(elements, groups);
  }

  Set<EnablingElementGroup> analyzeForElement(EnablingElement element, List<ProxyType> proxies,
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
