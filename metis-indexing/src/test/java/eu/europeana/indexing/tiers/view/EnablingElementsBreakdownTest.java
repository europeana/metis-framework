package eu.europeana.indexing.tiers.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.indexing.tiers.metadata.EnablingElement;
import eu.europeana.indexing.tiers.metadata.EnablingElement.EnablingElementGroup;
import eu.europeana.indexing.tiers.model.MetadataTier;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.Test;

class EnablingElementsBreakdownTest {

  @Test
  void objectCreationTest() {
    final List<String> distinctEnablingElementsList = List.of(EnablingElement.DC_CREATOR.name(),
        EnablingElement.EDM_CURRENT_LOCATION.name());
    final List<String> metadataGroupsList = List.of(EnablingElementGroup.PERSONAL.name(),
        EnablingElementGroup.GEOGRAPHICAL.name());
    final MetadataTier metadataTier = MetadataTier.TC;

    EnablingElementsBreakdown enablingElementsBreakdown = new EnablingElementsBreakdown(null, null,
        metadataTier);
    assertEquals(0, enablingElementsBreakdown.getDistinctEnablingElementsList().size());
    assertEquals(0, enablingElementsBreakdown.getDistinctEnablingElements());
    assertEquals(0, enablingElementsBreakdown.getMetadataGroups());
    assertEquals(0, enablingElementsBreakdown.getMetadataGroupsList().size());

    enablingElementsBreakdown = new EnablingElementsBreakdown(distinctEnablingElementsList, metadataGroupsList,
        metadataTier);
    assertTrue(
        CollectionUtils.isEqualCollection(distinctEnablingElementsList,
            enablingElementsBreakdown.getDistinctEnablingElementsList()));
    assertEquals(distinctEnablingElementsList.size(), enablingElementsBreakdown.getDistinctEnablingElements());
    assertTrue(CollectionUtils.isEqualCollection(metadataGroupsList, enablingElementsBreakdown.getMetadataGroupsList()));
    assertEquals(metadataGroupsList.size(), enablingElementsBreakdown.getMetadataGroups());
    assertEquals(metadataTier, enablingElementsBreakdown.getTier());
  }

}