package eu.europeana.normalization.dates.edtf;

import eu.europeana.normalization.dates.edtf.EDTFDatePart.YearPrecision;
import java.time.Year;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * This class validates instances of TemporalEntity. It validates the following: . If the start date of an interval is earlier
 * than the end date. . If a date has a month between 1 and 12 . If a date has a possible month day. . Checks if a day 30 or 31 is
 * possible for the month of the date, and checks that the 29th of February is on a leap year. . If a date is not in the future.
 */
public class EDTFValidator {

  public static boolean validate(AbstractEDTFDate edtf, boolean allowFutureDates) {
    boolean isValid = false;
    if (edtf instanceof InstantEDTFDate) {
      isValid = validateInstant((InstantEDTFDate) edtf, true);
    } else {
      isValid = validateInterval((IntervalEDTFDate) edtf);
    }
    return isValid && (allowFutureDates || validateNotInFuture(edtf));
  }

  private static boolean validateNotInFuture(AbstractEDTFDate edtf) {
    if (edtf instanceof InstantEDTFDate) {
      return validateInstantNotInFuture((InstantEDTFDate) edtf);
    }
    return validateIntervalNotInFuture((IntervalEDTFDate) edtf);
  }

  private static boolean validateInterval(IntervalEDTFDate edtf) {
    if (edtf.getStart() == null || edtf.getEnd() == null || !validateInstant(edtf.getStart(), false)
        || !validateInstant(edtf.getEnd(), false)) {
      return false;
    }

    EDTFDatePart sEDTFDatePart = edtf.getStart().getEdtfDatePart();
    EDTFDatePart eEDTFDatePart = edtf.getEnd().getEdtfDatePart();
    if ((eEDTFDatePart.isUnknown() || eEDTFDatePart.isUnspecified()) && (sEDTFDatePart.isUnknown()
        || sEDTFDatePart.isUnspecified())) {
      return false;
    }
    if ((eEDTFDatePart.isUnknown() || eEDTFDatePart.isUnspecified()) && !(sEDTFDatePart.isUnknown()
        || sEDTFDatePart.isUnspecified())) {
      return true;
    }
    if ((sEDTFDatePart.isUnknown() || sEDTFDatePart.isUnspecified()) && !(eEDTFDatePart.isUnknown()
        || eEDTFDatePart.isUnspecified())) {
      return true;
    }

    if (sEDTFDatePart.getYearPrecision() == null && eEDTFDatePart.getYearPrecision() == null) {
      if (sEDTFDatePart.getYear() > eEDTFDatePart.getYear()) {
        return false;
      }
      if (sEDTFDatePart.getYear() < eEDTFDatePart.getYear()) {
        return true;
      }
      if (sEDTFDatePart.getMonth() == null || eEDTFDatePart.getMonth() == null
          || sEDTFDatePart.getMonth() < eEDTFDatePart.getMonth()) {
        return true;
      }
      if (sEDTFDatePart.getMonth() > eEDTFDatePart.getMonth()) {
        return false;
      }
      if (sEDTFDatePart.getDay() == null || eEDTFDatePart.getDay() == null || sEDTFDatePart.getDay() <= eEDTFDatePart.getDay()) {
        return true;
      }
      return false;
      //		}else if(sDate.yearPrecision!=null || eDate.yearPrecision!=null){
    } else {
      int precisionAdjust = 0;
      Integer sYear = sEDTFDatePart.getYear();
      Integer eYear = eEDTFDatePart.getYear();
      if (sEDTFDatePart.getYearPrecision() != null) {
        switch (sEDTFDatePart.getYearPrecision()) {
          case DECADE:
            precisionAdjust = 10;
            break;
          case CENTURY:
            precisionAdjust = 100;
            break;
          case MILLENNIUM:
            precisionAdjust = 1000;
            break;
        }
        sYear = (sYear / precisionAdjust) * precisionAdjust;
      }
      if (eEDTFDatePart.getYearPrecision() != null) {
        switch (eEDTFDatePart.getYearPrecision()) {
          case DECADE:
            precisionAdjust = 10;
            break;
          case CENTURY:
            precisionAdjust = 100;
            break;
          case MILLENNIUM:
            precisionAdjust = 1000;
            break;
        }
        eYear = (eYear / precisionAdjust) * precisionAdjust;
      }
      return sYear <= eYear;
    }
  }

