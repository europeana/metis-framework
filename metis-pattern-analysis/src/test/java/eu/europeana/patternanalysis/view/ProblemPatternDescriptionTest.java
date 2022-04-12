package eu.europeana.patternanalysis.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ProblemPatternDescriptionTest {

  @Test
  void checkValues() {
    assertEquals("P1", ProblemPatternDescription.P1.getProblemPatternId().toString());
    assertEquals("WARNING", ProblemPatternDescription.P1.getProblemPatternSeverity().toString());
    assertEquals("CONCISENESS", ProblemPatternDescription.P1.getProblemPatternQualityDimension().toString());

    assertEquals("P2", ProblemPatternDescription.P2.getProblemPatternId().toString());
    assertEquals("WARNING", ProblemPatternDescription.P2.getProblemPatternSeverity().toString());
    assertEquals("CONCISENESS", ProblemPatternDescription.P2.getProblemPatternQualityDimension().toString());

    assertEquals("P3", ProblemPatternDescription.P3.getProblemPatternId().toString());
    assertEquals("NOTICE", ProblemPatternDescription.P3.getProblemPatternSeverity().toString());
    assertEquals("CONCISENESS", ProblemPatternDescription.P3.getProblemPatternQualityDimension().toString());

    assertEquals("P5", ProblemPatternDescription.P5.getProblemPatternId().toString());
    assertEquals("NOTICE", ProblemPatternDescription.P5.getProblemPatternSeverity().toString());
    assertEquals("ACCURACY", ProblemPatternDescription.P5.getProblemPatternQualityDimension().toString());

    assertEquals("P6", ProblemPatternDescription.P6.getProblemPatternId().toString());
    assertEquals("NOTICE", ProblemPatternDescription.P6.getProblemPatternSeverity().toString());
    assertEquals("ACCURACY", ProblemPatternDescription.P6.getProblemPatternQualityDimension().toString());

    assertEquals("P7", ProblemPatternDescription.P7.getProblemPatternId().toString());
    assertEquals("NOTICE", ProblemPatternDescription.P7.getProblemPatternSeverity().toString());
    assertEquals("COMPLETENESS", ProblemPatternDescription.P7.getProblemPatternQualityDimension().toString());

    assertEquals("P9", ProblemPatternDescription.P9.getProblemPatternId().toString());
    assertEquals("WARNING", ProblemPatternDescription.P9.getProblemPatternSeverity().toString());
    assertEquals("ACCURACY", ProblemPatternDescription.P9.getProblemPatternQualityDimension().toString());

    assertEquals("P12", ProblemPatternDescription.P12.getProblemPatternId().toString());
    assertEquals("NOTICE", ProblemPatternDescription.P12.getProblemPatternSeverity().toString());
    assertEquals("ACCURACY", ProblemPatternDescription.P12.getProblemPatternQualityDimension().toString());
  }

}