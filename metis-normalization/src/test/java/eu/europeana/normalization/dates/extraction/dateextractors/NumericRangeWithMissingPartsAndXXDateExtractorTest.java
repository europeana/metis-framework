package eu.europeana.normalization.dates.extraction.dateextractors;

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

class NumericRangeWithMissingPartsAndXXDateExtractorTest {

  private static final NumericRangeWithMissingPartsAndXxDateExtractor NUMERIC_RANGE_WITH_MISSING_PARTS_AND_XX_DATE_EXTRACTOR = new NumericRangeWithMissingPartsAndXxDateExtractor();

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
    final DateNormalizationResult dateNormalizationResult = NUMERIC_RANGE_WITH_MISSING_PARTS_AND_XX_DATE_EXTRACTOR.extract(input);
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

  // TODO: 02/11/2022 The unknown parts with ?? are not supported at all. Is that okay or should we support it??
  // TODO: 04/11/2022 Answer: This can be supported similarly to single dates.

  //  Check commented out cases
  // TODO: 02/11/2022 For unspecified date edges only the single "?" is allowed and not the "-" or "..". Is that okay??
  // TODO: 04/11/2022 Answer: This can be supported if we have a safe way to do it. To be investigated.

  // TODO: 02/11/2022 Also uncertain is not supported for XX(The single question mark at the beginning or end of each date)??
  // TODO: 04/11/2022 Answer: The uncertains should be supported similarly to the single dates.

  // TODO: 04/11/2022 Space separator does not seem to be implemented.
  // TODO: 04/11/2022 Separator between edges should BE accepted

  // TODO: 04/11/2022 There is one more separator "|" that has not been used in the single dates(non ranges)
  // TODO: 04/11/2022 The "|" should be supported in both XX and actual digits ranges.


  private static Stream<Arguments> extractYMD() {
    // TODO: 03/11/2022 None of the yearMonthArguments, yearMonthDayArguments work
    // TODO: 04/11/2022 Answer: the double unknown charachters should be supported, such as XX, UU, --
    return Stream.of(
        yearArguments(),
        yearMonthArguments(),
        yearMonthDayArguments()
    ).flatMap(Function.identity());
  }

  private static Stream<Arguments> extractDMY() {
    // TODO: 03/11/2022 None of the monthYearArguments, dayMonthYearArgument work
    return Stream.of(
        monthYearArguments(),
        dayMonthYearArguments()
    ).flatMap(Function.identity());
  }

  private static Stream<Arguments> yearArguments() {
    return Stream.of(
        year_SlashArguments(),
        year_SpacedDashArguments(),
        year_DashArguments()
        //Space separator is not supported in the old version
        //        year_SpaceArguments()
    ).flatMap(Function.identity());
  }

