package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.EdtfDatePart;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import eu.europeana.normalization.dates.extraction.dateextractors.NumericRangeWithMissingPartsPattern.NumericRangeSpecialCharacters;
import eu.europeana.normalization.dates.sanitize.DateFieldSanitizer;

/**
 * Patterns for numeric date ranges with variations in the separators of date components.
 * <p>We reuse the already existent {@link NumericWithMissingPartsDateExtractor} code for the edges.</p>
 */
public class NumericRangeWithMissingPartsDateExtractor implements DateExtractor {

  private static final NumericWithMissingPartsDateExtractor NUMERIC_WITH_MISSING_PARTS_DATE_EXTRACTOR = new NumericWithMissingPartsDateExtractor();

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
          rangeDate = new DateNormalizationResult(DateNormalizationExtractorMatchId.NUMERIC_RANGE_ALL_VARIANTS, inputValue,
              new IntervalEdtfDate((InstantEdtfDate) startDate.getEdtfDate(), (InstantEdtfDate) endDate.getEdtfDate()));
          break;
        }
      }
    }
    return rangeDate;
  }

  private DateNormalizationResult extractDateNormalizationResult(String dateString,
      NumericRangeSpecialCharacters numericRangeSpecialCharacters) {
    DateNormalizationResult dateNormalizationResult;
    // TODO: 01/11/2022 Potentially the unspecified part could be part of the NumericWithMissingPartsDateExtractor
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
}
