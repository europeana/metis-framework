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
public final class InstantEdtfDate extends AbstractEdtfDate implements Comparable<InstantEdtfDate> {

  private static final Logger LOGGER = LoggerFactory.getLogger(InstantEdtfDate.class);
  public static final int THRESHOLD_4_DIGITS_YEAR = 9999;

  private YearPrecision yearPrecision;
  private Year yearObj;
  private YearMonth yearMonthObj;
  private LocalDate yearMonthDayObj;

  private DateQualification dateQualification = DateQualification.EMPTY;
  private DateEdgeType dateEdgeType = DateEdgeType.DECLARED;

  public Year getYear() {
    return yearObj;
  }

  public YearMonth getYearMonth() {
    return yearMonthObj;
  }

  public LocalDate getYearMonthDay() {
    return yearMonthDayObj;
  }

  public YearPrecision getYearPrecision() {
    return yearPrecision;
  }

  public void setDateQualification(DateQualification dateQualification) {
    this.dateQualification = dateQualification;
  }

  public DateQualification getDateQualification() {
    return dateQualification;
  }

  @Override
  public boolean isYearPrecision() {
    return yearPrecision != null;
  }

  private InstantEdtfDate(EdtfDatePartBuilder edtfDatePartBuilder) {
    this.yearPrecision = edtfDatePartBuilder.yearPrecision;
    yearObj = edtfDatePartBuilder.yearObj;
    yearMonthObj = edtfDatePartBuilder.yearMonthObj;
    yearMonthDayObj = edtfDatePartBuilder.yearMonthDayObj;
    dateQualification = edtfDatePartBuilder.dateQualification;
  }

  private InstantEdtfDate(DateEdgeType dateEdgeType) {
    this.dateEdgeType = dateEdgeType;
  }

  @Override
  public InstantEdtfDate getFirstDay() {
    InstantEdtfDate firstDay = null;
    if (dateEdgeType == DateEdgeType.DECLARED) {
      // TODO: 25/07/2022 What about > THRESHOLD_4_DIGITS_YEAR??
      //The part where > THRESHOLD_4_DIGITS_YEAR is not possible because it's in the future, so we don't have to check it.
      //Verify though that the contents of this class are always considered valid before the call of this method.
      if (this.getYear().getValue() < -THRESHOLD_4_DIGITS_YEAR) {
        firstDay = new InstantEdtfDate.EdtfDatePartBuilder(this.getYear().getValue()).build(false);
      } else {
        firstDay = this.firstDayOfYearDatePart();
      }
    }

    return firstDay;
  }

  @Override
  public InstantEdtfDate getLastDay() {
    InstantEdtfDate lastDay = null;
    if (dateEdgeType == DateEdgeType.DECLARED) {
      if (this.getYear().getValue() < -THRESHOLD_4_DIGITS_YEAR) {
        lastDay = new InstantEdtfDate.EdtfDatePartBuilder(
            this.getYear().getValue()).build(false);
      } else {
        lastDay = this.lastDayOfYearDatePart();
      }
    }
    return lastDay;
  }

