package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.CENTURY_NUMERIC;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.CENTURY_RANGE_ROMAN;
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

class CenturyDateExtractorTest {
  private static final CenturyDateExtractor CENTURY_DATE_EXTRACTOR = new CenturyDateExtractor();

  void assertExtract(String input, String expected, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    final DateNormalizationResult dateNormalizationResult = CENTURY_DATE_EXTRACTOR.extractDateProperty(input, NO_QUALIFICATION);
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
  @MethodSource("extractNumericData")
  void extractNumeric(String input, String expected, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    assertExtract(input, expected, dateNormalizationExtractorMatchId);
  }

  @ParameterizedTest
  @MethodSource("extractRomanData")
  void extractRoman(String input, String expected, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    assertExtract(input, expected, dateNormalizationExtractorMatchId);
  }

  private static Stream<Arguments> extractNumericData() {
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

  private static Stream<Arguments> extractRomanData() {
    return Stream.of(
        //PATTERN_ROMAN
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
        of("IXX", null, null, null), // Invalid roman

        //PATTERN_ROMAN_RANGE
        //Uppercase
        of("I-II", "00XX/01XX", CENTURY_RANGE_ROMAN),
        of("II-III", "01XX/02XX", CENTURY_RANGE_ROMAN),
        of("III-IV", "02XX/03XX", CENTURY_RANGE_ROMAN),
        of("IV-V", "03XX/04XX", CENTURY_RANGE_ROMAN),
        of("V-VI", "04XX/05XX", CENTURY_RANGE_ROMAN),
        of("VI-VII", "05XX/06XX", CENTURY_RANGE_ROMAN),
        of("VII-VIII", "06XX/07XX", CENTURY_RANGE_ROMAN),
        of("VIII-IX", "07XX/08XX", CENTURY_RANGE_ROMAN),
        of("IX-X", "08XX/09XX", CENTURY_RANGE_ROMAN),
        of("X-XI", "09XX/10XX", CENTURY_RANGE_ROMAN),
        of("XI-XII", "10XX/11XX", CENTURY_RANGE_ROMAN),
        of("XII-XIII", "11XX/12XX", CENTURY_RANGE_ROMAN),
        of("XIII-XIV", "12XX/13XX", CENTURY_RANGE_ROMAN),
        of("XIV-XV", "13XX/14XX", CENTURY_RANGE_ROMAN),
        of("XV-XVI", "14XX/15XX", CENTURY_RANGE_ROMAN),
        of("XVI-XVII", "15XX/16XX", CENTURY_RANGE_ROMAN),
        of("XVII-XVIII", "16XX/17XX", CENTURY_RANGE_ROMAN),
        of("XVIII-XIX", "17XX/18XX", CENTURY_RANGE_ROMAN),
        of("XIX-XX", "18XX/19XX", CENTURY_RANGE_ROMAN),
        of("XX-XXI", "19XX/20XX", CENTURY_RANGE_ROMAN),

        //Lowercase
        of("i-ii", "00XX/01XX", CENTURY_RANGE_ROMAN),
        of("ii-iii", "01XX/02XX", CENTURY_RANGE_ROMAN),
        of("iii-iv", "02XX/03XX", CENTURY_RANGE_ROMAN),
        of("iv-v", "03XX/04XX", CENTURY_RANGE_ROMAN),
        of("v-vi", "04XX/05XX", CENTURY_RANGE_ROMAN),
        of("vi-vii", "05XX/06XX", CENTURY_RANGE_ROMAN),
        of("vii-viii", "06XX/07XX", CENTURY_RANGE_ROMAN),
        of("viii-ix", "07XX/08XX", CENTURY_RANGE_ROMAN),
        of("ix-x", "08XX/09XX", CENTURY_RANGE_ROMAN),
        of("x-xi", "09XX/10XX", CENTURY_RANGE_ROMAN),
        of("xi-xii", "10XX/11XX", CENTURY_RANGE_ROMAN),
        of("xii-xiii", "11XX/12XX", CENTURY_RANGE_ROMAN),
        of("xiii-xiv", "12XX/13XX", CENTURY_RANGE_ROMAN),
        of("xiv-xv", "13XX/14XX", CENTURY_RANGE_ROMAN),
        of("xv-xvi", "14XX/15XX", CENTURY_RANGE_ROMAN),
        of("xvi-xvii", "15XX/16XX", CENTURY_RANGE_ROMAN),
        of("xvii-xviii", "16XX/17XX", CENTURY_RANGE_ROMAN),
        of("xviii-xix", "17XX/18XX", CENTURY_RANGE_ROMAN),
        of("xix-xx", "18XX/19XX", CENTURY_RANGE_ROMAN),
        of("xx-xxi", "19XX/20XX", CENTURY_RANGE_ROMAN),

        //Prefixes
        of("s I-II", "00XX/01XX", CENTURY_RANGE_ROMAN),
        of("S I-II", "00XX/01XX", CENTURY_RANGE_ROMAN),
        of("s. I-II", "00XX/01XX", CENTURY_RANGE_ROMAN),
        of("S. I-II", "00XX/01XX", CENTURY_RANGE_ROMAN),
        of("sec.IV-VII", "03XX/06XX", CENTURY_RANGE_ROMAN),
        of("SEC.IV-VII", "03XX/06XX", CENTURY_RANGE_ROMAN),
        of("sec. IV-VII", "03XX/06XX", CENTURY_RANGE_ROMAN),
        of("SEC. IV-VII", "03XX/06XX", CENTURY_RANGE_ROMAN),
        of("saec.VII-XVIII", "06XX/17XX", CENTURY_RANGE_ROMAN),
        of("SAEC.VII-XVIII", "06XX/17XX", CENTURY_RANGE_ROMAN),
        of("saec. XVI-XVIII", "15XX/17XX", CENTURY_RANGE_ROMAN),
        of("SAEC. XVI-XVIII", "15XX/17XX", CENTURY_RANGE_ROMAN),

        //Other possibilities and uncertain
        of("s I-iI", "00XX/01XX", CENTURY_RANGE_ROMAN),
        of("  s I-II  ", "00XX/01XX", CENTURY_RANGE_ROMAN),
        of("?saec.X-XVIII", "09XX?/17XX?", CENTURY_RANGE_ROMAN),
        of("X-XVIII?", "09XX?/17XX?", CENTURY_RANGE_ROMAN),
        of("?saec.X-XVIII?", "09XX?/17XX?", CENTURY_RANGE_ROMAN),

        //Non matches
        of("S. XIIII-XIIIV", null, null), //Invalid roman
        of("S. XVIII-", null, null, null), //Open-ended incorrect
        of("sII-V", null, null), //Without a dot a space is required
        of("secVI-XVII", null, null), //Without a dot a space is required
        of("saecX-XVIII?", null, null) //Without a dot a space is required
    );
  }

}