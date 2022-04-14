package eu.europeana.patternanalysis.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.Test;

class DatasetProblemPatternAnalysisTest {

  @Test
  void objectCreationTest() {
    final ProblemOccurence problemOccurence1 = new ProblemOccurence("Duplicate titleA", List.of("recordId1", "recordId2"));
    final ProblemOccurence problemOccurence2 = new ProblemOccurence("Duplicate titleB");
    final RecordAnalysis recordAnalysis1 = new RecordAnalysis("recordId1", List.of(problemOccurence1, problemOccurence2));

    final RecordAnalysis recordAnalysis2 = new RecordAnalysis("recordId1", null);

    final ProblemPattern problemPattern1 = new ProblemPattern(
        ProblemPatternDescription.P2, 2, List.of(recordAnalysis1, recordAnalysis2));
    final ProblemPattern problemPattern2 = new ProblemPattern(ProblemPatternDescription.P2, 2, null);

    final LocalDateTime currentDate = LocalDateTime.now();
    final DatasetProblemPatternAnalysis datasetProblemPatternAnalysis1 = new DatasetProblemPatternAnalysis("datasetId1",
        currentDate,
        "VALIDATION_EXTERNAL", List.of(problemPattern1, problemPattern2));

    assertEquals("datasetId1", datasetProblemPatternAnalysis1.getDatasetId());
    assertEquals(0, currentDate.compareTo(datasetProblemPatternAnalysis1.getExecutionTimestamp()));
    assertEquals("VALIDATION_EXTERNAL", datasetProblemPatternAnalysis1.getExecutionStep());
    assertTrue(CollectionUtils.isEqualCollection(List.of(problemPattern1, problemPattern2),
        datasetProblemPatternAnalysis1.getProblemPatternList()));

    final DatasetProblemPatternAnalysis datasetProblemPatternAnalysis2 = new DatasetProblemPatternAnalysis("datasetId1",
        currentDate, "VALIDATION_EXTERNAL", null);
    assertNotNull(datasetProblemPatternAnalysis2.getProblemPatternList());
  }

}