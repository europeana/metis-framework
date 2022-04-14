package eu.europeana.patternanalysis.view;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Class containing the dataset analysis for problem patterns.
 */
public class DatasetProblemPatternAnalysis {

  private final String datasetId;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private final LocalDateTime executionTimestamp;
  private final String executionStep;
  private final List<ProblemPattern> problemPatternList;

  /**
   * Constructor with required parameters.
   *
   * @param datasetId the dataset id
   * @param executionTimestamp the execution timestamp
   * @param executionStep the execution step
   * @param problemPatternList the problem pattern list
   */
  public DatasetProblemPatternAnalysis(String datasetId, LocalDateTime executionTimestamp, String executionStep,
      List<ProblemPattern> problemPatternList) {
    this.datasetId = datasetId;
    this.executionTimestamp = executionTimestamp;
    this.executionStep = executionStep;
    this.problemPatternList = problemPatternList == null ? new ArrayList<>() : new ArrayList<>(problemPatternList);
  }

  public String getDatasetId() {
    return datasetId;
  }

  public List<ProblemPattern> getProblemPatternList() {
    return new ArrayList<>(problemPatternList);
  }

  public LocalDateTime getExecutionTimestamp() {
    return executionTimestamp;
  }

  public String getExecutionStep() {
    return executionStep;
  }
}
