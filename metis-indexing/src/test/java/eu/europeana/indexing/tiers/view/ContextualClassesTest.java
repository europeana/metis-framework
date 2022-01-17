package eu.europeana.indexing.tiers.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.metis.schema.jibx.PlaceType;
import eu.europeana.metis.schema.jibx.TimeSpanType;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.Test;

class ContextualClassesTest {

  @Test
  void objectCreationTest() {
    final MetadataTier metadataTier = MetadataTier.TC;

    ContextualClasses contextualClasses = new ContextualClasses(0, null, metadataTier);
    assertEquals(0, contextualClasses.getCompleteContextualResources());
    assertEquals(0, contextualClasses.getDistinctClassesOfCompleteContextualResources());
    assertEquals(0, contextualClasses.getDistinctClassesList().size());

    final List<String> distinctClassesList = List.of(TimeSpanType.class.getSimpleName(), PlaceType.class.getSimpleName());
    final int completeContextualResources = 5;
    contextualClasses = new ContextualClasses(completeContextualResources,
        distinctClassesList, metadataTier);

    assertEquals(completeContextualResources, contextualClasses.getCompleteContextualResources());
    assertEquals(distinctClassesList.size(), contextualClasses.getDistinctClassesList().size());
    assertTrue(CollectionUtils.isEqualCollection(distinctClassesList, contextualClasses.getDistinctClassesList()));
    assertEquals(metadataTier, contextualClasses.getMetadataTier());
  }

}