  private static boolean validateInstant(InstantEDTFDate edtf, boolean standalone) {
    EDTFDatePart edtfDatePart = edtf.getEdtfDatePart();
    if (edtfDatePart != null) {
      if (!(edtfDatePart.isUnknown() || edtfDatePart.isUnspecified())) {
        if (edtfDatePart.getYear() == null) {
          return false;
        }
        if (edtfDatePart.getYearPrecision() == null) {
          if (edtfDatePart.getMonth() != null) {
            if (edtfDatePart.getMonth() < 1 || edtfDatePart.getMonth() > 12) {
              return false;
            }
            if (edtfDatePart.getDay() != null) {
              if (edtfDatePart.getDay() < 1 || edtfDatePart.getDay() > 31) {
                return false;
              }
              if (edtfDatePart.getDay() == 31 && !isMonthOf31Days(edtfDatePart.getMonth())) {
                return false;
              }
              if (edtfDatePart.getMonth() == 2) {
                if (edtfDatePart.getDay() == 30 || (edtfDatePart.getDay() == 29 && !Year.isLeap(edtfDatePart.getYear()))) {
                  return false;
                }
              }
            }
          }
        }
      }
    }
    EDTFTimePart edtfTimePart = edtf.getEdtfTimePart();
    if (edtfTimePart != null) {
      if (edtfTimePart.getHour() >= 24 || edtfTimePart.getHour() < 0) {
        return false;
      }
      if (edtfTimePart.getMinute() != null && (edtfTimePart.getMinute() >= 60 || edtfTimePart.getMinute() < 0)) {
        return false;
      }
      if (edtfTimePart.getSecond() != null && (edtfTimePart.getSecond() >= 60 || edtfTimePart.getSecond() < 0)) {
        return false;
      }
      if (edtfTimePart.getMillisecond() != null && (edtfTimePart.getMillisecond() >= 1000 || edtfTimePart.getMillisecond() < 0)) {
        return false;
      }
      // TODO: validate timezone
    }
    if (standalone) {
      if ((edtfDatePart == null || edtfDatePart.isUnknown()) && edtfTimePart == null) {
        return false;
      }
    }
    return true;
  }

  private static boolean validateIntervalNotInFuture(IntervalEDTFDate edtf) {
    return validateInstantNotInFuture(edtf.getStart()) && validateInstantNotInFuture(edtf.getEnd());
  }

  private static boolean validateInstantNotInFuture(InstantEDTFDate edtf) {
    if (edtf.getEdtfDatePart() == null || edtf.getEdtfDatePart().isUnknown() || edtf.getEdtfDatePart().isUnspecified()) {
      return true;
    }
    int currentYear = new GregorianCalendar().get(Calendar.YEAR);
    if (edtf.getEdtfDatePart().getYearPrecision() == null) {
      return edtf.getEdtfDatePart().getYear() <= currentYear;
    } else if (edtf.getEdtfDatePart().getYearPrecision() == YearPrecision.MILLENNIUM) {
      currentYear = (currentYear / 1000) * 1000;
      return (edtf.getEdtfDatePart().getYear() * 1000) / 1000 <= currentYear;
    } else if (edtf.getEdtfDatePart().getYearPrecision() == YearPrecision.CENTURY) {
      currentYear = (currentYear / 100) * 100;
      return (edtf.getEdtfDatePart().getYear() * 100) / 100 <= currentYear;
    } else if (edtf.getEdtfDatePart().getYearPrecision() == YearPrecision.DECADE) {
      currentYear = (currentYear / 10) * 10;
      return (edtf.getEdtfDatePart().getYear() * 10) / 10 <= currentYear;
    }
    throw new IllegalArgumentException("This should never occour");
  }

  public static boolean isMonthOf31Days(Integer month) {
    month -= 1;// java Calendar starts at month 0
    return ((month == Calendar.JANUARY) || (month == Calendar.MARCH) || (month == Calendar.MAY)
        || (month == Calendar.JULY) || (month == Calendar.AUGUST) || (month == Calendar.OCTOBER)
        || (month == Calendar.DECEMBER));
  }

}
