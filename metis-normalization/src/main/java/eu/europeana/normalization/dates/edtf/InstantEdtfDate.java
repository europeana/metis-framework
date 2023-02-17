package eu.europeana.normalization.dates.edtf;

import static eu.europeana.normalization.dates.edtf.DateEdgeType.OPEN;
import static eu.europeana.normalization.dates.edtf.DateEdgeType.UNKNOWN;
import static eu.europeana.normalization.dates.edtf.DateQualification.NO_QUALIFICATION;
import static eu.europeana.normalization.dates.edtf.Iso8601Parser.ISO_8601_MINIMUM_YEAR_DIGITS;
import static java.util.Optional.ofNullable;

import eu.europeana.normalization.dates.YearPrecision;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

/**
 * Class representing the date part an EDTF date.
 * <p>
 * Support partial dates, including only centuries or decades (e.g., 19XX). The uncertain and approximate qualifiers, '?' and '~',
 * when applied together, are combined into a single qualifier character '%';
 * </p>
 */
public final class InstantEdtfDate extends AbstractEdtfDate implements Comparable<InstantEdtfDate> {

  public static final int THRESHOLD_4_DIGITS_YEAR = 9999;
  public static final char OVER_4_DIGITS_YEAR_PREFIX = 'Y';

  private YearPrecision yearPrecision;
  private Year year;
  private YearMonth yearMonth;
  private LocalDate yearMonthDay;

  private DateQualification dateQualification = NO_QUALIFICATION;
  private DateEdgeType dateEdgeType = DateEdgeType.DECLARED;

  public InstantEdtfDate(InstantEdtfDateBuilder instantEdtfDateBuilder) {
    yearPrecision = instantEdtfDateBuilder.getYearPrecision();
    year = instantEdtfDateBuilder.getYearObj();
    yearMonth = instantEdtfDateBuilder.getYearMonthObj();
    yearMonthDay = instantEdtfDateBuilder.getYearMonthDayObj();
    dateQualification = instantEdtfDateBuilder.getDateQualification();
  }

  private InstantEdtfDate(DateEdgeType dateEdgeType) {
    this.dateEdgeType = dateEdgeType;
  }

  public Year getYear() {
    return year;
  }

  public YearMonth getYearMonth() {
    return yearMonth;
  }

  public LocalDate getYearMonthDay() {
    return yearMonthDay;
  }

  public YearPrecision getYearPrecision() {
    return yearPrecision;
  }

  public DateQualification getDateQualification() {
    return dateQualification;
  }

  public DateEdgeType getDateEdgeType() {
    return dateEdgeType;
  }

  public static InstantEdtfDate getUnknownInstance() {
    return new InstantEdtfDate(UNKNOWN);
  }

  public static InstantEdtfDate getOpenInstance() {
    return new InstantEdtfDate(OPEN);
  }

  @Override
  public boolean isYearPrecision() {
    return yearPrecision != null;
  }

  @Override
  public InstantEdtfDate getFirstDay() {
    InstantEdtfDate firstDay = null;
    if (dateEdgeType == DateEdgeType.DECLARED) {
      // TODO: 25/07/2022 What about > THRESHOLD_4_DIGITS_YEAR??
      //The part where > THRESHOLD_4_DIGITS_YEAR is not possible because it's in the future, so we don't have to check it.
      //Verify though that the contents of this class are always considered valid before the call of this method.
      if (this.getYear().getValue() < -THRESHOLD_4_DIGITS_YEAR) {
        firstDay = new InstantEdtfDateBuilder(this.getYear().getValue()).build();
      } else {
        firstDay = this.firstDayOfYearDatePart();
      }
    }

    return firstDay;
  }

  public InstantEdtfDate firstDayOfYearDatePart() {
    final TemporalAccessor temporalAccessorFirstDay;
    if (yearMonthDay != null) {
      temporalAccessorFirstDay = yearMonthDay.with(TemporalAdjusters.firstDayOfYear());
    } else if (yearMonth != null) {
      temporalAccessorFirstDay = yearMonth.atDay(1);
    } else {
      // TODO: 13/02/2023 Check with Nuno why when it's CENTURY precision we add a year?
      temporalAccessorFirstDay = year
          .plusYears(yearPrecision == YearPrecision.CENTURY ? 1 : 0)
          .atMonthDay(MonthDay.of(1, 1));
    }
    return new InstantEdtfDateBuilder(temporalAccessorFirstDay).build();
  }

