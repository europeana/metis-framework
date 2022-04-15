package eu.europeana.patternanalysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.patternanalysis.view.ProblemPattern;
import eu.europeana.patternanalysis.view.ProblemPatternDescription;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

class ProblemPatternAnalyzerTest {

  @Test
  void analyzeRecord_P2() throws Exception {
    //Should contain two provider proxies that each contain a pair of identical title and description. All four values are identical on the two proxies.
    String xml = IOUtils.toString(new FileInputStream("src/test/resources/europeana_record_with_P2.xml"),
        StandardCharsets.UTF_8);
    final RDF rdf = new RdfConversionUtils().convertStringToRdf(xml);

    final ProblemPatternAnalyzer problemPatternAnalyzer = new ProblemPatternAnalyzer();
    final List<ProblemPattern> problemPatterns = problemPatternAnalyzer.analyzeRecord(rdf);

    assertNotNull(problemPatterns);
    assertEquals(1, problemPatterns.size());
    assertEquals(ProblemPatternDescription.P2, problemPatterns.get(0).getProblemPatternDescription());
    assertEquals(1, problemPatterns.get(0).getRecordAnalysisList().get(0).getProblemOccurrenceList().size());
  }

  @Test
  void analyzeRecord_P6() throws Exception {
    //Should contain one title that is not meaningful(too short)
    String xml = IOUtils.toString(new FileInputStream("src/test/resources/europeana_record_with_P6.xml"),
        StandardCharsets.UTF_8);
    final RDF rdf = new RdfConversionUtils().convertStringToRdf(xml);

    final ProblemPatternAnalyzer problemPatternAnalyzer = new ProblemPatternAnalyzer();
    final List<ProblemPattern> problemPatterns = problemPatternAnalyzer.analyzeRecord(rdf);

    assertNotNull(problemPatterns);
    assertEquals(1, problemPatterns.size());
    assertEquals(ProblemPatternDescription.P6, problemPatterns.get(0).getProblemPatternDescription());
    assertEquals(1, problemPatterns.get(0).getRecordAnalysisList().get(0).getProblemOccurrenceList().size());
  }
}