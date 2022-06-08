package eu.europeana.patternanalysis.view;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Arrays;

/**
 * Enum containing all available problem patterns.
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ProblemPatternDescription {

  P1(ProblemPatternId.P1, "Systematic use of the same title", ProblemPatternSeverity.WARNING,
      ProblemPatternQualityDimension.CONCISENESS),
  P2(ProblemPatternId.P2, "Equal title and description fields", ProblemPatternSeverity.WARNING,
      ProblemPatternQualityDimension.CONCISENESS),
  P3(ProblemPatternId.P3, "Near-Identical title and description fields", ProblemPatternSeverity.NOTICE,
      ProblemPatternQualityDimension.CONCISENESS),
  P5(ProblemPatternId.P5, "Unrecognizable title", ProblemPatternSeverity.NOTICE, ProblemPatternQualityDimension.ACCURACY),
  P6(ProblemPatternId.P6, "Non-meaningful title", ProblemPatternSeverity.NOTICE, ProblemPatternQualityDimension.ACCURACY),
  P7(ProblemPatternId.P7, "Missing description fields", ProblemPatternSeverity.NOTICE,
      ProblemPatternQualityDimension.COMPLETENESS),
  P9(ProblemPatternId.P9, "Very short description", ProblemPatternSeverity.WARNING, ProblemPatternQualityDimension.ACCURACY),
  P12(ProblemPatternId.P12, "Extremely long values", ProblemPatternSeverity.NOTICE, ProblemPatternQualityDimension.ACCURACY);

  private final ProblemPatternId problemPatternId;
  private final String problemPatternTitle;
  private final ProblemPatternSeverity problemPatternSeverity;
  private final ProblemPatternQualityDimension problemPatternQualityDimension;


  ProblemPatternDescription(ProblemPatternId problemPatternId,
      String problemPatternTitle, ProblemPatternSeverity problemPatternSeverity,
      ProblemPatternQualityDimension problemPatternQualityDimension) {
    this.problemPatternId = problemPatternId;
    this.problemPatternTitle = problemPatternTitle;
    this.problemPatternSeverity = problemPatternSeverity;
    this.problemPatternQualityDimension = problemPatternQualityDimension;
  }

  public ProblemPatternId getProblemPatternId() {
    return problemPatternId;
  }

  public String getProblemPatternTitle() {
    return problemPatternTitle;
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
