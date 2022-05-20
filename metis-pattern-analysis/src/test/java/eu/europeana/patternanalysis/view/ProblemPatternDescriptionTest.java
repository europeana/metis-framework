package eu.europeana.patternanalysis.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

class ProblemPatternDescriptionTest {

  @Test
  void checkValuesTest() {
    assertEquals("P1", ProblemPatternDescription.P1.getProblemPatternId().toString());
    assertEquals("Systematic use of the same title", ProblemPatternDescription.P1.getProblemPatternTitle());
    assertEquals("WARNING", ProblemPatternDescription.P1.getProblemPatternSeverity().toString());
    assertEquals("CONCISENESS", ProblemPatternDescription.P1.getProblemPatternQualityDimension().toString());

    assertEquals("P2", ProblemPatternDescription.P2.getProblemPatternId().toString());
    assertEquals("Equal title and description fields", ProblemPatternDescription.P2.getProblemPatternTitle());
    assertEquals("WARNING", ProblemPatternDescription.P2.getProblemPatternSeverity().toString());
    assertEquals("CONCISENESS", ProblemPatternDescription.P2.getProblemPatternQualityDimension().toString());

    assertEquals("P3", ProblemPatternDescription.P3.getProblemPatternId().toString());
    assertEquals("Near-Identical title and description fields", ProblemPatternDescription.P3.getProblemPatternTitle());
    assertEquals("NOTICE", ProblemPatternDescription.P3.getProblemPatternSeverity().toString());
    assertEquals("CONCISENESS", ProblemPatternDescription.P3.getProblemPatternQualityDimension().toString());

    assertEquals("P5", ProblemPatternDescription.P5.getProblemPatternId().toString());
    assertEquals("Unrecognizable title", ProblemPatternDescription.P5.getProblemPatternTitle());
    assertEquals("NOTICE", ProblemPatternDescription.P5.getProblemPatternSeverity().toString());
    assertEquals("ACCURACY", ProblemPatternDescription.P5.getProblemPatternQualityDimension().toString());

    assertEquals("P6", ProblemPatternDescription.P6.getProblemPatternId().toString());
    assertEquals("Non-meaningful title", ProblemPatternDescription.P6.getProblemPatternTitle());
    assertEquals("NOTICE", ProblemPatternDescription.P6.getProblemPatternSeverity().toString());
    assertEquals("ACCURACY", ProblemPatternDescription.P6.getProblemPatternQualityDimension().toString());

    assertEquals("P7", ProblemPatternDescription.P7.getProblemPatternId().toString());
    assertEquals("Missing description fields", ProblemPatternDescription.P7.getProblemPatternTitle());
    assertEquals("NOTICE", ProblemPatternDescription.P7.getProblemPatternSeverity().toString());
    assertEquals("COMPLETENESS", ProblemPatternDescription.P7.getProblemPatternQualityDimension().toString());

    assertEquals("P9", ProblemPatternDescription.P9.getProblemPatternId().toString());
    assertEquals("Very short description", ProblemPatternDescription.P9.getProblemPatternTitle());
    assertEquals("WARNING", ProblemPatternDescription.P9.getProblemPatternSeverity().toString());
    assertEquals("ACCURACY", ProblemPatternDescription.P9.getProblemPatternQualityDimension().toString());

    assertEquals("P12", ProblemPatternDescription.P12.getProblemPatternId().toString());
    assertEquals("Extremely long values", ProblemPatternDescription.P12.getProblemPatternTitle());
    assertEquals("NOTICE", ProblemPatternDescription.P12.getProblemPatternSeverity().toString());
    assertEquals("ACCURACY", ProblemPatternDescription.P12.getProblemPatternQualityDimension().toString());
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