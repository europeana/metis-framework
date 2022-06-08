package eu.europeana.indexing.tiers.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.metis.schema.jibx.PlaceType;
import eu.europeana.metis.schema.jibx.TimeSpanType;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;

class ContextualClassesBreakdownTest {

  @Test
  void objectCreationTest() {
    final MetadataTier metadataTier = MetadataTier.TC;

    ContextualClassesBreakdown contextualClassesBreakdown = new ContextualClassesBreakdown(0, null, metadataTier);
    assertEquals(0, contextualClassesBreakdown.getCompleteContextualResources());
    assertEquals(0, contextualClassesBreakdown.getDistinctClassesList().size());

    final Set<String> distinctClassesList = Set.of(TimeSpanType.class.getSimpleName(), PlaceType.class.getSimpleName());
    final int completeContextualResources = 5;
    contextualClassesBreakdown = new ContextualClassesBreakdown(completeContextualResources,
        distinctClassesList, metadataTier);

    assertEquals(completeContextualResources, contextualClassesBreakdown.getCompleteContextualResources());
    assertEquals(distinctClassesList.size(), contextualClassesBreakdown.getDistinctClassesList().size());
    assertTrue(CollectionUtils.isEqualCollection(distinctClassesList, contextualClassesBreakdown.getDistinctClassesList()));
    assertEquals(metadataTier, contextualClassesBreakdown.getMetadataTier());
  }

}