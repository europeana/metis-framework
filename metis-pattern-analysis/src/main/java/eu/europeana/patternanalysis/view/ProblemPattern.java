package eu.europeana.patternanalysis.view;

import java.util.ArrayList;
import java.util.List;

/**
 * Class containing the problem pattern including its {@link RecordAnalysis}.
 */
public class ProblemPattern {

  private final ProblemPatternDescription problemPatternDescription;
  private final int recordOccurrences;
  private final List<RecordAnalysis> recordAnalysisList;

  /**
   * Constructor with required parameters.
   *
   * @param problemPatternId the problem pattern id
   * @param recordOccurrences the record occurrences
   * @param recordAnalysisList the record analysis list
   */
  public ProblemPattern(ProblemPatternDescription problemPatternId, int recordOccurrences,
      List<RecordAnalysis> recordAnalysisList) {
    this.problemPatternDescription = problemPatternId;
    this.recordOccurrences = recordOccurrences;
    this.recordAnalysisList = recordAnalysisList == null ? new ArrayList<>() : new ArrayList<>(recordAnalysisList);
  }

  public ProblemPatternDescription getProblemPatternDescription() {
    return problemPatternDescription;
  }

  public int getRecordOccurrences() {
    return recordOccurrences;
  }

  public List<RecordAnalysis> getRecordAnalysisList() {
    return new ArrayList<>(recordAnalysisList);
  }
}
