package eu.europeana.patternanalysis.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ProblemPatternAnalysisTest {

  @Test
  void objectCreationTest() {
    final List<ProblemPattern> problemPatterns = List.of(new ProblemPattern(ProblemPatternDescription.P2, 1,
        List.of(new RecordAnalysis("recordId", List.of(new ProblemOccurrence("message"))))));
    final Set<String> titles = Set.of("titleA");
    final ProblemPatternAnalysis problemPatternAnalysis = new ProblemPatternAnalysis("rdfAbout", problemPatterns, titles);

    assertEquals(problemPatternAnalysis.getRdfAbout(), "rdfAbout");
    assertEquals(problemPatternAnalysis.getProblemPatterns().size(), problemPatterns.size());
    assertEquals(problemPatternAnalysis.getTitles().size(), titles.size());

    assertThrows(NullPointerException.class, () -> new ProblemPatternAnalysis(null, problemPatterns, titles));
    assertEquals(0, new ProblemPatternAnalysis("rdfAbout", null, titles).getProblemPatterns().size());
    assertEquals(0, new ProblemPatternAnalysis("rdfAbout", problemPatterns, null).getTitles().size());
  }
}