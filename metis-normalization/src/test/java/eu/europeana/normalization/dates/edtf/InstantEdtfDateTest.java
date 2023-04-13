package eu.europeana.normalization.dates.edtf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.of;

import eu.europeana.normalization.dates.YearPrecision;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class InstantEdtfDateTest {

  @ParameterizedTest
  @MethodSource
  void getFirstDay(InstantEdtfDate input, String expectedCentury, String expectedFirstDayFirstDayCentury,
      String expectedFirstDayLastDayCentury,
      String expectedFirstDay, String expectedLastDay) {
    InstantEdtfDate firstDay = input.getFirstDay();
    InstantEdtfDate lastDay = input.getLastDay();
    assertEquals(expectedFirstDay, firstDay.toString());
    assertEquals(expectedLastDay, lastDay.toString());
    assertEquals(expectedCentury, input.getCentury().toString());
    assertEquals(expectedFirstDayFirstDayCentury, firstDay.getCentury().toString());
    assertEquals(expectedFirstDayLastDayCentury, lastDay.getCentury().toString());
  }

  private static Stream<Arguments> getFirstDay() throws DateExtractionException {
    return Stream.of(
        of(new InstantEdtfDateBuilder(2011).build(), "21", "21", "21", "2011-01-01", "2011-12-31"),
        of(new InstantEdtfDateBuilder(1782).build(), "18", "18", "18", "1782-01-01", "1782-12-31"),
        of(new InstantEdtfDateBuilder(1804).build(), "19", "19", "19", "1804-01-01", "1804-12-31"),
        of(new InstantEdtfDateBuilder(19).withYearPrecision(YearPrecision.CENTURY).build(),
            "19", "20", "20", "1901-01-01", "2000-12-31"),
        of(new InstantEdtfDateBuilder(198).withYearPrecision(YearPrecision.DECADE).build(),
            "20", "20", "20", "1980-01-01", "1989-12-31"),
        of(new InstantEdtfDateBuilder(190).withYearPrecision(YearPrecision.DECADE).build(),
            "19", "19", "20", "1900-01-01", "1909-12-31"),
        of(new InstantEdtfDateBuilder(2018).withMonth(10).withDay(11).build(), "21", "21", "21", "2018-10-11", "2018-10-11"),
        of(new InstantEdtfDateBuilder(1989).withMonth(11).build(), "20", "20", "20", "1989-11-01", "1989-11-30")
    );
  }

}