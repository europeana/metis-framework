package eu.europeana.patternanalysis.view;

import java.util.ArrayList;
import java.util.List;

/**
 * Class containing the record analysis.
 */
public class RecordAnalysis {

  private final String recordId;
  private final List<ProblemOccurence> problemOccurenceList;

  /**
   * Constructor with required parameters.
   *
   * @param recordId the record id
   * @param problemOccurenceList the problem occurences list
   */
  public RecordAnalysis(String recordId, List<ProblemOccurence> problemOccurenceList) {
    this.recordId = recordId;
    this.problemOccurenceList = problemOccurenceList == null ? new ArrayList<>() : new ArrayList<>(problemOccurenceList);
  }

  public String getRecordId() {
    return recordId;
  }

  public List<ProblemOccurence> getProblemOccurenceList() {
    return new ArrayList<>(problemOccurenceList);
  }
}
