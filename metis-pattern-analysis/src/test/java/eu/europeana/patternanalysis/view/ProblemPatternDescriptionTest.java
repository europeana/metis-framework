package eu.europeana.patternanalysis.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

class ProblemPatternDescriptionTest {

  private void assertProblemPatternDescription(ProblemPatternDescription problemPatternDescription, String patternId,
      String problemTitle,
      String problemSeverity, String problemAccuracy) {
    assertEquals(patternId, problemPatternDescription.getProblemPatternId().toString());
    assertEquals(problemTitle, problemPatternDescription.getProblemPatternTitle());
    assertEquals(problemSeverity, problemPatternDescription.getProblemPatternSeverity().toString());
    assertEquals(problemAccuracy, problemPatternDescription.getProblemPatternQualityDimension().toString());
  }

  @Test
  void checkValuesTest() {
    assertProblemPatternDescription(ProblemPatternDescription.P1, "P1", "Systematic use of the same title (ignoring case)", "WARNING",
        "CONCISENESS");
    assertProblemPatternDescription(ProblemPatternDescription.P2, "P2", "Equal title and description fields", "WARNING",
        "CONCISENESS");
    assertProblemPatternDescription(ProblemPatternDescription.P3, "P3", "Near-Identical title and description fields", "WARNING",
        "CONCISENESS");
    assertProblemPatternDescription(ProblemPatternDescription.P5, "P5", "Unrecognizable title", "WARNING", "ACCURACY");
    assertProblemPatternDescription(ProblemPatternDescription.P6, "P6", "Non-meaningful title", "WARNING", "ACCURACY");
    assertProblemPatternDescription(ProblemPatternDescription.P7, "P7", "Missing description fields", "WARNING", "COMPLETENESS");
    assertProblemPatternDescription(ProblemPatternDescription.P9, "P9", "Very short description", "WARNING", "ACCURACY");
    assertProblemPatternDescription(ProblemPatternDescription.P12, "P12", "Extremely long values", "WARNING", "ACCURACY");
  }

  @Test
  void fromNameTest() {
    assertEquals(ProblemPatternDescription.P1, ProblemPatternDescription.fromName("P1"));
    assertEquals(ProblemPatternDescription.P2, ProblemPatternDescription.fromName("P2"));
    assertEquals(ProblemPatternDescription.P3, ProblemPatternDescription.fromName("P3"));
    assertEquals(ProblemPatternDescription.P5, ProblemPatternDescription.fromName("P5"));
    assertEquals(ProblemPatternDescription.P6, ProblemPatternDescription.fromName("P6"));
    assertEquals(ProblemPatternDescription.P7, ProblemPatternDescription.fromName("P7"));
    assertEquals(ProblemPatternDescription.P9, ProblemPatternDescription.fromName("P9"));
    assertEquals(ProblemPatternDescription.P12, ProblemPatternDescription.fromName("P12"));

    assertThrows(NoSuchElementException.class, () -> ProblemPatternDescription.fromName("invalid"));
  }

}
