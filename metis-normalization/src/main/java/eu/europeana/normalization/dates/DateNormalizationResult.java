package eu.europeana.normalization.dates;

import eu.europeana.normalization.dates.edtf.AbstractEdtfDate;
import eu.europeana.normalization.dates.edtf.EdtfDateWithLabel;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;

/**
 * Contains the result of a date normalisation.
 * <p>
 * It contains the pattern that was matched, if some cleaning was done, and the normalised value (if successfully normalised).
 * </p>
 */
public class DateNormalizationResult {

  private DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId;
  private CleanOperationId cleanOperationId;
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

  public static DateNormalizationResult getNoMatchResult(String originalInput) {
    return new DateNormalizationResult(DateNormalizationExtractorMatchId.NO_MATCH, originalInput, (AbstractEdtfDate) null);
  }

  public DateNormalizationResult(String originalInput) {
    this.originalInput = originalInput;
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

  public CleanOperationId getCleanOperationMatchId() {
    return cleanOperationId;
  }

  public void setCleanOperationMatchId(CleanOperationId cleanOperationId) {
    this.cleanOperationId = cleanOperationId;
  }

  @Override
  public String toString() {
    return "Match [matchId=" + dateNormalizationExtractorMatchId + ", cleanOperation=" + cleanOperationId + ", input="
        + originalInput
        + ", extracted="
        + normalizedEdtfDateWithLabel + "]";
  }

  public boolean isCompleteDate() {
    if (normalizedEdtfDateWithLabel == null || normalizedEdtfDateWithLabel.getEdtfDate().isTimeOnly()) {
      return false;
    }
    if (normalizedEdtfDateWithLabel.getEdtfDate() instanceof InstantEdtfDate) {
      return ((InstantEdtfDate) normalizedEdtfDateWithLabel.getEdtfDate()).getEdtfDatePart().getDay() != null;
    } else {
      IntervalEdtfDate intervalEdtfDate = (IntervalEdtfDate) normalizedEdtfDateWithLabel.getEdtfDate();
      if (intervalEdtfDate.getStart() != null && intervalEdtfDate.getEnd() != null) {
        if (intervalEdtfDate.getStart().getEdtfDatePart().isUnknown() || intervalEdtfDate.getStart().getEdtfDatePart()
                                                                                         .isUnspecified()) {
          return false;
        }
        if (intervalEdtfDate.getEnd().getEdtfDatePart().isUnknown() || intervalEdtfDate.getEnd().getEdtfDatePart()
                                                                                       .isUnspecified()) {
          return false;
        }
        if (intervalEdtfDate.getStart().getEdtfDatePart().getYearPrecision() != null
            || intervalEdtfDate.getEnd().getEdtfDatePart().getYearPrecision() != null) {
          return false;
        }
        if (intervalEdtfDate.getStart().getEdtfDatePart().getDay() != null
            && intervalEdtfDate.getStart().getEdtfDatePart().getDay() != null) {
          return true;
        }
        if (intervalEdtfDate.getStart().getEdtfDatePart().getMonth() == null
            && intervalEdtfDate.getStart().getEdtfDatePart().getMonth() == null) {
          return true;
        }
      }
      return false;
    }
  }

}
