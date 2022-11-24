package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS_XX;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_RANGE_ALL_VARIANTS;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_RANGE_ALL_VARIANTS_XX;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.EdtfDatePart;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import eu.europeana.normalization.dates.extraction.NumericRangeWithMissingPartsPattern;
import eu.europeana.normalization.dates.extraction.NumericRangeWithMissingPartsPattern.NumericRangeSpecialCharacters;
import eu.europeana.normalization.dates.sanitize.DateFieldSanitizer;

/**
 * Patterns for numeric date ranges with variations in the separators of date components.
 * <p>We reuse the already existent {@link NumericWithMissingPartsDateExtractor} code for the edges.</p>
 */
public class NumericRangeWithMissingPartsDateExtractor implements DateExtractor {

  private static final NumericWithMissingPartsDateExtractor NUMERIC_WITH_MISSING_PARTS_DATE_EXTRACTOR = new NumericWithMissingPartsDateExtractor();

  // TODO: 04/11/2022 Verify the below of 1000 year edges from previous code. Same applies for NumericRangeWithMissingPartsDateExtractor.
  // TODO: 04/11/2022 Also check EdtfDatePartNormalizerTest cases that are temporarily commented out
  //  if (dEnd.isUnspecified() && dStart.getYear() != null && dStart.getYear() < 1000) {
  //    return null;// these cases are ambiguous. Example '187-?'
  //  }

  public DateNormalizationResult extract(String inputValue) {
    final String sanitizedValue = DateFieldSanitizer.cleanSpacesAndTrim(inputValue);
    DateNormalizationResult startDate;
    DateNormalizationResult endDate;
    DateNormalizationResult rangeDate = null;
    for (NumericRangeSpecialCharacters numericRangeSpecialCharacters : NumericRangeSpecialCharacters.values()) {
      final String[] split = sanitizedValue.split(numericRangeSpecialCharacters.getDatesSeparator());
      //The split has to be exactly in two, and then we can verify. This also guarantees that the separator used is not used for unknown characters
      if (split.length == 2) {
        //Try extraction and verify
        startDate = extractDateNormalizationResult(split[0], numericRangeSpecialCharacters);
        endDate = extractDateNormalizationResult(split[1], numericRangeSpecialCharacters);
        if (startDate != null && endDate != null) {
          final DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId =
              getDateNormalizationExtractorId(startDate, endDate);
          final IntervalEdtfDate intervalEdtfDate = new IntervalEdtfDate((InstantEdtfDate) startDate.getEdtfDate(),
              (InstantEdtfDate) endDate.getEdtfDate());
          rangeDate = new DateNormalizationResult(dateNormalizationExtractorMatchId, inputValue, intervalEdtfDate);
          break;
        }
      }
    }
    return rangeDate;
  }

  private DateNormalizationResult extractDateNormalizationResult(String dateString,
      NumericRangeSpecialCharacters numericRangeSpecialCharacters) {
    final DateNormalizationResult dateNormalizationResult;
    if (numericRangeSpecialCharacters.getUnspecifiedCharacters() != null && dateString.matches(
        numericRangeSpecialCharacters.getUnspecifiedCharacters())) {
      dateNormalizationResult = new DateNormalizationResult(NUMERIC_ALL_VARIANTS, dateString,
          new InstantEdtfDate(EdtfDatePart.getUnspecifiedInstance()));
    } else {
      dateNormalizationResult = NUMERIC_WITH_MISSING_PARTS_DATE_EXTRACTOR.extract(dateString,
          NumericRangeWithMissingPartsPattern.values());
    }
    return dateNormalizationResult;
  }

  private static DateNormalizationExtractorMatchId getDateNormalizationExtractorId(DateNormalizationResult startDate,
      DateNormalizationResult endDate) {
    final boolean isStartXX = startDate.getDateNormalizationExtractorMatchId() == NUMERIC_ALL_VARIANTS_XX;
    final boolean isEndXX = endDate.getDateNormalizationExtractorMatchId() == NUMERIC_ALL_VARIANTS_XX;
    return isStartXX || isEndXX ? NUMERIC_RANGE_ALL_VARIANTS_XX : NUMERIC_RANGE_ALL_VARIANTS;
  }
}
