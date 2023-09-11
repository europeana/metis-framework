package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.CENTURY_NUMERIC;
import static eu.europeana.normalization.dates.edtf.DateQualification.NO_QUALIFICATION;
import static org.junit.jupiter.params.provider.Arguments.of;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CenturyNumericDateExtractorTest implements DateExtractorTest {

  private static final CenturyNumericDateExtractor CENTURY_DATE_EXTRACTOR = new CenturyNumericDateExtractor();

  void assertExtract(String input, String expected, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    final DateNormalizationResult dateNormalizationResult = CENTURY_DATE_EXTRACTOR.extractDateProperty(input, NO_QUALIFICATION);
    assertDateNormalizationResult(dateNormalizationResult, expected, dateNormalizationExtractorMatchId);
  }

  @ParameterizedTest
  @MethodSource
  void extractNumeric(String input, String expected, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    assertExtract(input, expected, dateNormalizationExtractorMatchId);
  }

  private static Stream<Arguments> extractNumeric() {
    return Stream.of(
        //PATTERN_YYYY
        of("18..", "18XX", CENTURY_NUMERIC),
        of("  18..  ", "18XX", CENTURY_NUMERIC),
        of("?18..", "18XX?", CENTURY_NUMERIC),
        of("18..?", "18XX?", CENTURY_NUMERIC),
        of("?18..?", "18XX?", CENTURY_NUMERIC),
        of("192?", null, null, null), //Too many digits
        of("1..", null, null, null), //Too few digits

        //PATTERN_ENGLISH
        of("1st century", "00XX", CENTURY_NUMERIC),
        of("2nd century", "01XX", CENTURY_NUMERIC),
        of("3rd century", "02XX", CENTURY_NUMERIC),
        of("11th century", "10XX", CENTURY_NUMERIC),
        of("  11th century  ", "10XX", CENTURY_NUMERIC),
        of("?11th century", "10XX?", CENTURY_NUMERIC),
        of("11th century?", "10XX?", CENTURY_NUMERIC),
        of("?11th century?", "10XX?", CENTURY_NUMERIC),
        of("12th century BC", null, null, null), // not supported
        of("[10th century]", null, null, null), // not supported
        of("11thcentury", null, null, null), //Incorrect spacing numeric
        of("11st century", null, null, null), //Incorrect suffix
        of("12rd century", null, null, null), //Incorrect suffix
        of("13st century", null, null, null), //Incorrect suffix
        of("21th century", null, null, null), //Incorrect suffix
        of("0st century", null, null, null), //Out of range
        of("22nd century", null, null, null) //Out of range
    );
  }

}