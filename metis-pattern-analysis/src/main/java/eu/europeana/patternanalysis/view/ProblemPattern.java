package eu.europeana.patternanalysis.view;

import java.util.List;

/**
 * Class containing the problem pattern including its {@link RecordAnalysis}.
 */
public record ProblemPattern(
    ProblemPatternDescription problemPatternDescription,
    int recordOccurrences,
    List<RecordAnalysis> recordAnalysisList) {

  /**
   * Constructor with required parameters.
   *
   * @param problemPatternDescription the problem pattern id
   * @param recordOccurrences the record occurrences
   * @param recordAnalysisList the record analysis list
   */
  public ProblemPattern {
    recordAnalysisList = (recordAnalysisList == null) ? List.of() : List.copyOf(recordAnalysisList);
  }
}
