package eu.europeana.normalization.dates.edtf;

import static eu.europeana.normalization.dates.extraction.DefaultDatesSeparator.SLASH_DELIMITER;
import static java.lang.String.format;

import java.util.EnumSet;
import java.util.Set;

/**
 * An EDTF date that represents a period of time specified by a start and end date with various degrees of precision
 */
public class IntervalEdtfDate extends AbstractEdtfDate {

  private InstantEdtfDate start;
  private InstantEdtfDate end;


  /**
   * Restricted constructor by provided {@link InstantEdtfDateBuilder}.
   * <p>All fields apart from the internal {@link IntervalEdtfDate#addQualification(DateQualification)}(for each boundary) are
   * strictly contained in the constructor. The date qualifications can be further extended to, for example, add an approximate
   * qualification for a date that was sanitized.</p>
   *
   * @param intervalEdtfDateBuilder the builder with all content verified
   */
  IntervalEdtfDate(IntervalEdtfDateBuilder intervalEdtfDateBuilder) {
    super(intervalEdtfDateBuilder.getLabel());
    this.start = intervalEdtfDateBuilder.getStart();
    this.end = intervalEdtfDateBuilder.getEnd();
  }

  @Override
  public void addQualification(DateQualification dateQualification) {
    start.addQualification(dateQualification);
    end.addQualification(dateQualification);
  }

  @Override
  public Set<DateQualification> getDateQualifications() {
    Set<DateQualification> dateQualifications = EnumSet.copyOf(start.getDateQualifications());
    dateQualifications.addAll(end.getDateQualifications());
    return dateQualifications;
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
    return format("%s%s%s", start.toString(), SLASH_DELIMITER.getStringRepresentation(), end.toString());
  }
}
