package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.DateNormalizationResultStatus;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDateBuilder;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import eu.europeana.normalization.dates.sanitize.DateFieldSanitizer;

/**
 * Extractor for BC and AD date ranges with variations in the separators of date components.
 * <p>We reuse the already existent {@link BcAdDateExtractor} code for the boundaries.</p>
 */
public class BcAdRangeDateExtractor extends AbstractDateExtractor {

  private static final BcAdDateExtractor BC_AD_DATE_EXTRACTOR = new BcAdDateExtractor();
  private static final String[] DATES_DELIMITERS = new String[]{"-", "/"};

  @Override
  public DateNormalizationResult extract(String inputValue, DateQualification requestedDateQualification,
      boolean flexibleDateBuild) throws DateExtractionException {
    final String sanitizedValue = DateFieldSanitizer.cleanSpacesAndTrim(inputValue);
    DateNormalizationResult startDateResult;
    DateNormalizationResult endDateResult;
    DateNormalizationResult rangeDate = DateNormalizationResult.getNoMatchResult(inputValue);

    for (String datesDelimiter : DATES_DELIMITERS) {
      // Split with -1 limit does not discard empty splits
      final String[] sanitizedDateSplitArray = sanitizedValue.split(datesDelimiter, -1);
      // The sanitizedDateSplitArray has to be exactly in two, and then we can verify.
      // This also guarantees that the separator used is not used for unknown characters.
      if (sanitizedDateSplitArray.length == 2) {
        // Try extraction and verify
        startDateResult = extractDateNormalizationResult(sanitizedDateSplitArray[0], requestedDateQualification,
            flexibleDateBuild);
        endDateResult = extractDateNormalizationResult(sanitizedDateSplitArray[1], requestedDateQualification, flexibleDateBuild);
        if (startDateResult.getDateNormalizationResultStatus() == DateNormalizationResultStatus.MATCHED
            && endDateResult.getDateNormalizationResultStatus() == DateNormalizationResultStatus.MATCHED) {
          final DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId = DateNormalizationExtractorMatchId.BC_AD;
          final IntervalEdtfDate intervalEdtfDate = new IntervalEdtfDateBuilder((InstantEdtfDate) startDateResult.getEdtfDate(),
              (InstantEdtfDate) endDateResult.getEdtfDate()).withFlexibleDateBuild(flexibleDateBuild).build();
          rangeDate = new DateNormalizationResult(dateNormalizationExtractorMatchId, inputValue, intervalEdtfDate);
          break;
        }
      }
    }
    return rangeDate;
  }

  private DateNormalizationResult extractDateNormalizationResult(String dateString, DateQualification requestedDateQualification,
      boolean flexibleDateBuild) throws DateExtractionException {
    return BC_AD_DATE_EXTRACTOR.extract(dateString, requestedDateQualification, flexibleDateBuild);
  }
}
