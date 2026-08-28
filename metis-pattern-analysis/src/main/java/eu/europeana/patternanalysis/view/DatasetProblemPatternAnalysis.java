package eu.europeana.patternanalysis.view;

import java.time.Instant;
import java.util.List;

/**
 * Class containing the dataset analysis for problem patterns.
 *
 * @param <T> the type of the execution step
 */
public record DatasetProblemPatternAnalysis<T>(
    String datasetId,
    T executionStep,
    Instant executionTimestamp,
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
