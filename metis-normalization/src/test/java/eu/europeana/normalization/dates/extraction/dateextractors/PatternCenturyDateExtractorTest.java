package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.CENTURY_NUMERIC;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.CENTURY_RANGE_ROMAN;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.CENTURY_ROMAN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PatternCenturyDateExtractorTest {

  private static final PatternCenturyDateExtractor patternCenturyDateExtractor = new PatternCenturyDateExtractor();

  @ParameterizedTest
  @MethodSource("extractData")
  void extract(String input, String expected, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    final DateNormalizationResult dateNormalizationResult = patternCenturyDateExtractor.extract(input);
    if (expected == null) {
      assertNull(dateNormalizationResult);
    } else {
      final String actual = dateNormalizationResult.getEdtfDate().toString();
      assertEquals(expected, actual);
      if (actual.contains("?")) {
        assertTrue(dateNormalizationResult.getEdtfDate().isUncertain());
      } else {
        assertFalse(dateNormalizationResult.getEdtfDate().isUncertain());
      }
      assertEquals(dateNormalizationExtractorMatchId, dateNormalizationResult.getDateNormalizationExtractorMatchId());
    }
  }

  private static Stream<Arguments> extractData() {
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
        Arguments.of("21th century", null, null, null), //Incorrect suffix
        Arguments.of("22rd century", null, null, null), //Incorrect suffix
        Arguments.of("23st century", null, null, null), //Incorrect suffix

        //PATTERN_ROMAN
        Arguments.of("s I", "00XX", CENTURY_ROMAN),
        Arguments.of("S IV", "03XX", CENTURY_ROMAN),
        Arguments.of("s V", "04XX", CENTURY_ROMAN),
        Arguments.of("s VI", "05XX", CENTURY_ROMAN),
        Arguments.of("s IX", "08XX", CENTURY_ROMAN),
        Arguments.of("s X", "09XX", CENTURY_ROMAN),
        Arguments.of("s XI", "10XX", CENTURY_ROMAN),
        Arguments.of("s XIV", "13XX", CENTURY_ROMAN),
        Arguments.of("s XV", "14XX", CENTURY_ROMAN),
        Arguments.of("s XVI", "15XX", CENTURY_ROMAN),
        Arguments.of("s XIX", "18XX", CENTURY_ROMAN),
        Arguments.of("s XX", "19XX", CENTURY_ROMAN),
        Arguments.of("s XXI", "20XX", CENTURY_ROMAN),
        Arguments.of("s. I", "00XX", CENTURY_ROMAN),
        Arguments.of("S.I", "00XX", CENTURY_ROMAN),
        Arguments.of("sec.I", "00XX", CENTURY_ROMAN),
        Arguments.of("SEC. I", "00XX", CENTURY_ROMAN),
        Arguments.of("secI", "00XX", CENTURY_ROMAN),
        Arguments.of("saec.I", "00XX", CENTURY_ROMAN),
        Arguments.of("SAEC. I", "00XX", CENTURY_ROMAN),
        // TODO: 12/09/2022 prefixes without space are not allowed if the dot does not exist.If the dot exists, spaces is optional
        Arguments.of("saecI", "00XX", CENTURY_ROMAN),
        Arguments.of("  s I  ", "00XX", CENTURY_ROMAN),
        Arguments.of("?s. I", "00XX?", CENTURY_ROMAN),
        Arguments.of("sec. I?", "00XX?", CENTURY_ROMAN),
        Arguments.of("?saec. I?", "00XX?", CENTURY_ROMAN),

        //PATTERN_ROMAN_CLEAN
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
        Arguments.of("I", "00XX", CENTURY_ROMAN),
        Arguments.of("I", "00XX", CENTURY_ROMAN),
        Arguments.of("I", "00XX", CENTURY_ROMAN),
        Arguments.of("I", "00XX", CENTURY_ROMAN),
        Arguments.of("I", "00XX", CENTURY_ROMAN),
        Arguments.of("I", "00XX", CENTURY_ROMAN),
        Arguments.of("I", "00XX", CENTURY_ROMAN),
        Arguments.of("I", "00XX", CENTURY_ROMAN),
        Arguments.of("  I  ", "00XX", CENTURY_ROMAN),
        Arguments.of("?I", "00XX?", CENTURY_ROMAN),
        Arguments.of("I?", "00XX?", CENTURY_ROMAN),
        Arguments.of("?I?", "00XX?", CENTURY_ROMAN),
        Arguments.of("MDCLXX", null, null, null), // Not supported range
        Arguments.of("IXX", null, null, null), // Invalid roman

        //PATTERN_ROMAN_RANGE
        Arguments.of("s.I-II", "00XX/01XX", CENTURY_RANGE_ROMAN),
        Arguments.of("s.II-III", "01XX/02XX", CENTURY_RANGE_ROMAN),
        Arguments.of("s.III-IV", "02XX/03XX", CENTURY_RANGE_ROMAN),
        Arguments.of("s.IV-V", "03XX/04XX", CENTURY_RANGE_ROMAN),
        Arguments.of("s.V-VI", "04XX/05XX", CENTURY_RANGE_ROMAN),
        Arguments.of("s.VI-VII", "05XX/06XX", CENTURY_RANGE_ROMAN),
        Arguments.of("s.VII-VIII", "06XX/07XX", CENTURY_RANGE_ROMAN),
        Arguments.of("s.VIII-IX", "07XX/08XX", CENTURY_RANGE_ROMAN),
        Arguments.of("s.IX-X", "08XX/09XX", CENTURY_RANGE_ROMAN),
        Arguments.of("s.X-XI", "09XX/10XX", CENTURY_RANGE_ROMAN),
        Arguments.of("s.XI-XII", "10XX/11XX", CENTURY_RANGE_ROMAN),
        Arguments.of("s.XII-XIII", "11XX/12XX", CENTURY_RANGE_ROMAN),
        Arguments.of("s.XIII-XIV", "12XX/13XX", CENTURY_RANGE_ROMAN),
        Arguments.of("s.XIV-XV", "13XX/14XX", CENTURY_RANGE_ROMAN),
        Arguments.of("s.XV-XVI", "14XX/15XX", CENTURY_RANGE_ROMAN),
        Arguments.of("s.XVI-XVII", "15XX/16XX", CENTURY_RANGE_ROMAN),
        Arguments.of("s.XVII-XVIII", "16XX/17XX", CENTURY_RANGE_ROMAN),
        Arguments.of("s.XVIII-XIX", "17XX/18XX", CENTURY_RANGE_ROMAN),
        Arguments.of("s.XIX-XX", "18XX/19XX", CENTURY_RANGE_ROMAN),
        Arguments.of("s.XX-XXI", "19XX/20XX", CENTURY_RANGE_ROMAN),
        Arguments.of("sII-V", "01XX/04XX", CENTURY_RANGE_ROMAN),
        Arguments.of("S.X-XIV", "09XX/13XX", CENTURY_RANGE_ROMAN),
        Arguments.of("sec.IV-VII", "03XX/06XX", CENTURY_RANGE_ROMAN),
        Arguments.of("sec. V-X", "04XX/09XX", CENTURY_RANGE_ROMAN),
        Arguments.of("secVI-XVII", "05XX/16XX", CENTURY_RANGE_ROMAN),
        Arguments.of("saec.VII-XVIII", "06XX/17XX", CENTURY_RANGE_ROMAN),
        Arguments.of("saec. XVI-XVIII", "15XX/17XX", CENTURY_RANGE_ROMAN),
        Arguments.of("saecX-XVIII", "09XX/17XX", CENTURY_RANGE_ROMAN),
        Arguments.of("?saecX-XVIII", "09XX?/17XX?", CENTURY_RANGE_ROMAN),
        Arguments.of("saecX-XVIII?", "09XX?/17XX?", CENTURY_RANGE_ROMAN),
        Arguments.of("?saecX-XVIII?", "09XX?/17XX?", CENTURY_RANGE_ROMAN),
        Arguments.of("S. XIIII-XIIIV", null, null), //Invalid roman
        Arguments.of("S. XVIII-", null, null, null), //Open-ended incorrect
        Arguments.of("XVI-XVIII", null, null, null) //Missing prefix
    );
  }

}