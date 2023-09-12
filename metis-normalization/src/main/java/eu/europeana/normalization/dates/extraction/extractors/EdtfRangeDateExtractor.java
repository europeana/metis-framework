package eu.europeana.normalization.dates.extraction.extractors;

import static eu.europeana.normalization.dates.DateNormalizationResult.getNoMatchResult;
import static eu.europeana.normalization.dates.edtf.DateBoundaryType.OPEN;
import static eu.europeana.normalization.dates.edtf.DateBoundaryType.UNKNOWN;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.DateNormalizationResultStatus;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import eu.europeana.normalization.dates.extraction.DefaultDatesSeparator;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Extractor for Edtf date ranges.
 * <p>We reuse the already existent {@link EdtfDateExtractor} code for the boundaries.</p>
 */
public class EdtfRangeDateExtractor extends AbstractRangeDateExtractor<DefaultDatesSeparator> {

  private static final EdtfDateExtractor EDTF_DATE_EXTRACTOR = new EdtfDateExtractor();

  @Override
  public List<DefaultDatesSeparator> getRangeDateQualifiers() {
    return new ArrayList<>(EnumSet.of(DefaultDatesSeparator.SLASH_DELIMITER));
  }

  @Override
  public DateNormalizationResultRangePair extractDateNormalizationResult(String startString, String endString,
      DefaultDatesSeparator rangeDateDelimiters, DateQualification requestedDateQualification, boolean flexibleDateBuild)
      throws DateExtractionException {
    DateNormalizationResult startDateNormalizationResult = extractInstant(startString, requestedDateQualification,
        flexibleDateBuild);
    DateNormalizationResult endDateNormalizationResult = extractInstant(endString, requestedDateQualification, flexibleDateBuild);
    InstantEdtfDate startInstantEdtfDate = (InstantEdtfDate) startDateNormalizationResult.getEdtfDate();
    InstantEdtfDate endInstantEdtfDate = (InstantEdtfDate) endDateNormalizationResult.getEdtfDate();

    //Are both ends unknown or open, then it is not a date
    if ((startInstantEdtfDate.getDateBoundaryType() == UNKNOWN || startInstantEdtfDate.getDateBoundaryType() == OPEN) &&
        (endInstantEdtfDate.getDateBoundaryType() == UNKNOWN || endInstantEdtfDate.getDateBoundaryType() == OPEN)) {
      startDateNormalizationResult = getNoMatchResult(startString);
      endDateNormalizationResult = getNoMatchResult(endString);
    }

    return new DateNormalizationResultRangePair(startDateNormalizationResult, endDateNormalizationResult);
  }

  private DateNormalizationResult extractInstant(String dateInput, DateQualification requestedDateQualification,
      boolean flexibleDateBuild) throws DateExtractionException {
    final DateNormalizationResult dateNormalizationResult;
    if (UNKNOWN.getDeserializedRepresentation().equals(dateInput)) {
      dateNormalizationResult = new DateNormalizationResult(DateNormalizationExtractorMatchId.EDTF, dateInput,
          InstantEdtfDate.getUnknownInstance());
    } else if (OPEN.getDeserializedRepresentation().equals(dateInput)) {
      dateNormalizationResult = new DateNormalizationResult(DateNormalizationExtractorMatchId.EDTF, dateInput,
          InstantEdtfDate.getOpenInstance());
    } else {
      dateNormalizationResult = EDTF_DATE_EXTRACTOR.extract(dateInput, requestedDateQualification, flexibleDateBuild);
    }
    return dateNormalizationResult;
  }

  @Override
  public boolean isRangeMatchSuccess(DefaultDatesSeparator rangeDateDelimiters, DateNormalizationResult startDateResult,
      DateNormalizationResult endDateResult) {
    return startDateResult.getDateNormalizationResultStatus() == DateNormalizationResultStatus.MATCHED
        && endDateResult.getDateNormalizationResultStatus() == DateNormalizationResultStatus.MATCHED;
  }

  @Override
  public DateNormalizationExtractorMatchId getDateNormalizationExtractorId(DateNormalizationResult startDateResult,
      DateNormalizationResult endDateResult) {
    return DateNormalizationExtractorMatchId.EDTF;
  }
}
