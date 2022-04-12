package eu.europeana.patternanalysis.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.Test;

class ProblemOccurenceTest {

  @Test
  void objectCreationTest() {
    final ProblemOccurence problemOccurence1 = new ProblemOccurence("Duplicate titleA", List.of("recordId1", "recordId2"));
    assertEquals("Duplicate titleA", problemOccurence1.getMessageReport());
    assertTrue(CollectionUtils.isEqualCollection(List.of("recordId2", "recordId1"), problemOccurence1.getAffectedRecordIds()));

    final ProblemOccurence problemOccurence2 = new ProblemOccurence("Duplicate titleB");
    assertNotNull(problemOccurence2.getAffectedRecordIds());
    assertEquals(0, problemOccurence2.getAffectedRecordIds().size());
  }

}