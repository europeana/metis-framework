package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.CENTURY_ROMAN;
import static eu.europeana.normalization.dates.edtf.DateQualification.NO_QUALIFICATION;
import static org.junit.jupiter.params.provider.Arguments.of;

import eu.europeana.normalization.dates.DateNormalizationResult;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CenturyRomaDateExtractorTest implements DateExtractorTest {

  private static final CenturyRomanDateExtractor ROMAN_CENTURY_DATE_EXTRACTOR = new CenturyRomanDateExtractor();

  void assertExtract(String input, String expected) {
    final DateNormalizationResult dateNormalizationResult = ROMAN_CENTURY_DATE_EXTRACTOR.extractDateProperty(input,
        NO_QUALIFICATION);
    assertDateNormalizationResult(dateNormalizationResult, expected, CENTURY_ROMAN);
  }

  @ParameterizedTest
  @MethodSource
  void extractRoman(String input, String expected) {
    assertExtract(input, expected);
  }

  private static Stream<Arguments> extractRoman() {
    return Stream.of(
        //Uppercase
        of("I", "00XX"),
        of("IV", "03XX"),
        of("V", "04XX"),
        of("VI", "05XX"),
        of("IX", "08XX"),
        of("X", "09XX"),
        of("XI", "10XX"),
        of("XIV", "13XX"),
        of("XV", "14XX"),
        of("XVI", "15XX"),
        of("XIX", "18XX"),
        of("XX", "19XX"),
        of("XXI", "20XX"),

        //Lower case
        of("i", "00XX"),
        of("iv", "03XX"),
        of("v", "04XX"),
        of("vi", "05XX"),
        of("ix", "08XX"),
        of("x", "09XX"),
        of("xi", "10XX"),
        of("xiv", "13XX"),
        of("xv", "14XX"),
        of("xvi", "15XX"),
        of("xix", "18XX"),
        of("xx", "19XX"),
        of("xxi", "20XX"),

        //Prefixes
        of("s I", "00XX"),
        of("s. I", "00XX"),
        of("S I", "00XX"),
        of("S.I", "00XX"),
        of("sec.I", "00XX"),
        of("SEC.I", "00XX"),
        of("sec. I", "00XX"),
        of("SEC. I", "00XX"),
        of("saec.I", "00XX"),
        of("SAEC.I", "00XX"),
        of("saec. I", "00XX"),
        of("SAEC. I", "00XX"),
        //Other possibilities and uncertain
        of("Ii", "01XX"),
        of("  s I  ", "00XX"),
        of("?s. I", "00XX?"),
        of("sec. I?", "00XX?"),
        of("?saec. I?", "00XX?"),
        of("  I  ", "00XX"),
        of("?I", "00XX?"),
        of("I?", "00XX?"),
        of("?I?", "00XX?"),
        //Non matches
        //Without a dot a space is required
        of("saecI", null),
        of("secI", null),
        // Not supported range
        of("MDCLXX", null),
        // Invalid roman
        of("IXX", null)
    );
  }

}