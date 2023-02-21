package eu.europeana.normalization.dates.edtf;

import eu.europeana.normalization.dates.YearPrecision;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder class for {@link InstantEdtfDate}.
 * <p>During {@link #build()} it will verify all the parameters that have been requested.
 * The {@link #build()}, if {@link #withAllowSwitchMonthDay(boolean)} was called with {@code true}, will also attempt a second
 * time by switching month and day values if the original values were invalid. Furthermore, there are a set of constructors that
 * can start the builder and will perform a build with specific characteristics:
 * <ul>
 *   <li>{@link InstantEdtfDateBuilder#InstantEdtfDateBuilder(Integer)} which initializes the builder with the minimum requirement of year value.</li>
 *   <li>{@link InstantEdtfDateBuilder#InstantEdtfDateBuilder(TemporalAccessor)} can be used instead to pass multiple values through a {@link TemporalAccessor}.
 *   This object during build will overwrite the date parts, if any <@code>.with</@code> methods were called, from the {@link TemporalAccessor}</li>
 * </ul>
 */
public class InstantEdtfDateBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(InstantEdtfDateBuilder.class);
  private Year yearObj;
  private YearMonth yearMonthObj;
  private LocalDate yearMonthDayObj;
  private Integer year;
  private Integer month;
  private Integer day;
  private YearPrecision yearPrecision;
  private TemporalAccessor temporalAccessor;

  private DateQualification dateQualification = DateQualification.NO_QUALIFICATION;
  private boolean allowSwitchMonthDay = false;

  /**
   * Constructor which initializes the builder with the minimum requirement of year value.
   *
   * @param year the year value
   */
  public InstantEdtfDateBuilder(final Integer year) {
    this.year = year;
  }

  /**
   * Constructor with {@link TemporalAccessor}.
   * <p>This object during build will overwrite the date parts, if any <@code>.with</@code> methods were called, from the
   * {@link TemporalAccessor}
   * </p>
   *
   * @param temporalAccessor the temporal accessor
   */
  public InstantEdtfDateBuilder(TemporalAccessor temporalAccessor) {
    this.temporalAccessor = temporalAccessor;
  }

  /**
   * Returns an instance of {@link InstantEdtfDate} created and validated from the fields set on this builder.
   *
   * @return the new instant edtf date
   * @throws DateTimeException if something went wrong during date validation
   */
  public InstantEdtfDate build() throws DateTimeException {
    InstantEdtfDate instantEdtfDate;
    try {
      instantEdtfDate = buildInternal();
    } catch (DateTimeException e) {
      LOGGER.debug("Year-Month-Day failed. Trying switching Month and Day", e);
      if (allowSwitchMonthDay && month != null && month >= 1 && day != null && day >= 1) {
        //Retry with swapping month and day
        swapMonthDay();
        parseMonthDay();
        instantEdtfDate = new InstantEdtfDate(this);
      } else {
        throw e;
      }
    }
    return instantEdtfDate;
  }

  private InstantEdtfDate buildInternal() throws DateTimeException {
    if (temporalAccessor != null) {
      LOGGER.debug("TemporalAccessor present. Overwriting values.");
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

  public InstantEdtfDateBuilder withMonth(int month) {
    this.month = month;
    return this;
  }

  public InstantEdtfDateBuilder withDay(int day) {
    this.day = day;
    return this;
  }

  public InstantEdtfDateBuilder withYearPrecision(YearPrecision yearPrecision) {
    this.yearPrecision = yearPrecision;
    return this;
  }

  public InstantEdtfDateBuilder withDateQualification(DateQualification dateQualification) {
    this.dateQualification = dateQualification;
    return this;
  }

  public InstantEdtfDateBuilder withAllowSwitchMonthDay(boolean allowSwitchMonthDay) {
    this.allowSwitchMonthDay = allowSwitchMonthDay;
    return this;
  }

  public Year getYearObj() {
    return yearObj;
  }

  public YearMonth getYearMonthObj() {
    return yearMonthObj;
  }

  public LocalDate getYearMonthDayObj() {
    return yearMonthDayObj;
  }

  public YearPrecision getYearPrecision() {
    return yearPrecision;
  }

  public DateQualification getDateQualification() {
    return dateQualification;
  }
}
