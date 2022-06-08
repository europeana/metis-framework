package eu.europeana.patternanalysis.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;

class ProblemPatternTest {

  @Test
  void objectCreationTest() {
    final ProblemOccurrence problemOccurrence1 = new ProblemOccurrence("Duplicate titleA", List.of("recordId1", "recordId2"));
    final ProblemOccurrence problemOccurrence2 = new ProblemOccurrence("Duplicate titleB");
    final RecordAnalysis recordAnalysis1 = new RecordAnalysis("recordId1", List.of(problemOccurrence1, problemOccurrence2));

    final RecordAnalysis recordAnalysis2 = new RecordAnalysis("recordId1", null);

    final ProblemPattern problemPattern1 = new ProblemPattern(
        ProblemPatternDescription.P2, 2, List.of(recordAnalysis1, recordAnalysis2));

    assertEquals(ProblemPatternDescription.P2, problemPattern1.getProblemPatternDescription());
    assertEquals(2, problemPattern1.getRecordOccurrences());
    assertTrue(
        CollectionUtils.isEqualCollection(List.of(recordAnalysis1, recordAnalysis2), problemPattern1.getRecordAnalysisList()));

    final ProblemPattern problemPattern2 = new ProblemPattern(ProblemPatternDescription.P2, 2, null);
    assertNotNull(problemPattern2.getRecordAnalysisList());
  }

}