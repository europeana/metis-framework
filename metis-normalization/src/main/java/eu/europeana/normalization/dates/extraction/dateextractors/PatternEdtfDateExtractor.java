package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.AbstractEdtfDate;
import eu.europeana.normalization.dates.edtf.EdtfParser;
import java.text.ParseException;

/**
 * The pattern for EDTF dates and compatible with ISO 8601 dates.
 */
public class PatternEdtfDateExtractor implements DateExtractor {

  final EdtfParser edtfParser = new EdtfParser();

  @Override
  public DateNormalizationResult extract(String inputValue) {
    try {
      AbstractEdtfDate edtfDate = edtfParser.parse(inputValue);
      return new DateNormalizationResult(DateNormalizationExtractorMatchId.EDTF, inputValue, edtfDate);
    } catch (ParseException | NumberFormatException e) {
      return null;
    }
  }
}
