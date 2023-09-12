package eu.europeana.normalization.dates.edtf;

import static eu.europeana.normalization.dates.edtf.DateQualification.NO_QUALIFICATION;
import static java.lang.String.format;

import eu.europeana.normalization.dates.YearPrecision;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import java.lang.invoke.MethodHandles;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;
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
 * The {@link #build()}, if {@link #withFlexibleDateBuild(boolean)} was called with {@code true}, will also attempt a second time
 * by switching month and day values if the original values were invalid. Furthermore, there are a set of constructors that can
 * start the builder and will perform a build with specific characteristics:
 * <ul>
 *   <li>{@link InstantEdtfDateBuilder#InstantEdtfDateBuilder(Integer)} which initializes the builder with the minimum requirement of year value.</li>
 *   <li>{@link InstantEdtfDateBuilder#InstantEdtfDateBuilder(TemporalAccessor)} can be used instead to pass multiple values through a {@link TemporalAccessor}.
 *   This object during build will overwrite the date parts, if any <@code>.with</@code> methods were called, from the {@link TemporalAccessor}</li>
 * </ul>
 */
public class InstantEdtfDateBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  public static final int THRESHOLD_4_DIGITS_YEAR = 9999;
  public static final char OVER_4_DIGITS_YEAR_PREFIX = 'Y';
  private Year yearObj;
  private Month monthObj;
  private LocalDate yearMonthDayObj;
  private Integer year;
  private Integer month;
  private Integer day;
  private YearPrecision yearPrecision = YearPrecision.YEAR;
  private DateQualification dateQualification = NO_QUALIFICATION;
  private boolean flexibleDateBuild = true;
  private boolean isLongYear = false;

  /**
   * Constructor copying values from a {@link DateBoundaryType#DECLARED} dates.
   * <p>For non DECLARED dates this has no effect.</p>
   *
   * @param instantEdtfDate the instant edtf date
   */
  public InstantEdtfDateBuilder(InstantEdtfDate instantEdtfDate) {
    if (instantEdtfDate.getDateBoundaryType() == DateBoundaryType.DECLARED) {
      yearPrecision = instantEdtfDate.getYearPrecision();
      year = instantEdtfDate.getYear().getValue() / yearPrecision.getDuration();
      if (instantEdtfDate.getMonth() != null) {
        month = instantEdtfDate.getMonth().getValue();
      }
      if (instantEdtfDate.getYearMonthDay() != null) {
        day = instantEdtfDate.getYearMonthDay().getDayOfMonth();
      }
      dateQualification = instantEdtfDate.getDateQualification();
    }
  }

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
    day = temporalAccessor.isSupported(ChronoField.DAY_OF_MONTH) ?
        temporalAccessor.get(ChronoField.DAY_OF_MONTH) : null;
    month = temporalAccessor.isSupported(ChronoField.MONTH_OF_YEAR) ?
        temporalAccessor.get(ChronoField.MONTH_OF_YEAR) : null;
    year = temporalAccessor.isSupported(ChronoField.YEAR) ?
        temporalAccessor.get(ChronoField.YEAR) : null;
  }

  /**
   * Returns an instance of {@link InstantEdtfDate} created and validated from the fields set on this builder.
   *
   * @return the new instant edtf date
   * @throws DateExtractionException if something went wrong during date validation
   */
  public InstantEdtfDate build() throws DateExtractionException {
    InstantEdtfDate instantEdtfDate = buildInternal();
    //Try once more if flexible date
    if (instantEdtfDate == null && isPositive(month) && isPositive(day) && flexibleDateBuild) {
      swapMonthDay();
      instantEdtfDate = buildInternal();
    }

    //Still nothing, we are done.
    if (instantEdtfDate == null) {
      throw new DateExtractionException("Could not instantiate date");
    }
    return instantEdtfDate;
  }

  private InstantEdtfDate buildInternal() {
    InstantEdtfDate instantEdtfDate = null;
    try {
      parseYear();
      parseMonthDay();
      validateDateNotInFuture();
      validateStrict();
      instantEdtfDate = new InstantEdtfDate(this);
    } catch (DateTimeException | DateExtractionException e) {
      LOGGER.debug("Date build failed.", e);
    }
    return instantEdtfDate;
  }

  private void parseYear() throws DateExtractionException {
    Objects.requireNonNull(year, "Year value can never be null");
    if (isLongYear && Math.abs(year) <= THRESHOLD_4_DIGITS_YEAR) {
      throw new DateExtractionException(
          format("isLong is %s indicating that year should have absolute value greater than %s",
              true, THRESHOLD_4_DIGITS_YEAR));
    } else if (!isLongYear && Math.abs(year) > THRESHOLD_4_DIGITS_YEAR) {
      throw new DateExtractionException(
          format("Year absolute value is greater than %s, and isLong is %s", THRESHOLD_4_DIGITS_YEAR, false));
    }
    yearObj = Year.of(year * yearPrecision.getDuration());
  }

  private void parseMonthDay() throws DateExtractionException {
    try {
      if (isPositive(month)) {
        monthObj = Month.of(month);
        if (isPositive(day)) {
          yearMonthDayObj = LocalDate.of(yearObj.getValue(), monthObj.getValue(), day);
        }
      }
    } catch (DateTimeException e) {
      throw new DateExtractionException("Failed to instantiate month and day", e);
    }
  }

  private boolean isPositive(Integer value) {
    return value != null && value > 0;
  }

  private void validateDateNotInFuture() throws DateExtractionException {
    try {
      final boolean isYearMonthDayInTheFuture = yearMonthDayObj != null && yearMonthDayObj.isAfter(LocalDate.now());
      final boolean isYearMonthInTheFuture = monthObj != null && YearMonth.of(yearObj.getValue(), month).isAfter(YearMonth.now());
      final boolean isYearInTheFuture = yearObj != null && yearObj.isAfter(Year.now());

      if (isYearMonthDayInTheFuture || isYearMonthInTheFuture || isYearInTheFuture) {
        throw new DateExtractionException("Date cannot be in the future");
      }

    } catch (DateTimeException e) {
      throw new DateExtractionException("Failed to instantiate month and day", e);
    }
  }

  private void validateStrict() throws DateExtractionException {
    //If it is not a long year, and we want to be strict we further validate
    boolean notLongYearAndStrictBuild = !isLongYear && !flexibleDateBuild;
    boolean isDateNonPrecise =
        dateQualification == DateQualification.UNCERTAIN || (yearPrecision != null && yearPrecision != YearPrecision.YEAR);
    boolean notCompleteDate = monthObj == null || yearMonthDayObj == null;
    if (notLongYearAndStrictBuild && (isDateNonPrecise || notCompleteDate)) {
      throw new DateExtractionException("Date is invalid according to our strict profile!");
    }
  }

  private void swapMonthDay() {
    Integer tempMonth = month;
    month = day;
    day = tempMonth;
  }

  /**
   * Add month value.
   *
   * @param month the month value
   * @return the extended builder
   */
  public InstantEdtfDateBuilder withMonth(int month) {
    this.month = month;
    return this;
  }

  /**
   * Add day value.
   *
   * @param day the day value
   * @return the extended builder
   */
  public InstantEdtfDateBuilder withDay(int day) {
    this.day = day;
    return this;
  }

  /**
   * Add year precision.
   *
   * @param yearPrecision the year precision
   * @return the extended builder
   */
  public InstantEdtfDateBuilder withYearPrecision(YearPrecision yearPrecision) {
    this.yearPrecision = yearPrecision;
    return this;
  }

  /**
   * Add date qualification.
   *
   * @param dateQualification the date qualification
   * @return the extended builder
   */
  public InstantEdtfDateBuilder withDateQualification(DateQualification dateQualification) {
    this.dateQualification = dateQualification;
    return this;
  }

  /**
   * Opt in/out for flexible date building.
   *
   * @param flexibleDateBuild the boolean (dis|en)abling the flexibility
   * @return the extended builder
   */
  public InstantEdtfDateBuilder withFlexibleDateBuild(boolean flexibleDateBuild) {
    this.flexibleDateBuild = flexibleDateBuild;
    return this;
  }

  /**
   * Declare the date is of long year format, prefixed with 'Y'.
   *
   * @return the extended builder
   */
  public InstantEdtfDateBuilder withLongYear() {
    this.isLongYear = true;
    return this;
  }

  public Year getYearObj() {
    return yearObj;
  }

  public Month getMonthObj() {
    return monthObj;
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
