package eu.europeana.patternanalysis.view;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Arrays;

/**
 * Enum containing all available problem patterns.
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ProblemPatternDescription {

  /**
   * Systematic use of the same title
   */
  P1(ProblemPatternId.P1, ProblemPatternSeverity.WARNING, ProblemPatternQualityDimension.CONCISENESS),
  /**
   * Equal title and description fields
   */
  P2(ProblemPatternId.P2, ProblemPatternSeverity.WARNING, ProblemPatternQualityDimension.CONCISENESS),
  /**
   * Near-Identical title and description fields
   */
  P3(ProblemPatternId.P3, ProblemPatternSeverity.NOTICE, ProblemPatternQualityDimension.CONCISENESS),
  /**
   * Unrecognizable title
   */
  P5(ProblemPatternId.P5, ProblemPatternSeverity.NOTICE, ProblemPatternQualityDimension.ACCURACY),
  /**
   * Non-meaningful title
   */
  P6(ProblemPatternId.P6, ProblemPatternSeverity.NOTICE, ProblemPatternQualityDimension.ACCURACY),
  /**
   * Missing description fields
   */
  P7(ProblemPatternId.P7, ProblemPatternSeverity.NOTICE, ProblemPatternQualityDimension.COMPLETENESS),
  /**
   * Very short description
   */
  P9(ProblemPatternId.P9, ProblemPatternSeverity.WARNING, ProblemPatternQualityDimension.ACCURACY),
  /**
   * Extremely long values
   */
  P12(ProblemPatternId.P12, ProblemPatternSeverity.NOTICE, ProblemPatternQualityDimension.ACCURACY);

  private final ProblemPatternId problemPatternId;
  private final ProblemPatternSeverity problemPatternSeverity;
  private final ProblemPatternQualityDimension problemPatternQualityDimension;


  ProblemPatternDescription(ProblemPatternId problemPatternId,
      ProblemPatternSeverity problemPatternSeverity,
      ProblemPatternQualityDimension problemPatternQualityDimension) {
    this.problemPatternId = problemPatternId;
    this.problemPatternSeverity = problemPatternSeverity;
    this.problemPatternQualityDimension = problemPatternQualityDimension;
  }

  public ProblemPatternId getProblemPatternId() {
    return problemPatternId;
  }

  public ProblemPatternSeverity getProblemPatternSeverity() {
    return problemPatternSeverity;
  }

  public ProblemPatternQualityDimension getProblemPatternQualityDimension() {
    return problemPatternQualityDimension;
  }

  /**
   * Retrieves an instance of the enum based on the provided enum name(ignore case) or else throws a runtime exception
   *
   * @param name the enum name
   * @return the enum object
   */
  public static ProblemPatternDescription fromName(String name) {
    return Arrays.stream(ProblemPatternDescription.values()).filter(value -> value.name().equalsIgnoreCase(name)).findFirst()
                 .orElseThrow();
  }

  /**
   * The problem pattern ids
   */
  public enum ProblemPatternId {
    P1, P2, P3, P5, P6, P7, P9, P12;
  }

  /**
   * The problem pattern severities
   */
  public enum ProblemPatternSeverity {
    NOTICE, WARNING, ERROR, FATAL
  }

  /**
   * The problem pattern quality dimensions
   */
  public enum ProblemPatternQualityDimension {
    ACCURACY, AVAILABILITY, COMPLETENESS, CONCISENESS, COMPLIANCE, CONSISTENCY, TIMELINESS, LICENSING, INTERLINKING, UNDERSTANDABILITY, REPRESENTATIONAL
  }
}
