package eu.europeana.normalization.dates.edtf;

import java.time.Year;

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
    if (startDate != null && endDate != null) {
      EdtfDatePart startDatePart = startDate.getEdtfDatePart();
      EdtfDatePart endDatePart = endDate.getEdtfDatePart();
      final boolean isStartDatePartSpecific = startDate.getDateEdgeType() == DateEdgeType.DECLARED;
      final boolean isEndDatePartSpecific = endDate.getDateEdgeType() == DateEdgeType.DECLARED;
      if (isStartDatePartSpecific && isEndDatePartSpecific) {
        isIntervalValid = startDatePart.compareTo(endDatePart) <= 0;
      } else {
        isIntervalValid = isStartDatePartSpecific || isEndDatePartSpecific;
      }
    } else {
      isIntervalValid = false;
    }

    return isIntervalValid;
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
    return instantEdtfDate.getDateEdgeType() == DateEdgeType.DECLARED || instantEdtfDate.getDateEdgeType() == DateEdgeType.OPEN;
  }

  private static boolean validateIntervalNotInFuture(IntervalEdtfDate intervalEdtfDate) {
    return validateInstantNotInFuture(intervalEdtfDate.getStart()) && validateInstantNotInFuture(intervalEdtfDate.getEnd());
  }


  // TODO: 20/07/2022 This only calculates years and not other parts of the date.
  //  (this probably won't capture a dates that is days/months in the future but on the current year?)
  //  Fix this to also check the other parts of the date as well.
  //  Perhaps the already existent validation of interval dates should be reused instead, with the end date the current date.
  // TODO: 14/02/2023 Also this can be done internally in the edtfDatePart during creation or in InstantEdtfDate part during creation
  private static boolean validateInstantNotInFuture(InstantEdtfDate instantEdtfDate) {
    final boolean isYearInPast;
    if (instantEdtfDate.getDateEdgeType() != DateEdgeType.DECLARED) {
      isYearInPast = true;
    } else {
      int currentYear = Year.now().getValue();
      final Integer edtfYear = instantEdtfDate.getEdtfDatePart().getYear().getValue();
      isYearInPast = edtfYear <= currentYear;
    }
    return isYearInPast;
  }

}
