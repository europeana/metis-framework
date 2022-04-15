package eu.europeana.patternanalysis.view;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Class containing the dataset analysis for problem patterns.
 */
public class DatasetProblemPatternAnalysis<T> {

  private final String datasetId;
  private final T executionStep;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private final LocalDateTime executionTimestamp;
  private final List<ProblemPattern> problemPatternList;

  /**
   * Constructor with required parameters.
   *
   * @param datasetId the dataset id
   * @param executionTimestamp the execution timestamp
   * @param executionStep the execution step
   * @param problemPatternList the problem pattern list
   */
  public DatasetProblemPatternAnalysis(String datasetId, T executionStep, LocalDateTime executionTimestamp,
      List<ProblemPattern> problemPatternList) {
    this.datasetId = datasetId;
    this.executionStep = executionStep;
    this.executionTimestamp = executionTimestamp;
    this.problemPatternList = problemPatternList == null ? new ArrayList<>() : new ArrayList<>(problemPatternList);
  }

  public String getDatasetId() {
    return datasetId;
  }

  public T getExecutionStep() {
    return executionStep;
  }

  public LocalDateTime getExecutionTimestamp() {
    return executionTimestamp;
  }

  public List<ProblemPattern> getProblemPatternList() {
    return new ArrayList<>(problemPatternList);
  }
}
