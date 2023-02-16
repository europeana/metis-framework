package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.AbstractEdtfDate;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.EdtfParser;
import java.time.DateTimeException;

/**
 * The pattern for EDTF dates and compatible with ISO 8601 dates.
 */
public class PatternEdtfDateExtractor implements DateExtractor {

  final EdtfParser edtfParser = new EdtfParser();

  @Override
  public DateNormalizationResult extract(String inputValue, DateQualification requestedDateQualification,
      boolean allowSwitchMonthDay) {
    try {
      AbstractEdtfDate edtfDate = edtfParser.parse(inputValue, requestedDateQualification, allowSwitchMonthDay);
      return new DateNormalizationResult(DateNormalizationExtractorMatchId.EDTF, inputValue, edtfDate);
    } catch (DateTimeException | NumberFormatException e) {
      return null;
    }
  }
}