  private static Stream<Arguments> year_SlashArguments() {
    return Stream.of(
        of("198X/199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198U/199U", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19--/19--", "19XX/19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19XX/19XX", "19XX/19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19UU/19UU", "19XX/19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19??/19??", "19XX/19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19???/19???", "19XX?/19XX?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Uncertain
        //of("19--?/19--?", "19XX?/19XX?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("?19--/?19--", "19XX?/19XX?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19XX?/19XX?", "19XX?/19XX?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("?19XX/?19XX", "19XX?/19XX?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19UU?/19UU?", "19XX?/19XX?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("?19UU/?19UU", "19XX?/19XX?", NUMERIC_RANGE_ALL_VARIANTS_XX),

        //Unspecified
        of("198X/?", "198X/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198U/?", "198X/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19--/?", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19XX/?", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19UU/?", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19UU?/?", "19XX?/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19??/?", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),

        //of("1989/-", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("198U/-", "198X/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19--/-", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19XX/-", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19UU/-", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19??/-", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),

        //of("1989/..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("198U/..", "198X/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19--/..", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19XX/..", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19UU/..", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19??/..", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),

        of("?/198X", "../198X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/198U", "../198X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/19--", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/19XX", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/19UU", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX)
        //of("?/19??", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX)

        //of("-/198X", "../198X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("-/198U", "../198X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("-/19--", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("-/19XX", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("-/19UU", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("-/19??", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),

        //of("../198X", "../198X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("../198U", "../198X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("../19--", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("../19XX", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("../19UU", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("../19??", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX)
    );
  }

  private static Stream<Arguments> year_SpacedDashArguments() {
    return Stream.of(
        of("198X - 199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198U - 199U", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19-- - 19--", "19XX/19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19XX - 19XX", "19XX/19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19UU - 19UU", "19XX/19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19?? - 19??", "19XX/19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19??? - 19???", "19XX?/19XX?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Uncertain
        //of("19--? - 19--?", "19XX?/19XX?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("?19-- - ?19--", "19XX?/19XX?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19XX? - 19XX?", "19XX?/19XX?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("?19XX - ?19XX", "19XX?/19XX?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19UU? - 19UU?", "19XX?/19XX?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("?19UU - ?19UU", "19XX?/19XX?", NUMERIC_RANGE_ALL_VARIANTS_XX),

        //Unspecified
        of("198X - ?", "198X/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198U - ?", "198X/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19-- - ?", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19XX - ?", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19UU - ?", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19UU? - ?", "19XX?/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19?? - ?", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),

        //of("1989 - -", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("198U - -", "198X/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19-- - -", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19XX - -", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19UU - -", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19?? - -", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),

        //of("1989 - ..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("198U - ..", "198X/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19-- - ..", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19XX - ..", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19UU - ..", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19?? - ..", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),

        of("? - 198X", "../198X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - 198U", "../198X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - 19--", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - 19XX", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - 19UU", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX)
        //of("? - 19??", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX)

        //of("- - 198X", "../198X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("- - 198U", "../198X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("- - 19--", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("- - 19XX", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("- - 19UU", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("- - 19??", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),

        //of(".. - 198X", "../198X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of(".. - 198U", "../198X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of(".. - 19--", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of(".. - 19XX", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of(".. - 19UU", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of(".. - 19??", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX)
    );
  }

  private static Stream<Arguments> year_DashArguments() {
    return Stream.of(
        of("198X-199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198U-199U", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Not supported but could be
        //        of("19---19--", "19XX/19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19XX-19XX", "19XX/19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19UU-19UU", "19XX/19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19??-19??", "19XX/19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19???-19???", "19XX?/19XX?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Uncertain
        //of("19--?-19--?", "19XX?/19XX?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("?19---?19--", "19XX?/19XX?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19XX?-19XX?", "19XX?/19XX?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("?19XX-?19XX", "19XX?/19XX?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19UU?-19UU?", "19XX?/19XX?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("?19UU-?19UU", "19XX?/19XX?", NUMERIC_RANGE_ALL_VARIANTS_XX),

        //Unspecified
        of("198X-?", "198X/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198U-?", "198X/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Not supported but could be
        //        of("19---?", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19XX-?", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19UU-?", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19UU?-?", "19XX?/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19??-?", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),

        //of("1989--", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("198U--", "198X/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19----", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19XX--", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19UU--", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19??--", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),

        //of("1989-..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("198U-..", "198X/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19---..", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19XX-..", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19UU-..", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19??-..", "19XX/..", NUMERIC_RANGE_ALL_VARIANTS_XX),

        of("?-198X", "../198X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?-198U", "../198X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Not supported but could be
        //        of("?-19--", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?-19XX", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?-19UU", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX)
        //of("? - 19??", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX)

        //of("--198X", "../198X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("--198U", "../198X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("--19--", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("--19XX", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("--19UU", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("--19??", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),

        //of("..-198X", "../198X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("..-198U", "../198X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("..-19--", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("..-19XX", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("..-19UU", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("..-19??", "../19XX", NUMERIC_RANGE_ALL_VARIANTS_XX)
    );
  }

  private static Stream<Arguments> year_SpaceArguments() {
    return Stream.of(
        of("198X 199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198U 199U", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19-- 19--", "19XX/19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19XX 19XX", "19XX/19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19UU 19UU", "19XX/19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19?? 19??", "19XX/19XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19??? 19???", "19XX?/19XX?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Uncertain
        //of("19--? 19--?", "19XX?/19XX?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("?19-- ?19--", "19XX?/19XX?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19XX? 19XX?", "19XX?/19XX?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("?19XX ?19XX", "19XX?/19XX?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("19UU? 19UU?", "19XX?/19XX?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //of("?19UU ?19UU", "19XX?/19XX?", NUMERIC_RANGE_ALL_VARIANTS_XX),

        //Unspecified does not apply on space separator
        of("198X ", null, null),
        of("198U ", null, null),
        of("19-- ", null, null),
        of("19XX ", null, null),
        of("19UU ", null, null),
        of(" 198X", null, null),
        of(" 198U", null, null),
        of(" 19--", null, null),
        of(" 19XX", null, null),
        of(" 19UU", null, null)
    );
  }

  private static Stream<Arguments> yearMonthArguments() {
    return Stream.of(
        yearMonth_SlashArguments(),
        yearMonth_SpacedDashArguments(),
        yearMonth_DashArguments(),
        yearMonth_SpaceArguments()
    ).flatMap(Function.identity());
  }

  private static Stream<Arguments> yearMonth_SlashArguments() {
    return Stream.of(
        //"[XU]" with "-" delimiter
        of("1989-XX/1990-XX", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-UU/1990-UU", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-??/1990-??", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989-XX/?1990-XX", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989-UU/?1990-UU", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989-??/?1990-??", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-XX?/1990-XX?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-UU?/1990-UU?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-???/1990-???", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified
        of("1989-XX/?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-XX/-", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-XX/..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-UU/?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-UU/-", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-UU/..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-??/?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-??/-", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-??/..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/1989-XX", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("-/1989-XX", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("../1989-XX", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/1989-UU", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("-/1989-UU", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("../1989-UU", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/1989-??", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("-/1989-??", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("../1989-??", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),

        //"[-XU]" with "." delimiter
        of("1989.XX/1990.XX", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.UU/1990.UU", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.??/1990.??", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.--/1990.--", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.XX/1990.XX", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.UU/1990.UU", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.??/1990.??", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.--/1990.--", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.XX?/1990.XX?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.UU?/1990.UU?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.???/1990.???", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.--?/1990.--?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and also some unknown digits on the year
        of("198X.XX/199X.XX", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X.UU/199X.UU", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19XX.--/20XX.--", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified
        of("1989.XX/?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.XX/-", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.XX/..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.UU/?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.UU/-", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.UU/..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.??/?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.??/-", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.??/..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.--/?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.--/-", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.--/..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/1989.XX", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("-/1989.XX", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("../1989.XX", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/1989.UU", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("-/1989.UU", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("../1989.UU", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/1989.??", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("-/1989.??", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("../1989.??", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/1989.--", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("-/1989.--", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("../1989.--", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX)
    );
  }

  private static Stream<Arguments> yearMonth_SpacedDashArguments() {
    return Stream.of(
        //"[XU]" with "-" delimiter
        of("1989-XX - 1990-XX", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-UU - 1990-UU", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-?? - 1990-??", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989-XX - ?1990-XX", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989-UU - ?1990-UU", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989-?? - ?1990-??", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-XX? - 1990-XX?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-UU? - 1990-UU?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-??? - 1990-???", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified
        of("1989-XX - ?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-XX - -", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-XX - ..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-UU - ?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-UU - -", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-UU - ..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-?? - ?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-?? - -", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-?? - ..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - 1989-XX", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - 1989-XX", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - 1989-XX", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - 1989-UU", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - 1989-UU", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - 1989-UU", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - 1989-??", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - 1989-??", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - 1989-??", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),

        //"[-XU]" with "./" delimiters
        //"[-XU]" with "." delimiters
        of("1989.XX - 1990.XX", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.UU - 1990.UU", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.?? - 1990.??", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.-- - 1990.--", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.XX - 1990.XX", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.UU - 1990.UU", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.?? - 1990.??", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.-- - 1990.--", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.XX? - 1990.XX?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.UU? - 1990.UU?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.??? - 1990.???", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.--? - 1990.--?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and also some unknown digits on the year
        of("198X.XX - 199X.XX", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X.UU - 199X.UU", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19XX.-- - 20XX.--", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified
        of("1989.XX - ?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.XX - -", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.XX - ..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.UU - ?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.UU - -", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.UU - ..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.?? - ?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.?? - -", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.?? - ..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.-- - ?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.-- - -", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.-- - ..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - 1989.XX", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - 1989.XX", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - 1989.XX", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - 1989.UU", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - 1989.UU", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - 1989.UU", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - 1989.??", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - 1989.??", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - 1989.??", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - 1989.--", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - 1989.--", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - 1989.--", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //"[-XU]" with "/" delimiter
        of("1989/XX - 1990/XX", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/UU - 1990/UU", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/?? - 1990/??", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/-- - 1990/--", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989/XX - 1990/XX", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989/UU - 1990/UU", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989/?? - 1990/??", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989/-- - 1990/--", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/XX? - 1990/XX?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/UU? - 1990/UU?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/??? - 1990/???", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/--? - 1990/--?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and also some unknown digits on the year
        of("198X/XX - 199X/XX", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X/UU - 199X/UU", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19XX/-- - 20XX/--", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified
        of("1989/XX - ?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/XX - -", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/XX - ..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/UU - ?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/UU - -", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/UU - ..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/?? - ?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/?? - -", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/?? - ..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/-- - ?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/-- - -", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/-- - ..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - 1989/XX", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - 1989/XX", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - 1989/XX", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - 1989/UU", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - 1989/UU", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - 1989/UU", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - 1989/??", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - 1989/??", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - 1989/??", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - 1989/--", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - 1989/--", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - 1989/--", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX)
    );
  }

  private static Stream<Arguments> yearMonth_DashArguments() {
    return Stream.of(
        //"[XU]" with "./" delimiters
        //"[XU]" with "." delimiters
        of("1989.XX-1990.XX", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.UU-1990.UU", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.??-1990.??", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.XX-1990.XX", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.UU-1990.UU", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.??-1990.??", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.XX?-1990.XX?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.UU?-1990.UU?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.???-1990.???", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and also some unknown digits on the year
        of("198X.XX-199X.XX", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X.UU-199X.UU", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified
        of("1989.XX-?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.XX--", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.XX-..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.UU-?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.UU--", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.UU-..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.??-?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.??--", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.??-..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?-1989.XX", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--1989.XX", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("..-1989.XX", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?-1989.UU", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--1989.UU", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("..-1989.UU", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?-1989.??", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--1989.??", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("..-1989.??", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //"[XU]" with "/" delimiter
        of("1989/XX-1990/XX", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/UU-1990/UU", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/??-1990/??", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989/XX-1990/XX", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989/UU-1990/UU", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989/??-1990/??", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/XX?-1990/XX?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/UU?-1990/UU?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/???-1990/???", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and also some unknown digits on the year
        of("198X/XX-199X/XX", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X/UU-199X/UU", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified
        of("1989/XX-?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/XX--", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/XX-..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/UU-?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/UU--", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/UU-..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/??-?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/??--", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/??-..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?-1989/XX", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--1989/XX", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("..-1989/XX", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?-1989/UU", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--1989/UU", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("..-1989/UU", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?-1989/??", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--1989/??", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("..-1989/??", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?-1989/--", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--1989/--", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("..-1989/--", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX)
    );
  }

  private static Stream<Arguments> yearMonth_SpaceArguments() {
    return Stream.of(
        //"[-XU]" with "-./" delimiters
        //"[-XU]" with "-" delimiter
        of("1989-XX 1990-XX", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-UU 1990-UU", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-?? 1990-??", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989--- 1990---", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989-XX ?1990-XX", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989-UU ?1990-UU", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989-?? ?1990-??", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989--- ?1990---", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-XX? 1990-XX?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-UU? 1990-UU?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-??? 1990-???", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989---? 1990---?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified does not apply on space separator
        of("1989-XX ", null, null),
        of("1989-UU ", null, null),
        of("1989-?? ", null, null),
        of("1989--- ", null, null),
        of(" 1989-XX", null, null),
        of(" 1989-UU", null, null),
        of(" 1989-??", null, null),
        of(" 1989---", null, null),

        //"[-XU]" with "." delimiters
        of("1989.XX 1990.XX", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.UU 1990.UU", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.?? 1990.??", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.-- 1990.--", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.XX 1990.XX", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.UU 1990.UU", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.?? 1990.??", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.-- 1990.--", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.XX? 1990.XX?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.UU? 1990.UU?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.??? 1990.???", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.--? 1990.--?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and also some unknown digits on the year
        of("198X.XX 199X.XX", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X.UU 199X.UU", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19XX.-- 20XX.--", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified does not apply on space separator
        of("1989.XX ", null, null),
        of("1989.UU ", null, null),
        of("1989.?? ", null, null),
        of("1989.-- ", null, null),
        of(" 1989.XX", null, null),
        of(" 1989.UU", null, null),
        of(" 1989.??", null, null),
        of(" 1989.--", null, null),

        //"[-XU]" with "/" delimiter
        of("1989/XX 1990/XX", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/UU 1990/UU", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/?? 1990/??", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/-- 1990/--", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989/XX 1990/XX", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989/UU 1990/UU", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989/?? 1990/??", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989/-- 1990/--", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/XX? 1990/XX?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/UU? 1990/UU?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/??? 1990/???", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/--? 1990/--?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and also some unknown digits on the year
        of("198X/XX 199X/XX", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X/UU 199X/UU", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19XX/-- 20XX/--", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified does not apply on space separator
        of("1989/XX ", null, null),
        of("1989/UU ", null, null),
        of("1989/?? ", null, null),
        of("1989/-- ", null, null),
        of(" 1989/XX", null, null),
        of(" 1989/UU", null, null),
        of(" 1989/??", null, null),
        of(" 1989/--", null, null)
    );
  }

  private static Stream<Arguments> yearMonthDayArguments() {
    return Stream.of(
        yearMonthDay_SlashArguments(),
        yearMonthDay_SpacedDashArguments(),
        yearMonthDay_DashArguments(),
        yearMonthDay_SpaceArguments()
    ).flatMap(Function.identity());
  }

  private static Stream<Arguments> yearMonthDay_SlashArguments() {
    return Stream.of(
        //"[XU]" with "-" delimiter
        of("1989-11-XX/1990-11-XX", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-UU/1990-11-UU", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-??/1990-11-??", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989-11-XX/?1990-11-XX", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989-11-UU/?1990-11-UU", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989-11-??/?1990-11-??", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-XX?/1990-11-XX?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-UU?/1990-11-UU?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-???/1990-11-???", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and day as well as some unknown digits on the year
        of("198X-11-XX/199X-11-XX", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X-UU-11/199X-UU-11", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X-UU-XX/199X-UU-XX", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X-UU-??/199X-UU-??", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X-??-XX/199X-??-XX", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19XX-XX-XX/20XX-XX-XX", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19??-??-??/20??-??-??", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified
        of("1989-11-XX/?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-XX/-", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-XX/..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-UU/?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-UU/-", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-UU/..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-??/?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-??/-", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-??/..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/1989-11-XX", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("-/1989-11-XX", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("../1989-11-XX", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/1989-11-UU", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("-/1989-11-UU", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("../1989-11-UU", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/1989-11-??", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("-/1989-11-??", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("../1989-11-??", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),

        //"[-XU]" with "." delimiter
        of("1989.11.XX/1990.11.XX", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.UU/1990.11.UU", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.??/1990.11.??", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.--/1990.11.--", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.11.XX/?1990.11.XX", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.11.UU/?1990.11.UU", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.11.??/?1990.11.??", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.11.--/?1990.11.--", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.XX?/1990.11.XX?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.UU?/1990.11.UU?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.???/1990.11.???", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.--?/1990.11.--?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and day as well as some unknown digits on the year
        of("198X.11.XX/199X.11.XX", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X.UU.11/199X.UU.11", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X.UU.XX/199X.UU.XX", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X.UU.??/199X.UU.??", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X.??.XX/199X.??.XX", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19XX.XX.XX/20XX.XX.XX", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19??.??.??/20??.??.??", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19--.--.--/20--.--.--", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified
        of("1989.11.XX/?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.XX/-", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.XX/..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.UU/?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.UU/-", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.UU/..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.??/?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.??/-", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.??/..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.--/?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.--/-", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.--/..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/1989.11.XX", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("-/1989.11.XX", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("../1989.11.XX", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/1989.11.UU", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("-/1989.11.UU", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("../1989.11.UU", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/1989.11.??", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("-/1989.11.??", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("../1989.11.??", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/1989.11.--", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("-/1989.11.--", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("../1989.11.--", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX)
    );
  }

  private static Stream<Arguments> yearMonthDay_SpacedDashArguments() {
    return Stream.of(
        //"[XU]" with "-" delimiter
        of("1989-11-XX - 1990-11-XX", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-UU - 1990-11-UU", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-?? - 1990-11-??", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989-11-XX - ?1990-11-XX", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989-11-UU - ?1990-11-UU", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989-11-?? - ?1990-11-??", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-XX? - 1990-11-XX?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-UU? - 1990-11-UU?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-??? - 1990-11-???", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and day as well as some unknown digits on the year
        of("198X-11-XX - 199X-11-XX", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X-UU-11 - 199X-UU-11", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X-UU-XX - 199X-UU-XX", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X-UU-?? - 199X-UU-??", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X-??-XX - 199X-??-XX", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19XX-XX-XX - 20XX-XX-XX", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19??-??-?? - 20??-??-??", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified
        of("1989-11-XX - ?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-XX - -", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-XX - ..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-UU - ?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-UU - -", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-UU - ..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-?? - ?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-?? - -", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-?? - ..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - 1989-11-XX", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - 1989-11-XX", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - 1989-11-XX", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - 1989-11-UU", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - 1989-11-UU", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - 1989-11-UU", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - 1989-11-??", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - 1989-11-??", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - 1989-11-??", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),

        //"[-XU]" with "./" delimiters
        //"[-XU]" with "." delimiter
        of("1989.11.XX - 1990.11.XX", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.UU - 1990.11.UU", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.?? - 1990.11.??", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.-- - 1990.11.--", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.11.XX - ?1990.11.XX", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.11.UU - ?1990.11.UU", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.11.?? - ?1990.11.??", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.11.-- - ?1990.11.--", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.XX? - 1990.11.XX?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.UU? - 1990.11.UU?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.??? - 1990.11.???", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.--? - 1990.11.--?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and day as well as some unknown digits on the year
        of("198X.11.XX - 199X.11.XX", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X.UU.11 - 199X.UU.11", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X.UU.XX - 199X.UU.XX", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X.UU.?? - 199X.UU.??", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X.??.XX - 199X.??.XX", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19XX.XX.XX - 20XX.XX.XX", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19??.??.?? - 20??.??.??", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19--.--.-- - 20--.--.--", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified
        of("1989.11.XX - ?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.XX - -", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.XX - ..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.UU - ?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.UU - -", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.UU - ..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.?? - ?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.?? - -", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.?? - ..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.-- - ?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.-- - -", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.-- - ..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - 1989.11.XX", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - 1989.11.XX", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - 1989.11.XX", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - 1989.11.UU", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - 1989.11.UU", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - 1989.11.UU", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - 1989.11.??", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - 1989.11.??", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - 1989.11.??", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - 1989.11.--", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - 1989.11.--", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - 1989.11.--", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //"[-XU]" with "/" delimiter
        of("1989/11/XX - 1990/11/XX", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/UU - 1990/11/UU", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/?? - 1990/11/??", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/-- - 1990/11/-", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989/11/XX - ?1990/11/XX", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989/11/UU - ?1990/11/UU", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989/11/?? - ?1990/11/??", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989/11/-- - ?1990/11/--", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/XX? - 1990/11/XX?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/UU? - 1990/11/UU?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/??? - 1990/11/???", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/--? - 1990/11/--?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and day as well as some unknown digits on the year
        of("198X/11/XX - 199X/11/XX", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X/UU/11 - 199X/UU/11", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X/UU/XX - 199X/UU/XX", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X/UU/?? - 199X/UU/??", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X/??/XX - 199X/??/XX", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19XX/XX/XX - 20XX/XX/XX", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19??/??/?? - 20??/??/??", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19--/--/-- - 20--/--/--", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified
        of("1989/11/XX - ?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/XX - -", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/XX - ..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/UU - ?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/UU - -", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/UU - ..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/?? - ?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/?? - -", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/?? - ..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/-- - ?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/-- - -", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/-- - ..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - 1989/11/XX", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - 1989/11/XX", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - 1989/11/XX", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - 1989/11/UU", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - 1989/11/UU", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - 1989/11/UU", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - 1989/11/??", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - 1989/11/??", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - 1989/11/??", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - 1989/11/--", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - 1989/11/--", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - 1989/11/--", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX)
    );
  }

  private static Stream<Arguments> yearMonthDay_DashArguments() {
    return Stream.of(
        //"[XU]" with "./" delimiters
        //"[XU]" with "." delimiter
        of("1989.11.XX-1990.11.XX", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.UU-1990.11.UU", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.??-1990.11.??", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.11.XX-?1990.11.XX", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.11.UU-?1990.11.UU", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.11.??-?1990.11.??", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.XX?-1990.11.XX?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.UU?-1990.11.UU?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.???-1990.11.???", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and day as well as some unknown digits on the year
        of("198X.11.XX-199X.11.XX", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X.UU.11-199X.UU.11", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X.UU.XX-199X.UU.XX", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X.UU.??-199X.UU.??", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X.??.XX-199X.??.XX", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19XX.XX.XX-20XX.XX.XX", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19??.??.??-20??.??.??", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified
        of("1989.11.XX-?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.XX--", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.XX-..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.UU-?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.UU--", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.UU-..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.??-?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.??--", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.??-..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?-1989.11.XX", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--1989.11.XX", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("..-1989.11.XX", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?-1989.11.UU", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--1989.11.UU", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("..-1989.11.UU", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?-1989.11.??", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--1989.11.??", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("..-1989.11.??", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //"[-XU]" with "/" delimiter
        of("1989/11/XX-1990/11/XX", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/UU-1990/11/UU", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/??-1990/11/??", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989/11/XX-?1990/11/XX", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989/11/UU-?1990/11/UU", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989/11/??-?1990/11/??", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/XX?-1990/11/XX?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/UU?-1990/11/UU?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/???-1990/11/???", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and day as well as some unknown digits on the year
        of("198X/11/XX-199X/11/XX", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X/UU/11-199X/UU/11", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X/UU/XX-199X/UU/XX", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X/UU/??-199X/UU/??", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X/??/XX-199X/??/XX", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19XX/XX/XX-20XX/XX/XX", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19??/??/??-20??/??/??", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified
        of("1989/11/XX-?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/XX--", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/XX-..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/UU-?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/UU--", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/UU-..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/??-?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/??--", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/??-..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?-1989/11/XX", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--1989/11/XX", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("..-1989/11/XX", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?-1989/11/UU", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--1989/11/UU", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("..-1989/11/UU", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?-1989/11/??", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--1989/11/??", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("..-1989/11/??", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX)
    );
  }

  private static Stream<Arguments> yearMonthDay_SpaceArguments() {
    return Stream.of(
        //"[-XU]" with "-./" delimiters
        //"[-XU]" with "-" delimiter
        of("1989-11-XX 1990-11-XX", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-UU 1990-11-UU", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-?? 1990-11-??", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11--- 1990-11--", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989-11-XX ?1990-11-XX", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989-11-UU ?1990-11-UU", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989-11-?? ?1990-11-??", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989-11--- ?1990-11---", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-XX? 1990-11-XX?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-UU? 1990-11-UU?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11-??? 1990-11-???", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989-11---? 1990-11---?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and day as well as some unknown digits on the year
        of("198X-11-XX 199X-11-XX", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X-UU-11 199X-UU-11", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X-UU-XX 199X-UU-XX", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X-UU-?? 199X-UU-??", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X-??-XX 199X-??-XX", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19XX-XX-XX 20XX-XX-XX", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19??-??-?? 20??-??-??", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19-------- 20--------", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified does not apply on space separator
        of("1989-11-XX ", null, null),
        of("1989-11-UU ", null, null),
        of("1989-11-?? ", null, null),
        of("1989-11--- ", null, null),
        of(" 1989-11-XX ", null, null),
        of(" 1989-11-UU ", null, null),
        of(" 1989-11-?? ", null, null),
        of(" 1989-11--- ", null, null),
        //"[-XU]" with "." delimiter
        of("1989.11.XX 1990.11.XX", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.UU 1990.11.UU", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.?? 1990.11.??", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.-- 1990.11.--", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.11.XX ?1990.11.XX", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.11.UU ?1990.11.UU", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.11.?? ?1990.11.??", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989.11.-- ?1990.11.--", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.XX? 1990.11.XX?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.UU? 1990.11.UU?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.??? 1990.11.???", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989.11.--? 1990.11.--?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and day as well as some unknown digits on the year
        of("198X.11.XX 199X.11.XX", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X.UU.11 199X.UU.11", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X.UU.XX 199X.UU.XX", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X.UU.?? 199X.UU.??", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X.??.XX 199X.??.XX", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19XX.XX.XX 20XX.XX.XX", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19??.??.?? 20??.??.??", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19--.--.-- 20--.--.--", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified does not apply on space separator
        of("1989.11.XX ", null, null),
        of("1989.11.UU ", null, null),
        of("1989.11.?? ", null, null),
        of("1989.11.-- ", null, null),
        of(" 1989.11.XX ", null, null),
        of(" 1989.11.UU ", null, null),
        of(" 1989.11.?? ", null, null),
        of(" 1989.11.-- ", null, null),
        //"[-XU]" with "/" delimiter
        of("1989/11/XX 1990/11/XX", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/UU 1990/11/UU", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/?? 1990/11/??", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/-- 1990/11/--", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989/11/XX ?1990/11/XX", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989/11/UU ?1990/11/UU", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989/11/?? ?1990/11/??", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?1989/11/-- ?1990/11/--", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/XX? 1990/11/XX?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/UU? 1990/11/UU?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/??? 1990/11/???", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("1989/11/--? 1990/11/--?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and day as well as some unknown digits on the year
        of("198X/11/XX 199X/11/XX", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X/UU/11 199X/UU/11", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X/UU/XX 199X/UU/XX", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X/UU/?? 199X/UU/??", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("198X/??/XX 199X/??/XX", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19XX/XX/XX 20XX/XX/XX", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19??/??/?? 20??/??/??", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("19--/--/-- 20--/--/--", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified does not apply on space separator
        of("1989/11/XX ", null, null),
        of("1989/11/UU ", null, null),
        of("1989/11/?? ", null, null),
        of("1989/11/-- ", null, null),
        of(" 1989/11/XX ", null, null),
        of(" 1989/11/UU ", null, null),
        of(" 1989/11/?? ", null, null),
        of(" 1989/11/-- ", null, null)
    );
  }

  private static Stream<Arguments> monthYearArguments() {
    return Stream.of(
        monthYear_SlashArguments(),
        monthYear_SpacedDashArguments(),
        monthYear_DashArguments(),
        monthYear_SpaceArguments()
    ).flatMap(Function.identity());
  }

  private static Stream<Arguments> monthYear_SlashArguments() {
    return Stream.of(
        //"[XU]" with "-" delimiter
        of("XX-1989/XX-1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU-1989/UU-1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-1989/??-1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?XX-1989/?XX-1990", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?UU-1989/?UU-1990", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("???-1989/???-1990", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX-1989?/XX-1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU-1989?/UU-1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-1989?/??-1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified
        of("XX-1989/?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX-1989/-", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX-1989/..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU-1989/?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU-1989/-", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU-1989/..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-1989/?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-1989/-", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-1989/..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/XX-1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("-/XX-1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("../XX-1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/UU-1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("-/UU-1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("../UU-1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/??-1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("-/??-1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("../??-1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),

        //"[-XU]" with "." delimiter
        of("XX.1989/XX.1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.1989/UU.1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.1989/??.1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.1989/--.1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?XX.1989/XX.1990", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?UU.1989/UU.1990", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("???.1989/??.1990", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?--.1989/--.1990", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.1989?/XX.1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.1989?/UU.1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.1989?/??.1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.1989?/--.1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and also some unknown digits on the year
        of("XX.198X/XX.199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.198X/UU.199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.19XX/--.20XX", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified
        of("XX.1989/?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.1989/-", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.1989/..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.1989/?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.1989/-", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.1989/..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.1989/?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.1989/-", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.1989/..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.1989/?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.1989/-", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.1989/..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/XX.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("-/XX.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("../XX.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/UU.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("-/UU.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("../UU.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/??.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("-/??.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("../??.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/--.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("-/--.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("../--.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX)
    );
  }

  private static Stream<Arguments> monthYear_SpacedDashArguments() {
    return Stream.of(
        //"[XU]" with "-" delimiter
        of("XX-1989 - XX-1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU-1989 - UU-1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-1989 - ??-1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?XX-1989 - ?XX-1990", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?UU-1989 - ?UU-1990", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("???-1989 - ???-1990", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX-1989? - XX-1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU-1989? - UU-1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-1989? - ??-1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified
        of("XX-1989 - ?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX-1989 - -", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX-1989 - ..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU-1989 - ?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU-1989 - -", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU-1989 - ..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-1989 - ?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-1989 - -", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-1989 - ..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - XX-1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - XX-1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - XX-1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - UU-1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - UU-1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - UU-1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - ??-1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - ??-1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - ??-1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),

        //"[-XU]" with "./" delimiters
        //"[-XU]" with "." delimiters
        of("XX.1989 - XX.1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.1989 - UU.1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.1989 - ??.1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.1989 - --.1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?XX.1989 - XX.1990", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?UU.1989 - UU.1990", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("???.1989 - ??.1990", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?--.1989 - --.1990", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.1989? - XX.1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.1989? - UU.1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.1989? - ??.1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.1989? - --.1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and also some unknown digits on the year
        of("XX.198X - XX.199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.198X - UU.199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.19XX - --.20XX.--", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified
        of("XX.1989 - ?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.1989 - -", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.1989 - ..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.1989 - ?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.1989 - -", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.1989 - ..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.1989 - ?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.1989 - -", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.1989 - ..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.1989 - ?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.1989 - -", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.1989 - ..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - XX.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - XX.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - XX.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - UU.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - UU.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - UU.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - ??.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - ??.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - ??.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - --.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - --.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - --.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //"[-XU]" with "/" delimiter
        of("XX/1989 - XX/1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU/1989 - UU/1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/1989 - ??/1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--/1989 - --/1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?XX/1989 - XX/1990", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?UU/1989 - UU/1990", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("???/1989 - ??/1990", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?--/1989 - --/1990", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX/1989? - XX/1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU/1989? - UU/1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/1989? - ??/1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--/1989? - --/1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and also some unknown digits on the year
        of("XX/198X - XX/199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU/198X - UU/199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--/19XX - --/20XX", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified
        of("XX/1989 - ?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX/1989 - -", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX/1989 - ..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU/1989 - ?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU/1989 - -", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU/1989 - ..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/1989 - ?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/1989 - -", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/1989 - ..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--/1989 - ?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--/1989 - -", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--/1989 - ..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - XX/1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - XX/1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - XX/1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - UU/1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - UU/1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - UU/1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - ??/1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - ??/1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - ??/1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - --/1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - --/1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - --/1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX)
    );
  }

  private static Stream<Arguments> monthYear_DashArguments() {
    return Stream.of(
        //"[XU]" with "./" delimiters
        //"[XU]" with "." delimiters
        of("XX.1989-XX.1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.1989-UU.1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.1989-??.1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?XX.1989-XX.1990", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?UU.1989-UU.1990", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("???.1989-??.1990", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.1989?-XX.1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.1989?-UU.1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.1989?-??.1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and also some unknown digits on the year
        of("XX.198X-XX.199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.198X-199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified
        of("XX.1989-?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.1989--", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.1989-..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.1989-?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.1989--", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.1989-..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.1989-?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.1989--", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.1989-..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?-XX.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--XX.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("..-XX.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?-UU.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--UU.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("..-UU.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?-??.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--??.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("..-??.1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //"[XU]" with "/" delimiter
        of("XX/1989-XX/1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU/1989-UU/1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/1989-??/1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?XX/1989-XX/1990", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?UU/1989-UU/1990", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("???/1989-??/1990", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX/1989?-XX/1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU/1989?-UU/1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/1989?-??/1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and also some unknown digits on the year
        of("XX/198X-XX/199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU/198X-UU/199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified
        of("XX/1989-?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX/1989--", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX/1989-..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU/1989-?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU/1989--", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU/1989-..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/1989-?", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/1989--", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/1989-..", "1989/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?-XX/1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--XX/1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("..-XX/1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?-UU/1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--UU/1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("..-UU/1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?-??/1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--??/1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("..-??/1989", "../1989", NUMERIC_RANGE_ALL_VARIANTS_XX)
    );
  }

  private static Stream<Arguments> monthYear_SpaceArguments() {
    return Stream.of(
        //"[-XU]" with "-./" delimiters
        //"[-XU]" with "-" delimiter
        of("XX-1989 XX-1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU-1989 UU-1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-1989 ??-1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("---1989 ---1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?XX-1989 ?XX-1990", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?UU-1989 ?UU-1990", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("???-1989 ???-1990", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?---1989 ?---1990", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX-1989? XX-1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU-1989? UU-1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-1989? ??-1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("---1989? ---1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified does not apply on space separator
        of("XX-1989 ", null, null),
        of("UU-1989 ", null, null),
        of("??-1989 ", null, null),
        of("---1989 ", null, null),
        of(" XX-1989", null, null),
        of(" UU-1989", null, null),
        of(" ??-1989", null, null),
        of(" ---1989", null, null),

        //"[-XU]" with "." delimiters
        of("XX.1989 XX.1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.1989 UU.1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.1989 ??.1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.1989 --.1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?XX.1989 XX.1990", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?UU.1989 UU.1990", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("???.1989 ??.1990", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?--.1989 --.1990", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.1989? XX.1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.1989? UU.1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.1989? ??.1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.1989? --.1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and also some unknown digits on the year
        of("XX.198X XX.199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.198X UU.199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.19XX --.20XX", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified does not apply on space separator
        of("XX.1989 ", null, null),
        of("UU.1989 ", null, null),
        of("??.1989 ", null, null),
        of("--.1989 ", null, null),
        of(" XX.1989", null, null),
        of(" UU.1989", null, null),
        of(" ??.1989", null, null),
        of(" --.1989", null, null),

        //"[-XU]" with "/" delimiter
        of("XX/1989 XX/1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU/1989 UU/1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/1989 ??/1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--/1989 --/1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?XX/1989 XX/1990", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?UU/1989 UU/1990", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("???/1989 ??/1990", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?--/1989 --/1990", "1989?/1990", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX/1989? XX/1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU/1989? UU/1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/1989? ??/1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--/1989? --/1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and also some unknown digits on the year
        of("XX/198X XX/199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU/198X UU/199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--/19XX 20XX", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified does not apply on space separator
        of("XX/1989 ", null, null),
        of("UU/1989 ", null, null),
        of("??/1989 ", null, null),
        of("--/1989 ", null, null),
        of(" XX/1989", null, null),
        of(" UU/1989", null, null),
        of(" ??/1989", null, null),
        of(" --/1989", null, null)
    );
  }

  private static Stream<Arguments> dayMonthYearArguments() {
    return Stream.of(
        dayMonthYear_SlashArguments(),
        dayMonthYear_SpacedDashArguments(),
        dayMonthYear_DashArguments(),
        dayMonthYear_SpaceArguments()
    ).flatMap(Function.identity());
  }

  private static Stream<Arguments> dayMonthYear_SlashArguments() {
    return Stream.of(
        //"[XU]" with "-" delimiter
        of("XX-11-1989/XX-11-1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU-11-1989/UU-11-1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-11-1989/??-11-1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?XX-11-1989/?XX-11-1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?UU-11-1989/?UU-11-1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("???-11-1989/???-11-1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX-11-1989?/XX-11-1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU-11-1989?/UU-11-1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-11-1989?/??-11-1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and day as well as some unknown digits on the year
        of("XX-11-198X/XX-11-199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("11-UU-198X/11-UU-199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX-UU-198X/XX-UU-199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-UU-198X/??-UU-199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX-??-198X/XX-??-199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX-XX-19XX/XX-XX-20XX", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-??-19??/??-??-20??", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified
        of("XX-11-1989/?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX-11-1989/-", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX-11-1989/..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU-11-1989/?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU-11-1989/-", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU-11-1989/..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-11-1989/?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-11-1989/-", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-11-1989/..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/XX-11-1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("-/XX-11-1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("../XX-11-1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/UU-11-1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("-/UU-11-1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("../UU-11-1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/??-11-1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("-/??-11-1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("../??-11-1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),

        //"[-XU]" with "." delimiter
        of("XX.11.1989/XX.11.1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.11.1989/UU.11.1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.11.1989?/??.11.1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.11.1989/--.11.1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?XX.11.1989/?XX.11.1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?UU.11.1989/?UU.11.1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("???.11.1989/???.11.1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?--.11.1989/?--.11.1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.11.1989?/XX.11.1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.11.1989?/UU.11.1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.11.1989?/??.11.1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.11.1989?/--.11.1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and day as well as some unknown digits on the year
        of("XX.11.198X/XX.11.199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("11.UU.198X/11.UU.199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.UU.198X/XX.UU.199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.UU.198X/??.UU.199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.??.198X/XX.??.199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.XX.19XX/XX.XX.20XX", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.??.19??/??.??.20??", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.--.19--/--.--.20--", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified
        of("XX.11.1989/?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.11.1989/-", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.11.1989/..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.11.1989/?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.11.1989/-", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.11.1989/..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.11.1989/?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.11.1989/-", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.11.1989/..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.11.1989/?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.11.1989/-", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.11.1989/..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/XX.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("-/XX.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("../XX.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/UU.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("-/UU.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("../UU.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/??.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("-/??.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("../??.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?/--.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("-/--.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("../--.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX)
    );
  }

  private static Stream<Arguments> dayMonthYear_SpacedDashArguments() {
    return Stream.of(
        //"[XU]" with "-" delimiter
        of("XX-11-1989 - XX-11-1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU-11-1989 - UU-11-1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-11-1989 - ??-11-1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?XX-11-1989 - ?XX-11-1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?UU-11-1989 - ?UU-11-1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("???-11-1989 - ???-11-1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX-11-1989? - XX-11-1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU-11-1989? - UU-11-1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-11-1989? - ??-11-1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and day as well as some unknown digits on the year
        of("XX-11-198X - XX-11-199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("11-UU-198X - 11-UU-199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX-UU-198X - XX-UU-199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-UU-198X - ??-UU-199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX-??-198X - XX-??-199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX-XX-19XX - XX-XX-20XX", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-??-19?? - ??-??-20??", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified
        of("XX-11-1989 - ?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX-11-1989 - -", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX-11-1989 - ..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU-11-1989 - ?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU-11-1989 - -", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU-11-1989 - ..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-11-1989 - ?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-11-1989 - -", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-11-1989 - ..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - XX-11-1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - XX-11-1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - XX-11-1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - UU-11-1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - UU-11-1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - UU-11-1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - ??-11-1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - ??-11-1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - ??-11-1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),

        //"[-XU]" with "./" delimiters
        //"[-XU]" with "." delimiter
        of("XX.11.1989 - XX.11.1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.11.1989 - UU.11.1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.11.1989 - ??.11.1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.11.1989 - --.11.1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?XX.11.1989 - ?XX.11.1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?UU.11.1989 - ?UU.11.1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("???.11.1989 - ???.11.1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?--.11.1989 - ?--.11.1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.11.1989? - XX.11.1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.11.1989? - UU.11.1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.11.1989? - ??.11.1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.11.1989? - --.11.1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and day as well as some unknown digits on the year
        of("XX.11.198X - XX.11.199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("11.UU.198X - 11.UU.199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.UU.198X - XX.UU.199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.UU.198X - ??.UU.199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.??.198X - XX.??.199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.XX.19XX - XX.XX.20XX", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.??.19?? - ??.??.20??", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.--.19-- - --.--.20--", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified
        of("XX.11.1989 - ?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.11.1989 - -", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.11.1989 - ..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.11.1989 - ?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.11.1989 - -", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.11.1989 - ..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.11.1989 - ?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.11.1989 - -", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.11.1989 - ..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.11.1989 - ?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.11.1989 - -", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.11.1989 - ..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - XX.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - XX.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - XX.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - UU.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - UU.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - UU.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - ??.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - ??.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - ??.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - --.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - --.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - --.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //"[-XU]" with "/" delimiter
        of("XX/11/1989 - XX/11/1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU/11/1989 - UU/11/1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/11/1989 - ??/11/1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--/11/1989 - --/11/1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?XX/11/1989 - ?XX/11/1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?UU/11/1989 - ?UU/11/1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("???/11/1989 - ???/11/1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?--/11/1989 - ?--/11/1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX/11/1989? - XX/11/1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU/11/1989? - UU/11/1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/11/1989? - ??/11/1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--/11/1989? - --/11/1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and day as well as some unknown digits on the year
        of("XX/11/198X - XX/11/199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("11/UU/198X - 11/UU/199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX/UU/198X - XX/UU/199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/UU/198X - ??/UU/199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX/??/198X - XX/??/199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX/XX/19XX - XX/XX/20XX", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/??/19?? - ??/??/20??", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--/--/19-- - --/--/20--", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified
        of("XX/11/1989 - ?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX/11/1989 - -", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX/11/1989 - ..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU/11/1989 - ?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU/11/1989 - -", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU/11/1989 - ..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/11/1989 - ?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/11/1989 - -", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/11/1989 - ..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--/11/1989 - ?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--/11/1989 - -", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--/11/1989 - ..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - XX/11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - XX/11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - XX/11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - UU/11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - UU/11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - UU/11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - ??/11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - ??/11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - ??/11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("? - --/11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("- - --/11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of(".. - --/11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX)
    );
  }

  private static Stream<Arguments> dayMonthYear_DashArguments() {
    return Stream.of(
        //"[XU]" with "./" delimiters
        //"[XU]" with "." delimiter
        of("XX.11.1989-XX.11.1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.11.1989-UU.11.1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.11.1989-??.11.1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?XX.11.1989-?XX.11.1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?UU.11.1989-?UU.11.1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("???.11.1989-???.11.1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.11.1989?-XX.11.1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.11.1989?-UU.11.1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.11.1989?-??.11.1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and day as well as some unknown digits on the year
        of("XX.11.198X-XX.11.199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("11.UU.198X-11.UU.199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.UU.198X-UU.XX.199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.UU.198X-??.UU.199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.??.198X-XX.??.199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.XX.19XX-XX.XX.20XX", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.??.19??-??.??.20??", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified
        of("XX.11.1989-?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.11.1989--", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.11.1989-..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.11.1989-?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.11.1989--", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.11.1989-..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.11.1989-?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.11.1989--", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.11.1989-..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?-XX.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--XX.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("..-XX.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?-UU.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--UU.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("..-UU.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?-??.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--??.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("..-??.11.1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //"[-XU]" with "/" delimiter
        of("XX/11/1989-XX/11/1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU/11/1989-UU/11/1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/11/1989-??/11/1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?XX/11/1989-?XX/11/1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?UU/11/1989-?UU/11/1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("???/11/1989-???/11/1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX/11/1989?-XX/11/1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU/11/1989?-UU/11/1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/11/1989?-??/11/1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and day as well as some unknown digits on the year
        of("XX/11/198X-XX/11/199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU/11/198X-11/UU/199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX/UU/198X-XX/UU/199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/UU/198X-??/UU/199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX/??/198X-XX/??/199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX/XX/19XX-XX/XX/20XX", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/??/19??-??/??/20??", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified
        of("XX/11/1989-?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX/11/1989--", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX/11/1989-..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU/11/1989-?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU/11/1989--", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU/11/1989-..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/11/1989-?", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/11/1989--", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/11/1989-..", "1989-11/..", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?-XX/11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--XX/11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("..-XX/11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?-UU/11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--UU/11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("..-UU/11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?-??/11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--??/11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("..-??/11/1989", "../1989-11", NUMERIC_RANGE_ALL_VARIANTS_XX)
    );
  }

  private static Stream<Arguments> dayMonthYear_SpaceArguments() {
    return Stream.of(
        //"[-XU]" with "-./" delimiters
        //"[-XU]" with "-" delimiter
        of("XX-11-1989 XX-11-1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU-11-1989 UU-11-1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-11-1989 ??-11-1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("---11-1989 ---11-1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?XX-11-1989 ?XX-11-1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?UU-11-1989 ?UU-11-1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("???-11-1989 ???-11-1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?---11-1989 ?---11-1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX-11-1989? XX-11-1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU-11-1989? UU-11-1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-11-1989? ??-11-1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("---11-1989? ---11-1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and day as well as some unknown digits on the year
        of("XX-11-198X XX-11-199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("11-UU-198X 11-UU-199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX-UU-198X XX-UU-199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-UU-198X ??-UU-199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX-??-198X XX-??-199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX-XX-19XX XX-XX-20XX", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??-??-19?? ??-??-20??", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("------19-- ------20--", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified does not apply on space separator
        of("XX-11-1989 ", null, null),
        of("UU-11-1989 ", null, null),
        of("??-11-1989 ", null, null),
        of("---11-1989 ", null, null),
        of(" XX-11-1989 ", null, null),
        of(" UU-11-1989 ", null, null),
        of(" ??-11-1989 ", null, null),
        of(" ---11-1989 ", null, null),
        //"[-XU]" with "." delimiter
        of("XX.11.1989 XX.11.1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.11.1989 UU.11.1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.11.1989 ??.11.1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.11.1989 --.11.1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?XX.11.1989 ?XX.11.1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?UU.11.1989 ?UU.11.1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("???.11.1989 ???.11.1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?--.11.1989 ?--.11.1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.11.1989? XX.11.1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU.11.1989? UU.11.1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.11.1989? ??.11.1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.11.1989? --.11.1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and day as well as some unknown digits on the year
        of("XX.11.198X XX.11.199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("11.UU.198X 11.UU.199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.UU.198X XX.UU.199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.UU.198X ??.UU.199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.??.198X XX.??.199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX.XX.19XX XX.XX.20XX", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??.??.19?? ??.??.20??", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--.--.19-- --.--.20--", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified does not apply on space separator
        of("XX.11.1989 ", null, null),
        of("UU.11.1989 ", null, null),
        of("??.11.1989 ", null, null),
        of("--.11.1989 ", null, null),
        of(" XX.11.1989 ", null, null),
        of(" UU.11.1989 ", null, null),
        of(" ??.11.1989 ", null, null),
        of(" --.11.1989 ", null, null),
        //"[-XU]" with "/" delimiter
        of("XX/11/1989 XX/11/1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU/11/1989 UU/11/1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/11/1989 ??/11/1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--/11/1989 --/11/1990", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?XX/11/1989 ?XX/11/1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?UU/11/1989 ?UU/11/1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("???/11/1989 ???/11/1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("?--/11/1989 ?--/11/1990", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX/11/1989? XX/11/1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("UU/11/1989? UU/11/1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/11/1989? ??/11/1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--/11/1989? --/11/1990?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unknown month and day as well as some unknown digits on the year
        of("XX/11/198X XX/11/199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("11/UU/198X 11/UU/199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX/UU/198X XX/UU/199X", "198X/199X", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/UU/198X ??/UU/199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX/??/198X XX/??/199X", "198X-11/199X-11", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("XX/XX/19XX XX/XX/20XX", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("??/??/19?? ??/??/20??", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        of("--/--/19-- --/--/20--", "19XX/20XX", NUMERIC_RANGE_ALL_VARIANTS_XX),
        //Unspecified does not apply on space separator
        of("XX/11/1989 ", null, null),
        of("UU/11/1989 ", null, null),
        of("??/11/1989 ", null, null),
        of("--/11/1989 ", null, null),
        of(" XX/11/1989 ", null, null),
        of(" UU/11/1989 ", null, null),
        of(" ??/11/1989 ", null, null),
        of(" --/11/1989 ", null, null)
    );
  }
}