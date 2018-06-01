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
   * Adds minutes to a provided date.
   *
   * @param minutes the minutes to be added
   * @param date the date used to add minutes to
   * @return the new date with the added minutes
   */
  public static Date addMinutesToDate(long minutes, Date date) {
    long dateInMillis = date.getTime();
    return new Date(dateInMillis + TimeUnit.MINUTES.toMillis(minutes));
  }

  /**
   * Adds minutes to a provided date.
   *
   * @param minutes the minutes to be added
   * @param date the date used to add minutes to
   * @return the new date with the added minutes
   */
  public static Date subtractMinutesToDate(long minutes, Date date) {
    long dateInMillis = date.getTime();
    return new Date(dateInMillis - TimeUnit.MINUTES.toMillis(minutes));
  }

}
