package eu.europeana.patternanalysis.view;

import java.util.ArrayList;
import java.util.List;

/**
 * Class containing the record analysis.
 */
public class RecordAnalysis {

  private final String recordId;
  private final List<ProblemOccurrence> problemOccurrenceList;

  /**
   * Constructor with required parameters.
   *
   * @param recordId the record id
   * @param problemOccurrenceList the problem occurrences list
   */
  public RecordAnalysis(String recordId, List<ProblemOccurrence> problemOccurrenceList) {
    this.recordId = recordId;
    this.problemOccurrenceList = problemOccurrenceList == null ? new ArrayList<>() : new ArrayList<>(problemOccurrenceList);
  }

  public String getRecordId() {
    return recordId;
  }

  public List<ProblemOccurrence> getProblemOccurrenceList() {
    return new ArrayList<>(problemOccurrenceList);
  }
}
