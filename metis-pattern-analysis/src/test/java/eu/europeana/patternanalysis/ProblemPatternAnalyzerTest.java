package eu.europeana.patternanalysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import eu.europeana.metis.schema.convert.SerializationException;
import eu.europeana.patternanalysis.view.ProblemPattern;
import eu.europeana.patternanalysis.view.ProblemPatternAnalysis;
import eu.europeana.patternanalysis.view.ProblemPatternDescription;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ProblemPatternAnalyzerTest {

  public static final String FILE_XML_NO_PROBLEM_PATTERNS_LOCATION = "src/test/resources/europeana_record_no_problem_patterns.xml";
  public static final String FILE_XML_EMPTY_CHOICES_LOCATION = "src/test/resources/europeana_record_empty_proxy_choices.xml";
  public static final String FILE_XML_P2_LOCATION = "src/test/resources/europeana_record_with_P2.xml";
  public static final String FILE_XML_P3_LOCATION = "src/test/resources/europeana_record_with_P3.xml";
  public static final String FILE_XML_P5_LOCATION = "src/test/resources/europeana_record_with_P5.xml";
  public static final String FILE_XML_P6_LOCATION = "src/test/resources/europeana_record_with_P6.xml";
  public static final String FILE_XML_P7_LOCATION = "src/test/resources/europeana_record_with_P7.xml";
  public static final String FILE_XML_P7_DESCRIPTIONS_EMPTY_LOCATION = "src/test/resources/europeana_record_with_P7_descriptions_empty.xml";
  public static final String FILE_XML_P9_LOCATION = "src/test/resources/europeana_record_with_P9.xml";
  public static final String FILE_XML_P12_LOCATION = "src/test/resources/europeana_record_with_P12.xml";

  private static Stream<Arguments> analyzeRecord() {
    return Stream.of(
        //Should not have any problem patterns generated
        Arguments.of(FILE_XML_NO_PROBLEM_PATTERNS_LOCATION, 0, null, 0),
        //Should not have any choices on the provider proxy(to check for null list), therefore reporting only P7
        Arguments.of(FILE_XML_EMPTY_CHOICES_LOCATION, 1, ProblemPatternDescription.P7, 1),
        //Should contain two provider proxies that each contain a pair of identical title and description. All four values are identical on the two proxies.
        Arguments.of(FILE_XML_P2_LOCATION, 1, ProblemPatternDescription.P2, 1),
        //Should contain identical titles, very similar ones and also completely different ones
        Arguments.of(FILE_XML_P3_LOCATION, 2, ProblemPatternDescription.P3, 1),
        //Should contain valid titles in different languages and unrecognizable titles
        Arguments.of(FILE_XML_P5_LOCATION, 1, ProblemPatternDescription.P5, 3),
        //Should contain one title that is not meaningful(too short)
        Arguments.of(FILE_XML_P6_LOCATION, 1, ProblemPatternDescription.P6, 1),
        //Should not contain any descriptions
        Arguments.of(FILE_XML_P7_LOCATION, 1, ProblemPatternDescription.P7, 1),
        //Should contain multiple descriptions that are "empty"
        Arguments.of(FILE_XML_P7_DESCRIPTIONS_EMPTY_LOCATION, 1, ProblemPatternDescription.P7, 1),
        //Should contain a description with length less than threshold
        Arguments.of(FILE_XML_P9_LOCATION, 1, ProblemPatternDescription.P9, 2),
        //Should contain a title with length more than threshold
        Arguments.of(FILE_XML_P12_LOCATION, 1, ProblemPatternDescription.P12, 1)
    );
  }

  private ProblemPatternDescription getRequestedProblemPattern(ProblemPatternDescription problemPatternDescription,
      List<ProblemPattern> problemPatterns) {
    return problemPatterns.stream()
                          .map(ProblemPattern::getProblemPatternDescription)
                          .filter(patternDescription -> patternDescription == problemPatternDescription).findFirst().orElse(null);
  }

  private ProblemPatternAnalysis analyzeProblemPatternsForFile(String fileLocation) throws IOException, SerializationException {
    String xml = IOUtils.toString(new FileInputStream(fileLocation), StandardCharsets.UTF_8);

    final ProblemPatternAnalyzer problemPatternAnalyzer = new ProblemPatternAnalyzer();
    return problemPatternAnalyzer.analyzeRecord(xml);
  }

  private int getRequestedProblemOccurrencesSize(ProblemPatternDescription problemPatternDescription,
      List<ProblemPattern> problemPatterns) {
    return problemPatterns.stream()
                          .filter(problemPattern -> problemPattern.getProblemPatternDescription()
                              == problemPatternDescription)
                          .map(problemPattern -> problemPattern.getRecordAnalysisList().get(0).getProblemOccurrenceList().size())
                          .findFirst().orElse(0);
  }

  @ParameterizedTest(name = "[{index}] - For file:{0}, totalPatterns:{1}, patternId:{2}, totalOccurrences:{3}")
  @MethodSource
  void analyzeRecord(String fileLocation, int totalPatterns, ProblemPatternDescription problemPatternDescription,
      int totalOccurrences) throws Exception {
    final ProblemPatternAnalysis problemPatternAnalysis = analyzeProblemPatternsForFile(fileLocation);

    assertNotNull(problemPatternAnalysis);
    assertEquals(totalPatterns, problemPatternAnalysis.getProblemPatterns().size());
    assertEquals(problemPatternDescription,
        getRequestedProblemPattern(problemPatternDescription, problemPatternAnalysis.getProblemPatterns()));
    assertEquals(totalOccurrences,
        getRequestedProblemOccurrencesSize(problemPatternDescription, problemPatternAnalysis.getProblemPatterns()));
  }
}