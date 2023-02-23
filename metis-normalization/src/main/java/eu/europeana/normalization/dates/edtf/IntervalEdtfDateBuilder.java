package eu.europeana.normalization.dates.edtf;

import java.time.DateTimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntervalEdtfDateBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(IntervalEdtfDateBuilder.class);

  private InstantEdtfDate start;
  private InstantEdtfDate end;
  private String label;

  private boolean allowSwitchStartEnd = false;

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

  public IntervalEdtfDate build() {
    try {
      validateInterval();
    } catch (DateTimeException e) {
      LOGGER.debug("Start and End dates failed. Trying switching Start nad End", e);
      if (allowSwitchStartEnd) {
        //Retry with swapping month and day
        switchStartWithEnd();
      } else {
        throw e;
      }
      validateInterval();
    }
    return new IntervalEdtfDate(this);
  }

  private void switchStartWithEnd() {
    InstantEdtfDate tempStart = this.start;
    this.start = this.end;
    this.end = tempStart;
  }

  /**
   * Validate interval.
   * <p>It validates:
   * <ul>
   *   <li>that edges are not null</li>
   *   <li>If both dates are {@link DateEdgeType#DECLARED} then the period has to be valid. The start must be before the end.</li>
   *   <li>If any of the dates are not marked as {@link DateEdgeType#DECLARED}, then no further validation is performed and the
   *   period is considered valid(For example a period ../1989-11-01).</li>
   * </ul>
   *  and that the period is valid(e.g start is not after end).</p>
   */
  private void validateInterval() {
    if (start == null || end == null) {
      throw new DateTimeException("Interval date edges should not be null");
    }

    final boolean isIntervalValid;
    final boolean isStartDatePartSpecific = start.getDateEdgeType() == DateEdgeType.DECLARED;
    final boolean isEndDatePartSpecific = end.getDateEdgeType() == DateEdgeType.DECLARED;
    if (isStartDatePartSpecific && isEndDatePartSpecific) {
      isIntervalValid = start.compareTo(end) <= 0;
    } else {
      isIntervalValid = isStartDatePartSpecific || isEndDatePartSpecific;
    }

    if (!isIntervalValid) {
      throw new DateTimeException("Interval date edges were not valid");
    }
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