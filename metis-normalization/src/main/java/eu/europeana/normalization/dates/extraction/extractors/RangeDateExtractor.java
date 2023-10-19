package eu.europeana.normalization.dates.extraction.extractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import eu.europeana.normalization.dates.extraction.DatesSeparator;
import java.util.List;

/**
 * The interface for range date extractors.
 *
 * @param <T> the object containing delimiters/separators for dates
 */
public interface RangeDateExtractor<T extends DatesSeparator> {

  List<T> getRangeDateQualifiers();

  /**
   * Extract the start and end date normalization result pair.
   * <p>At this stage we just perform an extraction, the range is not verified yet.</p>
   *
   * @param startString the start date string
   * @param endString the end date string
   * @param rangeDateDelimiters the range date delimiters
   * @param allowDayMonthSwap the boolean opting flexible date build
   * @return the start and end date result pair
   * @throws DateExtractionException if the date extraction failed
   */
  DateNormalizationResultRangePair extractDateNormalizationResult(
      String startString, String endString, T rangeDateDelimiters,
      boolean allowDayMonthSwap) throws DateExtractionException;

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
   * Class wrapping a pair of start and end dates.
   */
  class DateNormalizationResultRangePair {

    final DateNormalizationResult startDateNormalizationResult;
    final DateNormalizationResult endDateNormalizationResult;

    public DateNormalizationResultRangePair(DateNormalizationResult startDateNormalizationResult,
        DateNormalizationResult endDateNormalizationResult) {
      this.startDateNormalizationResult = startDateNormalizationResult;
      this.endDateNormalizationResult = endDateNormalizationResult;
    }

    public DateNormalizationResult getStartDateNormalizationResult() {
      return startDateNormalizationResult;
    }

    public DateNormalizationResult getEndDateNormalizationResult() {
      return endDateNormalizationResult;
    }
  }
}
