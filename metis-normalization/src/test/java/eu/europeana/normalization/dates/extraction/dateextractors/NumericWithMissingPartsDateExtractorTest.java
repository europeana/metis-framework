package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS;
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

class NumericWithMissingPartsDateExtractorTest {

  private static final NumericWithMissingPartsDateExtractor NUMERIC_WITH_MISSING_PARTS_DATE_EXTRACTOR = new NumericWithMissingPartsDateExtractor();

  @ParameterizedTest
  @MethodSource
  void extractYMD(String input, String expected, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    extract(input, expected, dateNormalizationExtractorMatchId);
  }

  @ParameterizedTest
  @MethodSource
  void extractDMY(String input, String expected, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    extract(input, expected, dateNormalizationExtractorMatchId);
  }

  @ParameterizedTest
  @MethodSource
  void extractYMD_XX(String input, String expected, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    extract(input, expected, dateNormalizationExtractorMatchId);
  }

  @ParameterizedTest
  @MethodSource
  void extractDMY_XX(String input, String expected, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    extract(input, expected, dateNormalizationExtractorMatchId);
  }

  void extract(String input, String expected, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    final DateNormalizationResult dateNormalizationResult = NUMERIC_WITH_MISSING_PARTS_DATE_EXTRACTOR.extract(input);
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

    return Stream.of(
        //YEAR
        //A month and day can be missing
        of("1989", "1989", NUMERIC_ALL_VARIANTS),
        of("?1989", "1989?", NUMERIC_ALL_VARIANTS),
        of("1989?", "1989?", NUMERIC_ALL_VARIANTS),
        of("?1989?", "1989?", NUMERIC_ALL_VARIANTS),

        //YEAR-MONTH
        of("1989-11", "1989-11", NUMERIC_ALL_VARIANTS),
        of("1989.11", "1989-11", NUMERIC_ALL_VARIANTS),
        of("1989/11", "1989-11", NUMERIC_ALL_VARIANTS),
        of("?1989-11", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("?1989.11", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("?1989/11", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("1989-11?", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("1989.11?", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("1989/11?", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("?1989-11?", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("?1989.11?", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("?1989/11?", "1989-11?", NUMERIC_ALL_VARIANTS),

        //YEAR-MONTH-DAY
        of("1989-11-01", "1989-11-01", NUMERIC_ALL_VARIANTS),
        // TODO: 26/09/2022 Make sure this is checked later on, on validation code
        of("1989-13-32", "1989-13-32", NUMERIC_ALL_VARIANTS),
        //Some missing digits are allowed
        of("989-1-1", "0989-01-01", NUMERIC_ALL_VARIANTS),
        of("?1989-11-01", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("1989-11-01?", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("?1989-11-01?", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("1989.11.01", "1989-11-01", NUMERIC_ALL_VARIANTS),
        of("?1989.11.01", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("1989.11.01?", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("?1989.11.01?", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        //Some missing digits are allowed
        of("989.1.1", "0989-01-01", NUMERIC_ALL_VARIANTS),
        of("1989/11/01", "1989-11-01", NUMERIC_ALL_VARIANTS),
        of("?1989/11/01", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("1989/11/01?", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("?1989/11/01?", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        //Some missing digits are allowed
        of("989/1/1", "0989-01-01", NUMERIC_ALL_VARIANTS),
        of("?989.1.1", "0989-01-01?", NUMERIC_ALL_VARIANTS),
        //Combination of separators
        of("?989/1-1", "0989-01-01?", NUMERIC_ALL_VARIANTS),
        of("?989-1/1", "0989-01-01?", NUMERIC_ALL_VARIANTS),
        of("9989-99/99", "9989-99-99", NUMERIC_ALL_VARIANTS),
        of("9989/99-99", "9989-99-99", NUMERIC_ALL_VARIANTS),
        of("?989-99/99", "0989-99-99?", NUMERIC_ALL_VARIANTS),
        of("?989-99/99?", "0989-99-99?", NUMERIC_ALL_VARIANTS),

        //Too few digits on year
        of("89-01-01", null, null),
        of("89.1.1", null, null),
        of("89/1/1", null, null),
        //Too many digits on year
        of("12345-01-01", null, null),
        //Too many digits on month
        of("1234-123-12", null, null),
        //Too many digits on day
        of("1234-12-123", null, null)
    );
  }

  private static Stream<Arguments> extractDMY() {

    return Stream.of(
        //MONTH-YEAR
        of("11-1989", "1989-11", NUMERIC_ALL_VARIANTS),
        of("11.1989", "1989-11", NUMERIC_ALL_VARIANTS),
        of("11/1989", "1989-11", NUMERIC_ALL_VARIANTS),
        of("?11-1989", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("?11.1989", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("?11/1989", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("11-1989?", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("11.1989?", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("11/1989?", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("?11-1989?", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("?11.1989?", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("?11/1989?", "1989-11?", NUMERIC_ALL_VARIANTS),

        //DAY-MONTH-YEAR
        of("01-11-1989", "1989-11-01", NUMERIC_ALL_VARIANTS),
        // TODO: 26/09/2022 Make sure this is checked later on, on validation code
        of("32-13-1989", "1989-13-32", NUMERIC_ALL_VARIANTS),
        //Some missing digits are allowed
        of("1-1-989", "0989-01-01", NUMERIC_ALL_VARIANTS),
        of("?01-11-1989", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("01-11-1989?", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("?01-11-1989?", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("01.11.1989", "1989-11-01", NUMERIC_ALL_VARIANTS),
        of("?01.11.1989", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("01.11.1989?", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("?01.11.1989?", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        //Some missing digits are allowed
        of("1.1.989", "0989-01-01", NUMERIC_ALL_VARIANTS),
        of("01/11/1989", "1989-11-01", NUMERIC_ALL_VARIANTS),
        of("?01/11/1989", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("01/11/1989?", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("?01/11/1989?", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        //Some missing digits are allowed
        of("1/1/989", "0989-01-01", NUMERIC_ALL_VARIANTS),
        of("?1.1.989", "0989-01-01?", NUMERIC_ALL_VARIANTS),
        //Combination of separators
        of("?1-1/989", "0989-01-01?", NUMERIC_ALL_VARIANTS),
        of("?1/1-989", "0989-01-01?", NUMERIC_ALL_VARIANTS),
        of("99/99-9989", "9989-99-99", NUMERIC_ALL_VARIANTS),
        of("99-99/9989", "9989-99-99", NUMERIC_ALL_VARIANTS),
        of("?99/99-989", "0989-99-99?", NUMERIC_ALL_VARIANTS),

        //Too few digits on year
        of("01-01-89", null, null),
        of("1.1.89", null, null),
        of("1/1/89", null, null),
        //Too many digits on year
        of("01-01-12345", null, null),
        //Too many digits on month
        of("12-123-1234", null, null),
        //Too many digits on day
        of("123-12-1234", null, null),

        //Other invalids
        //Spaces should not match
        of("1989 11 01", null, null),
        //Double dashes should not match
        of("1989--11--01", null, null),
        //Double dots should not match
        of("1989..11..01", null, null),
        //Double slashes should not match
        of("1989//11//01", null, null),
        //Ambiguous
        of("198?", null, null),
        of("?99/99-989?", null, null)
    );
  }

  private static Stream<Arguments> extractYMD_XX() {

    return Stream.of(
        //YEAR
        of("198X", "198X", NUMERIC_ALL_VARIANTS_XX),
        of("198U", "198X", NUMERIC_ALL_VARIANTS_XX),
        of("19--", "19XX", NUMERIC_ALL_VARIANTS_XX),
        of("19XX", "19XX", NUMERIC_ALL_VARIANTS_XX),
        of("19UU", "19XX", NUMERIC_ALL_VARIANTS_XX),
        of("19??", "19XX", NUMERIC_ALL_VARIANTS_XX),
        of("19???", "19XX?", NUMERIC_ALL_VARIANTS_XX),
        //3 digits and question mark is ambiguous
        of("198?", null, null),
        //3 digits and dash is ambiguous
        of("198-", null, null),
        //3 digits a dash and a question mark is ambiguous
        of("198-?", null, null),

        //YEAR-MONTH
        of("1989.XX", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("1989.UU", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("1989.??", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("1989.--", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("1989/XX", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("1989/UU", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("1989/??", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("1989/--", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("1989-XX", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("1989-UU", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("1989-??", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("?1989.XX", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989.UU", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989.??", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989.--", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989/XX", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989/UU", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989/??", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989/--", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989-XX", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989-UU", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?1989-??", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("1989.XX?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("1989.UU?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("1989.???", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("1989.--?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("1989/XX?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("1989/UU?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("1989/???", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("1989/--?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("1989-XX?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("1989-UU?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("1989-???", "1989?", NUMERIC_ALL_VARIANTS_XX),
        //Unknown month and also some unknown digits on the year
        of("198X.XX", "198X", NUMERIC_ALL_VARIANTS_XX),
        of("198X.UU", "198X", NUMERIC_ALL_VARIANTS_XX),
        of("198X/UU", "198X", NUMERIC_ALL_VARIANTS_XX),
        of("198X/--", "198X", NUMERIC_ALL_VARIANTS_XX),
        of("19XX/XX", "19XX", NUMERIC_ALL_VARIANTS_XX),
        of("19XX/--", "19XX", NUMERIC_ALL_VARIANTS_XX),
        of("19XX.--", "19XX", NUMERIC_ALL_VARIANTS_XX),

        //If delimiter is dash, then dashes cannot be on month
        of("1989---", null, null),
        of("?1989---", null, null),
        of("1989---?", null, null),

        //YEAR-MONTH-DAY
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
        //Unknown month and day as well as some unknown digits on the year
        of("198X.11.XX", "198X-11", NUMERIC_ALL_VARIANTS_XX),
        of("198X.UU.11", "198X", NUMERIC_ALL_VARIANTS_XX),
        of("198X/UU/XX", "198X", NUMERIC_ALL_VARIANTS_XX),
        of("198X/--/xx", "198X", NUMERIC_ALL_VARIANTS_XX),
        of("19XX/XX/99", "19XX", NUMERIC_ALL_VARIANTS_XX),
        of("19XX/--/--", "19XX", NUMERIC_ALL_VARIANTS_XX),
        of("19XX.--.--", "19XX", NUMERIC_ALL_VARIANTS_XX),
        of("19XX-11-99?", "19XX-11-99?", NUMERIC_ALL_VARIANTS_XX),
        of("19UU-XX-99?", "19XX?", NUMERIC_ALL_VARIANTS_XX),
        of("19UU-??-99?", "19XX?", NUMERIC_ALL_VARIANTS_XX),
        of("19UU/--/99?", "19XX?", NUMERIC_ALL_VARIANTS_XX),
        //Both month and day unknown
        of("1989-??-??", "1989", NUMERIC_ALL_VARIANTS_XX),
        //Lowercase
        of("1989.11.xx", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("1989.11.uu", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("1989/11/xx", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("1989/11/uu", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("1989-11-xx", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("1989-11-uu", "1989-11", NUMERIC_ALL_VARIANTS_XX),

        //Invalids
        of("1989-11---", null, null),
        of("1989.11.X", null, null),
        of("1989.11.U", null, null),
        of("1989.11.?", null, null),
        of("1989.11.-", null, null),
        of("1989-????", null, null),
        //Too many digits on year
        of("12345.01.01", null, null),
        //Too many digits on month
        of("1234.123.12", null, null),
        //Too many digits on day
        of("1234.12.123", null, null)
    );
  }

  private static Stream<Arguments> extractDMY_XX() {

    return Stream.of(
        //YEAR-MONTH
        of("XX.1989", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("UU.1989", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("??.1989", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("--.1989", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("XX/1989", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("UU/1989", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("??/1989", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("--/1989", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("XX-1989", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("UU-1989", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("??-1989", "1989", NUMERIC_ALL_VARIANTS_XX),
        of("?XX.1989", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?UU.1989", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("???.1989", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?--.1989", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?XX/1989", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?UU/1989", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("???/1989", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?--/1989", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?XX-1989", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("?UU-1989", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("???-1989", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("XX.1989?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("UU.1989?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("??.1989?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("--.1989?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("XX/1989?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("UU/1989?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("??/1989?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("--/1989?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("XX-1989?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("UU-1989?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        of("??-1989?", "1989?", NUMERIC_ALL_VARIANTS_XX),
        //If delimiter is dash, then dashes cannot be on month
        of("---1989", null, null),
        of("?---1989", null, null),
        of("---1989?", null, null),

        //YEAR-MONTH-DAY
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
        //Unknown month and day as well as some unknown digits on the year
        of("XX.11.198X", "198X-11", NUMERIC_ALL_VARIANTS_XX),
        of("11.UU.198X", "198X", NUMERIC_ALL_VARIANTS_XX),
        of("XX/UU/198X", "198X", NUMERIC_ALL_VARIANTS_XX),
        of("xx/--/198X", "198X", NUMERIC_ALL_VARIANTS_XX),
        of("99/XX/19XX", "19XX", NUMERIC_ALL_VARIANTS_XX),
        of("--/--/19XX", "19XX", NUMERIC_ALL_VARIANTS_XX),
        of("--.--.19XX", "19XX", NUMERIC_ALL_VARIANTS_XX),
        of("?99-11-19XX", "19XX-11-99?", NUMERIC_ALL_VARIANTS_XX),
        of("?99-XX-19UU", "19XX?", NUMERIC_ALL_VARIANTS_XX),
        of("?99-??-19UU", "19XX?", NUMERIC_ALL_VARIANTS_XX),
        of("?99/--/19UU", "19XX?", NUMERIC_ALL_VARIANTS_XX),
        //Both month and day unknown
        of("??-??-1989", "1989", NUMERIC_ALL_VARIANTS_XX),
        //Lowercase
        of("xx.11.1989", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("uu.11.1989", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("xx/11/1989", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("uu/11/1989", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("xx-11-1989", "1989-11", NUMERIC_ALL_VARIANTS_XX),
        of("uu-11-1989", "1989-11", NUMERIC_ALL_VARIANTS_XX),

        //Invalids
        of("---11-1989", null, null),
        of("X.11.1989", null, null),
        of("U.11.1989", null, null),
        of("?.11.1989", null, null),
        of("-.11.1989", null, null),
        of("????-1989", null, null),
        //Too many digits on year
        of("01.01.12345", null, null),
        //Too many digits on month
        of("12.123.1234", null, null),
        //Too many digits on day
        of("123.12.1234", null, null)
    );
  }

}