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
      isValid = validateInstant((InstantEdtfDate) edtfDate, true);
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

  // TODO: 20/07/2022 It checks interval of date parts but not time parts??
  private static boolean validateInterval(IntervalEdtfDate intervalEdtfDate) {
    final InstantEdtfDate startDate = intervalEdtfDate.getStart();
    final InstantEdtfDate endDate = intervalEdtfDate.getEnd();
    final boolean isIntervalValid;
    if (startDate != null && validateInstant(startDate, false) && endDate != null && validateInstant(endDate, false)) {
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
    // TODO: 20/07/2022 Should we be using the java.time classes Year, YearMonth, LocalDate etc?
    // TODO: 20/07/2022 No check for null year but we check for null month and day?
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

  private static Integer adjustYearWithPrecision(Integer year, YearPrecision yearPrecision) {
    // TODO: 25/07/2022 Is this precision adjustment correct? How should the rounding be for positive and negatives?
    // TODO: 25/07/2022 Some examples of rounding:
    // TODO: 25/07/2022 1325/100 * 100 = 1300
    // TODO: 25/07/2022 -1325/100 * 100 = -1300
    // TODO: 25/07/2022  1325/100 * 100 = 1300 VERSUS 1375/100 * 100 = 1300
    final Integer adjustedYear;
    if (yearPrecision != null) {
      final int precisionAdjust = yearPrecision.getDuration();
      adjustedYear = (year / precisionAdjust) * precisionAdjust;
    } else {
      adjustedYear = year;
    }
    return adjustedYear;
  }

  private static boolean validateInstant(InstantEdtfDate instantEdtfDate, boolean standalone) {
    boolean isInstantValid = false;
    EdtfDatePart edtfDatePart = instantEdtfDate.getEdtfDatePart();
    if (validateDatePart(edtfDatePart)) {
      EdtfTimePart edtfTimePart = instantEdtfDate.getEdtfTimePart();
      if (validateTimePart(edtfTimePart)) {
        // TODO: 20/07/2022 Does this mean that if it's not standalone, it is then allowed to have both null/unknown??
        if (standalone) {
          //Not valid if both parts null/unknown
          final boolean isBothPartsNullOrUnknown = (edtfDatePart == null || edtfDatePart.isUnknown()) && edtfTimePart == null;
          isInstantValid = !isBothPartsNullOrUnknown;
        } else {
          isInstantValid = true;
        }
      }
    }
    return isInstantValid;
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

  private static boolean validateTimePart(EdtfTimePart edtfTimePart) {
    final boolean isTimePartValid;
    if (edtfTimePart == null) {
      isTimePartValid = true;
    } else {
      final boolean isHourValid = edtfTimePart.getHour() >= 0 && edtfTimePart.getHour() < 24;
      final boolean isMinuteValid =
          edtfTimePart.getMinute() != null && (edtfTimePart.getMinute() >= 0 && edtfTimePart.getMinute() < 60);
      final boolean isSecondValid =
          edtfTimePart.getSecond() != null && (edtfTimePart.getSecond() >= 0 && edtfTimePart.getSecond() < 60);
      final boolean isMillisecondValid =
          edtfTimePart.getMillisecond() != null && (edtfTimePart.getMillisecond() >= 0 && edtfTimePart.getMillisecond() < 1000);
      isTimePartValid = isHourValid && isMinuteValid && isSecondValid && isMillisecondValid;
    }
    return isTimePartValid;
  }

  private static boolean validateIntervalNotInFuture(IntervalEdtfDate intervalEdtfDate) {
    return validateInstantNotInFuture(intervalEdtfDate.getStart()) && validateInstantNotInFuture(intervalEdtfDate.getEnd());
  }

  // TODO: 20/07/2022 This only calculates years and not other parts of the date.
  //  (this probably won't capture a dates that is days/months in the future but on the current year?)
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
