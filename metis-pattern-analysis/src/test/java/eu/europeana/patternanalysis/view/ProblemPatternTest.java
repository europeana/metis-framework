package eu.europeana.patternanalysis.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.Test;

class ProblemPatternTest {

  @Test
  void objectCreationTest() {
    final ProblemOccurence problemOccurence1 = new ProblemOccurence("Duplicate titleA", List.of("recordId1", "recordId2"));
    final ProblemOccurence problemOccurence2 = new ProblemOccurence("Duplicate titleB");
    final RecordAnalysis recordAnalysis1 = new RecordAnalysis("recordId1", List.of(problemOccurence1, problemOccurence2));

    final RecordAnalysis recordAnalysis2 = new RecordAnalysis("recordId1", null);

    final ProblemPattern problemPattern1 = new ProblemPattern(
        ProblemPatternDescription.P2, 2, List.of(recordAnalysis1, recordAnalysis2));

    assertEquals(ProblemPatternDescription.P2, problemPattern1.getProblemPatternDescription());
    assertEquals(2, problemPattern1.getRecordOccurences());
    assertTrue(
        CollectionUtils.isEqualCollection(List.of(recordAnalysis1, recordAnalysis2), problemPattern1.getRecordAnalysisList()));

    final ProblemPattern problemPattern2 = new ProblemPattern(ProblemPatternDescription.P2, 2, null);
    assertNotNull(problemPattern2.getRecordAnalysisList());
  }

}