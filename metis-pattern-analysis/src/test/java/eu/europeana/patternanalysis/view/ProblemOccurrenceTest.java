package eu.europeana.patternanalysis.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;

class ProblemOccurrenceTest {

  @Test
  void objectCreationTest() {
    final ProblemOccurrence problemOccurrence1 = new ProblemOccurrence("Duplicate titleA", List.of("recordId1", "recordId2"));
    assertEquals("Duplicate titleA", problemOccurrence1.getMessageReport());
    assertTrue(CollectionUtils.isEqualCollection(List.of("recordId2", "recordId1"), problemOccurrence1.getAffectedRecordIds()));

    final ProblemOccurrence problemOccurrence2 = new ProblemOccurrence("Duplicate titleA", null);
    assertEquals("Duplicate titleA", problemOccurrence2.getMessageReport());
    assertEquals(0, problemOccurrence2.getAffectedRecordIds().size());

    final ProblemOccurrence problemOccurrence3 = new ProblemOccurrence("Duplicate titleB");
    assertNotNull(problemOccurrence3.getAffectedRecordIds());
    assertEquals(0, problemOccurrence3.getAffectedRecordIds().size());
  }
}