package eu.europeana.normalization.dates.edtf;

import eu.europeana.normalization.dates.extraction.DateExtractionException;
import java.lang.invoke.MethodHandles;
import java.time.DateTimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder class for {@link IntervalEdtfDate}.
 * <p>
 * During {@link #build()} it will verify all the parameters that have been requested. The {@link #build()}, if
 * {@link #withAllowStartEndSwap(boolean)} was called with {@code true}, will also attempt a second time by switching
 * start and end values if the original values were invalid.
 * </p>
 */
public class IntervalEdtfDateBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private InstantEdtfDate start;
  private InstantEdtfDate end;
  private String label;
  private boolean allowStartEndSwap = true;

  /**
   * Constructor which initializes the builder with the start and end date boundaries.
   * <p>Boundaries should never be null</p>
   *
   * @param start the start date
   * @param end the end date
   */
  public IntervalEdtfDateBuilder(InstantEdtfDate start, InstantEdtfDate end) {
    this.start = start;
    this.end = end;
  }

  /**
   * Add label if any for the interval
   *
   * @param label the interval specific label
   * @return the extended builder
   */
  public IntervalEdtfDateBuilder withLabel(String label) {
    this.label = label;
    return this;
  }

  /**
   * Opt in/out for start end swap if original values failed validation.
   *
   * @param allowStartEndSwap the boolean (dis|en)abling the start and end swap
   * @return the extended builder
   */
  public IntervalEdtfDateBuilder withAllowStartEndSwap(boolean allowStartEndSwap) {
    this.allowStartEndSwap = allowStartEndSwap;
    return this;
  }

  /**
   * Returns an instance of {@link IntervalEdtfDate} created and validated from the fields set on this builder.
   *
   * @return the new interval edtf date
   * @throws DateExtractionException if something went wrong during date validation
   */
  public IntervalEdtfDate build() throws DateExtractionException {
    IntervalEdtfDate intervalEdtfDate;
    intervalEdtfDate = buildInternal();
    //Try once more if switching allowed
    if (intervalEdtfDate == null && allowStartEndSwap) {
      switchStartWithEnd();
      intervalEdtfDate = buildInternal();
    }

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
   *   <li>that boundaries are not null</li>
   *   <li>If both dates are {@link DateBoundaryType#DECLARED} then the period has to be valid. The start must be before the end.</li>
   *   <li>If any of the dates are not marked as {@link DateBoundaryType#DECLARED}, then no further validation is performed and the
   *   period is considered valid(For example a period ../1989-11-01).</li>
   * </ul>
   *  and that the period is valid(e.g start is not after end).</p>
   */
  private IntervalEdtfDate buildInternal() {
    IntervalEdtfDate intervalEdtfDate = null;
    try {
      final boolean isIntervalValid;
      if (start == null || end == null) {
        isIntervalValid = false;
      } else {
        final boolean isStartDatePartSpecific = start.getDateBoundaryType() == DateBoundaryType.DECLARED;
        final boolean isEndDatePartSpecific = end.getDateBoundaryType() == DateBoundaryType.DECLARED;
        if (isStartDatePartSpecific && isEndDatePartSpecific) {
          isIntervalValid = start.compareTo(end) <= 0;
        } else {
          isIntervalValid = isStartDatePartSpecific || isEndDatePartSpecific;
        }
      }

      if (isIntervalValid) {
        intervalEdtfDate = new IntervalEdtfDate(this);
      }
    } catch (DateTimeException e) {
      LOGGER.debug("Date build failed.", e);
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
