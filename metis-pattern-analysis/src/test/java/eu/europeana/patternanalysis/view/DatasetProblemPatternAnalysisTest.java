package eu.europeana.patternanalysis.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;

class DatasetProblemPatternAnalysisTest {

  @Test
  void objectCreationTest() {
    final ProblemOccurrence problemOccurrence1 = new ProblemOccurrence("Duplicate titleA", List.of("recordId1", "recordId2"));
    final ProblemOccurrence problemOccurrence2 = new ProblemOccurrence("Duplicate titleB");
    final RecordAnalysis recordAnalysis1 = new RecordAnalysis("recordId1", List.of(problemOccurrence1, problemOccurrence2));

    final RecordAnalysis recordAnalysis2 = new RecordAnalysis("recordId1", null);

    final ProblemPattern problemPattern1 = new ProblemPattern(
        ProblemPatternDescription.P2, 2, List.of(recordAnalysis1, recordAnalysis2));
    final ProblemPattern problemPattern2 = new ProblemPattern(ProblemPatternDescription.P2, 2, null);

    final LocalDateTime currentDate = LocalDateTime.now();
    final DatasetProblemPatternAnalysis<String> datasetProblemPatternAnalysis1 = new DatasetProblemPatternAnalysis<>("datasetId1",
        "VALIDATION_EXTERNAL", currentDate
        , List.of(problemPattern1, problemPattern2));

    assertEquals("datasetId1", datasetProblemPatternAnalysis1.getDatasetId());
    assertEquals(0, currentDate.compareTo(datasetProblemPatternAnalysis1.getExecutionTimestamp()));
    assertEquals("VALIDATION_EXTERNAL", datasetProblemPatternAnalysis1.getExecutionStep());
    assertTrue(CollectionUtils.isEqualCollection(List.of(problemPattern1, problemPattern2),
        datasetProblemPatternAnalysis1.getProblemPatternList()));

    final DatasetProblemPatternAnalysis<String> datasetProblemPatternAnalysis2 = new DatasetProblemPatternAnalysis<>("datasetId1",
        "VALIDATION_EXTERNAL", currentDate, null);
    assertNotNull(datasetProblemPatternAnalysis2.getProblemPatternList());
  }

}