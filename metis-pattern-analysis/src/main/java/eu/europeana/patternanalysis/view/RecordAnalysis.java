package eu.europeana.patternanalysis.view;

import java.util.List;

/**
 * Class containing the record analysis.
 */
public record RecordAnalysis(
    String recordId,
    List<ProblemOccurrence> problemOccurrenceList) {

  /**
   * Constructor with required parameters.
   *
   * @param recordId the record id
   * @param problemOccurrenceList the problem occurrences list
   */
  public RecordAnalysis {
    problemOccurrenceList = (problemOccurrenceList == null) ? List.of() : List.copyOf(problemOccurrenceList);
  }
}
