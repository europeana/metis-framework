package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.CENTURY_ROMAN;
import static eu.europeana.normalization.dates.edtf.DateQualification.NO_QUALIFICATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.of;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.DateNormalizationResultStatus;
import eu.europeana.normalization.dates.edtf.DateQualification;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RomanCenturyNumericDateExtractorTest {

  private static final CenturyRomanDateExtractor ROMAN_CENTURY_DATE_EXTRACTOR = new CenturyRomanDateExtractor();

  void assertExtract(String input, String expected, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    final DateNormalizationResult dateNormalizationResult = ROMAN_CENTURY_DATE_EXTRACTOR.extractDateProperty(input,
        NO_QUALIFICATION);
    if (expected == null) {
      assertEquals(DateNormalizationResultStatus.NO_MATCH, dateNormalizationResult.getDateNormalizationResultStatus());
    } else {
      final String actual = dateNormalizationResult.getEdtfDate().toString();
      assertEquals(expected, actual);
      assertEquals(actual.contains("?"),
          dateNormalizationResult.getEdtfDate().getDateQualification() == DateQualification.UNCERTAIN);
      assertEquals(dateNormalizationExtractorMatchId, dateNormalizationResult.getDateNormalizationExtractorMatchId());
    }
  }

  @ParameterizedTest
  @MethodSource
  void extractRoman(String input, String expected, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    assertExtract(input, expected, dateNormalizationExtractorMatchId);
  }

  private static Stream<Arguments> extractRoman() {
    return Stream.of(
        //Uppercase
        of("I", "00XX", CENTURY_ROMAN),
        of("IV", "03XX", CENTURY_ROMAN),
        of("V", "04XX", CENTURY_ROMAN),
        of("VI", "05XX", CENTURY_ROMAN),
        of("IX", "08XX", CENTURY_ROMAN),
        of("X", "09XX", CENTURY_ROMAN),
        of("XI", "10XX", CENTURY_ROMAN),
        of("XIV", "13XX", CENTURY_ROMAN),
        of("XV", "14XX", CENTURY_ROMAN),
        of("XVI", "15XX", CENTURY_ROMAN),
        of("XIX", "18XX", CENTURY_ROMAN),
        of("XX", "19XX", CENTURY_ROMAN),
        of("XXI", "20XX", CENTURY_ROMAN),

        //Lower case
        of("i", "00XX", CENTURY_ROMAN),
        of("iv", "03XX", CENTURY_ROMAN),
        of("v", "04XX", CENTURY_ROMAN),
        of("vi", "05XX", CENTURY_ROMAN),
        of("ix", "08XX", CENTURY_ROMAN),
        of("x", "09XX", CENTURY_ROMAN),
        of("xi", "10XX", CENTURY_ROMAN),
        of("xiv", "13XX", CENTURY_ROMAN),
        of("xv", "14XX", CENTURY_ROMAN),
        of("xvi", "15XX", CENTURY_ROMAN),
        of("xix", "18XX", CENTURY_ROMAN),
        of("xx", "19XX", CENTURY_ROMAN),
        of("xxi", "20XX", CENTURY_ROMAN),

        //Prefixes
        of("s I", "00XX", CENTURY_ROMAN),
        of("s. I", "00XX", CENTURY_ROMAN),
        of("S I", "00XX", CENTURY_ROMAN),
        of("S.I", "00XX", CENTURY_ROMAN),
        of("sec.I", "00XX", CENTURY_ROMAN),
        of("SEC.I", "00XX", CENTURY_ROMAN),
        of("sec. I", "00XX", CENTURY_ROMAN),
        of("SEC. I", "00XX", CENTURY_ROMAN),
        of("saec.I", "00XX", CENTURY_ROMAN),
        of("SAEC.I", "00XX", CENTURY_ROMAN),
        of("saec. I", "00XX", CENTURY_ROMAN),
        of("SAEC. I", "00XX", CENTURY_ROMAN),
        //Other possibilities and uncertain
        of("Ii", "01XX", CENTURY_ROMAN),
        of("  s I  ", "00XX", CENTURY_ROMAN),
        of("?s. I", "00XX?", CENTURY_ROMAN),
        of("sec. I?", "00XX?", CENTURY_ROMAN),
        of("?saec. I?", "00XX?", CENTURY_ROMAN),
        of("  I  ", "00XX", CENTURY_ROMAN),
        of("?I", "00XX?", CENTURY_ROMAN),
        of("I?", "00XX?", CENTURY_ROMAN),
        of("?I?", "00XX?", CENTURY_ROMAN),
        //Non matches
        of("saecI", null, null), //Without a dot a space is required
        of("secI", null, null), //Without a dot a space is required
        of("MDCLXX", null, null, null), // Not supported range
        of("IXX", null, null, null) // Invalid roman
    );
  }

}