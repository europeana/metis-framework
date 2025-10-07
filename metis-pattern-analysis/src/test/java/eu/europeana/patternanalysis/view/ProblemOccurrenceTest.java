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
    assertEquals("Duplicate titleA", problemOccurrence1.messageReport());
    assertTrue(CollectionUtils.isEqualCollection(List.of("recordId2", "recordId1"), problemOccurrence1.affectedRecordIds()));

    final ProblemOccurrence problemOccurrence2 = new ProblemOccurrence("Duplicate titleA", null);
    assertEquals("Duplicate titleA", problemOccurrence2.messageReport());
    assertEquals(0, problemOccurrence2.affectedRecordIds().size());

    final ProblemOccurrence problemOccurrence3 = new ProblemOccurrence("Duplicate titleB");
    assertNotNull(problemOccurrence3.affectedRecordIds());
    assertEquals(0, problemOccurrence3.affectedRecordIds().size());
  }
}