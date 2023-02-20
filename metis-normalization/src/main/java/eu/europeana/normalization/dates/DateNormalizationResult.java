package eu.europeana.normalization.dates;

import eu.europeana.normalization.dates.edtf.AbstractEdtfDate;
import eu.europeana.normalization.dates.edtf.EdtfDatePart;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import eu.europeana.normalization.dates.sanitize.SanitizeOperation;

/**
 * Contains the result of a date normalisation.
 * <p>
 * It contains the pattern that was matched, if some sanitizing was performed, and the normalised value (if successfully
 * normalised).
 * </p>
 */
public class DateNormalizationResult {

  private DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId;
  private SanitizeOperation sanitizeOperation;
  private String originalInput;
  private AbstractEdtfDate edtfDate;

  /**
   * Constructor with all parameters.
   *
   * @param dateNormalizationExtractorMatchId the date normalization extractor match identifier
   * @param originalInput the original input value
   * @param edtfDate the date result
   */
  public DateNormalizationResult(DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId,
      String originalInput, AbstractEdtfDate edtfDate) {
    this.dateNormalizationExtractorMatchId = dateNormalizationExtractorMatchId;
    this.originalInput = originalInput;
    this.edtfDate = edtfDate;
  }

  /**
   * Get an instance of a date normalization result for no matches.
   *
   * @param originalInput the original input value
   * @return the no match result
   */
  public static DateNormalizationResult getNoMatchResult(String originalInput) {
    return new DateNormalizationResult(DateNormalizationExtractorMatchId.NO_MATCH, originalInput, null);
  }

  public DateNormalizationExtractorMatchId getDateNormalizationExtractorMatchId() {
    return dateNormalizationExtractorMatchId;
  }

  public void setDateNormalizationExtractorMatchId(DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    this.dateNormalizationExtractorMatchId = dateNormalizationExtractorMatchId;
  }

  public SanitizeOperation getSanitizeOperation() {
    return sanitizeOperation;
  }

  public void setCleanOperation(SanitizeOperation sanitizeOperation) {
    this.sanitizeOperation = sanitizeOperation;
  }

  public String getOriginalInput() {
    return originalInput;
  }

  public void setOriginalInput(String originalInput) {
    this.originalInput = originalInput;
  }

  public AbstractEdtfDate getEdtfDate() {
    return edtfDate;
  }

  public void setEdtfDate(AbstractEdtfDate edtfDate) {
    this.edtfDate = edtfDate;
  }

  /**
   * Checks if a date is complete.
   * <p>This method is used for the generic properties normalization</p>
   * <p>
   * A date is considered complete if:
   *   <ul>
   *     <li>it contains a date part</li>
   *     <li>it is precise</li>
   *     <li>for intervals: either, both dates are only years(without month or day), or, both dates have month day present</li>
   *   </ul>
   * </p>
   *
   * @return true if the date is complete
   */
  public boolean isCompleteDate() {
    boolean isCompleteDate = true;
    if (edtfDate == null || edtfDate.isTimeOnly()) {
      isCompleteDate = false;
    } else if (edtfDate instanceof InstantEdtfDate) {
      final EdtfDatePart edtfDatePart = ((InstantEdtfDate) edtfDate).getEdtfDatePart();
      if (isDateNonPrecise(edtfDatePart) || isMonthDayNotComplete(edtfDatePart)) {
        isCompleteDate = false;
      }
    } else {
      final EdtfDatePart startEdtfDatePart = ((IntervalEdtfDate) edtfDate).getStart()
                                                                          .getEdtfDatePart();
      final EdtfDatePart endEdtfDatePart = ((IntervalEdtfDate) edtfDate).getEnd()
                                                                        .getEdtfDatePart();

      if (areBothDatesSpecified(startEdtfDatePart, endEdtfDatePart) &&
          (areDatesNonPrecise(startEdtfDatePart, endEdtfDatePart) || !isOnlyYearsOrComplete(startEdtfDatePart,
              endEdtfDatePart))) {
        isCompleteDate = false;
      }
    }
    return isCompleteDate;
  }

  private boolean areBothDatesSpecified(EdtfDatePart startEdtfDatePart, EdtfDatePart endEdtfDatePart) {
    return startEdtfDatePart != null && endEdtfDatePart != null && !startEdtfDatePart.isUnspecified()
        && !endEdtfDatePart.isUnspecified();
  }

  private boolean areDatesNonPrecise(EdtfDatePart startEdtfDatePart, EdtfDatePart endEdtfDatePart) {
    return isDateNonPrecise(startEdtfDatePart) || isDateNonPrecise(endEdtfDatePart);
  }

  private boolean isDateNonPrecise(EdtfDatePart edtfDatePart) {
    return edtfDatePart.isUnknown() || edtfDatePart.isUncertain() || edtfDatePart.getYearPrecision() != null;
  }

  private boolean isMonthDayNotComplete(EdtfDatePart edtfDatePart) {
    return !isMonthDayComplete(edtfDatePart);
  }

  private boolean isMonthDayComplete(EdtfDatePart edtfDatePart) {
    return edtfDatePart.getMonth() != null && edtfDatePart.getDay() != null;
  }

  private boolean isOnlyYearsOrComplete(EdtfDatePart startEdtfDatePart, EdtfDatePart endEdtfDatePart) {
    final boolean isOnlyYear = startEdtfDatePart.getMonth() == null && endEdtfDatePart.getMonth() == null &&
        startEdtfDatePart.getDay() == null && endEdtfDatePart.getDay() == null;
    final boolean isCompleteDate = isMonthDayComplete(startEdtfDatePart) && isMonthDayComplete(endEdtfDatePart);

    return isOnlyYear || isCompleteDate;
  }

}
