package eu.europeana.normalization.dates.edtf;

import static eu.europeana.normalization.dates.edtf.DateBoundaryType.DECLARED;
import static eu.europeana.normalization.dates.edtf.DateBoundaryType.OPEN;
import static eu.europeana.normalization.dates.edtf.DateBoundaryType.UNKNOWN;
import static eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder.THRESHOLD_4_DIGITS_YEAR;
import static eu.europeana.normalization.dates.edtf.Iso8601Parser.ISO_8601_MINIMUM_YEAR_DIGITS;
import static java.lang.Math.abs;
import static java.util.Optional.ofNullable;

import eu.europeana.normalization.dates.YearPrecision;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import java.lang.invoke.MethodHandles;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.TemporalAccessor;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class representing the date part of an EDTF date.
 * <p>
 * Support partial dates, including only centuries or decades (e.g., 19XX). The uncertain and approximate qualifiers, '?' and '~',
 * when applied together, are combined into a single qualifier character '%';
 * </p>
 */
public final class InstantEdtfDate extends AbstractEdtfDate implements Comparable<InstantEdtfDate> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private Year year;
  private Month month;
  private LocalDate yearMonthDay;
  private YearPrecision yearPrecision;
  private Set<DateQualification> dateQualifications = EnumSet.noneOf(DateQualification.class);
  private DateBoundaryType dateBoundaryType = DECLARED;

  /**
   * Restricted constructor by provided {@link InstantEdtfDateBuilder}.
   * <p>All fields apart from {@link #dateQualifications} are strictly contained in the constructor. The date qualifications can
   * be further extended to, for example, add an approximate qualification for a date that was sanitized.</p>
   *
   * @param instantEdtfDateBuilder the builder with all content verified
   */
  InstantEdtfDate(InstantEdtfDateBuilder instantEdtfDateBuilder) {
    yearPrecision = instantEdtfDateBuilder.getYearPrecision();
    year = instantEdtfDateBuilder.getYearObj();
    month = instantEdtfDateBuilder.getMonthObj();
    yearMonthDay = instantEdtfDateBuilder.getYearMonthDayObj();
    dateQualifications = instantEdtfDateBuilder.getDateQualifications();
  }

  private InstantEdtfDate(DateBoundaryType dateBoundaryType) {
    this.dateBoundaryType = dateBoundaryType;
  }

  @Override
  public void addQualification(DateQualification dateQualification) {
    this.dateQualifications.add(dateQualification);
  }

  /**
   * Create an {@link DateBoundaryType#UNKNOWN} instant.
   *
   * @return the instant date created
   */
  public static InstantEdtfDate getUnknownInstance() {
    return new InstantEdtfDate(UNKNOWN);
  }

  /**
   * Create an {@link DateBoundaryType#OPEN} instant.
   *
   * @return the instant date created
   */
  public static InstantEdtfDate getOpenInstance() {
    return new InstantEdtfDate(OPEN);
  }

  @Override
  public InstantEdtfDate getFirstDay() {
    InstantEdtfDate firstDay = null;
    try {
      if (dateBoundaryType == DECLARED) {
        if (this.getYear().getValue() < -THRESHOLD_4_DIGITS_YEAR) {
          firstDay = new InstantEdtfDateBuilder(this.getYear().getValue()).build();
        } else {
          firstDay = this.firstDayOfYearDatePart();
        }
      }
    } catch (DateExtractionException e) {
      LOGGER.error("Creating first day of instant failed!", e);
    }

    return firstDay;
  }

  /**
   * Get the date that correspond to the first day of the year.
   * <p>
   *   <ul>
   *     <li>For full dates e.g. 1989-11-01 it is identical 1989-11-01</li>
   *     <li>For dates without day e.g. 1989-11 it is 1989-11-01</li>
   *     <li>For dates with only year e.g. 1989 it is 1989-01-01</li>
   *     <li>For dates with only year and precision {@link YearPrecision#CENTURY} e.g. 1900 it is 1901-01-01</li>
   *     <li>For dates with only year and precision {@link YearPrecision#DECADE} e.g. 1980 it is 1980-01-01</li>
   *   </ul>
   *   Century example range explained <a href="https://en.wikipedia.org/wiki/20th_century>20th_century</a>
   * </p>
   *
   * @return the date instance corresponding to the first day of the year for this date
   * @throws DateExtractionException if creation of the date failed
   */
  private InstantEdtfDate firstDayOfYearDatePart() throws DateExtractionException {
    final TemporalAccessor temporalAccessorFirstDay;
    if (yearMonthDay != null) {
      temporalAccessorFirstDay = yearMonthDay;
    } else if (month != null) {
      temporalAccessorFirstDay = YearMonth.of(year.getValue(), month).atDay(1);
    } else {
      final MonthDay january01 = MonthDay.of(Month.JANUARY, 1);
      temporalAccessorFirstDay = year.plusYears(yearPrecision == YearPrecision.CENTURY ? 1 : 0).atMonthDay(january01);
    }

    return new InstantEdtfDateBuilder(temporalAccessorFirstDay).build();
  }

  @Override
  public InstantEdtfDate getLastDay() {
    InstantEdtfDate lastDay = null;
    try {
      if (dateBoundaryType == DECLARED) {
        if (this.getYear().getValue() < -THRESHOLD_4_DIGITS_YEAR) {
          lastDay = new InstantEdtfDateBuilder(this.getYear().getValue()).build();
        } else {
          lastDay = this.lastDayOfYearDatePart();
        }
      }
    } catch (DateExtractionException e) {
      LOGGER.error("Creating last day of instant failed!", e);
    }
    return lastDay;
  }

  /**
   * Get the date that correspond to the last day of the year.
   * <p>
   *   <ul>
   *     <li>For full dates e.g. 1989-11-01 it is identical 1989-11-01</li>
   *     <li>For dates without day e.g. 1989-11 it is 1989-11-30</li>
   *     <li>For dates with only year e.g. 1989 it is 1989-12-31</li>
   *     <li>For dates with only year and precision {@link YearPrecision#CENTURY} e.g. 1900 it is 2000-12-31</li>
   *     <li>For dates with only year and precision {@link YearPrecision#DECADE} e.g. 1980 it is 1989-12-31</li>
   *   </ul>
   *   Century example range explained <a href="https://en.wikipedia.org/wiki/20th_century>20th_century</a>
   * </p>
   *
   * @return the date instance corresponding to the last day of the year for this date
   * @throws DateExtractionException if creation of the date failed
   */
  private InstantEdtfDate lastDayOfYearDatePart() throws DateExtractionException {
    final TemporalAccessor temporalAccessorLastDay;
    if (yearMonthDay != null) {
      temporalAccessorLastDay = yearMonthDay;
    } else if (month != null) {
      temporalAccessorLastDay = YearMonth.of(year.getValue(), month).atEndOfMonth();
    } else {
      final Year adjustedYear;
      if (yearPrecision == YearPrecision.CENTURY) {
        adjustedYear = year.plusYears(yearPrecision.getDuration());
      } else if (yearPrecision == YearPrecision.DECADE) {
        adjustedYear = year.plusYears(yearPrecision.getDuration() - 1L);
      } else {
        adjustedYear = year;
      }
      final MonthDay december31 = MonthDay.of(Month.DECEMBER, Month.DECEMBER.maxLength());
      temporalAccessorLastDay = adjustedYear.atMonthDay(december31);
    }
    return new InstantEdtfDateBuilder(temporalAccessorLastDay).build();

  }

  @Override
  public boolean isOpen() {
    return dateBoundaryType == OPEN;
  }

  public Integer getCentury() {
    int centuryDivision = year.getValue() / YearPrecision.CENTURY.getDuration();
    int centuryModulo = year.getValue() % YearPrecision.CENTURY.getDuration();
    //For case 1900 it is 19th. For case 1901 it is 20th century
    return (centuryModulo == 0) ? centuryDivision : (centuryDivision + 1);
  }

  /**
   * Adjusts a year with padding and optional precision that replace right most digits with 'X's.
   * <p>
   * There are two possibilities:
   *   <ul>
   *     <li>The year is precise therefore it will be left padded with 0 to the max of 4 digits in total</li>
   *     <li>The year is not precise which will be left padded with 0 to the max of 4 digits in total and then the right most
   *     digits are replaced with 'X's based on the year precision. Eg. a year -900 with century precision will become -09XX</li>
   *   </ul>
   * </p>
   *
   * @return the adjusted year
   */
  private String serializeYear() {
    final DecimalFormat decimalFormat = new DecimalFormat("0000");
    final String paddedYear = decimalFormat.format(Math.abs(year.getValue()));

    final String prefix = year.getValue() < 0 ? "-" : "";
    final int trailingZeros = Integer.numberOfTrailingZeros(yearPrecision.getDuration());
    final String yearAdjusted = paddedYear.substring(0, ISO_8601_MINIMUM_YEAR_DIGITS - trailingZeros) + "X".repeat(trailingZeros);
    return prefix + yearAdjusted;
  }

  @Override
  public String toString() {
    final StringBuilder stringBuilder = new StringBuilder();
    if (dateBoundaryType != DECLARED) {
      stringBuilder.append(dateBoundaryType.getSerializedRepresentation());
    } else if (abs(year.getValue()) > THRESHOLD_4_DIGITS_YEAR) {
      stringBuilder.append(InstantEdtfDateBuilder.OVER_4_DIGITS_YEAR_PREFIX).append(year.getValue());
    } else {
      stringBuilder.append(serializeYear());

      final DecimalFormat decimalFormat = new DecimalFormat("00");
      stringBuilder.append(
          ofNullable(month).map(Month::getValue).map(decimalFormat::format).map(m -> "-" + m).orElse(""));
      stringBuilder.append(
          ofNullable(yearMonthDay).map(LocalDate::getDayOfMonth).map(decimalFormat::format).map(d -> "-" + d).orElse(""));
    }
    stringBuilder.append(DateQualification.getCharacterFromQualifications(dateQualifications));
    return stringBuilder.toString();
  }

  @Override
  public int compareTo(InstantEdtfDate other) {
    int comparatorValue = this.year.compareTo(other.year);
    if (comparatorValue == 0 && this.month != null && other.month != null) {
      comparatorValue = this.month.compareTo(other.month);
      if (comparatorValue == 0 && this.yearMonthDay != null && other.yearMonthDay != null) {
        comparatorValue = this.yearMonthDay.compareTo(other.yearMonthDay);
      }
    }
    return comparatorValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InstantEdtfDate that = (InstantEdtfDate) o;
    return yearPrecision == that.yearPrecision && Objects.equals(year, that.year) && Objects.equals(month,
        that.month) && Objects.equals(yearMonthDay, that.yearMonthDay) && dateQualifications == that.dateQualifications
        && dateBoundaryType == that.dateBoundaryType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(yearPrecision, year, month, yearMonthDay, dateQualifications, dateBoundaryType);
  }

  public Year getYear() {
    return year;
  }

  public Month getMonth() {
    return month;
  }

  public LocalDate getYearMonthDay() {
    return yearMonthDay;
  }

  public YearPrecision getYearPrecision() {
    return yearPrecision;
  }

  public Set<DateQualification> getDateQualifications() {
    return EnumSet.copyOf(dateQualifications);
  }

  public DateBoundaryType getDateBoundaryType() {
    return dateBoundaryType;
  }
}
