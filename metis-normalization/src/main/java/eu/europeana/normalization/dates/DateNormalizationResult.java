package eu.europeana.normalization.dates;

import eu.europeana.normalization.dates.cleaning.CleanOperation;
import eu.europeana.normalization.dates.edtf.AbstractEdtfDate;
import eu.europeana.normalization.dates.edtf.EdtfDatePart;
import eu.europeana.normalization.dates.edtf.EdtfDateWithLabel;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;

/**
 * Contains the result of a date normalisation.
 * <p>
 * It contains the pattern that was matched, if some cleaning was done, and the normalised value (if successfully normalised).
 * </p>
 */
public class DateNormalizationResult {

  private DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId;
  private CleanOperation cleanOperation;
  private String originalInput;
  private EdtfDateWithLabel normalizedEdtfDateWithLabel;

  public DateNormalizationResult(DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId, String originalInput,
      EdtfDateWithLabel normalizedEdtfDateWithLabel) {
    super();
    this.dateNormalizationExtractorMatchId = dateNormalizationExtractorMatchId;
    this.originalInput = originalInput;
    this.normalizedEdtfDateWithLabel = normalizedEdtfDateWithLabel;
  }

  public DateNormalizationResult(DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId, String originalInput,
      AbstractEdtfDate normalizedEdtfDateWithLabel) {
    super();
    this.dateNormalizationExtractorMatchId = dateNormalizationExtractorMatchId;
    this.originalInput = originalInput;
    this.normalizedEdtfDateWithLabel = normalizedEdtfDateWithLabel == null ? null : new EdtfDateWithLabel(
        normalizedEdtfDateWithLabel);
  }

  public DateNormalizationResult(String originalInput) {
    this.originalInput = originalInput;
  }

  public static DateNormalizationResult getNoMatchResult(String originalInput) {
    return new DateNormalizationResult(DateNormalizationExtractorMatchId.NO_MATCH, originalInput, (AbstractEdtfDate) null);
  }

  public DateNormalizationExtractorMatchId getMatchId() {
    return dateNormalizationExtractorMatchId;
  }

  public String getOriginalInput() {
    return originalInput;
  }

  public EdtfDateWithLabel getNormalizedEdtfDateWithLabel() {
    return normalizedEdtfDateWithLabel;
  }

  public void setDateNormalizationExtractorMatchId(DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    this.dateNormalizationExtractorMatchId = dateNormalizationExtractorMatchId;
  }

  public void setOriginalInput(String originalInput) {
    this.originalInput = originalInput;
  }

  public void setNormalizedEdtfDateWithLabel(EdtfDateWithLabel normalizedEdtfDateWithLabel) {
    this.normalizedEdtfDateWithLabel = normalizedEdtfDateWithLabel;
  }

  public CleanOperation getCleanOperationMatchId() {
    return cleanOperation;
  }

  public void setCleanOperationMatchId(CleanOperation cleanOperation) {
    this.cleanOperation = cleanOperation;
  }

  @Override
  public String toString() {
    return "Match [matchId=" + dateNormalizationExtractorMatchId + ", cleanOperation=" + cleanOperation + ", input="
        + originalInput
        + ", extracted="
        + normalizedEdtfDateWithLabel + "]";
  }

  // TODO: 22/07/2022 Double check if this is correct, it has not been tested since it's used in the untested generic property method
  public boolean isCompleteDate() {
    boolean isCompleteDate = true;
    if (normalizedEdtfDateWithLabel == null || normalizedEdtfDateWithLabel.getEdtfDate().isTimeOnly()) {
      isCompleteDate = false;
    } else if (normalizedEdtfDateWithLabel.getEdtfDate() instanceof IntervalEdtfDate) {
      final EdtfDatePart startEdtfDatePart = ((IntervalEdtfDate) normalizedEdtfDateWithLabel.getEdtfDate()).getStart()
                                                                                                           .getEdtfDatePart();
      final EdtfDatePart endEdtfDatePart = ((IntervalEdtfDate) normalizedEdtfDateWithLabel.getEdtfDate()).getEnd()
                                                                                                         .getEdtfDatePart();

      if (startEdtfDatePart != null && endEdtfDatePart != null &&
          (isEitherDateNonPrecise(startEdtfDatePart, endEdtfDatePart) || isDayMonthNotComplete(startEdtfDatePart,
              endEdtfDatePart))) {
        isCompleteDate = false;
      }
    }
    return isCompleteDate;
  }

  private boolean isEitherDateNonPrecise(EdtfDatePart startEdtfDatePart, EdtfDatePart endEdtfDatePart) {
    return startEdtfDatePart.isUnknown() || startEdtfDatePart.isUncertain()
        || endEdtfDatePart.isUnknown() || endEdtfDatePart.isUncertain()
        || startEdtfDatePart.getYearPrecision() != null || endEdtfDatePart.getYearPrecision() != null;
  }

  // TODO: 22/07/2022 Is this calculation correct??
  private boolean isDayMonthNotComplete(EdtfDatePart startEdtfDatePart, EdtfDatePart endEdtfDatePart) {
    return startEdtfDatePart.getDay() == null
        || endEdtfDatePart.getDay() == null || startEdtfDatePart.getMonth() != null
        || endEdtfDatePart.getMonth() != null;
  }

}
