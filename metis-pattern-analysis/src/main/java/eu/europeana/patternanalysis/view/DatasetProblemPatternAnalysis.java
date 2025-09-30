package eu.europeana.patternanalysis.view;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Class containing the dataset analysis for problem patterns.
 *
 * @param <T> the type of the execution step
 */
public record DatasetProblemPatternAnalysis<T>(
    String datasetId,
    T executionStep,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    LocalDateTime executionTimestamp,
    List<ProblemPattern> problemPatternList) {

  /**
   * Constructor with required parameters.
   *
   * @param datasetId the dataset id
   * @param executionTimestamp the execution timestamp
   * @param executionStep the execution step
   * @param problemPatternList the problem pattern list
   */
  public DatasetProblemPatternAnalysis {
    problemPatternList = (problemPatternList == null) ? List.of() : List.copyOf(problemPatternList);
  }
}
