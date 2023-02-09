package eu.europeana.normalization.dates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.europeana.normalization.dates.edtf.EdtfParser;
import java.text.ParseException;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests to validate EDTF format parser Level 0 Level 0 specifies features of ISO 8601-1; Extended format EDTF requires
 * “extended format” as defined in 8601: hyphen between calendar components and colon between clock components (e.g.
 * 2005-09-24T10:00:00). “Basic format" as defined in ISO 8601, which omits separators (e.g. 20050924T100000), is not permitted.
 *
 * @see <a href="https://www.loc.gov/standards/datetime/">EDTF library specification</a>
 */
class DateLevel0Test {

  private EdtfParser edtfParser = new EdtfParser();

  //  EDTF Level 0 adopts representations of a time interval where both the start and end are dates: start and end date only; that is, both start and duration, and duration and end, are excluded. Time of day is excluded.

  @ParameterizedTest
  @MethodSource
  void dateIntervalRepresentation(String input, String expected) throws ParseException {
    parse(input, expected);
  }

  private void parse(String input, String expected) throws ParseException {
    if (expected == null) {
      assertThrows(ParseException.class, () -> edtfParser.parse(input));
    } else {
      assertEquals(expected, edtfParser.parse(input).toString());
    }
  }

  private static Stream<Arguments> dateIntervalRepresentation() {
    return Stream.of(
        Arguments.of("1989/1990", "1989/1990"),
        Arguments.of("1989-11/1990-11", "1989-11/1990-11"),
        Arguments.of("1989-11-01/1990-11-01", "1989-11-01/1990-11-01"),
        Arguments.of("1989-11-01/1990-11", "1989-11-01/1990-11"),
        Arguments.of("1989-11-01/1990", "1989-11-01/1990"),
        Arguments.of("1989/1990-11", "1989/1990-11"),
        Arguments.of("1989/1990-11-01", "1989/1990-11-01"),
        Arguments.of("1989-00/1990-00", "1989/1990"),
        Arguments.of("1989-00-00/1990-00-00", "1989/1990"),
        //Spaces not valid
        Arguments.of("1989 / 1990", null),
        //Dash not valid
        Arguments.of("1989-1990", null),
        //Missing digits
        Arguments.of("989-1990", null),
        Arguments.of("1989-990", null)
    );
  }
}
