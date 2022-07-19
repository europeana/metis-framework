package eu.europeana.normalization.dates.edtf;

import eu.europeana.normalization.dates.edtf.Date.YearPrecision;
import java.time.Year;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * This class validates instances of TemporalEntity. It validates the following: . If the start date of an interval is earlier
 * than the end date. . If a date has a month between 1 and 12 . If a date has a possible month day. . Checks if a day 30 or 31 is
 * possible for the month of the date, and checks that the 29th of February is on a leap year. . If a date is not in the future.
 */
public class EdtfValidator {

  public static boolean validate(TemporalEntity edtf, boolean allowFutureDates) {
    boolean isValid = false;
    if (edtf instanceof Instant) {
      isValid = validateInstant((Instant) edtf, true);
    } else {
      isValid = validateInterval((Interval) edtf);
    }
    return isValid && (allowFutureDates || validateNotInFuture(edtf));
  }

  private static boolean validateNotInFuture(TemporalEntity edtf) {
    if (edtf instanceof Instant) {
      return validateInstantNotInFuture((Instant) edtf);
    }
    return validateIntervalNotInFuture((Interval) edtf);
  }

  private static boolean validateInterval(Interval edtf) {
    if (edtf.getStart() == null || edtf.getEnd() == null || !validateInstant(edtf.getStart(), false)
        || !validateInstant(edtf.getEnd(), false)) {
      return false;
    }

    Date sDate = edtf.getStart().getDate();
    Date eDate = edtf.getEnd().getDate();
    if ((eDate.isUnkown() || eDate.isUnspecified()) && (sDate.isUnkown() || sDate.isUnspecified())) {
      return false;
    }
    if ((eDate.isUnkown() || eDate.isUnspecified()) && !(sDate.isUnkown() || sDate.isUnspecified())) {
      return true;
    }
    if ((sDate.isUnkown() || sDate.isUnspecified()) && !(eDate.isUnkown() || eDate.isUnspecified())) {
      return true;
    }

    if (sDate.yearPrecision == null && eDate.yearPrecision == null) {
      if (sDate.year > eDate.year) {
        return false;
      }
      if (sDate.year < eDate.year) {
        return true;
      }
      if (sDate.month == null || eDate.month == null || sDate.month < eDate.month) {
        return true;
      }
      if (sDate.month > eDate.month) {
        return false;
      }
      if (sDate.day == null || eDate.day == null || sDate.day <= eDate.day) {
        return true;
      }
      return false;
      //		}else if(sDate.yearPrecision!=null || eDate.yearPrecision!=null){
    } else {
      int precisionAdjust = 0;
      Integer sYear = sDate.year;
      Integer eYear = eDate.year;
      if (sDate.yearPrecision != null) {
        switch (sDate.yearPrecision) {
          case DECADE:
            precisionAdjust = 10;
            break;
          case CENTURY:
            precisionAdjust = 100;
            break;
          case MILLENIUM:
            precisionAdjust = 1000;
            break;
        }
        sYear = (sYear / precisionAdjust) * precisionAdjust;
      }
      if (eDate.yearPrecision != null) {
        switch (eDate.yearPrecision) {
          case DECADE:
            precisionAdjust = 10;
            break;
          case CENTURY:
            precisionAdjust = 100;
            break;
          case MILLENIUM:
            precisionAdjust = 1000;
            break;
        }
        eYear = (eYear / precisionAdjust) * precisionAdjust;
      }
      return sYear <= eYear;
    }
  }

  private static boolean validateInstant(Instant edtf, boolean standalone) {
    Date date = edtf.getDate();
    if (date != null) {
      if (!(date.isUnkown() || date.isUnspecified())) {
        if (date.year == null) {
          return false;
        }
        if (date.yearPrecision == null) {
          if (date.month != null) {
            if (date.month < 1 || date.month > 12) {
              return false;
            }
            if (date.day != null) {
              if (date.day < 1 || date.day > 31) {
                return false;
              }
              if (date.day == 31 && !isMonthOf31Days(date.month)) {
                return false;
              }
              if (date.month == 2) {
                if (date.day == 30 || (date.day == 29 && !Year.isLeap(date.year))) {
                  return false;
                }
              }
            }
          }
        }
      }
    }
    Time t = edtf.getTime();
    if (t != null) {
      if (t.hour >= 24 || t.hour < 0) {
        return false;
      }
      if (t.minute != null && (t.minute >= 60 || t.minute < 0)) {
        return false;
      }
      if (t.second != null && (t.second >= 60 || t.second < 0)) {
        return false;
      }
      if (t.millisecond != null && (t.millisecond >= 1000 || t.millisecond < 0)) {
        return false;
      }
      // TODO: validate timezone
    }
    if (standalone) {
      if ((date == null || date.isUnkown()) && t == null) {
        return false;
      }
    }
    return true;
  }

  private static boolean validateIntervalNotInFuture(Interval edtf) {
    return validateInstantNotInFuture(edtf.getStart()) && validateInstantNotInFuture(edtf.getEnd());
  }

  private static boolean validateInstantNotInFuture(Instant edtf) {
    if (edtf.getDate() == null || edtf.getDate().isUnkown() || edtf.getDate().isUnspecified()) {
      return true;
    }
    int currentYear = new GregorianCalendar().get(Calendar.YEAR);
    if (edtf.getDate().getYearPrecision() == null) {
      return edtf.getDate().getYear() <= currentYear;
    } else if (edtf.getDate().getYearPrecision() == YearPrecision.MILLENIUM) {
      currentYear = (currentYear / 1000) * 1000;
      return (edtf.getDate().getYear() * 1000) / 1000 <= currentYear;
    } else if (edtf.getDate().getYearPrecision() == YearPrecision.CENTURY) {
      currentYear = (currentYear / 100) * 100;
      return (edtf.getDate().getYear() * 100) / 100 <= currentYear;
    } else if (edtf.getDate().getYearPrecision() == YearPrecision.DECADE) {
      currentYear = (currentYear / 10) * 10;
      return (edtf.getDate().getYear() * 10) / 10 <= currentYear;
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
