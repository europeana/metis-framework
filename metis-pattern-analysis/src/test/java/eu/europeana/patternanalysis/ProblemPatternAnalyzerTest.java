package eu.europeana.patternanalysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.convert.SerializationException;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.patternanalysis.view.ProblemPattern;
import eu.europeana.patternanalysis.view.ProblemPatternDescription;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

class ProblemPatternAnalyzerTest {

  public static final String FILE_XML_P2_LOCATION = "src/test/resources/europeana_record_with_P2.xml";
  public static final String FILE_XML_P5_LOCATION = "src/test/resources/europeana_record_with_P5.xml";
  public static final String FILE_XML_P6_LOCATION = "src/test/resources/europeana_record_with_P6.xml";
  public static final String FILE_XML_P7_LOCATION = "src/test/resources/europeana_record_with_P7.xml";
  public static final String FILE_XML_P7_DESCRIPTIONS_EMPTY_LOCATION = "src/test/resources/europeana_record_with_P7_descriptions_empty.xml";

  private ProblemPatternDescription getFirstProblemPatternDescription(List<ProblemPattern> problemPatterns) {
    return problemPatterns.get(0).getProblemPatternDescription();
  }

  private int getFirstProblemOccurrencesSize(List<ProblemPattern> problemPatterns) {
    return problemPatterns.get(0).getRecordAnalysisList().get(0).getProblemOccurrenceList().size();
  }

  private List<ProblemPattern> analyzeProblemPatternsForFile(String fileLocation) throws IOException, SerializationException {
    String xml = IOUtils.toString(new FileInputStream(fileLocation), StandardCharsets.UTF_8);
    final RDF rdf = new RdfConversionUtils().convertStringToRdf(xml);

    final ProblemPatternAnalyzer problemPatternAnalyzer = new ProblemPatternAnalyzer();
    return problemPatternAnalyzer.analyzeRecord(rdf);
  }

  @Test
  void analyzeRecord_P2() throws Exception {
    //Should contain two provider proxies that each contain a pair of identical title and description. All four values are identical on the two proxies.
    final List<ProblemPattern> problemPatterns = analyzeProblemPatternsForFile(FILE_XML_P2_LOCATION);

    assertNotNull(problemPatterns);
    assertEquals(1, problemPatterns.size());
    assertEquals(ProblemPatternDescription.P2, getFirstProblemPatternDescription(problemPatterns));
    assertEquals(1, getFirstProblemOccurrencesSize(problemPatterns));
  }

  @Test
  void analyzeRecord_P5() throws Exception {
    //Should contain valid titles in different languages and unrecognizable titles
    final List<ProblemPattern> problemPatterns = analyzeProblemPatternsForFile(FILE_XML_P5_LOCATION);

    assertNotNull(problemPatterns);
    assertEquals(1, problemPatterns.size());
    assertEquals(ProblemPatternDescription.P5, getFirstProblemPatternDescription(problemPatterns));
    assertEquals(2, getFirstProblemOccurrencesSize(problemPatterns));
  }

  @Test
  void analyzeRecord_P6() throws Exception {
    //Should contain one title that is not meaningful(too short)
    final List<ProblemPattern> problemPatterns = analyzeProblemPatternsForFile(FILE_XML_P6_LOCATION);

    assertNotNull(problemPatterns);
    assertEquals(1, problemPatterns.size());
    assertEquals(ProblemPatternDescription.P6, getFirstProblemPatternDescription(problemPatterns));
    assertEquals(1, getFirstProblemOccurrencesSize(problemPatterns));
  }

  @Test
  void analyzeRecord_P7() throws Exception {
    //Should not contain any descriptions
    final List<ProblemPattern> problemPatterns = analyzeProblemPatternsForFile(FILE_XML_P7_LOCATION);

    assertNotNull(problemPatterns);
    assertEquals(1, problemPatterns.size());
    assertEquals(ProblemPatternDescription.P7, getFirstProblemPatternDescription(problemPatterns));
    assertEquals(1, getFirstProblemOccurrencesSize(problemPatterns));
  }

  @Test
  void analyzeRecord_P7_DescriptionsEmpty() throws Exception {
    //Should contain multiple descriptions that are "empty"
    final List<ProblemPattern> problemPatterns = analyzeProblemPatternsForFile(FILE_XML_P7_DESCRIPTIONS_EMPTY_LOCATION);

    assertNotNull(problemPatterns);
    assertEquals(1, problemPatterns.size());
    assertEquals(ProblemPatternDescription.P7, getFirstProblemPatternDescription(problemPatterns));
    assertEquals(1, getFirstProblemOccurrencesSize(problemPatterns));
  }
}