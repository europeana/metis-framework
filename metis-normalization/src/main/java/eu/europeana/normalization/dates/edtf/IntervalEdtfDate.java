package eu.europeana.normalization.dates.edtf;

import static eu.europeana.normalization.dates.extraction.DefaultDatesSeparator.SLASH_DELIMITER;
import static java.lang.String.format;

/**
 * An EDTF date that represents a period of time specified by a start and end date with various degrees of precision
 */
public class IntervalEdtfDate extends AbstractEdtfDate {

  private InstantEdtfDate start;
  private InstantEdtfDate end;

  IntervalEdtfDate(IntervalEdtfDateBuilder intervalEdtfDateBuilder) {
    super(intervalEdtfDateBuilder.getLabel());
    this.start = intervalEdtfDateBuilder.getStart();
    this.end = intervalEdtfDateBuilder.getEnd();
  }

  @Override
  public void overwriteQualification(DateQualification dateQualification) {
    start.overwriteQualification(dateQualification);
    end.overwriteQualification(dateQualification);
  }

  @Override
  public DateQualification getDateQualification() {
    return start.getDateQualification().compareTo(end.getDateQualification()) >= 0
        ? start.getDateQualification() : end.getDateQualification();
  }

  @Override
  public boolean isOpen() {
    return start.getDateBoundaryType() == DateBoundaryType.OPEN || end.getDateBoundaryType() == DateBoundaryType.OPEN;
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
    return format("%s%s%s", start.toString(), SLASH_DELIMITER.getDatesSeparator(), end.toString());
  }
}
