package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_RANGE_ALL_VARIANTS;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_RANGE_ALL_VARIANTS_XX;
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
    return Stream.of(
        yearArguments(),
        yearMonthArguments(),
        yearMonthDayArguments(),
        yearMonthDayInvalidArguments()
    ).flatMap(Function.identity());
  }

  private static Stream<Arguments> extractDMY() {
    return Stream.of(
        monthYearArguments(),
        dayMonthYearArguments(),
        dayMonthYearInvalidArguments()
    ).flatMap(Function.identity());
  }

  private static Stream<Arguments> yearArguments() {
    return Stream.of(
        year_SlashArguments(),
        year_SpacedDashArguments(),
        year_PipeArguments(),
        year_DashArguments(),
        year_SpaceArguments()
    ).flatMap(Function.identity());
  }

  private static Stream<Arguments> year_SlashArguments() {
    return Stream.of(
        of("1989/1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS),
        of("198/199", "0198/0199", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989?/1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989/?1990", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS),
        //Unspecified
        of("1989/?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/-", "1989/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?/1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS),
        of("-/1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS),
        of("../1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS)
    );
  }

  private static Stream<Arguments> year_SpacedDashArguments() {
    return Stream.of(
        of("1989 - 1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS),
        of("198 - 199", "0198/0199", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989? - 1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989 - ?1990", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS),
        //Unspecified
        of("1989 - ?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989 - -", "1989/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989 - ..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("? - 1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS),
        of("- - 1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS),
        of(".. - 1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS)
    );
  }

  private static Stream<Arguments> year_PipeArguments() {
    return Stream.of(
        of("1989|1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS),
        of("198|199", "0198/0199", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989?|1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989|?1990", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS),
        //Unspecified
        of("1989|?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989|-", "1989/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989|..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?|1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS),
        of("-|1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS),
        of("..|1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS)
    );
  }

  private static Stream<Arguments> year_DashArguments() {
    return Stream.of(
        of("1989-1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS),
        of("198-199", "0198/0199", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989?-1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989-?1990", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS),
        //Unspecified
        of("1989-?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?-1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS),
        of("..-1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS)
    );
  }

  private static Stream<Arguments> year_SpaceArguments() {
    return Stream.of(
        of("1989 1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS),
        of("198 199", "0198/0199", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989? 1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989 ?1990", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS),
        //Unspecified does not apply on space separator
        of("1989 ", null, null),
        of("1989 ..", null, null),
        of(" 1989", null, null)
    );
  }

  private static Stream<Arguments> yearMonthArguments() {
    return Stream.of(
        yearMonth_SlashArguments(),
        yearMonth_SpacedDashArguments(),
        yearMonth_PipeArguments(),
        yearMonth_DashArguments(),
        yearMonth_SpaceArguments()
    ).flatMap(Function.identity());
  }

  private static Stream<Arguments> yearMonth_SlashArguments() {
    return Stream.of(
        of("1989-11/1990-11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11/1990.11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("198-11/199-11", "0198-11/0199-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("198.11/199.11", "0198-11/0199-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11?/1990-11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11?/1990.11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989-11/?1990-11", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989.11/?1990.11", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        //Unspecified
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
        of("../1989.11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS)
    );
  }

  private static Stream<Arguments> yearMonth_SpacedDashArguments() {
    return Stream.of(
        of("1989-11 - 1990-11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11 - 1990.11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11 - 1990/11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("198-11 - 199-11", "0198-11/0199-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("198.11 - 199.11", "0198-11/0199-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("198/11 - 199/11", "0198-11/0199-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11? - 1990-11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11? - 1990.11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11? - 1990/11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989-11 - ?1990-11", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989.11 - ?1990.11", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989/11 - ?1990/11", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("198.11 - 199.11", "0198-11/0199-11", NUMERIC_RANGE_ALL_VARIANTS),
        //Unspecified
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
        of(".. - 1989.11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS)
    );
  }

  private static Stream<Arguments> yearMonth_PipeArguments() {
    return Stream.of(
        of("1989-11|1990-11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11|1990.11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11|1990/11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("198-11|199-11", "0198-11/0199-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("198.11|199.11", "0198-11/0199-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("198/11|199/11", "0198-11/0199-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11?|1990-11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11?|1990.11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11?|1990/11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989-11|?1990-11", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989.11|?1990.11", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989/11|?1990/11", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("198.11|199.11", "0198-11/0199-11", NUMERIC_RANGE_ALL_VARIANTS),
        //Unspecified
        of("1989/11|?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11|-", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11|..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?|1989/11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("-|1989/11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("..|1989/11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11|?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11|-", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11|..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?|1989-11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("-|1989-11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("..|1989-11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11|?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11|-", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11|..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?|1989.11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("-|1989.11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("..|1989.11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS)
    );
  }

  private static Stream<Arguments> yearMonth_DashArguments() {
    return Stream.of(
        of("1989.11-1990.11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11-1990/11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("198.11-199.11", "0198-11/0199-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("198/11-199/11", "0198-11/0199-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11?-1990.11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11?-1990/11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989/11-?1990/11", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989.11-?1990.11", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        //Unspecified
        of("1989/11-?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        //This is a special case, because the "-" separator does not support "-" unspecified, so it is caught from a later pattern
        of("1989/11--", "1989/11XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11-..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?-1989/11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("--1989/11", null, null),
        of("..-1989/11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11-?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11--", null, null),
        of("1989.11-..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?-1989.11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("--1989.11", null, null),
        of("..-1989.11", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS)
    );
  }

  private static Stream<Arguments> yearMonth_SpaceArguments() {
    return Stream.of(
        of("1989-11 1990-11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11 1990.11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11 1990/11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("198-11 199-11", "0198-11/0199-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("198.11 199.11", "0198-11/0199-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("198/11 199/11", "0198-11/0199-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11? 1990-11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11? 1990.11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11? 1990/11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989-11 ?1990-11", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989.11 ?1990.11", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989/11 ?1990/11", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        //Unspecified does not apply on space separator
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
        yearMonthDay_SlashArguments(),
        yearMonthDay_SpacedDashArguments(),
        yearMonthDay_PipeArguments(),
        yearMonthDay_DashArguments(),
        yearMonthDay_SpaceArguments()
    ).flatMap(Function.identity());
  }

  private static Stream<Arguments> yearMonthDay_SlashArguments() {
    return Stream.of(
        of("1989-11-01/1990-11-01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01/1990.11.01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("198-11-01/199-11-01", "0198-11-01/0199-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("198.11.01/199.11.01", "0198-11-01/0199-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        //Some missing digits are allowed
        of("989-1-1/990-11-01", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("989.1.1/990.11.01", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01?/1990-11-01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01?/1990.11.01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989-11-01/?1990-11-01", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989.11.01/?1990.11.01", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        //Combination of date parts separators
        of("1989-11-01/1990.11.01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01?/1990.11.01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        //Unspecified
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
        of("../1989.11.01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS)
    );
  }

  private static Stream<Arguments> yearMonthDay_SpacedDashArguments() {
    return Stream.of(
        of("1989-11-01 - 1990-11-01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01 - 1990.11.01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11/01 - 1990/11/01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("198-11-01 - 199-11-01", "0198-11-01/0199-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("198.11.01 - 199.11.01", "0198-11-01/0199-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("198/11/01 - 199/11/01", "0198-11-01/0199-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        //Some missing digits are allowed
        of("989-1-1 - 990-11-01", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("989.1.1 - 990.11.01", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("989/1/1 - 990/11/01", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01? - 1990-11-01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01? - 1990.11.01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11/01? - 1990/11/01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989-11-01 - ?1990-11-01", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989.11.01 - ?1990.11.01", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989/11/01 - ?1990/11/01", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        //Combination of date parts separators
        of("1989-11-01 - 1990.11.01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01 - 1990/11/01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01? - 1990.11.01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01? - 1990/11/01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        //Unspecified
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
        of(".. - 1989.11.01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS)
    );
  }

  private static Stream<Arguments> yearMonthDay_PipeArguments() {
    return Stream.of(
        of("1989-11-01|1990-11-01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01|1990.11.01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11/01|1990/11/01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("198-11-01|199-11-01", "0198-11-01/0199-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("198.11.01|199.11.01", "0198-11-01/0199-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("198/11/01|199/11/01", "0198-11-01/0199-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        //Some missing digits are allowed
        of("989-1-1|990-11-01", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("989.1.1|990.11.01", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("989/1/1|990/11/01", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01?|1990-11-01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01?|1990.11.01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11/01?|1990/11/01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989-11-01|?1990-11-01", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989.11.01|?1990.11.01", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989/11/01|?1990/11/01", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        //Combination of date parts separators
        of("1989-11-01|1990.11.01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01|1990/11/01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01?|1990.11.01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01?|1990/11/01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        //Unspecified
        of("1989/11/01|?", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11/01|-", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11/01|..", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?|1989/11/01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("-|1989/11/01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("..|1989/11/01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01|?", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01|-", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01|..", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?|1989-11-01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("-|1989-11-01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("..|1989-11-01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01|?", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01|-", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01|..", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?|1989.11.01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("-|1989.11.01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("..|1989.11.01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS)
    );
  }

  private static Stream<Arguments> yearMonthDay_DashArguments() {
    return Stream.of(
        of("1989/11/01-1990/11/01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01-1990.11.01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("198.11.01-199.11.01", "0198-11-01/0199-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("198/11/01-199/11/01", "0198-11-01/0199-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        //Some missing digits are allowed
        of("989.1.1-990.11.01", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("989/1/1-990/11/01", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11/01?-1990/11/01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01?-1990.11.01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989/11/01-?1990/11/01", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989.11.01-?1990.11.01", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        //Combination of date parts separators
        of("1989/11/01-1990.11.01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11/01?-1990.11.01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        //Unspecified
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
        of("..-1989.11.01", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS)
    );
  }

  private static Stream<Arguments> yearMonthDay_SpaceArguments() {
    return Stream.of(
        of("1989-11-01 1990-11-01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01 1990.11.01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11/01 1990/11/01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("198-11-01 199-11-01", "0198-11-01/0199-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("198.11.01 199.11.01", "0198-11-01/0199-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("198/11/01 199/11/01", "0198-11-01/0199-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        //Some missing digits are allowed
        of("989-1-1 990-11-01", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("989.1.1 990.11.01", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("989/1/1 990/11/01", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01? 1990-11-01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01? 1990.11.01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11/01? 1990/11/01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989-11-01 ?1990-11-01", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989.11.01 ?1990.11.01", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989/11/01 ?1990/11/01", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        //Combination of date parts separators
        of("1989-11-01 1990.11.01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01 1990/11/01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01 1990/11/01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01? 1990.11.01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01? 1990/11/01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01? 1990/11/01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        //Unspecified does not apply on space separator
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

  private static Stream<Arguments> monthYearArguments() {
    return Stream.of(
        monthYear_SlashArguments(),
        monthYear_SpacedDashArguments(),
        monthYear_PipeArguments(),
        monthYear_DashArguments(),
        monthYear_SpaceArguments()
    ).flatMap(Function.identity());
  }

  private static Stream<Arguments> monthYear_SlashArguments() {
    return Stream.of(
        of("11-1989/11-1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.1989/11.1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11-198/11-199", "0198-11/0199-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.198/11.199", "0198-11/0199-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11-1989?/11-1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.1989?/11.1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?11-1989/?11-1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?11.1989/?11.1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        //Unspecified
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
        of("../11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS)
    );
  }

  private static Stream<Arguments> monthYear_SpacedDashArguments() {
    return Stream.of(
        of("11-1989 - 11-1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.1989 - 11.1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11/1989 - 11/1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11-198 - 11-199", "0198-11/0199-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.198 - 11.199", "0198-11/0199-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11/198 - 11/199", "0198-11/0199-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11-1989? - 11-1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.1989? - 11.1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("11/1989? - 11/1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?11-1989 - ?11-1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?11.1989 - ?11.1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?11/1989 - ?11/1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        //Unspecified
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
        of(".. - 11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS)
    );
  }

  private static Stream<Arguments> monthYear_PipeArguments() {
    return Stream.of(
        of("11-1989|11-1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.1989|11.1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11/1989|11/1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11-198|11-199", "0198-11/0199-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.198|11.199", "0198-11/0199-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11/198|11/199", "0198-11/0199-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11-1989?|11-1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.1989?|11.1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("11/1989?|11/1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?11-1989|?11-1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?11.1989|?11.1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?11/1989|?11/1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        //Unspecified
        of("11/1989|?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("11/1989|-", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("11/1989|..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?|11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("-|11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("..|11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11-1989|?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("11-1989|-", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("11-1989|..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?|11-1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("-|11-1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("..|11-1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.1989|?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.1989|-", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.1989|..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?|11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("-|11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("..|11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS)
    );
  }

  private static Stream<Arguments> monthYear_DashArguments() {
    return Stream.of(
        of("11.1989-11.1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11/1989-11/1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.198-11.199", "0198-11/0199-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11/198-11/199", "0198-11/0199-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.1989?-11.1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("11/1989?-11/1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?11/1989-?11/1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?11.1989-?11.1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        //Unspecified
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
        of("..-11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS)
    );
  }

  private static Stream<Arguments> monthYear_SpaceArguments() {
    return Stream.of(
        of("11-1989 11-1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.1989 11.1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11/1989 11/1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11-198 11-199", "0198-11/0199-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.198 11.199", "0198-11/0199-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11/198 11/199", "0198-11/0199-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("11-1989? 11-1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("11.1989? 11.1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("11/1989? 11/1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?11-1989 ?11-1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?11.1989 ?11.1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?11/1989 ?11/1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        //Unspecified does not apply on space separator
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
        dayMonthYear_SlashArguments(),
        dayMonthYear_SpacedDashArguments(),
        dayMonthYear_PipeArguments(),
        dayMonthYear_DashArguments(),
        dayMonthYear_SpaceArguments()
    ).flatMap(Function.identity());
  }

  private static Stream<Arguments> dayMonthYear_SlashArguments() {
    return Stream.of(
        of("01-11-1989/01-11-1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989/01.11.1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-198/01-11-199", "0198-11-01/0199-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.198/01.11.199", "0198-11-01/0199-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        //Some missing digits are allowed
        of("1-1-989/01-11-990", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1.1.989/01.11.990", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989?/01-11-1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989?/01.11.1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?01-11-1989/?01-11-1990", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?01.11.1989/?01.11.1990", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        //Combination of date parts separators
        of("01-11-1989/01.11.1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989?/01.11.1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        //Unspecified
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
        of("../01.11.1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS)
    );
  }

  private static Stream<Arguments> dayMonthYear_SpacedDashArguments() {
    return Stream.of(
        of("01-11-1989 - 01-11-1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989 - 01.11.1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01/11/1989 - 01/11/1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-198 - 01-11-199", "0198-11-01/0199-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.198 - 01.11.199", "0198-11-01/0199-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01/11/198 - 01-11-199", "0198-11-01/0199-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        //Some missing digits are allowed
        of("1-1-989 - 01-11-990", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1.1.989 - 01.11.990", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1/1/989 - 01/11/990", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989? - 01-11-1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989? - 01.11.1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("01/11/1989? - 01/11/1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?01-11-1989 - ?01-11-1990", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?01-11-1989 - ?01.11.1990", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?01/11/1989 - ?01/11/1990", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        //Combination of date parts separators
        of("01-11-1989 - 01.11.1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989 - 01/11/1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989? - 01.11.1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989? - 01/11/1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        //Unspecified
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
        of(".. - 01.11.1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS)
    );
  }

  private static Stream<Arguments> dayMonthYear_PipeArguments() {
    return Stream.of(
        of("01-11-1989|01-11-1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989|01.11.1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01/11/1989|01/11/1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-198|01-11-199", "0198-11-01/0199-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.198|01.11.199", "0198-11-01/0199-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01/11/198|01-11-199", "0198-11-01/0199-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        //Some missing digits are allowed
        of("1-1-989|01-11-990", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1.1.989|01.11.990", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1/1/989|01/11/990", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989?|01-11-1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989?|01.11.1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("01/11/1989?|01/11/1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?01-11-1989|?01-11-1990", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?01-11-1989|?01.11.1990", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?01/11/1989|?01/11/1990", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        //Combination of date parts separators
        of("01-11-1989|01.11.1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989|01/11/1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989?|01.11.1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989?|01/11/1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        //Unspecified
        of("01/11/1989|?", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("01/11/1989|-", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("01/11/1989|..", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?|01/11/1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("-|01/11/1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("..|01/11/1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989|?", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989|-", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989|..", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?|01-11-1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("-|01-11-1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("..|01-11-1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989|?", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989|-", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989|..", "1989-11-01/..", NUMERIC_RANGE_ALL_VARIANTS),
        of("?|01.11.1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("-|01.11.1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("..|01.11.1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS)
    );
  }

  private static Stream<Arguments> dayMonthYear_DashArguments() {
    return Stream.of(
        of("01/11/1989-01/11/1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989-01.11.1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.198-01.11.199", "0198-11-01/0199-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01/11/198-01/11/199", "0198-11-01/0199-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        //Some missing digits are allowed
        of("1.1.989-01.11.990", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1/1/989-01/11/990", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01/11/1989?-01/11/1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989?-01.11.1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?01/11/1989-?01/11/1990", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?01.11.1989-?01.11.1990", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        //Combination of date parts separators
        of("01/11/1989-01.11.1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01/11/1989?-01.11.1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        //Unspecified
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
        of("..-01.11.1989", "../1989-11-01", NUMERIC_RANGE_ALL_VARIANTS)
    );
  }

  private static Stream<Arguments> dayMonthYear_SpaceArguments() {
    return Stream.of(
        of("01-11-1989 01-11-1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989 01.11.1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01/11/1989 01/11/1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-198 01-11-199", "0198-11-01/0199-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.198 01.11.199", "0198-11-01/0199-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01/11/198 01-11-199", "0198-11-01/0199-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        //Some missing digits are allowed
        of("1-1-989 01-11-990", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1.1.989 01.11.990", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1/1/989 01/11/990", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989? 01-11-1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989? 01.11.1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("01/11/1989? 01/11/1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?01-11-1989 ?01-11-1990", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?01.11.1989 ?01.11.1990", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?01/11/1989 ?01/11/1990", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        //Combination of date parts separators
        of("01-11-1989 01.11.1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989 01/11/1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989 01/11/1990", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989? 01.11.1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("01-11-1989? 01/11/1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("01.11.1989? 01/11/1990?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        //Unspecified does not apply on space separator
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