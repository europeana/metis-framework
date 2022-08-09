package eu.europeana.normalization.dates.edtf;

import eu.europeana.normalization.dates.YearPrecision;
import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * Class representing the date part an EDTF date.
 * <p>
 * Support partial dates, including only centuries or decades (e.g., 19XX)
 * </p>
 */
public class EdtfDatePart implements Serializable {

  private static final long serialVersionUID = -7497880706682687923L;
  public static final int THRESHOLD_4_DIGITS_YEAR = 9999;

  private boolean uncertain;
  private boolean approximate;
  private boolean unknown;
  // TODO: 25/07/2022 What is unspecified? It's not in the documentation, is it the same as unknown?
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
  public void switchDayAndMonth() {
    if (day != null) {
      int tempDay = day;
      setDay(month);
      setMonth(tempDay);
    }
  }

  public static EdtfDatePart getUnknownInstance() {
    final EdtfDatePart edtfDatePart = new EdtfDatePart();
    edtfDatePart.setUnknown(true);
    return edtfDatePart;
  }

  public static EdtfDatePart getUnspecifiedInstance() {
    final EdtfDatePart edtfDatePart = new EdtfDatePart();
    edtfDatePart.setUnspecified(true);
    return edtfDatePart;
  }

  @Override
  public String toString() {
    final StringBuilder stringBuilder = new StringBuilder();
    if (unspecified) {
      stringBuilder.append("..");
    } else if (year < -THRESHOLD_4_DIGITS_YEAR || year > THRESHOLD_4_DIGITS_YEAR) {
      stringBuilder.append("Y").append(year);
    } else {
      stringBuilder.append(serializeYear());

      //Append Month and day
      final DecimalFormat decimalFormat = new DecimalFormat("00");
      if (month != null && month > 0) {
        stringBuilder.append("-").append(decimalFormat.format(month));
        if (day != null && day > 0) {
          stringBuilder.append("-").append(decimalFormat.format(day));
        }
      }
      //Append approximate/uncertain
      if (approximate && uncertain) {
        stringBuilder.append("%");
      } else if (approximate) {
        stringBuilder.append("~");
      } else if (uncertain) {
        stringBuilder.append("?");
      }
    }
    return stringBuilder.toString();
  }

  /**
   * Adjusts a year with padding and optional precision that replace right most digits with 'X's.
   * <p>
   * There are two possibilities:
   *   <ul>
   *     <li>The year is precise therefore it will be left padded with 0 to the max of 4 digits in total</li>
   *     <li>The year is not precise which will be left padded with 0 to the max of 4 digits in total and then the right most
   *     digits are replaces with 'X's based on the year precision. Eg. a year -900 with century precision will become -09XX</li>
   *   </ul>
   * </p>
   *
   * @return the adjusted year
   */
  private String serializeYear() {
    final DecimalFormat decimalFormat = new DecimalFormat("0000");
    final String paddedYear = decimalFormat.format(Math.abs(year));

    final String prefix = year < 0 ? "-" : "";
    final String yearAdjusted;
    if (yearPrecision == null) {
      yearAdjusted = paddedYear;
    } else {
      final int trailingZeros = Integer.numberOfTrailingZeros(yearPrecision.getDuration());
      yearAdjusted = paddedYear.substring(0, 4 - trailingZeros) + "X".repeat(trailingZeros);
    }
    return prefix + yearAdjusted;
  }
}
