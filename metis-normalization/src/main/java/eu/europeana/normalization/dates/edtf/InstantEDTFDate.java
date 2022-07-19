package eu.europeana.normalization.dates.edtf;

import eu.europeana.normalization.dates.edtf.EDTFDatePart.YearPrecision;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.Year;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Part of an EDTF date that represents a point in time with various degrees of precision
 */
public class InstantEDTFDate extends AbstractEDTFDate implements Serializable {

  private static final long serialVersionUID = -4111050222535744456L;
  private EDTFDatePart edtfDatePart;
  private EDTFTimePart edtfTimePart;

  public InstantEDTFDate(EDTFDatePart edtfDatePart, EDTFTimePart edtfTimePart) {
    super();
    this.edtfDatePart = edtfDatePart;
    this.edtfTimePart = edtfTimePart;
  }

  public InstantEDTFDate(EDTFDatePart edtfDatePart) {
    super();
    this.edtfDatePart = edtfDatePart;
  }

  public InstantEDTFDate(EDTFTimePart parseEDTFTimePart) {
    this(null, parseEDTFTimePart);
  }

  public InstantEDTFDate(Date date) {
    GregorianCalendar gregorianCalendar = new GregorianCalendar();
    gregorianCalendar.setTime(date);
    edtfDatePart = new EDTFDatePart();
    edtfDatePart.setYear(gregorianCalendar.get(Calendar.YEAR));
    edtfDatePart.setMonth(gregorianCalendar.get(Calendar.MONTH));
    edtfDatePart.setDay(gregorianCalendar.get(Calendar.DAY_OF_MONTH));
    edtfTimePart = new EDTFTimePart();
    edtfTimePart.setHour(gregorianCalendar.get(Calendar.HOUR_OF_DAY));
    edtfTimePart.setMinute(gregorianCalendar.get(Calendar.MINUTE));
    edtfTimePart.setSecond(gregorianCalendar.get(Calendar.SECOND));
    edtfTimePart.setMillisecond(gregorianCalendar.get(Calendar.MILLISECOND));
    // TODO: convert timezone
  }

  @Override
  public boolean isTimeOnly() {
    return edtfDatePart == null;
  }

  public EDTFDatePart getEdtfDatePart() {
    return edtfDatePart;
  }

  public void setEdtfDatePart(EDTFDatePart edtfDatePart) {
    this.edtfDatePart = edtfDatePart;
  }

  public EDTFTimePart getEdtfTimePart() {
    return edtfTimePart;
  }

  public void setEdtfTimePart(EDTFTimePart edtfTimePart) {
    this.edtfTimePart = edtfTimePart;
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
  public void switchDayMonth() {
    if (edtfDatePart != null) {
      edtfDatePart.switchDayWithMonth();
    }
  }

  @Override
  public AbstractEDTFDate copy() {
    try {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(bytes);
      out.writeObject(this);
      out.close();
      ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
      return (InstantEDTFDate) in.readObject();
    } catch (ClassNotFoundException | IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public InstantEDTFDate getFirstDay() {
    InstantEDTFDate firstDay = null;
    if (getEdtfDatePart() != null) {
      if (!getEdtfDatePart().isUnknown() && !getEdtfDatePart().isUnspecified()) {
        firstDay = (InstantEDTFDate) this.copy();
        firstDay.setEdtfTimePart(null);
        firstDay.setApproximate(false);
        firstDay.setUncertain(false);
        firstDay.getEdtfDatePart().setYearPrecision(null);
        if (getEdtfDatePart().getYearPrecision() != null) {
          if (getEdtfDatePart().getYearPrecision() == YearPrecision.CENTURY) {
            firstDay.getEdtfDatePart().setYear(firstDay.getEdtfDatePart().getYear() + 1);
          }
          firstDay.getEdtfDatePart().setMonth(1);
          firstDay.getEdtfDatePart().setDay(1);
        } else if (getEdtfDatePart().getYear() > -9999 && getEdtfDatePart().getYear() < 9999) {
          if (getEdtfDatePart().getMonth() != null && getEdtfDatePart().getMonth() > 0) {
            firstDay.getEdtfDatePart().setDay(1);
          } else {
            firstDay.getEdtfDatePart().setMonth(1);
            firstDay.getEdtfDatePart().setDay(1);
          }
        } else if (getEdtfDatePart().getYear() < -9999) {
          EDTFDatePart newEDTFDatePart = new EDTFDatePart();
          newEDTFDatePart.setYear(getEdtfDatePart().getYear());
          firstDay = new InstantEDTFDate(newEDTFDatePart);
        }
      }
    }
    return firstDay;
  }

  @Override
  public InstantEDTFDate getLastDay() {
    InstantEDTFDate lastDay = null;
    if (getEdtfDatePart() != null) {
      if (!getEdtfDatePart().isUnknown() && !getEdtfDatePart().isUnspecified()) {
        lastDay = (InstantEDTFDate) this.copy();
        lastDay.setEdtfTimePart(null);
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
        } else if (getEdtfDatePart().getYear() > -9999 && getEdtfDatePart().getYear() < 9999) {
          if (getEdtfDatePart().getMonth() != null && getEdtfDatePart().getMonth() > 0) {
            if (EDTFValidator.isMonthOf31Days(getEdtfDatePart().getMonth())) {
              lastDay.getEdtfDatePart().setDay(31);
            } else if (getEdtfDatePart().getMonth() == 2) {
              if (Year.isLeap(getEdtfDatePart().getYear())) {
                lastDay.getEdtfDatePart().setDay(29);
              } else {
                lastDay.getEdtfDatePart().setDay(28);
              }
            } else {
              lastDay.getEdtfDatePart().setDay(30);
            }
          } else {
            lastDay.getEdtfDatePart().setMonth(12);
            lastDay.getEdtfDatePart().setDay(31);
          }
        } else if (getEdtfDatePart().getYear() < -9999) {
          EDTFDatePart newEDTFDatePart = new EDTFDatePart();
          newEDTFDatePart.setYear(getEdtfDatePart().getYear());
          lastDay = new InstantEDTFDate(newEDTFDatePart);
        }
      }
    }
    return lastDay;
  }

  @Override
  public void removeTime() {
    edtfTimePart = null;
  }

  public Integer getCentury() {
    if (getEdtfDatePart().getYear() < 0) {
      return -1;
    }
    if (getEdtfDatePart().getYearPrecision() == null) {
      int hundreds = getEdtfDatePart().getYear() / 100;
      int remainder = getEdtfDatePart().getYear() % 100;
      if (remainder == 0) {
        return hundreds;
      } else {
        return hundreds + 1;
      }
    } else {
      int hundreds = getEdtfDatePart().getYear() / 100;
      return hundreds + 1;
    }
  }

}
