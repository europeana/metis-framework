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
import java.util.List;

/**
 * The interface adding the option to a reusable range extractor functionality
 * {@link #extractRange(String, DateQualification, boolean)}.
 *
 * @param <T> the object containing delimiters/separators for dates
 */
public interface RangeDateExtractor<T> {

  List<T> getDateDelimiters();

  /**
   * Get the dates separator.
   *
   * @param dateDelimiters the date delimiters object containing the dates separator
   * @return the dates separator
   */
  String getDatesSeparator(T dateDelimiters);

  /**
   * Extract the date normalization result for the range.
   *
   * @param dateString the date string
   * @param rangeDateDelimiters the range date delimiters
   * @param requestedDateQualification the requested qualification
   * @param flexibleDateBuild the boolean opting flexible date build
   * @return the date normalization result
   * @throws DateExtractionException if the date extraction failed
   */
  DateNormalizationResult extractStartDateNormalizationResult(String dateString, T rangeDateDelimiters,
      DateQualification requestedDateQualification, boolean flexibleDateBuild) throws DateExtractionException;

  /**
   * Extract the date normalization result for the range.
   *
   * @param startDateNormalizationResult the extracted start date boundary
   * @param dateString the date string
   * @param rangeDateDelimiters the range date delimiters
   * @param requestedDateQualification the requested qualification
   * @param flexibleDateBuild the boolean opting flexible date build
   * @return the date normalization result
   * @throws DateExtractionException if the date extraction failed
   */
  DateNormalizationResult extractEndDateNormalizationResult(DateNormalizationResult startDateNormalizationResult,
      String dateString, T rangeDateDelimiters, DateQualification requestedDateQualification, boolean flexibleDateBuild)
      throws DateExtractionException;

  /**
   * Checks if a provided date range was successfully extracted
   *
   * @param rangeDateDelimiters the range date delimiters
   * @param startDateResult the extracted start date boundary
   * @param endDateResult the extracted end date boundary
   * @return the boolean representing a successful date range extraction
   */
  boolean isRangeMatchSuccess(T rangeDateDelimiters, DateNormalizationResult startDateResult,
      DateNormalizationResult endDateResult);

  /**
   * Get the date normalization extractor match identifier from the two date boundaries.
   *
   * @param startDateResult the start date boundary
   * @param endDateResult the end date boundary
   * @return the date normalization extractor match identifier
   */
  DateNormalizationExtractorMatchId getDateNormalizationExtractorId(DateNormalizationResult startDateResult,
      DateNormalizationResult endDateResult);

  /**
   * Extract the date normalization result for a range.
   * <p>
   * The date is split in two boundaries using the {@link T} to provide the separators. The result will contain the first split
   * that is exactly splitting the original value in two parts(boundaries) and those two boundaries are valid parsable boundaries
   * or null if none found.
   * </p>
   *
   * @param inputValue the range value to attempt parsing
   * @param requestedDateQualification the overwriting value of date qualification, if any
   * @param flexibleDateBuild the flag indicating if during creating of the dates we are flexible with validation
   * @return the date normalization result
   * @throws DateExtractionException if anything happened during the extraction of the date
   */
  default DateNormalizationResult extractRange(String inputValue, DateQualification requestedDateQualification,
      boolean flexibleDateBuild) throws DateExtractionException {
    final String sanitizedValue = DateFieldSanitizer.cleanSpacesAndTrim(inputValue);
    DateNormalizationResult startDateResult;
    DateNormalizationResult endDateResult = DateNormalizationResult.getNoMatchResult(inputValue);
    DateNormalizationResult rangeDate = DateNormalizationResult.getNoMatchResult(inputValue);
    for (T numericRangeDateDelimiters : getDateDelimiters()) {
      // Split with -1 limit does not discard empty splits
      final String[] sanitizedDateSplitArray = sanitizedValue.split(getDatesSeparator(numericRangeDateDelimiters), -1);
      // The sanitizedDateSplitArray has to be exactly in two, and then we can verify.
      // This also guarantees that the separator used is not used for unknown characters.
      if (sanitizedDateSplitArray.length == 2) {
        // Try extraction and verify
        startDateResult = extractStartDateNormalizationResult(sanitizedDateSplitArray[0], numericRangeDateDelimiters,
            requestedDateQualification,
            flexibleDateBuild);
        if (startDateResult.getDateNormalizationResultStatus() == DateNormalizationResultStatus.MATCHED) {
          endDateResult = extractEndDateNormalizationResult(startDateResult, sanitizedDateSplitArray[1],
              numericRangeDateDelimiters,
              requestedDateQualification, flexibleDateBuild);
        }
        if (isRangeMatchSuccess(numericRangeDateDelimiters, startDateResult, endDateResult)) {
          final DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId =
              getDateNormalizationExtractorId(startDateResult, endDateResult);
          final IntervalEdtfDate intervalEdtfDate = new IntervalEdtfDateBuilder((InstantEdtfDate) startDateResult.getEdtfDate(),
              (InstantEdtfDate) endDateResult.getEdtfDate()).withFlexibleDateBuild(flexibleDateBuild).build();
          rangeDate = new DateNormalizationResult(dateNormalizationExtractorMatchId, inputValue, intervalEdtfDate);
          break;
        }

      }
    }
    return rangeDate;
  }
}
