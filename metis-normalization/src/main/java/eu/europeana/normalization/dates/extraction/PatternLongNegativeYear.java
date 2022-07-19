package eu.europeana.normalization.dates.extraction;

import eu.europeana.normalization.dates.Match;
import eu.europeana.normalization.dates.MatchId;
import eu.europeana.normalization.dates.edtf.EDTFDatePart;
import eu.europeana.normalization.dates.edtf.InstantEDTFDate;
import eu.europeana.normalization.dates.edtf.IntervalEDTFDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A year before 1 AD with more than 4 digits. This pattern is typically used in archaeological contexts. The year may contain
 * between 5 and 9 digits. Aso includes the pattern for ranges of this kind of years.
 */
public class PatternLongNegativeYear implements DateExtractor {

  Pattern patYyyyyy = Pattern.compile("\\s*(?<uncertain>\\?)?(?<year>\\-\\d{5,9})(?<uncertain2>\\?)?\\s*",
      Pattern.CASE_INSENSITIVE);
  Pattern patYyyyyyRange = Pattern.compile(
      "\\s*(?<uncertain>\\?)?(?<year>\\-\\d{5,9})\\s*/\\s*(?<year2>\\-\\d{5,9})(?<uncertain2>\\?)?\\s*",
      Pattern.CASE_INSENSITIVE);

  public Match extract(String inputValue) {
    Matcher m;
    m = patYyyyyy.matcher(inputValue);
    if (m.matches()) {
      EDTFDatePart d = new EDTFDatePart();
      d.setYear(Integer.parseInt(m.group("year")));
      if (m.group("uncertain") != null || m.group("uncertain2") != null) {
        d.setUncertain(true);
      }
      return new Match(MatchId.LongYear, inputValue, new InstantEDTFDate(d));
    }
    m = patYyyyyyRange.matcher(inputValue);
    if (m.matches()) {
      EDTFDatePart start = new EDTFDatePart();
      start.setYear(Integer.parseInt(m.group("year")));
      EDTFDatePart end = new EDTFDatePart();
      end.setYear(Integer.parseInt(m.group("year2")));
      IntervalEDTFDate intervalEDTFDate = new IntervalEDTFDate(new InstantEDTFDate(start), new InstantEDTFDate(end));
      if (m.group("uncertain") != null || m.group("uncertain2") != null) {
        intervalEDTFDate.setUncertain(true);
      }
      return new Match(MatchId.LongYear, inputValue, intervalEDTFDate);
    }
    return null;
  }

}
