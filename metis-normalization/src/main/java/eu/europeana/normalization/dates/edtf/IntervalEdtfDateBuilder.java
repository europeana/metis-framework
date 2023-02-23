package eu.europeana.normalization.dates.edtf;

import eu.europeana.normalization.dates.extraction.DateExtractionException;

/**
 * Builder class for {@link IntervalEdtfDate}.
 * <p>
 * During {@link #build()} it will verify all the parameters that have been requested. The {@link #build()}, if
 * {@link #withAllowSwitchStartEnd(boolean)} was called with {@code true}, will also attempt a second time by switching start and
 * end values if the original values were invalid.
 * </p>
 */
public class IntervalEdtfDateBuilder {

  private InstantEdtfDate start;
  private InstantEdtfDate end;
  private String label;

  private boolean allowSwitchStartEnd = false;

  /**
   * Constructor which initializes the builder with the the start and end date edges.
   *
   * @param start the start date
   * @param end the end date
   */
  public IntervalEdtfDateBuilder(InstantEdtfDate start, InstantEdtfDate end) {
    this.start = start;
    this.end = end;
  }

  public IntervalEdtfDateBuilder withLabel(String label) {
    this.label = label;
    return this;
  }

  public IntervalEdtfDateBuilder withAllowSwitchStartEnd(boolean allowSwitchStartEnd) {
    this.allowSwitchStartEnd = allowSwitchStartEnd;
    return this;
  }

  public IntervalEdtfDate build() throws DateExtractionException {
    IntervalEdtfDate intervalEdtfDate;
    intervalEdtfDate = buildInternal();
    //Try once more if switching allowed
    if (intervalEdtfDate == null && allowSwitchStartEnd) {
      //Retry with swapping month and day
      switchStartWithEnd();
      intervalEdtfDate = buildInternal();
    }

    //Still nothing, we are done.
    if (intervalEdtfDate == null) {
      throw new DateExtractionException("Could not instantiate date");
    }
    return intervalEdtfDate;
  }

  private void switchStartWithEnd() {
    InstantEdtfDate tempStart = this.start;
    this.start = this.end;
    this.end = tempStart;
  }

  /**
   * Returns an instance of {@link IntervalEdtfDate} created and validated from the fields set on this builder.
   * <p>It validates:
   * <ul>
   *   <li>that edges are not null</li>
   *   <li>If both dates are {@link DateEdgeType#DECLARED} then the period has to be valid. The start must be before the end.</li>
   *   <li>If any of the dates are not marked as {@link DateEdgeType#DECLARED}, then no further validation is performed and the
   *   period is considered valid(For example a period ../1989-11-01).</li>
   * </ul>
   *  and that the period is valid(e.g start is not after end).</p>
   */
  private IntervalEdtfDate buildInternal() {
    IntervalEdtfDate intervalEdtfDate = null;
    final boolean isIntervalValid;
    if (start == null || end == null) {
      isIntervalValid = false;
    } else {
      final boolean isStartDatePartSpecific = start.getDateEdgeType() == DateEdgeType.DECLARED;
      final boolean isEndDatePartSpecific = end.getDateEdgeType() == DateEdgeType.DECLARED;
      if (isStartDatePartSpecific && isEndDatePartSpecific) {
        isIntervalValid = start.compareTo(end) <= 0;
      } else {
        isIntervalValid = isStartDatePartSpecific || isEndDatePartSpecific;
      }
    }

    if (isIntervalValid) {
      intervalEdtfDate = new IntervalEdtfDate(this);
    }

    return intervalEdtfDate;
  }

  public InstantEdtfDate getStart() {
    return start;
  }

  public InstantEdtfDate getEnd() {
    return end;
  }

  public String getLabel() {
    return label;
  }
}