package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.CENTURY_NUMERIC;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.CENTURY_RANGE_ROMAN;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.CENTURY_ROMAN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CenturyDateExtractorTest {

  private static final CenturyDateExtractor CENTURY_DATE_EXTRACTOR = new CenturyDateExtractor();

  void extract(String input, String expected, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    final DateNormalizationResult dateNormalizationResult = CENTURY_DATE_EXTRACTOR.extract(input);
    if (expected == null) {
      assertNull(dateNormalizationResult);
    } else {
      final String actual = dateNormalizationResult.getEdtfDate().toString();
      assertEquals(expected, actual);
      assertEquals(actual.contains("?"), dateNormalizationResult.getEdtfDate().isUncertain());
      assertEquals(dateNormalizationExtractorMatchId, dateNormalizationResult.getDateNormalizationExtractorMatchId());
    }
  }

  @ParameterizedTest
  @MethodSource("extractNumericData")
  void extractNumeric(String input, String expected, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    extract(input, expected, dateNormalizationExtractorMatchId);
  }

  @ParameterizedTest
  @MethodSource("extractRomanData")
  void extractRoman(String input, String expected, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    extract(input, expected, dateNormalizationExtractorMatchId);
  }

  private static Stream<Arguments> extractNumericData() {
    return Stream.of(
        //PATTERN_YYYY
        Arguments.of("18..", "18XX", CENTURY_NUMERIC),
        Arguments.of("  18..  ", "18XX", CENTURY_NUMERIC),
        Arguments.of("?18..", "18XX?", CENTURY_NUMERIC),
        Arguments.of("18..?", "18XX?", CENTURY_NUMERIC),
        Arguments.of("?18..?", "18XX?", CENTURY_NUMERIC),
        Arguments.of("192?", null, null, null), //Too many digits
        Arguments.of("1..", null, null, null), //Too few digits

        //PATTERN_ENGLISH
        Arguments.of("1st century", "00XX", CENTURY_NUMERIC),
        Arguments.of("2nd century", "01XX", CENTURY_NUMERIC),
        Arguments.of("3rd century", "02XX", CENTURY_NUMERIC),
        Arguments.of("11th century", "10XX", CENTURY_NUMERIC),
        Arguments.of("  11th century  ", "10XX", CENTURY_NUMERIC),
        Arguments.of("?11th century", "10XX?", CENTURY_NUMERIC),
        Arguments.of("11th century?", "10XX?", CENTURY_NUMERIC),
        Arguments.of("?11th century?", "10XX?", CENTURY_NUMERIC),
        Arguments.of("12th century BC", null, null, null), // not supported
        Arguments.of("[10th century]", null, null, null), // not supported
        Arguments.of("11thcentury", null, null, null), //Incorrect spacing numeric
        Arguments.of("11st century", null, null, null), //Incorrect suffix
        Arguments.of("12rd century", null, null, null), //Incorrect suffix
        Arguments.of("13st century", null, null, null), //Incorrect suffix
        Arguments.of("21th century", null, null, null), //Incorrect suffix
        Arguments.of("0st century", null, null, null), //Out of range
        Arguments.of("22nd century", null, null, null) //Out of range
    );
  }

  private static Stream<Arguments> extractRomanData() {
    return Stream.of(
        //PATTERN_ROMAN
        //Uppercase
        Arguments.of("I", "00XX", CENTURY_ROMAN),
        Arguments.of("IV", "03XX", CENTURY_ROMAN),
        Arguments.of("V", "04XX", CENTURY_ROMAN),
        Arguments.of("VI", "05XX", CENTURY_ROMAN),
        Arguments.of("IX", "08XX", CENTURY_ROMAN),
        Arguments.of("X", "09XX", CENTURY_ROMAN),
        Arguments.of("XI", "10XX", CENTURY_ROMAN),
        Arguments.of("XIV", "13XX", CENTURY_ROMAN),
        Arguments.of("XV", "14XX", CENTURY_ROMAN),
        Arguments.of("XVI", "15XX", CENTURY_ROMAN),
        Arguments.of("XIX", "18XX", CENTURY_ROMAN),
        Arguments.of("XX", "19XX", CENTURY_ROMAN),
        Arguments.of("XXI", "20XX", CENTURY_ROMAN),

        //Lower case
        Arguments.of("i", "00XX", CENTURY_ROMAN),
        Arguments.of("iv", "03XX", CENTURY_ROMAN),
        Arguments.of("v", "04XX", CENTURY_ROMAN),
        Arguments.of("vi", "05XX", CENTURY_ROMAN),
        Arguments.of("ix", "08XX", CENTURY_ROMAN),
        Arguments.of("x", "09XX", CENTURY_ROMAN),
        Arguments.of("xi", "10XX", CENTURY_ROMAN),
        Arguments.of("xiv", "13XX", CENTURY_ROMAN),
        Arguments.of("xv", "14XX", CENTURY_ROMAN),
        Arguments.of("xvi", "15XX", CENTURY_ROMAN),
        Arguments.of("xix", "18XX", CENTURY_ROMAN),
        Arguments.of("xx", "19XX", CENTURY_ROMAN),
        Arguments.of("xxi", "20XX", CENTURY_ROMAN),

        //Prefixes
        Arguments.of("s I", "00XX", CENTURY_ROMAN),
        Arguments.of("s. I", "00XX", CENTURY_ROMAN),
        Arguments.of("S I", "00XX", CENTURY_ROMAN),
        Arguments.of("S.I", "00XX", CENTURY_ROMAN),
        Arguments.of("sec.I", "00XX", CENTURY_ROMAN),
        Arguments.of("SEC.I", "00XX", CENTURY_ROMAN),
        Arguments.of("sec. I", "00XX", CENTURY_ROMAN),
        Arguments.of("SEC. I", "00XX", CENTURY_ROMAN),
        Arguments.of("saec.I", "00XX", CENTURY_ROMAN),
        Arguments.of("SAEC.I", "00XX", CENTURY_ROMAN),
        Arguments.of("saec. I", "00XX", CENTURY_ROMAN),
        Arguments.of("SAEC. I", "00XX", CENTURY_ROMAN),
        //Other possibilities and uncertain
        Arguments.of("Ii", "01XX", CENTURY_ROMAN),
        Arguments.of("  s I  ", "00XX", CENTURY_ROMAN),
        Arguments.of("?s. I", "00XX?", CENTURY_ROMAN),
        Arguments.of("sec. I?", "00XX?", CENTURY_ROMAN),
        Arguments.of("?saec. I?", "00XX?", CENTURY_ROMAN),
        Arguments.of("  I  ", "00XX", CENTURY_ROMAN),
        Arguments.of("?I", "00XX?", CENTURY_ROMAN),
        Arguments.of("I?", "00XX?", CENTURY_ROMAN),
        Arguments.of("?I?", "00XX?", CENTURY_ROMAN),
        //Non matches
        Arguments.of("saecI", null, null), //Without a dot a space is required
        Arguments.of("secI", null, null), //Without a dot a space is required
        Arguments.of("MDCLXX", null, null, null), // Not supported range
        Arguments.of("IXX", null, null, null), // Invalid roman

        //PATTERN_ROMAN_RANGE
        //Uppercase
        Arguments.of("I-II", "00XX/01XX", CENTURY_RANGE_ROMAN),
        Arguments.of("II-III", "01XX/02XX", CENTURY_RANGE_ROMAN),
        Arguments.of("III-IV", "02XX/03XX", CENTURY_RANGE_ROMAN),
        Arguments.of("IV-V", "03XX/04XX", CENTURY_RANGE_ROMAN),
        Arguments.of("V-VI", "04XX/05XX", CENTURY_RANGE_ROMAN),
        Arguments.of("VI-VII", "05XX/06XX", CENTURY_RANGE_ROMAN),
        Arguments.of("VII-VIII", "06XX/07XX", CENTURY_RANGE_ROMAN),
        Arguments.of("VIII-IX", "07XX/08XX", CENTURY_RANGE_ROMAN),
        Arguments.of("IX-X", "08XX/09XX", CENTURY_RANGE_ROMAN),
        Arguments.of("X-XI", "09XX/10XX", CENTURY_RANGE_ROMAN),
        Arguments.of("XI-XII", "10XX/11XX", CENTURY_RANGE_ROMAN),
        Arguments.of("XII-XIII", "11XX/12XX", CENTURY_RANGE_ROMAN),
        Arguments.of("XIII-XIV", "12XX/13XX", CENTURY_RANGE_ROMAN),
        Arguments.of("XIV-XV", "13XX/14XX", CENTURY_RANGE_ROMAN),
        Arguments.of("XV-XVI", "14XX/15XX", CENTURY_RANGE_ROMAN),
        Arguments.of("XVI-XVII", "15XX/16XX", CENTURY_RANGE_ROMAN),
        Arguments.of("XVII-XVIII", "16XX/17XX", CENTURY_RANGE_ROMAN),
        Arguments.of("XVIII-XIX", "17XX/18XX", CENTURY_RANGE_ROMAN),
        Arguments.of("XIX-XX", "18XX/19XX", CENTURY_RANGE_ROMAN),
        Arguments.of("XX-XXI", "19XX/20XX", CENTURY_RANGE_ROMAN),

        //Lowercase
        Arguments.of("i-ii", "00XX/01XX", CENTURY_RANGE_ROMAN),
        Arguments.of("ii-iii", "01XX/02XX", CENTURY_RANGE_ROMAN),
        Arguments.of("iii-iv", "02XX/03XX", CENTURY_RANGE_ROMAN),
        Arguments.of("iv-v", "03XX/04XX", CENTURY_RANGE_ROMAN),
        Arguments.of("v-vi", "04XX/05XX", CENTURY_RANGE_ROMAN),
        Arguments.of("vi-vii", "05XX/06XX", CENTURY_RANGE_ROMAN),
        Arguments.of("vii-viii", "06XX/07XX", CENTURY_RANGE_ROMAN),
        Arguments.of("viii-ix", "07XX/08XX", CENTURY_RANGE_ROMAN),
        Arguments.of("ix-x", "08XX/09XX", CENTURY_RANGE_ROMAN),
        Arguments.of("x-xi", "09XX/10XX", CENTURY_RANGE_ROMAN),
        Arguments.of("xi-xii", "10XX/11XX", CENTURY_RANGE_ROMAN),
        Arguments.of("xii-xiii", "11XX/12XX", CENTURY_RANGE_ROMAN),
        Arguments.of("xiii-xiv", "12XX/13XX", CENTURY_RANGE_ROMAN),
        Arguments.of("xiv-xv", "13XX/14XX", CENTURY_RANGE_ROMAN),
        Arguments.of("xv-xvi", "14XX/15XX", CENTURY_RANGE_ROMAN),
        Arguments.of("xvi-xvii", "15XX/16XX", CENTURY_RANGE_ROMAN),
        Arguments.of("xvii-xviii", "16XX/17XX", CENTURY_RANGE_ROMAN),
        Arguments.of("xviii-xix", "17XX/18XX", CENTURY_RANGE_ROMAN),
        Arguments.of("xix-xx", "18XX/19XX", CENTURY_RANGE_ROMAN),
        Arguments.of("xx-xxi", "19XX/20XX", CENTURY_RANGE_ROMAN),

        //Prefixes
        Arguments.of("s I-II", "00XX/01XX", CENTURY_RANGE_ROMAN),
        Arguments.of("S I-II", "00XX/01XX", CENTURY_RANGE_ROMAN),
        Arguments.of("s. I-II", "00XX/01XX", CENTURY_RANGE_ROMAN),
        Arguments.of("S. I-II", "00XX/01XX", CENTURY_RANGE_ROMAN),
        Arguments.of("sec.IV-VII", "03XX/06XX", CENTURY_RANGE_ROMAN),
        Arguments.of("SEC.IV-VII", "03XX/06XX", CENTURY_RANGE_ROMAN),
        Arguments.of("sec. IV-VII", "03XX/06XX", CENTURY_RANGE_ROMAN),
        Arguments.of("SEC. IV-VII", "03XX/06XX", CENTURY_RANGE_ROMAN),
        Arguments.of("saec.VII-XVIII", "06XX/17XX", CENTURY_RANGE_ROMAN),
        Arguments.of("SAEC.VII-XVIII", "06XX/17XX", CENTURY_RANGE_ROMAN),
        Arguments.of("saec. XVI-XVIII", "15XX/17XX", CENTURY_RANGE_ROMAN),
        Arguments.of("SAEC. XVI-XVIII", "15XX/17XX", CENTURY_RANGE_ROMAN),

        //Other possibilities and uncertain
        Arguments.of("s I-iI", "00XX/01XX", CENTURY_RANGE_ROMAN),
        Arguments.of("  s I-II  ", "00XX/01XX", CENTURY_RANGE_ROMAN),
        Arguments.of("?saec.X-XVIII", "09XX?/17XX?", CENTURY_RANGE_ROMAN),
        Arguments.of("X-XVIII?", "09XX?/17XX?", CENTURY_RANGE_ROMAN),
        Arguments.of("?saec.X-XVIII?", "09XX?/17XX?", CENTURY_RANGE_ROMAN),

        //Non matches
        Arguments.of("S. XIIII-XIIIV", null, null), //Invalid roman
        Arguments.of("S. XVIII-", null, null, null), //Open-ended incorrect
        Arguments.of("sII-V", null, null), //Without a dot a space is required
        Arguments.of("secVI-XVII", null, null), //Without a dot a space is required
        Arguments.of("saecX-XVIII?", null, null) //Without a dot a space is required
    );
  }

}