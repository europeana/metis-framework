package eu.europeana.normalization.dates.extraction;

import eu.europeana.normalization.dates.Match;
import eu.europeana.normalization.dates.MatchId;
import eu.europeana.normalization.dates.edtf.EdtfParser;
import eu.europeana.normalization.dates.edtf.TemporalEntity;
import java.text.ParseException;

/**
 * The pattern for EDTF dates. Also compatible with ISO 8601 dates.
 */
public class PatternEdtf implements DateExtractor {

  EdtfParser parser = new EdtfParser();

  public Match extract(String inputValue) {
    try {
      TemporalEntity parsed = parser.parse(inputValue);
      parsed.removeTime();
      return new Match(MatchId.Edtf, inputValue, parsed);
    } catch (ParseException | NumberFormatException e) {
      return null;
    }
  }

}
