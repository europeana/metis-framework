package eu.europeana.metis.utils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * General {@link Date} utilities methods
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-05-31
 */
public final class DateUtils {

  private DateUtils() {
  }

  /**
   * Calculates the difference in time, of two {@link Date}s, based on a {@link TimeUnit}
   *
   * @param pastDate the older date
   * @param futureDate the newer date
   * @param timeUnit the time unit used to calculate the difference
   * @return the amount of {@link TimeUnit}s of the difference
   */
  public static long calculateDateDifference(Date pastDate, Date futureDate, TimeUnit timeUnit) {
    long diffInMillies = futureDate.getTime() - pastDate.getTime();
    return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
  }

  /**
   * Modifies a {@link Date} by a {@link TimeUnit} {@code amount}.
   * <p>This means that the {@code timeUnit} defines the type of the {@code amount} to be added to or
   * subtracted from the {@code date}</p>
   *
   * @param date the date used that will be modified
   * @param amount the amount of units to be added or subtracted, so it can be a negative value
   * @param timeUnit the time unit used define the type of the {@code amount} parameter
   * @return the new converted date
   */
  public static Date modifyDateByTimeUnitAmount(Date date, long amount, TimeUnit timeUnit) {
    long dateInMillis = date.getTime();
    return new Date(dateInMillis + TimeUnit.MILLISECONDS.convert(amount, timeUnit));
  }
}
