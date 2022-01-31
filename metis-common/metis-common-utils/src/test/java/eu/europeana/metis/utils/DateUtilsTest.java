package eu.europeana.metis.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit test for {@link DateUtils}
 *
 * @author Jorge Ortiz
 * @since 31-01-2022
 */
class DateUtilsTest {

  private static Stream<Arguments> provideExpectedTimeAndTimeUnits() {
    return Stream.of(
        Arguments.of(86422000000000L,TimeUnit.NANOSECONDS),
        Arguments.of(86422000000L,TimeUnit.MICROSECONDS),
        Arguments.of(86422000L,TimeUnit.MILLISECONDS),
        Arguments.of(86422L,TimeUnit.SECONDS),
        Arguments.of(1440L,TimeUnit.MINUTES),
        Arguments.of(24L,TimeUnit.HOURS),
        Arguments.of(1L,TimeUnit.DAYS));
  }

  @ParameterizedTest
  @MethodSource("provideExpectedTimeAndTimeUnits")
  void calculateDateDifferenceInSeconds(long expectedTime, TimeUnit timeUnit) {
    final Date pastDate = Date.from(Instant.parse("2022-01-27T14:49:38.00Z"));
    final Date futureDate = Date.from(Instant.parse("2022-01-28T14:50:00.00Z"));

    final long actualDifference = DateUtils.calculateDateDifference(pastDate, futureDate, timeUnit);

    assertEquals(expectedTime, actualDifference);
  }

  private static Stream<Arguments> provideExpectedDateAndTimeUnits() {
    return Stream.of(
        Arguments.of(Date.from(Instant.parse("2022-01-27T14:49:38.000005000Z")),TimeUnit.NANOSECONDS),
        Arguments.of(Date.from(Instant.parse("2022-01-27T14:49:38.005000000Z")),TimeUnit.MICROSECONDS),
        Arguments.of(Date.from(Instant.parse("2022-01-27T14:49:43.000000000Z")),TimeUnit.MILLISECONDS),
        Arguments.of(Date.from(Instant.parse("2022-01-27T16:12:58.000000000Z")),TimeUnit.SECONDS),
        Arguments.of(Date.from(Instant.parse("2022-01-31T02:09:38.000000000Z")),TimeUnit.MINUTES),
        Arguments.of(Date.from(Instant.parse("2022-08-23T22:49:38.000000000Z")),TimeUnit.HOURS),
        Arguments.of(Date.from(Instant.parse("2035-10-06T14:49:38.000000000Z")),TimeUnit.DAYS));
  }

  @ParameterizedTest
  @MethodSource("provideExpectedDateAndTimeUnits")
  void modifyDateByTimeUnitAmount(Date expectedDate, TimeUnit timeUnit) {
    final Date pastDate = Date.from(Instant.parse("2022-01-27T14:49:38.000000000Z"));

    final Date modifiedDate = DateUtils.modifyDateByTimeUnitAmount(pastDate,5000, timeUnit);

    assertEquals(expectedDate, modifiedDate);
  }
}