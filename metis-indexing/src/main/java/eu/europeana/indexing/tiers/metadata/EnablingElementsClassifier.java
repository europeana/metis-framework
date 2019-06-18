package eu.europeana.indexing.tiers.metadata;

import eu.europeana.corelib.definitions.jibx.AboutType;
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

    // Gather the contextual objects in one map.
    final List<AboutType> contextualObjects = new ArrayList<>(entity.getAgents());
    contextualObjects.addAll(entity.getConcepts());
    contextualObjects.addAll(entity.getPlaces());
    contextualObjects.addAll(entity.getTimeSpans());
    final Map<String, Set<Class<? extends AboutType>>> contextualObjectMap = contextualObjects
        .stream().collect(Collectors.groupingBy(AboutType::getAbout,
            Collectors.mapping(AboutType::getClass, Collectors.toSet())));

    // Go by all the enabling elements and match them.
    final Set<EnablingElement> elements = EnumSet.noneOf(EnablingElement.class);
    final Set<EnablingElementGroup> groups = EnumSet.noneOf(EnablingElementGroup.class);
    for (EnablingElement element : EnablingElement.values()) {
      final Set<EnablingElementGroup> groupsToAdd = element
          .analyze(entity.getProviderProxies(), contextualObjectMap);
      if (!groupsToAdd.isEmpty()) {
        elements.add(element);
        groups.addAll(groupsToAdd);
      }
    }

    // Compute the tier.
    final MetadataTier tier;
    if (groups.size() >= MIN_GROUPS_TIER_C && elements.size() >= MIN_ELEMENTS_TIER_C) {
      tier = MetadataTier.TC;
    } else if (groups.size() >= MIN_GROUPS_TIER_B && elements.size() >= MIN_ELEMENTS_TIER_B) {
      tier = MetadataTier.TB;
    } else if (groups.size() >= MIN_GROUPS_TIER_A && elements.size() >= MIN_ELEMENTS_TIER_A) {
      tier = MetadataTier.TA;
    } else {
      tier = MetadataTier.T0;
    }

    // Done
    return tier;
  }
}