  public InstantEdtfDate firstDayOfYearDatePart() {
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

  public InstantEdtfDate lastDayOfYearDatePart() {
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

  @Override
  public boolean isOpen() {
    return dateEdgeType == DateEdgeType.OPEN;
  }

  public DateEdgeType getDateEdgeType() {
    return dateEdgeType;
  }

  public static InstantEdtfDate getUnknownInstance() {
    return new InstantEdtfDate(DateEdgeType.UNKNOWN);
  }

  public static InstantEdtfDate getOpenInstance() {
    return new InstantEdtfDate(DateEdgeType.OPEN);
  }

  public Integer getCentury() {
    int century = (yearObj.getValue() / YearPrecision.CENTURY.getDuration()) + 1;
    if (yearPrecision == null) {
      century += 1;
    }
    return century;
  }

  @Override
  public String toString() {
    final StringBuilder stringBuilder = new StringBuilder();
    // TODO: 10/02/2023 Can we also use Temporal here?
    if (dateEdgeType == DateEdgeType.OPEN || dateEdgeType == DateEdgeType.UNKNOWN) {
      stringBuilder.append("..");
    } else if (yearObj.getValue() < -THRESHOLD_4_DIGITS_YEAR || yearObj.getValue() > THRESHOLD_4_DIGITS_YEAR) {
      stringBuilder.append("Y").append(yearObj.getValue());
    } else {
      stringBuilder.append(serializeYear());

      //Append Month and day
      final DecimalFormat decimalFormat = new DecimalFormat("00");
      if (yearMonthObj != null) {
        stringBuilder.append("-").append(decimalFormat.format(yearMonthObj.getMonthValue()));
        if (yearMonthDayObj != null) {
          stringBuilder.append("-").append(decimalFormat.format(yearMonthDayObj.getDayOfMonth()));
        }
      }
    }
    if (dateQualification != null && dateQualification != DateQualification.EMPTY) {
      stringBuilder.append(dateQualification.getCharacter());
    }
    return stringBuilder.toString();
  }

  @Override
  public int compareTo(InstantEdtfDate other) {
    int comparatorValue = this.yearObj.compareTo(other.yearObj);
    if (comparatorValue == 0 && this.yearMonthObj != null && other.yearMonthObj != null) {
      comparatorValue = this.yearMonthObj.compareTo(other.yearMonthObj);
      if (comparatorValue == 0 && this.yearMonthDayObj != null && other.yearMonthDayObj != null) {
        comparatorValue = this.yearMonthDayObj.compareTo(other.yearMonthDayObj);
      }
    }
    return comparatorValue;
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
    final String paddedYear = decimalFormat.format(Math.abs(yearObj.getValue()));

    final String prefix = yearObj.getValue() < 0 ? "-" : "";
    final String yearAdjusted;
    if (yearPrecision == null) {
      yearAdjusted = paddedYear;
    } else {
      final int trailingZeros = Integer.numberOfTrailingZeros(yearPrecision.getDuration());
      yearAdjusted = paddedYear.substring(0, 4 - trailingZeros) + "X".repeat(trailingZeros);
    }
    return prefix + yearAdjusted;
  }

  /**
   * Builder class for {@link InstantEdtfDate}.
   * <p>During {@link #build()} it will verify all the parameters that have been requested.
   * The {@link #build(boolean)} will also attempt a second time by switching month and day values if the original value were
   * invalid. Furthermore if the constructor {@link EdtfDatePartBuilder#EdtfDatePartBuilder(TemporalAccessor)} is used, it will
   * overwrite any previous values added with the {@code .with} prefixed methods.</p>
   */
  public static class EdtfDatePartBuilder {

    private Year yearObj;
    private YearMonth yearMonthObj;
    private LocalDate yearMonthDayObj;
    private Integer year;
    private Integer month;
    private Integer day;
    private YearPrecision yearPrecision;
    private TemporalAccessor temporalAccessor;

    private DateQualification dateQualification = DateQualification.EMPTY;

    public EdtfDatePartBuilder(InstantEdtfDate instantEdtfDate) throws DateTimeException {
      yearPrecision = instantEdtfDate.yearPrecision;
      yearObj = instantEdtfDate.yearObj;
      yearMonthObj = instantEdtfDate.yearMonthObj;
      yearMonthDayObj = instantEdtfDate.yearMonthDayObj;
      dateQualification = instantEdtfDate.dateQualification;
    }

    public EdtfDatePartBuilder(TemporalAccessor temporalAccessor) throws DateTimeException {
      this.temporalAccessor = temporalAccessor;
    }

    public EdtfDatePartBuilder(final Integer year) {
      this.year = year;
    }

    public InstantEdtfDate build(boolean allowSwitchMonthDay) throws DateTimeException {
      InstantEdtfDate instantEdtfDate;
      try {
        instantEdtfDate = build();
      } catch (DateTimeException e) {
        LOGGER.debug("Year-Month-Day failed. Trying switching Month and Day", e);
        if (allowSwitchMonthDay) {
          //Retry with switching month and day
          swapMonthDay();
          parseMonthDay();
          instantEdtfDate = new InstantEdtfDate(this);
        } else {
          throw e;
        }
      }
      return instantEdtfDate;
    }

    private InstantEdtfDate build() throws DateTimeException {

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
      //Try initialization only if it is not a copy object
      if (yearObj == null) {
        Objects.requireNonNull(year, "Year value can never be null");
        yearObj = Year.of(year);
        parseMonthDay();
      }
      return new InstantEdtfDate(this);
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

    public EdtfDatePartBuilder withDateQualification(DateQualification dateQualification) {
      this.dateQualification = dateQualification;
      return this;
    }
  }
}
