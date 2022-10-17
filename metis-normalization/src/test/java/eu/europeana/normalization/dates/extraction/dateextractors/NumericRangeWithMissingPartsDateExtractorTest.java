package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_RANGE_ALL_VARIANTS;
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
      if (actual.contains("?")) {
        assertTrue(dateNormalizationResult.getEdtfDate().isUncertain());
      } else {
        assertFalse(dateNormalizationResult.getEdtfDate().isUncertain());
      }
      assertEquals(dateNormalizationExtractorMatchId, dateNormalizationResult.getDateNormalizationExtractorMatchId());
    }
  }

  private static Stream<Arguments> extractYMD() {
    // TODO: 17/10/2022 The '?' is not supported in the beginning of the date in ranges but in single dates it
    //  is(NumericWithMissingPartsDateExtractor). What should we follow?

    return Stream.of(
        //YEAR
        //Numeric range '/'
        //A month and day can be missing
        of("1989/1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989?/1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989/?1990", null, NUMERIC_RANGE_ALL_VARIANTS),
        //Dates separator ' - '
        of("1989 - 1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989? - 1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989 - ?1990", null, NUMERIC_RANGE_ALL_VARIANTS),
        //Dates separator '-'
        of("1989-1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989?-1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989-?1990", null, NUMERIC_RANGE_ALL_VARIANTS),
        //Dates separator ' '(space)
        of("1989 1990", "1989/1990", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989? 1990?", "1989?/1990?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989 ?1990", null, NUMERIC_RANGE_ALL_VARIANTS),

        //YEAR-MONTH
        //Dates separator '/'
        of("1989-11/1990-11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11/1990.11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11?/1990-11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11?/1990.11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989-11/?1990-11", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989.11/?1990.11", null, NUMERIC_RANGE_ALL_VARIANTS),

        //Dates separator ' - '
        of("1989-11 - 1990-11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11 - 1990.11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11 - 1990/11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11? - 1990-11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11? - 1990.11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11? - 1990/11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989-11 - ?1990-11", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989.11 - ?1990.11", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989/11 - ?1990/11", null, NUMERIC_RANGE_ALL_VARIANTS),

        //Dates separator '-'
        of("1989.11-1990.11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11-1990/11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11?-1990.11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11?-1990/11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989/11-?1990/11", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989.11-?1990.11", null, NUMERIC_RANGE_ALL_VARIANTS),

        //Dates separator ' '(space)
        of("1989-11 1990-11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11 1990.11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11 1990/11", "1989-11/1990-11", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11? 1990-11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11? 1990.11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11? 1990/11?", "1989-11?/1990-11?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989-11 ?1990-11", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989.11 ?1990.11", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989/11 ?1990/11", null, NUMERIC_RANGE_ALL_VARIANTS),

        //YEAR-MONTH-DAY
        //Dates separator '/'
        of("1989-11-01/1990-11-01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01/1990.11.01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        //Some missing digits are allowed
        of("989-1-1/990-11-01", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("989.1.1/990.11.01", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01?/1990-11-01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01?/1990.11.01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989-11-01/?1990-11-01", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989.11.01/?1990.11.01", null, NUMERIC_RANGE_ALL_VARIANTS),
        //Dates separator ' - '
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
        //Dates separator '-'
        of("1989/11/01-1990/11/01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01-1990.11.01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        //Some missing digits are allowed
        of("989.1.1-990.11.01", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("989/1/1-990/11/01", "0989-01-01/0990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11/01?-1990/11/01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01?-1990.11.01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989/11/01-?1990/11/01", null, NUMERIC_RANGE_ALL_VARIANTS),
        of("?1989.11.01-?1990.11.01", null, NUMERIC_RANGE_ALL_VARIANTS),
        //Dates separator ' '(space)
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

        //Combination of separators
        of("1989-11-01/1990.11.01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01?/1990.11.01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),

        of("1989-11-01 - 1990.11.01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01 - 1990/11/01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01? - 1990.11.01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01? - 1990/11/01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),

        of("1989/11/01-1990.11.01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989/11/01?-1990.11.01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),

        of("1989-11-01 1990.11.01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01 1990/11/01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01 1990/11/01", "1989-11-01/1990-11-01", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01? 1990.11.01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989-11-01? 1990/11/01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),
        of("1989.11.01? 1990/11/01?", "1989-11-01?/1990-11-01?", NUMERIC_RANGE_ALL_VARIANTS),

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

    return Stream.of(
        //MONTH-YEAR
        //        of("11-1989", "1989-11", NUMERIC_RANGE_ALL_VARIANTS)
    );
  }

}