  @Override
  public InstantEdtfDate getLastDay() {
    InstantEdtfDate lastDay = null;
    if (dateEdgeType == DateEdgeType.DECLARED) {
      if (this.getYear().getValue() < -THRESHOLD_4_DIGITS_YEAR) {
        lastDay = new InstantEdtfDateBuilder(this.getYear().getValue()).build();
      } else {
        lastDay = this.lastDayOfYearDatePart();
      }
    }
    return lastDay;
  }

  public InstantEdtfDate lastDayOfYearDatePart() {
    final TemporalAccessor temporalAccessorLastDay;
    if (yearMonthDay != null) {
      temporalAccessorLastDay = yearMonthDay.with(TemporalAdjusters.lastDayOfYear());
    } else if (yearMonth != null) {
      temporalAccessorLastDay = yearMonth.atEndOfMonth();
    } else {
      // TODO: 13/02/2023 Check with Nuno the year additions.
      if (yearPrecision == YearPrecision.CENTURY) {
        temporalAccessorLastDay = year.plusYears(100).atMonthDay(MonthDay.of(Month.DECEMBER, 31));
      } else if (yearPrecision == YearPrecision.DECADE) {
        temporalAccessorLastDay = year.plusYears(9).atMonthDay(MonthDay.of(Month.DECEMBER, 31));
      } else {
        temporalAccessorLastDay = year.atMonthDay(MonthDay.of(Month.DECEMBER, 31));
      }
    }
    return new InstantEdtfDateBuilder(temporalAccessorLastDay).build();

  }

  @Override
  public boolean isOpen() {
    return dateEdgeType == OPEN;
  }

  public Integer getCentury() {
    int century = (year.getValue() / YearPrecision.CENTURY.getDuration()) + 1;
    if (yearPrecision == null) {
      century += 1;
    }
    return century;
  }

  @Override
  public String toString() {
    final StringBuilder stringBuilder = new StringBuilder();
    if (dateEdgeType == OPEN || dateEdgeType == UNKNOWN) {
      stringBuilder.append(OPEN.getStringRepresentation());
    } else if (year.getValue() < -THRESHOLD_4_DIGITS_YEAR || year.getValue() > THRESHOLD_4_DIGITS_YEAR) {
      stringBuilder.append(OVER_4_DIGITS_YEAR_PREFIX).append(year.getValue());
    } else {
      stringBuilder.append(serializeYear());

      final DecimalFormat decimalFormat = new DecimalFormat("00");
      stringBuilder.append(
          ofNullable(yearMonth).map(YearMonth::getMonthValue).map(decimalFormat::format).map(month -> "-" + month).orElse(""));
      stringBuilder.append(
          ofNullable(yearMonthDay).map(LocalDate::getDayOfMonth).map(decimalFormat::format).map(month -> "-" + month).orElse(""));
    }
    stringBuilder.append(ofNullable(dateQualification).filter(dq -> dq != NO_QUALIFICATION).map(DateQualification::getCharacter)
                                                      .map(Objects::toString).orElse(""));
    return stringBuilder.toString();
  }

  @Override
  public int compareTo(InstantEdtfDate other) {
    int comparatorValue = this.year.compareTo(other.year);
    if (comparatorValue == 0 && this.yearMonth != null && other.yearMonth != null) {
      comparatorValue = this.yearMonth.compareTo(other.yearMonth);
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
    return yearPrecision == that.yearPrecision && Objects.equals(year, that.year) && Objects.equals(yearMonth,
        that.yearMonth) && Objects.equals(yearMonthDay, that.yearMonthDay) && dateQualification == that.dateQualification
        && dateEdgeType == that.dateEdgeType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(yearPrecision, year, yearMonth, yearMonthDay, dateQualification, dateEdgeType);
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
    final String yearAdjusted;
    if (yearPrecision == null) {
      yearAdjusted = paddedYear;
    } else {
      final int trailingZeros = Integer.numberOfTrailingZeros(yearPrecision.getDuration());
      yearAdjusted = paddedYear.substring(0, ISO_8601_MINIMUM_YEAR_DIGITS - trailingZeros) + "X".repeat(trailingZeros);
    }
    return prefix + yearAdjusted;
  }

}
