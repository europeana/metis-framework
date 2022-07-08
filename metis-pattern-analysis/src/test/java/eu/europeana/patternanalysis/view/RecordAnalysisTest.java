package eu.europeana.patternanalysis.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;

class RecordAnalysisTest {

  @Test
  void objectCreationTest() {
    final ProblemOccurrence problemOccurrence1 = new ProblemOccurrence("Duplicate titleA", List.of("recordId1", "recordId2"));
    final ProblemOccurrence problemOccurrence2 = new ProblemOccurrence("Duplicate titleB");
    final RecordAnalysis recordAnalysis1 = new RecordAnalysis("recordId1", List.of(problemOccurrence1, problemOccurrence2));

    assertEquals("recordId1", recordAnalysis1.getRecordId());
    assertTrue(CollectionUtils.isEqualCollection(List.of(problemOccurrence1, problemOccurrence2),
        recordAnalysis1.getProblemOccurrenceList()));

    final RecordAnalysis recordAnalysis2 = new RecordAnalysis("recordId1", null);
    assertNotNull(recordAnalysis2.getProblemOccurrenceList());
  }

}