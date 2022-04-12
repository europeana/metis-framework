package eu.europeana.patternanalysis.view;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Enum containing all available problem patterns.
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ProblemPatternDescription {

  P1(ProblemPatternId.P1, ProblemPatternSeverity.WARNING, ProblemPatternQualityDimension.CONCISENESS),
  P2(ProblemPatternId.P2, ProblemPatternSeverity.WARNING, ProblemPatternQualityDimension.CONCISENESS),
  P3(ProblemPatternId.P3, ProblemPatternSeverity.NOTICE, ProblemPatternQualityDimension.CONCISENESS),
  P5(ProblemPatternId.P5, ProblemPatternSeverity.NOTICE, ProblemPatternQualityDimension.ACCURACY),
  P6(ProblemPatternId.P6, ProblemPatternSeverity.NOTICE, ProblemPatternQualityDimension.ACCURACY),
  P7(ProblemPatternId.P7, ProblemPatternSeverity.NOTICE, ProblemPatternQualityDimension.COMPLETENESS),
  P9(ProblemPatternId.P9, ProblemPatternSeverity.WARNING, ProblemPatternQualityDimension.ACCURACY),
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

  enum ProblemPatternId {
    P1, P2, P3, P5, P6, P7, P9, P12;
  }

  enum ProblemPatternSeverity {
    NOTICE, WARNING, ERROR, FATAL
  }

  enum ProblemPatternQualityDimension {
    ACCURACY, AVAILABILITY, COMPLETENESS, CONCISENESS, COMPLIANCE, CONSISTENCY, TIMELINESS, LICENSING, INTERLINKING, UNDERSTANDABILITY, REPRESENTATIONAL
  }
}
