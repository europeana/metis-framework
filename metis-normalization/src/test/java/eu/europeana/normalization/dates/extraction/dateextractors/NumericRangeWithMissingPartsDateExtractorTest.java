package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_RANGE_ALL_VARIANTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.of;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class NumericRangeWithMissingPartsDateExtractorTest {

  private static final NumericRangeWithMissingPartsDateExtractor NUMERIC_RANGE_WITH_MISSING_PARTS_DATE_EXTRACTOR = new NumericRangeWithMissingPartsDateExtractor();

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

  void extract(String input, String expected, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    final DateNormalizationResult dateNormalizationResult = NUMERIC_RANGE_WITH_MISSING_PARTS_DATE_EXTRACTOR.extract(input);
    if (expected == null) {
      assertNull(dateNormalizationResult);
    } else {
      final String actual = dateNormalizationResult.getEdtfDate().toString();
      assertEquals(expected, actual);
      assertEquals(actual.contains("?"), dateNormalizationResult.getEdtfDate().isUncertain());
      assertEquals(actual.contains(".."), dateNormalizationResult.getEdtfDate().isUnspecified());
      assertEquals(dateNormalizationExtractorMatchId, dateNormalizationResult.getDateNormalizationExtractorMatchId());
    }
  }

  private static Stream<Arguments> extractYMD() {
    // TODO: 17/10/2022 The '?' is not supported in the beginning of the date in ranges but in single dates it
    //  is(NumericWithMissingPartsDateExtractor). What should we follow?
    // TODO: 18/10/2022 The unspecified ""(empty) value for dates separator " "(space) does not work. Is that okay?

    return Stream.of(
                     yearArguments(),
                     yearMonthArguments(),
                     yearMonthDayArguments(),
                     yearMonthDayInvalidArguments())
                 .flatMap(Function.identity());
  }

  private static Stream<Arguments> yearArguments() {
    return Stream.of(
        //DATE SEPARATOR '/'
        of("1989/1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989?/1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989/?1990", null, NUMERIC_RANGE_ALL_VARIANTS),
        //--Unspecified
        of("1989/?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/-", "1989/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?/1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS),
        of("-/1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS),
        of("../1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS),

        //DATE SEPARATOR  ' - '
        of("1989 - 1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989? - 1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989 - ?1990", null, NUMERIC_RANGE_ALL_VARIANTS),
        //--Unspecified
        of("1989 - ?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989 - -", "1989/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989 - ..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("? - 1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS),
        of("- - 1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS),
        of(".. - 1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS),

        //DATE SEPARATOR '-'
        of("1989-1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989?-1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989-?1990", null, NUMERIC_RANGE_ALL_VARIANTS),
        //--Unspecified
        of("1989-?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?-1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS),
        of("..-1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS),

        //DATE SEPARATOR ' '(space)
        of("1989 1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989? 1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989 ?1990", null, NUMERIC_RANGE_ALL_VARIANTS),
        //--Unspecified
        of("1989 ", null, null),
        of(" 1989", null, null)
    );
  }

  private static Stream<Arguments> yearMonthArguments() {
    return Stream.of(
        //DATE SEPARATOR '/'
        of("1989-11/1990-11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11/1990.11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11?/1990-11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11?/1990.11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989-11/?1990-11", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989.11/?1990.11", null, NUMERIC_RANGE_ALL_VARIANTS),
        //--Unspecified
        of("1989-11/?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11/-", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11/..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?/1989-11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("-/1989-11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("../1989-11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11/?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11/-", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11/..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?/1989.11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("-/1989.11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("../1989.11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),

        //DATE SEPARATOR ' - '
        of("1989-11 - 1990-11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11 - 1990.11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11 - 1990/11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11? - 1990-11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11? - 1990.11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11? - 1990/11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989-11 - ?1990-11", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989.11 - ?1990.11", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989/11 - ?1990/11", null, NUMERIC_RANGE_ALL_VARIANTS),
        //--Unspecified
        of("1989/11 - ?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11 - -", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11 - ..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("? - 1989/11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("- - 1989/11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of(".. - 1989/11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11 - ?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11 - -", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11 - ..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("? - 1989-11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("- - 1989-11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of(".. - 1989-11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11 - ?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11 - -", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11 - ..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("? - 1989.11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("- - 1989.11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of(".. - 1989.11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),

        //DATE SEPARATOR '-'
        of("1989.11-1990.11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11-1990/11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11?-1990.11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11?-1990/11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989/11-?1990/11", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989.11-?1990.11", null, NUMERIC_RANGE_ALL_VARIANTS),
        //--Unspecified
        of("1989/11-?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11--", null, null),
        of("1989/11-..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?-1989/11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("--1989/11", null, null),
        of("..-1989/11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11-?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11--", null, null),
        of("1989.11-..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?-1989.11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("--1989.11", null, null),
        of("..-1989.11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),

        //DATE SEPARATOR ' '(space)
        of("1989-11 1990-11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11 1990.11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11 1990/11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11? 1990-11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11? 1990.11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11? 1990/11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989-11 ?1990-11", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989.11 ?1990.11", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989/11 ?1990/11", null, NUMERIC_RANGE_ALL_VARIANTS),
        //--Unspecified
        of("1989/11 ", null, null),
        of(" 1989/11", null, null),
        of("1989-11 ", null, null),
        of(" 1989-11", null, null),
        of("1989.11 ", null, null),
        of(" 1989.11", null, null)
    );
  }

  private static Stream<Arguments> yearMonthDayArguments() {
    return Stream.of(
        //DATE SEPARATOR '/'
        of("1989-11-01/1990-11-01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01/1990.11.01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        //Some missing digits are allowed
        of("989-1-1/990-11-01", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("989.1.1/990.11.01", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01?/1990-11-01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01?/1990.11.01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989-11-01/?1990-11-01", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989.11.01/?1990.11.01", null, NUMERIC_RANGE_ALL_VARIANTS),
        //Combination of date parts separators
        of("1989-11-01/1990.11.01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01?/1990.11.01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        //--Unspecified
        of("1989-11-01/?", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01/-", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01/..", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?/1989-11-01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("-/1989-11-01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("../1989-11-01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01/?", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01/-", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01/..", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?/1989.11.01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("-/1989.11.01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("../1989.11.01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),

        //DATE SEPARATOR ' - '
        of("1989-11-01 - 1990-11-01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01 - 1990.11.01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11/01 - 1990/11/01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        //Some missing digits are allowed
        of("989-1-1 - 990-11-01", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("989.1.1 - 990.11.01", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("989/1/1 - 990/11/01", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01? - 1990-11-01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01? - 1990.11.01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11/01? - 1990/11/01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989-11-01 - ?1990-11-01", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989.11.01 - ?1990.11.01", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989/11/01 - ?1990/11/01", null, NUMERIC_RANGE_ALL_VARIANTS),
        //Combination of date parts separators
        of("1989-11-01 - 1990.11.01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01 - 1990/11/01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01? - 1990.11.01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01? - 1990/11/01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        //--Unspecified
        of("1989/11/01 - ?", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11/01 - -", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11/01 - ..", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("? - 1989/11/01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("- - 1989/11/01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of(".. - 1989/11/01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01 - ?", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01 - -", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01 - ..", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("? - 1989-11-01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("- - 1989-11-01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of(".. - 1989-11-01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01 - ?", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01 - -", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01 - ..", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("? - 1989.11.01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("- - 1989.11.01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of(".. - 1989.11.01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),

        //DATE SEPARATOR '-'
        of("1989/11/01-1990/11/01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01-1990.11.01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        //Some missing digits are allowed
        of("989.1.1-990.11.01", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("989/1/1-990/11/01", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11/01?-1990/11/01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01?-1990.11.01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989/11/01-?1990/11/01", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989.11.01-?1990.11.01", null, NUMERIC_RANGE_ALL_VARIANTS),
        //Combination of date parts separators
        of("1989/11/01-1990.11.01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11/01?-1990.11.01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        //--Unspecified
        of("1989/11/01-?", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11/01--", null, null),
        of("1989/11/01-..", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?-1989/11/01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("--1989/11/01", null, null),
        of("..-1989/11/01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01-?", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01--", null, null),
        of("1989.11.01-..", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?-1989.11.01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("--1989.11.01", null, null),
        of("..-1989.11.01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),

        //DATE SEPARATOR ' '(space)
        of("1989-11-01 1990-11-01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01 1990.11.01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11/01 1990/11/01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        //Some missing digits are allowed
        of("989-1-1 990-11-01", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("989.1.1 990.11.01", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("989/1/1 990/11/01", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01? 1990-11-01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01? 1990.11.01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11/01? 1990/11/01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989-11-01 ?1990-11-01", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989.11.01 ?1990.11.01", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989/11/01 ?1990/11/01", null, NUMERIC_RANGE_ALL_VARIANTS),
        //Combination of date parts separators
        of("1989-11-01 1990.11.01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01 1990/11/01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01 1990/11/01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01? 1990.11.01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01? 1990/11/01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01? 1990/11/01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        //--Unspecified
        of("1989/11/01 ", null, null),
        of(" 1989/11/01", null, null),
        of("1989-11-01 ", null, null),
        of(" 1989-11-01", null, null),
        of("1989.11.01 ", null, null),
        of(" 1989.11.01", null, null)
    );
  }

  private static Stream<Arguments> yearMonthDayInvalidArguments() {
    return Stream.of(
        //Too few digits on year
        of("89-01-01/90-01-01", null, null),
        of("89.1.1/90.1.1", null, null),
        of("89/1/1-90/1/1", null, null),
        //Too many digits on year
        of("12345-01-01/12346-01-01", null, null),
        //Too many digits on month
        of("1234-123-12/1235-123-12", null, null),
        //Too many digits on day
        of("1234-12-123/1235-12-123", null, null)
    );
  }

  private static Stream<Arguments> extractDMY() {
    return Stream.of(monthYearArguments(),
                     dayMonthYearArguments(),
                     dayMonthYearInvalidArguments())
                 .flatMap(Function.identity());
  }

  private static Stream<Arguments> monthYearArguments() {
    return Stream.of(
        //DATE SEPARATOR '/'
        of("11-1989/11-1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.1989/11.1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11-1989?/11-1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.1989?/11.1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?11-1989/?11-1990", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?11.1989/?11.1990", null, NUMERIC_RANGE_ALL_VARIANTS),
        //--Unspecified
        of("11-1989/?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("11-1989/-", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("11-1989/..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?/11-1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("-/11-1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("../11-1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.1989/?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.1989/-", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.1989/..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?/11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("-/11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("../11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),

        //DATE SEPARATOR ' - '
        of("11-1989 - 11-1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.1989 - 11.1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11/1989 - 11/1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11-1989? - 11-1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.1989? - 11.1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("11/1989? - 11/1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?11-1989 - ?11-1990", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?11.1989 - ?11.1990", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?11/1989 - ?11/1990", null, NUMERIC_RANGE_ALL_VARIANTS),
        //--Unspecified
        of("11/1989 - ?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("11/1989 - -", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("11/1989 - ..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("? - 11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("- - 11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of(".. - 11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11-1989 - ?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("11-1989 - -", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("11-1989 - ..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("? - 11-1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("- - 11-1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of(".. - 11-1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.1989 - ?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.1989 - -", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.1989 - ..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("? - 11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("- - 11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of(".. - 11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),

        //DATE SEPARATOR '-'
        of("11.1989-11.1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11/1989-11/1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.1989?-11.1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("11/1989?-11/1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?11/1989-?11/1990", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?11.1989-?11.1990", null, NUMERIC_RANGE_ALL_VARIANTS),
        //--Unspecified
        of("11/1989-?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("11/1989--", null, null),
        of("11/1989-..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?-11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("--11/1989", null, null),
        of("..-11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.1989-?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.1989--", null, null),
        of("11.1989-..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?-11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("--11.1989", null, null),
        of("..-11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),

        //DATE SEPARATOR ' '(space)
        of("11-1989 11-1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.1989 11.1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11/1989 11/1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11-1989? 11-1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.1989? 11.1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("11/1989? 11/1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?11-1989 ?11-1990", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?11.1989 ?11.1990", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?11/1989 ?11/1990", null, NUMERIC_RANGE_ALL_VARIANTS),
        //--Unspecified
        of("11/1989 ", null, null),
        of(" 11/1989", null, null),
        of("11-1989 ", null, null),
        of(" 11-1989", null, null),
        of("11.1989", null, null),
        of(" 11.1989", null, null)
    );
  }

  private static Stream<Arguments> dayMonthYearArguments() {
    return Stream.of(
        //DATE SEPARATOR '/'
        of("01-11-1989/01-11-1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989/01.11.1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        //Some missing digits are allowed
        of("1-1-989/01-11-990", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1.1.989/01.11.990", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989?/01-11-1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989?/01.11.1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?01-11-1989/?01-11-1990", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?01.11.1989/?01.11.1990", null, NUMERIC_RANGE_ALL_VARIANTS),
        //Combination of date parts separators
        of("01-11-1989/01.11.1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989?/01.11.1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        //--Unspecified
        of("01-11-1989/?", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989/-", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989/..", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?/01-11-1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("-/01-11-1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("../01-11-1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989/?", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989/-", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989/..", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?/01.11.1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("-/01.11.1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("../01.11.1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),

        //DATE SEPARATOR ' - '
        of("01-11-1989 - 01-11-1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989 - 01.11.1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01/11/1989 - 01/11/1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        //Some missing digits are allowed
        of("1-1-989 - 01-11-990", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1.1.989 - 01.11.990", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1/1/989 - 01/11/990", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989? - 01-11-1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989? - 01.11.1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("01/11/1989? - 01/11/1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?01-11-1989 - ?01-11-1990", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?01-11-1989 - ?01.11.1990", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?01/11/1989 - ?01/11/1990", null, NUMERIC_RANGE_ALL_VARIANTS),
        //Combination of date parts separators
        of("01-11-1989 - 01.11.1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989 - 01/11/1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989? - 01.11.1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989? - 01/11/1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        //--Unspecified
        of("01/11/1989 - ?", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("01/11/1989 - -", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("01/11/1989 - ..", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("? - 01/11/1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("- - 01/11/1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of(".. - 01/11/1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989 - ?", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989 - -", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989 - ..", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("? - 01-11-1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("- - 01-11-1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of(".. - 01-11-1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989 - ?", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989 - -", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989 - ..", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("? - 01.11.1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("- - 01.11.1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of(".. - 01.11.1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),

        //DATE SEPARATOR '-'
        of("01/11/1989-01/11/1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989-01.11.1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        //Some missing digits are allowed
        of("1.1.989-01.11.990", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1/1/989-01/11/990", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01/11/1989?-01/11/1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989?-01.11.1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?01/11/1989-?01/11/1990", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?01.11.1989-?01.11.1990", null, NUMERIC_RANGE_ALL_VARIANTS),
        //Combination of date parts separators
        of("01/11/1989-01.11.1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01/11/1989?-01.11.1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        //--Unspecified
        of("01/11/1989-?", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("01/11/1989--", null, null),
        of("01/11/1989-..", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?-01/11/1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("--01/11/1989", null, null),
        of("..-01/11/1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989-?", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989--", null, null),
        of("01.11.1989-..", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?-01.11.1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("--01.11.1989", null, null),
        of("..-01.11.1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),

        //DATE SEPARATOR ' '(space)
        of("01-11-1989 01-11-1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989 01.11.1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01/11/1989 01/11/1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        //Some missing digits are allowed
        of("1-1-989 01-11-990", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1.1.989 01.11.990", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1/1/989 01/11/990", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989? 01-11-1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989? 01.11.1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("01/11/1989? 01/11/1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?01-11-1989 ?01-11-1990", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?01.11.1989 ?01.11.1990", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?01/11/1989 ?01/11/1990", null, NUMERIC_RANGE_ALL_VARIANTS),
        //Combination of date parts separators
        of("01-11-1989 01.11.1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989 01/11/1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989 01/11/1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989? 01.11.1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989? 01/11/1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989? 01/11/1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        //--Unspecified
        of("01/11/1989 ", null, null),
        of(" 01/11/1989", null, null),
        of("01-11-1989 ", null, null),
        of(" 01-11-1989", null, null),
        of("01.11.1989 ", null, null),
        of(" 01.11.1989", null, null)
    );
  }

  private static Stream<Arguments> dayMonthYearInvalidArguments() {
    return Stream.of(
        //Too few digits on year
        of("01-01-89/01-01-90", null, null),
        of("1.1.89/1.1.90", null, null),
        of("1/1/89-1/1/90", null, null),
        //Too many digits on year
        of("01-01-12345/01-01-12346", null, null),
        //Too many digits on month
        of("12-123-1234/12-123-1235", null, null),
        //Too many digits on day
        of("123-12-1234/123-12-1235", null, null)
    );
  }
}