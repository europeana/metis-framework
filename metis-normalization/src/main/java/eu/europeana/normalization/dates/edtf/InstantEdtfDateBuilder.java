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
 * The {@link #build(boolean)} will also attempt a second time by switching month and day values if the original value were
 * invalid. Furthermore if the constructor {@link InstantEdtfDateBuilder#InstantEdtfDateBuilder(TemporalAccessor)} is used, it
 * will overwrite any previous values added with the {@code .with} prefixed methods.</p>
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

  private DateQualification dateQualification = DateQualification.EMPTY;

  public InstantEdtfDateBuilder(InstantEdtfDate instantEdtfDate) throws DateTimeException {
    yearPrecision = instantEdtfDate.getYearPrecision();
    yearObj = instantEdtfDate.getYear();
    yearMonthObj = instantEdtfDate.getYearMonth();
    yearMonthDayObj = instantEdtfDate.getYearMonthDay();
    dateQualification = instantEdtfDate.getDateQualification();
  }

  public InstantEdtfDateBuilder(TemporalAccessor temporalAccessor) throws DateTimeException {
    this.temporalAccessor = temporalAccessor;
  }

  public InstantEdtfDateBuilder(final Integer year) {
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
