package eu.europeana.normalization.dates.edtf;

import eu.europeana.normalization.dates.YearPrecision;
import java.time.Month;
import java.time.Year;
import java.util.EnumSet;

/**
 * This class validates instances of EDTF dates.
 * <p>It validates the following:
 * <ul>
 *  <li>If the start date of an interval is earlier than the end date.</li>
 *  <li>If a date has a month between 1 and 12.</li>
 *  <li>If a date has a possible month day.</li>
 *  <li>Checks if a day 30 or 31 is possible for the month of the date, and checks that the 29th of February is on a leap year.</li>
 *  <li>If a date is not in the future.</li>
 * </ul>
 * </p>
 */
public final class EdtfValidator {

  private static final EnumSet<Month> MONTHS_WITH_31_DAYS = EnumSet.of(Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY,
      Month.AUGUST, Month.OCTOBER, Month.DECEMBER);

  private EdtfValidator() {
  }

  public static boolean validate(AbstractEdtfDate edtfDate, boolean allowFutureDates) {
    boolean isValid;
    if (edtfDate instanceof InstantEdtfDate) {
      isValid = validateInstant((InstantEdtfDate) edtfDate);
    } else {
      isValid = validateInterval((IntervalEdtfDate) edtfDate);
    }
    return isValid && (allowFutureDates || validateNotInFuture(edtfDate));
  }

  private static boolean validateNotInFuture(AbstractEdtfDate edtfDate) {
    if (edtfDate instanceof InstantEdtfDate) {
      return validateInstantNotInFuture((InstantEdtfDate) edtfDate);
    }
    return validateIntervalNotInFuture((IntervalEdtfDate) edtfDate);
  }


  /**
   * The interval validation only checks for the date part and not the time part of the date.
   * <p>It has been decided that only the date part should be checked, ignoring the time part. This
   * could mean that the interval is technically not valid (e.g. start and end are on the same date but the start is later than
   * the end). But since we are only interested in dates, we accept this.</p>
   *
   * @param intervalEdtfDate the interval date to check
   * @return true if it's valid
   */
  private static boolean validateInterval(IntervalEdtfDate intervalEdtfDate) {
    final InstantEdtfDate startDate = intervalEdtfDate.getStart();
    final InstantEdtfDate endDate = intervalEdtfDate.getEnd();
    final boolean isIntervalValid;
    if (startDate != null && validateInstantOfInterval(startDate) && endDate != null && validateInstantOfInterval(endDate)) {
      EdtfDatePart startDatePart = startDate.getEdtfDatePart();
      EdtfDatePart endDatePart = endDate.getEdtfDatePart();
      final boolean isStartDatePartSpecific = !startDatePart.isUnknown() && !startDatePart.isUnspecified();
      final boolean isEndDatePartSpecific = !endDatePart.isUnknown() && !endDatePart.isUnspecified();
      if (isStartDatePartSpecific && isEndDatePartSpecific) {
        if (startDatePart.getYearPrecision() == null && endDatePart.getYearPrecision() == null) {
          isIntervalValid = validateSpecificIntervalDates(startDatePart, endDatePart);
        } else {
          //Validate year using precision instead
          final Integer adjustedStartYear = adjustYearWithPrecision(startDatePart.getYear(), startDatePart.getYearPrecision());
          final Integer adjustedEndYear = adjustYearWithPrecision(endDatePart.getYear(), endDatePart.getYearPrecision());
          isIntervalValid = adjustedStartYear <= adjustedEndYear;
        }
      } else {
        isIntervalValid = isStartDatePartSpecific || isEndDatePartSpecific;
      }
    } else {
      isIntervalValid = false;
    }

    return isIntervalValid;
  }

  private static boolean validateSpecificIntervalDates(EdtfDatePart startDatePart, EdtfDatePart endDatePart) {
    // TODO: 20/07/2022 Should we be using the java.time classes Year, YearMonth, LocalDate etc? (to be handled with MET-4726)
    //Sanity check: years should not be null at this stage, but we check to be sure
    if (startDatePart.getYear() == null || endDatePart.getYear() == null) {
      throw new IllegalArgumentException("Year cannot be null for start or end dates");
    }
    boolean isDatesValid = false;
    if (startDatePart.getYear().equals(endDatePart.getYear())) {
      if (startDatePart.getMonth() == null || endDatePart.getMonth() == null
          || startDatePart.getMonth() < endDatePart.getMonth()) {
        isDatesValid = true;
      } else if (startDatePart.getMonth().equals(endDatePart.getMonth())) {
        isDatesValid =
            startDatePart.getDay() == null || endDatePart.getDay() == null || startDatePart.getDay() <= endDatePart.getDay();
      }
    } else {
      isDatesValid = startDatePart.getYear() < endDatePart.getYear();
    }
    return isDatesValid;
  }

