package eu.europeana.normalization.dates.edtf;

import static java.lang.String.format;

/**
 * An EDTF date that represents a period of time specified by a start and end date with various degrees of precision
 */
public class IntervalEdtfDate extends AbstractEdtfDate {

  public static final String DATES_SEPARATOR = "/";
  private InstantEdtfDate start;
  private InstantEdtfDate end;

  public IntervalEdtfDate(InstantEdtfDate start, InstantEdtfDate end) {
    this.start = start;
    this.end = end;
  }

  public IntervalEdtfDate(String label, InstantEdtfDate start, InstantEdtfDate end) {
    super(label);
    this.start = start;
    this.end = end;
  }

  public void switchStartWithEnd() {
    InstantEdtfDate tempStart = this.start;
    this.start = this.end;
    this.end = tempStart;
  }

  @Override
  public DateQualification getDateQualification() {
    if (start != null) {
      return start.getDateQualification();
    }
    if (end != null) {
      return end.getDateQualification();
    }
    return null;
  }

  @Override
  public boolean isYearPrecision() {
    return (start != null && start.getYearPrecision() != null) || (end != null
        && end.getYearPrecision() != null);
  }

  @Override
  public boolean isOpen() {
    return (start != null && start.getDateEdgeType() == DateEdgeType.OPEN) || (end != null
        && end.getDateEdgeType() == DateEdgeType.OPEN);
  }

  @Override
  public InstantEdtfDate getFirstDay() {
    return start == null ? null : start.getFirstDay();
  }

  @Override
  public InstantEdtfDate getLastDay() {
    return end == null ? null : end.getLastDay();
  }

  public InstantEdtfDate getStart() {
    return start;
  }

  public void setStart(InstantEdtfDate start) {
    this.start = start;
  }

  public InstantEdtfDate getEnd() {
    return end;
  }

  public void setEnd(InstantEdtfDate end) {
    this.end = end;
  }

  @Override
  public String toString() {
    return format("%s%s%s", start.toString(), DATES_SEPARATOR, end.toString());
  }
}
