package eu.europeana.normalization.dates.edtf;

/**
 * This class validates instances of EDTF dates.
 * <p>It validates the following:
 * <ul>
 *  <li>If the start date of an interval is earlier than the end date.</li>
 * </ul>
 * </p>
 */
public final class EdtfValidator {

  private EdtfValidator() {
  }

  public static boolean validate(AbstractEdtfDate edtfDate) {
    boolean isValid;
    if (edtfDate instanceof InstantEdtfDate) {
      isValid = validateInstant((InstantEdtfDate) edtfDate);
    } else {
      isValid = validateInterval((IntervalEdtfDate) edtfDate);
    }
    return isValid;
  }

  /**
   * The interval validation only checks for the date part and not the time part of the date.
   * <p>It has been decided that only the date part should be checked, ignoring the time part. This
   * could mean that the interval is technically not valid (e.g. start and end are on the same date but the start is later than
   * the end). But since we are only interested in dates, we accept this.</p>
   *
   * @param intervalEdtfDate the interval date to check
   * @return true if it's valid
   */
  private static boolean validateInterval(IntervalEdtfDate intervalEdtfDate) {
    final InstantEdtfDate startDate = intervalEdtfDate.getStart();
    final InstantEdtfDate endDate = intervalEdtfDate.getEnd();
    final boolean isIntervalValid;
    if (startDate != null && endDate != null) {
      final boolean isStartDatePartSpecific = startDate.getDateEdgeType() == DateEdgeType.DECLARED;
      final boolean isEndDatePartSpecific = endDate.getDateEdgeType() == DateEdgeType.DECLARED;
      if (isStartDatePartSpecific && isEndDatePartSpecific) {
        isIntervalValid = startDate.compareTo(endDate) <= 0;
      } else {
        isIntervalValid = isStartDatePartSpecific || isEndDatePartSpecific;
      }
    } else {
      isIntervalValid = false;
    }

    return isIntervalValid;
  }

  /**
   * Validates an instant date.
   * <p>It contains general validity of date part and in addition it <b>cannot</b> have date part null or
   * unknown.</p>
   *
   * @param instantEdtfDate the instant date to validate
   * @return true if the instant is valid
   */
  private static boolean validateInstant(InstantEdtfDate instantEdtfDate) {
    return instantEdtfDate.getDateEdgeType() == DateEdgeType.DECLARED || instantEdtfDate.getDateEdgeType() == DateEdgeType.OPEN;
  }
}
