package eu.europeana.normalization.dates.edtf;

import static java.lang.String.format;

/**
 * An EDTF date that represents a period of time specified by a start and end date with various degrees of precision
 */
public class IntervalEdtfDate extends AbstractEdtfDate {

  public static final String DATES_SEPARATOR = "/";
  private InstantEdtfDate start;
  private InstantEdtfDate end;

  IntervalEdtfDate(IntervalEdtfDateBuilder intervalEdtfDateBuilder) {
    super(intervalEdtfDateBuilder.getLabel());
    this.start = intervalEdtfDateBuilder.getStart();
    this.end = intervalEdtfDateBuilder.getEnd();
  }

  @Override
  public DateQualification getDateQualification() {
    // TODO: 24/02/2023 To verify what this should return.
    if (start.getDateQualification() != DateQualification.NO_QUALIFICATION) {
      return start.getDateQualification();
    } else {
      return end.getDateQualification();
    }
  }

  @Override
  public boolean isOpen() {
    return start.getDateEdgeType() == DateEdgeType.OPEN || end.getDateEdgeType() == DateEdgeType.OPEN;
  }

  @Override
  public InstantEdtfDate getFirstDay() {
    return start.getFirstDay();
  }

  @Override
  public InstantEdtfDate getLastDay() {
    return end.getLastDay();
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
