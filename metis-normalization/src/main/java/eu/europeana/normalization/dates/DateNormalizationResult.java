package eu.europeana.normalization.dates;

import eu.europeana.normalization.dates.cleaning.CleanOperation;
import eu.europeana.normalization.dates.edtf.AbstractEdtfDate;
import eu.europeana.normalization.dates.edtf.EdtfDatePart;
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
  private AbstractEdtfDate edtfDate;

  public DateNormalizationResult(DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId,
      String originalInput, AbstractEdtfDate edtfDate) {
    this.dateNormalizationExtractorMatchId = dateNormalizationExtractorMatchId;
    this.originalInput = originalInput;
    this.edtfDate = edtfDate;
  }

  public static DateNormalizationResult getNoMatchResult(String originalInput) {
    return new DateNormalizationResult(DateNormalizationExtractorMatchId.NO_MATCH, originalInput, null);
  }

  public DateNormalizationExtractorMatchId getDateNormalizationExtractorMatchId() {
    return dateNormalizationExtractorMatchId;
  }

  public void setDateNormalizationExtractorMatchId(DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    this.dateNormalizationExtractorMatchId = dateNormalizationExtractorMatchId;
  }

  public CleanOperation getCleanOperationMatchId() {
    return cleanOperation;
  }

  public void setCleanOperationMatchId(CleanOperation cleanOperation) {
    this.cleanOperation = cleanOperation;
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

  @Override
  public String toString() {
    return "Match [matchId=" + dateNormalizationExtractorMatchId + ", cleanOperation=" + cleanOperation + ", input="
        + originalInput
        + ", extracted="
        + edtfDate + "]";
  }

  // TODO: 22/07/2022 Double check if this is correct, it has not been tested since it's used in the untested generic property method
  public boolean isCompleteDate() {
    boolean isCompleteDate = true;
    if (edtfDate == null || edtfDate.isTimeOnly()) {
      isCompleteDate = false;
    } else if (edtfDate instanceof IntervalEdtfDate) {
      final EdtfDatePart startEdtfDatePart = ((IntervalEdtfDate) edtfDate).getStart()
                                                                          .getEdtfDatePart();
      final EdtfDatePart endEdtfDatePart = ((IntervalEdtfDate) edtfDate).getEnd()
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
