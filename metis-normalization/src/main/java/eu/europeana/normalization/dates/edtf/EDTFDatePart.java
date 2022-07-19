package eu.europeana.normalization.dates.edtf;

import java.io.Serializable;

/**
 * Class representing the date part an EDTF date.
 * <p>
 * Support partial dates, including only centuries or decades (e.g., 19XX)
 * </p>
 */
public class EDTFDatePart implements Serializable {

  private static final long serialVersionUID = -7497880706682687923L;

  /**
   * Enum indicating the year precision of the date part
   */
  public enum YearPrecision {
    MILLENNIUM, CENTURY, DECADE
  }

  private boolean uncertain;
  private boolean approximate;
  private boolean unknown;
  private boolean unspecified;

  private Integer year;
  private Integer month;
  private Integer day;

  private YearPrecision yearPrecision;

  public boolean isUncertain() {
    return uncertain;
  }

  public void setUncertain(boolean uncertain) {
    this.uncertain = uncertain;
  }

  public boolean isApproximate() {
    return approximate;
  }

  public void setApproximate(boolean approximate) {
    this.approximate = approximate;
  }

  public boolean isUnknown() {
    return unknown;
  }

  public void setUnknown(boolean unknown) {
    this.unknown = unknown;
  }

  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
  }

  public Integer getMonth() {
    return month;
  }

  public void setMonth(Integer month) {
    this.month = month == null || month == 0 ? null : month;
  }

  public Integer getDay() {
    return day;
  }

  public void setDay(Integer day) {
    this.day = day == null || day == 0 ? null : day;
  }

  public boolean isUnspecified() {
    return unspecified;
  }

  public void setUnspecified(boolean unspecified) {
    this.unspecified = unspecified;
  }

  public YearPrecision getYearPrecision() {
    return yearPrecision;
  }

  public void setYearPrecision(YearPrecision yearPrecision) {
    this.yearPrecision = yearPrecision;
  }

  /**
   * Switches the values of the day and month.
   */
  public void switchDayWithMonth() {
    if (day != null) {
      int tempDay = day;
      setDay(month);
      setMonth(tempDay);
    }
  }

  public static EDTFDatePart getUnknownInstance() {
    final EDTFDatePart edtfDatePart = new EDTFDatePart();
    edtfDatePart.setUnknown(true);
    return edtfDatePart;
  }

  public static EDTFDatePart getUnspecifiedInstance() {
    final EDTFDatePart edtfDatePart = new EDTFDatePart();
    edtfDatePart.setUnspecified(true);
    return edtfDatePart;
  }

}