  /**
   * Adjusts the year value based on the {@link YearPrecision} supplied.
   * <p>The adjustment is not a rounding operation but a discarding operation of the right most digits</p>
   * <p>
   * Examples of discarding operations for {@link YearPrecision#CENTURY}:
   *   <ul>
   *     <li>1325/100 * 100 = 1300</li>
   *     <li>-1325/100 * 100 = -1300</li>
   *     <li>1375/100 * 100 = 1300</li>
   *   </ul>
   * </p>
   *
   * @param year the year to adjust
   * @param yearPrecision the year precision to use for the adjustment
   * @return the adjusted year
   */
  private static Integer adjustYearWithPrecision(Integer year, YearPrecision yearPrecision) {
    final Integer adjustedYear;
    if (yearPrecision != null) {
      final int precisionAdjust = yearPrecision.getDuration();
      adjustedYear = (year / precisionAdjust) * precisionAdjust;
    } else {
      adjustedYear = year;
    }
    return adjustedYear;
  }

  /**
   * Validates an instant date.
   * <p>It contains general validity of date part and in addition it <b>cannot</b> have date part null or
   * unknown.</p>
   *
   * @param instantEdtfDate the instant date to validate
   * @return true if the instant is valid
   */
  private static boolean validateInstant(InstantEdtfDate instantEdtfDate) {
    return validateInstantOfInterval(instantEdtfDate)
        && instantEdtfDate.getEdtfDatePart() != null
        && !instantEdtfDate.getEdtfDatePart().isUnknown();
  }

  private static boolean validateInstantOfInterval(InstantEdtfDate instantEdtfDate) {
    return validateDatePart(instantEdtfDate.getEdtfDatePart());
  }

  private static boolean validateDatePart(EdtfDatePart edtfDatePart) {
    boolean isDatePartValid = true;
    if (edtfDatePart != null && !(edtfDatePart.isUnknown() || edtfDatePart.isUnspecified())) {
      if (edtfDatePart.getYear() == null) {
        isDatePartValid = false;
      }
      if (edtfDatePart.getYearPrecision() == null && edtfDatePart.getMonth() != null) {
        if (edtfDatePart.getMonth() < 1 || edtfDatePart.getMonth() > 12) {
          isDatePartValid = false;
        } else {
          isDatePartValid = isDatePartDayValid(edtfDatePart);
        }
      }
    }
    return isDatePartValid;
  }

  private static boolean isDatePartDayValid(EdtfDatePart edtfDatePart) {
    final boolean isDayValid;
    if (edtfDatePart.getDay() == null) {
      isDayValid = true;
    } else {
      final boolean isValidDayRange = edtfDatePart.getDay() > 0 && edtfDatePart.getDay() <= 31;
      final boolean isNot31Or31AndValid =
          edtfDatePart.getDay() != 31 || MONTHS_WITH_31_DAYS.contains(Month.of(edtfDatePart.getMonth()));
      final boolean isNotFebruaryOrFebruaryAndValidDay = edtfDatePart.getMonth() != 2 || isValidFebruaryDay(edtfDatePart);
      isDayValid = isValidDayRange && isNot31Or31AndValid && isNotFebruaryOrFebruaryAndValidDay;
    }
    return isDayValid;
  }

  private static boolean isValidFebruaryDay(EdtfDatePart edtfDatePart) {
    return (edtfDatePart.getDay() > 0 && edtfDatePart.getDay() < 30 && edtfDatePart.getDay() != 29) || Year.isLeap(
        edtfDatePart.getYear());
  }

  private static boolean validateIntervalNotInFuture(IntervalEdtfDate intervalEdtfDate) {
    return validateInstantNotInFuture(intervalEdtfDate.getStart()) && validateInstantNotInFuture(intervalEdtfDate.getEnd());
  }


  // TODO: 20/07/2022 This only calculates years and not other parts of the date.
  //  (this probably won't capture a dates that is days/months in the future but on the current year?)
  //  Fix this to also check the other parts of the date as well.
  //  Perhaps the already existent validation of interval dates should be reused instead, with the end date the current date.
  private static boolean validateInstantNotInFuture(InstantEdtfDate instantEdtfDate) {
    final boolean isYearInPast;
    //If null or not specific it's valid
    if (instantEdtfDate.getEdtfDatePart() == null || instantEdtfDate.getEdtfDatePart().isUnknown()
        || instantEdtfDate.getEdtfDatePart().isUnspecified()) {
      isYearInPast = true;
    } else {
      int currentYear = Year.now().getValue();
      final Integer edtfYear = instantEdtfDate.getEdtfDatePart().getYear();
      final YearPrecision yearPrecision = instantEdtfDate.getEdtfDatePart().getYearPrecision();

      final Integer adjustedCurrentYear = adjustYearWithPrecision(currentYear, yearPrecision);
      final Integer adjustedEdtfYear = adjustYearWithPrecision(edtfYear, yearPrecision);
      isYearInPast = adjustedEdtfYear <= adjustedCurrentYear;
    }
    return isYearInPast;
  }

}
