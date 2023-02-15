package eu.europeana.normalization.dates.edtf;

/**
 * An EDTF date that represents a period of time specified by a start and end date with various degrees of precision
 */
public class IntervalEdtfDate extends AbstractEdtfDate {

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
  public void setDateQualification(DateQualification dateQualification) {
    if (start != null && start.getEdtfDatePart() != null) {
      start.setDateQualification(dateQualification);
    }
    if (end != null && end.getEdtfDatePart() != null) {
      end.setDateQualification(dateQualification);
    }
  }

  @Override
  public DateQualification getDateQualification() {
    if (start != null && start.getEdtfDatePart() != null) {
      return start.getDateQualification();
    }
    if (end != null && end.getEdtfDatePart() != null) {
      return end.getDateQualification();
    }
    return null;
  }

  @Override
  public boolean isYearPrecision() {
    return (start != null && start.getEdtfDatePart().getYearPrecision() != null) || (end != null
        && end.getEdtfDatePart().getYearPrecision() != null);
  }

  @Override
  public boolean isUnspecified() {
    return (start != null && start.isUnspecified()) || (end != null && end.isUnspecified());
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
    return String.format("%s/%s", start.toString(), end.toString());
  }
}
