package eu.europeana.normalization.dates;

import static eu.europeana.normalization.dates.DateNormalizationResultStatus.MATCHED;

import eu.europeana.normalization.dates.edtf.AbstractEdtfDate;
import eu.europeana.normalization.dates.edtf.DateEdgeType;
import eu.europeana.normalization.dates.edtf.DateQualification;
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

  private DateNormalizationResultStatus dateNormalizationResultStatus = MATCHED;
  private SanitizeOperation sanitizeOperation;
  private final DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId;
  private final String originalInput;
  private final AbstractEdtfDate edtfDate;

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

  private DateNormalizationResult(DateNormalizationResultStatus dateNormalizationResultStatus, String originalInput) {
    this(null, originalInput, null);
    this.dateNormalizationResultStatus = dateNormalizationResultStatus;
  }

  public DateNormalizationResult(DateNormalizationResult dateNormalizationResult, SanitizeOperation sanitizeOperation) {
    this(dateNormalizationResult.getDateNormalizationExtractorMatchId(), dateNormalizationResult.getOriginalInput(),
        dateNormalizationResult.getEdtfDate());
    this.sanitizeOperation = sanitizeOperation;
  }

  public DateNormalizationResultStatus getDateNormalizationResultStatus() {
    return dateNormalizationResultStatus;
  }

  /**
   * Get an instance of a date normalization result for no matches.
   *
   * @param originalInput the original input value
   * @return the no match result
   */
  public static DateNormalizationResult getNoMatchResult(String originalInput) {
    return new DateNormalizationResult(DateNormalizationResultStatus.NO_MATCH, originalInput);
  }

  public DateNormalizationExtractorMatchId getDateNormalizationExtractorMatchId() {
    return dateNormalizationExtractorMatchId;
  }

  public SanitizeOperation getSanitizeOperation() {
    return sanitizeOperation;
  }

  public String getOriginalInput() {
    return originalInput;
  }

  public AbstractEdtfDate getEdtfDate() {
    return edtfDate;
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
  // TODO: 28/02/2023 Check if this is still relevant and create some test cases if so.
  public boolean isCompleteDate() {
    boolean isCompleteDate = true;
    if (edtfDate == null) {
      isCompleteDate = false;
    } else if (edtfDate instanceof InstantEdtfDate) {
      if (isDateNonPrecise(edtfDate) || isMonthDayNotComplete((InstantEdtfDate) edtfDate)) {
        isCompleteDate = false;
      }
    } else {
      final InstantEdtfDate startInstantEdtfDate = ((IntervalEdtfDate) edtfDate).getStart();
      final InstantEdtfDate endInstantEdtfDate = ((IntervalEdtfDate) edtfDate).getEnd();

      if (areBothDatesSpecified(startInstantEdtfDate, endInstantEdtfDate) &&
          (areDatesNonPrecise(startInstantEdtfDate, endInstantEdtfDate) || !isOnlyYearsOrComplete(startInstantEdtfDate,
              endInstantEdtfDate))) {
        isCompleteDate = false;
      }
    }
    return isCompleteDate;
  }

  private boolean areBothDatesSpecified(InstantEdtfDate startInstantEdtfDate, InstantEdtfDate endInstantEdtfDate) {
    return startInstantEdtfDate != null && endInstantEdtfDate != null &&
        startInstantEdtfDate.getDateEdgeType() == DateEdgeType.DECLARED
        && endInstantEdtfDate.getDateEdgeType() == DateEdgeType.DECLARED;
  }

  private boolean areDatesNonPrecise(InstantEdtfDate startInstantEdtfDate, InstantEdtfDate endInstantEdtfDate) {
    return isDateNonPrecise(startInstantEdtfDate) || isDateNonPrecise(endInstantEdtfDate);
  }

  private boolean isDateNonPrecise(AbstractEdtfDate abstractEdtfDate) {
    DateQualification dateQualification = abstractEdtfDate.getDateQualification();
    // TODO: 15/02/2023 Check this instruction. It used to be like that
    //  return edtfDatePart.isUnknown() || edtfDatePart.isUncertain() || edtfDatePart.getYearPrecision() != null;
    //  but do we actually need the check on unknown?
    return (dateQualification == DateQualification.UNCERTAIN) || abstractEdtfDate.isYearPrecision();
  }

  private boolean isMonthDayNotComplete(InstantEdtfDate instantEdtfDate) {
    return !isMonthDayComplete(instantEdtfDate);
  }

  private boolean isMonthDayComplete(InstantEdtfDate instantEdtfDate) {
    return instantEdtfDate.getYearMonthDay() != null;
  }

  private boolean isOnlyYearsOrComplete(InstantEdtfDate startInstantEdtfDate, InstantEdtfDate endInstantEdtfDate) {
    final boolean isOnlyYear = startInstantEdtfDate.getMonth() == null && endInstantEdtfDate.getMonth() == null;
    final boolean isCompleteDate = isMonthDayComplete(startInstantEdtfDate) && isMonthDayComplete(endInstantEdtfDate);

    return isOnlyYear || isCompleteDate;
  }

}
