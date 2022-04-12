package eu.europeana.patternanalysis.view;

import java.util.ArrayList;
import java.util.List;

/**
 * Class containing the problem pattern including its {@link RecordAnalysis}.
 */
public class ProblemPattern {

  private final ProblemPatternDescription problemPatternDescription;
  private final int recordOccurences;
  private final List<RecordAnalysis> recordAnalysisList;

  /**
   * Constructor with required parameters.
   *
   * @param problemPatternId the problem pattern id
   * @param recordOccurences the record occurences
   * @param recordAnalysisList the record analysis list
   */
  public ProblemPattern(ProblemPatternDescription problemPatternId, int recordOccurences,
      List<RecordAnalysis> recordAnalysisList) {
    this.problemPatternDescription = problemPatternId;
    this.recordOccurences = recordOccurences;
    this.recordAnalysisList = recordAnalysisList == null ? new ArrayList<>() : new ArrayList<>(recordAnalysisList);
  }

  public ProblemPatternDescription getProblemPatternDescription() {
    return problemPatternDescription;
  }

  public int getRecordOccurences() {
    return recordOccurences;
  }

  public List<RecordAnalysis> getRecordAnalysisList() {
    return new ArrayList<>(recordAnalysisList);
  }
}
