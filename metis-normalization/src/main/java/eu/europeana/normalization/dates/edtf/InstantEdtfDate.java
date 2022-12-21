package eu.europeana.normalization.dates.edtf;

import eu.europeana.normalization.dates.YearPrecision;
import java.time.Month;
import java.time.Year;
import org.apache.commons.lang3.SerializationUtils;

/**
 * Part of an EDTF date that represents a point in time with various degrees of precision
 */
public class InstantEdtfDate extends AbstractEdtfDate {

  private static final long serialVersionUID = -4111050222535744456L;

  public static final int THRESHOLD_4_DIGITS_YEAR = 9999;
  private EdtfDatePart edtfDatePart;

  public InstantEdtfDate(EdtfDatePart edtfDatePart) {
    this.edtfDatePart = edtfDatePart;
  }

  @Override
  public boolean isTimeOnly() {
    return edtfDatePart == null;
  }

  public EdtfDatePart getEdtfDatePart() {
    return edtfDatePart;
  }

  public void setEdtfDatePart(EdtfDatePart edtfDatePart) {
    this.edtfDatePart = edtfDatePart;
  }

  @Override
  public void setApproximate(boolean approximate) {
    edtfDatePart.setApproximate(approximate);
  }

  @Override
  public void setUncertain(boolean uncertain) {
    edtfDatePart.setUncertain(uncertain);
  }

  @Override
  public boolean isApproximate() {
    return edtfDatePart.isApproximate();
  }

  @Override
  public boolean isUncertain() {
    return edtfDatePart.isUncertain();
  }

  @Override
  public boolean isUnspecified() {
    return edtfDatePart.isUnspecified();
  }

  @Override
  public void switchDayAndMonth() {
    if (edtfDatePart != null) {
      edtfDatePart.switchDayAndMonth();
    }
  }

  @Override
  public InstantEdtfDate getFirstDay() {
    InstantEdtfDate firstDay = null;
    if (getEdtfDatePart() != null && !getEdtfDatePart().isUnknown() && !getEdtfDatePart().isUnspecified()) {
      firstDay = SerializationUtils.clone(this);
      firstDay.setApproximate(false);
      firstDay.setUncertain(false);
      firstDay.getEdtfDatePart().setYearPrecision(null);
      if (getEdtfDatePart().getYearPrecision() != null) {
        if (getEdtfDatePart().getYearPrecision() == YearPrecision.CENTURY) {
          firstDay.getEdtfDatePart().setYear(firstDay.getEdtfDatePart().getYear() + 1);
        }
        firstDay.getEdtfDatePart().setMonth(1);
        firstDay.getEdtfDatePart().setDay(1);
      } else if (getEdtfDatePart().getYear() > -THRESHOLD_4_DIGITS_YEAR
          && getEdtfDatePart().getYear() < THRESHOLD_4_DIGITS_YEAR) {
        if (getEdtfDatePart().getMonth() != null && getEdtfDatePart().getMonth() > 0) {
          firstDay.getEdtfDatePart().setDay(1);
        } else {
          firstDay.getEdtfDatePart().setMonth(1);
          firstDay.getEdtfDatePart().setDay(1);
        }
      }

      // TODO: 25/07/2022 What about > THRESHOLD_4_DIGITS_YEAR??
      //The part where > THRESHOLD_4_DIGITS_YEAR is not possible because it's in the future, so we don't have to check it.
      //Verify though that the contents of this class are always considered valid before the call of this method.
      else if (getEdtfDatePart().getYear() < -THRESHOLD_4_DIGITS_YEAR) {
        EdtfDatePart newEdtfDatePart = new EdtfDatePart();
        newEdtfDatePart.setYear(getEdtfDatePart().getYear());
        firstDay = new InstantEdtfDate(newEdtfDatePart);
      }
    }
    return firstDay;
  }

  @Override
  public InstantEdtfDate getLastDay() {
    InstantEdtfDate lastDay = null;
    if (getEdtfDatePart() != null && !getEdtfDatePart().isUnknown() && !getEdtfDatePart().isUnspecified()) {
      lastDay = SerializationUtils.clone(this);
      lastDay.setApproximate(false);
      lastDay.setUncertain(false);
      lastDay.getEdtfDatePart().setYearPrecision(null);
      if (getEdtfDatePart().getYearPrecision() != null) {
        if (getEdtfDatePart().getYearPrecision() == YearPrecision.CENTURY) {
          lastDay.getEdtfDatePart().setYear(lastDay.getEdtfDatePart().getYear() + 100);
          lastDay.getEdtfDatePart().setMonth(12);
          lastDay.getEdtfDatePart().setDay(31);
        } else if (getEdtfDatePart().getYearPrecision() == YearPrecision.DECADE) {
          lastDay.getEdtfDatePart().setYear(lastDay.getEdtfDatePart().getYear() + 9);
          lastDay.getEdtfDatePart().setMonth(12);
          lastDay.getEdtfDatePart().setDay(31);
        }
      } else if (getEdtfDatePart().getYear() > -THRESHOLD_4_DIGITS_YEAR
          && getEdtfDatePart().getYear() < THRESHOLD_4_DIGITS_YEAR) {
        if (getEdtfDatePart().getMonth() != null && getEdtfDatePart().getMonth() > 0) {
          lastDay.getEdtfDatePart().setDay(lastDayBasedOnMonth());
        } else {
          lastDay.getEdtfDatePart().setMonth(12);
          lastDay.getEdtfDatePart().setDay(31);
        }
      } else if (getEdtfDatePart().getYear() < -THRESHOLD_4_DIGITS_YEAR) {
        EdtfDatePart newEdtfDatePart = new EdtfDatePart();
        newEdtfDatePart.setYear(getEdtfDatePart().getYear());
        lastDay = new InstantEdtfDate(newEdtfDatePart);
      }
    }
    return lastDay;
  }

  private int lastDayBasedOnMonth() {
    return Month.of(getEdtfDatePart().getMonth()).length(Year.isLeap(getEdtfDatePart().getYear()));
  }

  public Integer getCentury() {
    final int century;

    // TODO: 25/07/2022 getEdtfDatePart() or getEdtfDatePart().getYear() might be null??
    //Better to check both for nullity and if they are null we then throw an exception.
    if (getEdtfDatePart().getYear() < 0) {
      century = -1;
    } else if (getEdtfDatePart().getYearPrecision() == null) {
      int hundreds = getEdtfDatePart().getYear() / 100;
      int remainder = getEdtfDatePart().getYear() % 100;
      century = (remainder == 0) ? hundreds : (hundreds + 1);
    } else {
      int hundreds = getEdtfDatePart().getYear() / 100;
      century = hundreds + 1;
    }
    return century;
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    if (this.getEdtfDatePart() != null) {
      stringBuilder.append(edtfDatePart.toString());
    }
    return stringBuilder.toString();
  }
}
