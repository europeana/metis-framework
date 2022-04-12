package eu.europeana.patternanalysis.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.Test;

class RecordAnalysisTest {

  @Test
  void objectCreationTest() {
    final ProblemOccurence problemOccurence1 = new ProblemOccurence("Duplicate titleA", List.of("recordId1", "recordId2"));
    final ProblemOccurence problemOccurence2 = new ProblemOccurence("Duplicate titleB");
    final RecordAnalysis recordAnalysis1 = new RecordAnalysis("recordId1", List.of(problemOccurence1, problemOccurence2));

    assertEquals("recordId1", recordAnalysis1.getRecordId());
    assertTrue(CollectionUtils.isEqualCollection(List.of(problemOccurence1, problemOccurence2),
        recordAnalysis1.getProblemOccurenceList()));

    final RecordAnalysis recordAnalysis2 = new RecordAnalysis("recordId1", null);
    assertNotNull(recordAnalysis2.getProblemOccurenceList());
  }

}