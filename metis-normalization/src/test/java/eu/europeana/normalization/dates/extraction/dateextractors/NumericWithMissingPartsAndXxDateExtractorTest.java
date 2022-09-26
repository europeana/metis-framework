package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS_XX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.of;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class NumericWithMissingPartsAndXxDateExtractorTest {

  private static final NumericWithMissingPartsAndXxDateExtractor NUMERIC_WITH_MISSING_PARTS_AND_XX_DATE_EXTRACTOR = new NumericWithMissingPartsAndXxDateExtractor();

  @ParameterizedTest
  @MethodSource
  void extractYMD(String input, String expected, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    extract(input, expected, dateNormalizationExtractorMatchId);
  }

  @ParameterizedTest
  @MethodSource
  void extractMDY(String input, String expected, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    extract(input, expected, dateNormalizationExtractorMatchId);
  }

  void extract(String input, String expected, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    final DateNormalizationResult dateNormalizationResult = NUMERIC_WITH_MISSING_PARTS_AND_XX_DATE_EXTRACTOR.extract(input);
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

  private static Stream<Arguments> extractYMD() {

    //    (Some of those also captured here and NumericWithMissingPartsDateExtractor)
    return Stream.of(
        //YEAR
        of("1989", "1989", NUMERIC_ALL_VARIANTS_XX), //A month and day can be missing
        of("198X", "198X", NUMERIC_ALL_VARIANTS_XX),
        of("198U", "198X", NUMERIC_ALL_VARIANTS_XX),
        of("19--", "19XX", NUMERIC_ALL_VARIANTS_XX),
        of("19XX", "19XX", NUMERIC_ALL_VARIANTS_XX),
        of("19UU", "19XX", NUMERIC_ALL_VARIANTS_XX),
        of("19??", "19XX", NUMERIC_ALL_VARIANTS_XX),
        of("19???", "19XX?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("1989?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        //Only three digits not accepted
        of("989", null, null),
        //3 digits and question mark is ambiguous
        of("198?", null, null),
        //3 digits and dash is ambiguous
        of("198-", null, null),
        //3 digits a dash and a question mark is ambiguous
        of("198-?", null, null),

        //YEAR-MONTH
        of("1989.11", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("1989.XX", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("1989.UU", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("1989.??", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("1989.--", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("1989/11", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("1989/XX", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("1989/UU", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("1989/??", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("1989/--", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("1989-XX", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("1989-UU", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("1989-??", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("?1989.11", "1989-11?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989.XX", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989.UU", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989.??", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989.--", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989/11", "1989-11?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989/XX", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989/UU", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989/??", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989/--", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989-11", "1989-11?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989-XX", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989-UU", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989-??", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("1989.11?", "1989-11?", NUMERIC_ALL_VARIANTS_XX),
        of("1989.XX?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("1989.UU?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("1989.???", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("1989.--?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("1989/11?", "1989-11?", NUMERIC_ALL_VARIANTS_XX),
        of("1989/XX?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("1989/UU?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("1989/???", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("1989/--?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("1989-11?", "1989-11?", NUMERIC_ALL_VARIANTS_XX),
        of("1989-XX?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("1989-UU?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("1989-???", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989.11?", "1989-11?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989/11?", "1989-11?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989-11?", "1989-11?", NUMERIC_ALL_VARIANTS_XX),
        //If delimiter is dash, then dashes cannot be on month
        of("1989---", null, null),
        of("?1989---", null, null),
        of("1989---?", null, null),

        //YEAR-MONTH-DAY
        of("1989.11.01", "1989-11-01", NUMERIC_ALL_VARIANTS_XX),
        of("1989-11-01", "1989-11-01", NUMERIC_ALL_VARIANTS_XX),
        of("1989/11/01", "1989-11-01", NUMERIC_ALL_VARIANTS_XX),
        of("1989.13.32", "1989-13-32", NUMERIC_ALL_VARIANTS_XX),
        of("1989.11.XX", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("1989.11.UU", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("1989.11.??", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("1989.11.--", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("1989/11/XX", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("1989/11/UU", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("1989/11/??", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("1989/11/--", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("1989-11-XX", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("1989-11-UU", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("1989-11-??", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        //Both month and day unknown
        of("1989-??-??", "1989", NUMERIC_ALL_VARIANTS_XX),

        //Lowercase
        of("1989.11.xx", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("1989.11.uu", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("1989/11/xx", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("1989/11/uu", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("1989-11-xx", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("1989-11-uu", "1989-11", NUMERIC_ALL_VARIANTS_XX),

        //Uncertain cases
        of("?1989.11.01", "1989-11-01?", NUMERIC_ALL_VARIANTS_XX),
        of("1989.11.01?", "1989-11-01?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989.11.01?", "1989-11-01?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989/11/01", "1989-11-01?", NUMERIC_ALL_VARIANTS_XX),
        of("1989/11/01?", "1989-11-01?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989/11/01?", "1989-11-01?", NUMERIC_ALL_VARIANTS_XX),

        //Invalids
        of("1989-11---", null, null),
        of("1989.11.X", null, null),
        of("1989.11.U", null, null),
        of("1989.11.?", null, null),
        of("1989.11.-", null, null),
        of("1989-????", null, null),
        of("989.1.1", null, null), //Too few digits on year
        of("1989.1.1", null, null), //Too few digits on month
        of("1989.11.1", null, null), //Too few digits on day
        of("12345.01.01", null, null), //Too many digits on year
        of("1234.123.12", null, null), //Too many digits on month
        of("1234.12.123", null, null) //Too many digits on day
    );
  }

  private static Stream<Arguments> extractMDY() {

    //    (Some of those also captured here and NumericWithMissingPartsDateExtractor)
    return Stream.of(

        //YEAR-MONTH
        of("11.1989", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("XX.1989", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("UU.1989", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("??.1989", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("--.1989", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("11/1989", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("XX/1989", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("UU/1989", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("??/1989", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("--/1989", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("XX-1989", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("UU-1989", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("??-1989", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("?11.1989", "1989-11?", NUMERIC_ALL_VARIANTS_XX),
        of("?XX.1989", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?UU.1989", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("???.1989", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?--.1989", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?11/1989", "1989-11?", NUMERIC_ALL_VARIANTS_XX),
        of("?XX/1989", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?UU/1989", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("???/1989", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?--/1989", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?11-1989", "1989-11?", NUMERIC_ALL_VARIANTS_XX),
        of("?XX-1989", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?UU-1989", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("???-1989", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("11.1989?", "1989-11?", NUMERIC_ALL_VARIANTS_XX),
        of("XX.1989?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("UU.1989?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("??.1989?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("--.1989?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("11/1989?", "1989-11?", NUMERIC_ALL_VARIANTS_XX),
        of("XX/1989?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("UU/1989?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("??/1989?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("--/1989?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("11-1989?", "1989-11?", NUMERIC_ALL_VARIANTS_XX),
        of("XX-1989?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("UU-1989?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("??-1989?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?11.1989?", "1989-11?", NUMERIC_ALL_VARIANTS_XX),
        of("?11/1989?", "1989-11?", NUMERIC_ALL_VARIANTS_XX),
        of("?11-1989?", "1989-11?", NUMERIC_ALL_VARIANTS_XX),
        //If delimiter is dash, then dashes cannot be on month
        of("---1989", null, null),
        of("?---1989", null, null),
        of("---1989?", null, null),

        //YEAR-MONTH-DAY
        of("01.11.1989", "1989-11-01", NUMERIC_ALL_VARIANTS_XX),
        of("01-11-1989", "1989-11-01", NUMERIC_ALL_VARIANTS_XX),
        of("01/11/1989", "1989-11-01", NUMERIC_ALL_VARIANTS_XX),
        of("32.13.1989", "1989-13-32", NUMERIC_ALL_VARIANTS_XX),
        of("XX.11.1989", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("UU.11.1989", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("??.11.1989", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("--.11.1989", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("XX/11/1989", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("UU/11/1989", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("??/11/1989", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("--/11/1989", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("XX-11-1989", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("UU-11-1989", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("??-11-1989", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        //Both month and day unknown
        of("??-??-1989", "1989", NUMERIC_ALL_VARIANTS_XX),

        //Lowercase
        of("xx.11.1989", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("uu.11.1989", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("xx/11/1989", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("uu/11/1989", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("xx-11-1989", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("uu-11-1989", "1989-11", NUMERIC_ALL_VARIANTS_XX),

        //Uncertain cases
        of("?01.11.1989", "1989-11-01?", NUMERIC_ALL_VARIANTS_XX),
        of("01.11.1989?", "1989-11-01?", NUMERIC_ALL_VARIANTS_XX),
        of("?01.11.1989?", "1989-11-01?", NUMERIC_ALL_VARIANTS_XX),
        of("?01/11/1989", "1989-11-01?", NUMERIC_ALL_VARIANTS_XX),
        of("01/11/1989?", "1989-11-01?", NUMERIC_ALL_VARIANTS_XX),
        of("?01/11/1989?", "1989-11-01?", NUMERIC_ALL_VARIANTS_XX),

        //Invalids
        of("---11-1989", null, null),
        of("X.11.1989", null, null),
        of("U.11.1989", null, null),
        of("?.11.1989", null, null),
        of("-.11.1989", null, null),
        of("????-1989", null, null),
        of("1.1.989", null, null), //Too few digits on year
        of("1.1.1989", null, null), //Too few digits on month
        of("1.11.1989", null, null), //Too few digits on day
        of("01.01.12345", null, null), //Too many digits on year
        of("12.123.1234", null, null), //Too many digits on month
        of("123.12.1234", null, null) //Too many digits on day
    );
  }
}