package eu.europeana.normalization.dates.edtf;

import eu.europeana.normalization.dates.YearPrecision;
import java.text.DecimalFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class representing the date part an EDTF date.
 * <p>
 * Support partial dates, including only centuries or decades (e.g., 19XX). The uncertain and approximate qualifiers, '?' and '~',
 * when applied together, are combined into a single qualifier character '%';
 * </p>
 */
public class EdtfDatePart {

  private static final Logger LOGGER = LoggerFactory.getLogger(EdtfDatePart.class);

  public static final int THRESHOLD_4_DIGITS_YEAR = 9999;

  private Year yearObj;
  private YearMonth yearMonthObj;
  private LocalDate yearMonthDayObj;

  private boolean uncertain;
  private boolean approximate;

  /**
   * Indicates whether the date is unknown (e.g. if the input EDTF-compliant date interval string was equal to
   * '<code>1900/?</code>').
   */
  private boolean unknown;

  /**
   * Indicates whether the date is unspecified (e.g. if the input EDTF-compliant date interval string was equal to
   * '<code>1900/</code>').
   */
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
    if (day == null || day == 0) {
    } else {
      this.day = day;
    }
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

  public EdtfDatePart() {
  }

  public EdtfDatePart(EdtfDatePartBuilder edtfDatePartBuilder) {
    this.yearPrecision = edtfDatePartBuilder.yearPrecision;

    yearObj = edtfDatePartBuilder.yearObj;
    yearMonthObj = edtfDatePartBuilder.yearMonthObj;
    yearMonthDayObj = edtfDatePartBuilder.yearMonthDayObj;
    this.year = yearObj.getValue();
    this.month = yearMonthObj == null ? null : yearMonthObj.getMonthValue();
    this.day = yearMonthDayObj == null ? null : yearMonthDayObj.getDayOfMonth();

  }

  public EdtfDatePart firstDayOfYearDatePart() {
    final TemporalAccessor temporalAccessorFirstDay;
    if (yearMonthDayObj != null) {
      temporalAccessorFirstDay = yearMonthDayObj.with(TemporalAdjusters.firstDayOfYear());
    } else if (yearMonthObj != null) {
      temporalAccessorFirstDay = yearMonthObj.atDay(1);
    } else {
      // TODO: 13/02/2023 Check with Nuno why when it's CENTURY precision we add a year?
      temporalAccessorFirstDay = yearObj
          .plusYears(yearPrecision == YearPrecision.CENTURY ? 1 : 0)
          .atMonthDay(MonthDay.of(1, 1));
    }
    return new EdtfDatePartBuilder(temporalAccessorFirstDay).build();
  }

  public EdtfDatePart lastDayOfYearDatePart() {
    final TemporalAccessor temporalAccessorLastDay;
    if (yearMonthDayObj != null) {
      temporalAccessorLastDay = yearMonthDayObj.with(TemporalAdjusters.lastDayOfYear());
    } else if (yearMonthObj != null) {
      temporalAccessorLastDay = yearMonthObj.atEndOfMonth();
    } else {
      // TODO: 13/02/2023 Check with Nuno the year additions.
      if (yearPrecision == YearPrecision.CENTURY) {
        temporalAccessorLastDay = yearObj.plusYears(100).atMonthDay(MonthDay.of(Month.DECEMBER, 31));
      } else if (yearPrecision == YearPrecision.DECADE) {
        temporalAccessorLastDay = yearObj.plusYears(9).atMonthDay(MonthDay.of(Month.DECEMBER, 31));
      } else {
        temporalAccessorLastDay = yearObj.atMonthDay(MonthDay.of(Month.DECEMBER, 31));
      }
    }
    return new EdtfDatePartBuilder(temporalAccessorLastDay).build();

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
    // TODO: 10/02/2023 Can we also use Temporal here?
    if (unknown || unspecified) {
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
    }
    // TODO: 10/02/2023 Perhaps those should be centralized somehow
    //Append approximate/uncertain
    if (approximate && uncertain) {
      stringBuilder.append("%");
    } else if (approximate) {
      stringBuilder.append("~");
    } else if (uncertain) {
      stringBuilder.append("?");
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

  public static class EdtfDatePartBuilder {

    private Year yearObj;
    private YearMonth yearMonthObj;
    private LocalDate yearMonthDayObj;
    private Integer year;
    private Integer month;
    private Integer day;
    private YearPrecision yearPrecision;
    private TemporalAccessor temporalAccessor;

    public EdtfDatePartBuilder(EdtfDatePart edtfDatePart) throws DateTimeException {
      yearPrecision = edtfDatePart.yearPrecision;
      year = edtfDatePart.year;
      month = edtfDatePart.month;
      day = edtfDatePart.day;
    }

    public EdtfDatePartBuilder(TemporalAccessor temporalAccessor) throws DateTimeException {
      this.temporalAccessor = temporalAccessor;
    }

    public EdtfDatePartBuilder(final Integer year) {
      this.year = year;
    }

    public EdtfDatePart build(boolean allowSwitchMonthDay) throws DateTimeException {
      EdtfDatePart edtfDatePart;
      try {
        edtfDatePart = build();
      } catch (DateTimeException e) {
        LOGGER.debug("Year-Month-Day failed. Trying switching Month and Day", e);
        if (allowSwitchMonthDay) {
          //Retry with switching month and day
          swapMonthDay();
          parseMonthDay();
          edtfDatePart = new EdtfDatePart(this);
        } else {
          throw e;
        }
      }
      return edtfDatePart;
    }

    private EdtfDatePart build() throws DateTimeException {

      if (temporalAccessor != null) {
        LOGGER.debug("TemporalAccessor present. Overwriting values.");
        // TODO: 13/02/2023 Check TemporalQuery alternative option
        day = temporalAccessor.isSupported(ChronoField.DAY_OF_MONTH) ?
            temporalAccessor.get(ChronoField.DAY_OF_MONTH) : null;
        month = temporalAccessor.isSupported(ChronoField.MONTH_OF_YEAR) ?
            temporalAccessor.get(ChronoField.MONTH_OF_YEAR) : null;
        year = temporalAccessor.isSupported(ChronoField.YEAR) ?
            temporalAccessor.get(ChronoField.YEAR) : null;
      }

      Objects.requireNonNull(year, "Year value can never be null");
      yearObj = Year.of(year);
      parseMonthDay();
      return new EdtfDatePart(this);
    }

    private void swapMonthDay() {
      Integer tempMonth = month;
      month = day;
      day = tempMonth;
    }

    private void parseMonthDay() {
      if (month != null && month >= 1) {
        yearMonthObj = YearMonth.of(yearObj.getValue(), month);
        if (day != null && day >= 1) {
          yearMonthDayObj = LocalDate.of(yearMonthObj.getYear(), yearMonthObj.getMonth(), day);
        }
      }
    }

    public EdtfDatePartBuilder withMonth(int month) {
      this.month = month;
      return this;
    }

    public EdtfDatePartBuilder withDay(int day) {

      this.day = day;
      return this;
    }

    public EdtfDatePartBuilder withYearPrecision(YearPrecision yearPrecision) {
      this.yearPrecision = yearPrecision;
      return this;
    }
  }

}
