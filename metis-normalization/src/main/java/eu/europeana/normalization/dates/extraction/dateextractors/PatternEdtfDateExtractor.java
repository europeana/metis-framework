package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.Match;
import eu.europeana.normalization.dates.MatchId;
import eu.europeana.normalization.dates.edtf.AbstractEDTFDate;
import eu.europeana.normalization.dates.edtf.EDTFParser;
import java.text.ParseException;

/**
 * The pattern for EDTF dates. Also compatible with ISO 8601 dates.
 */
public class PatternEdtfDateExtractor implements DateExtractor {

  EDTFParser parser = new EDTFParser();

  public Match extract(String inputValue) {
    try {
      AbstractEDTFDate parsed = parser.parse(inputValue);
      parsed.removeTime();
      return new Match(MatchId.Edtf, inputValue, parsed);
    } catch (ParseException | NumberFormatException e) {
      return null;
    }
  }

}
