package eu.europeana.normalization.dates.extraction;

import eu.europeana.normalization.dates.Match;
import eu.europeana.normalization.dates.MatchId;
import eu.europeana.normalization.dates.edtf.EDTFDatePart;
import eu.europeana.normalization.dates.edtf.EDTFDatePart.YearPrecision;
import eu.europeana.normalization.dates.edtf.InstantEDTFDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A decade represented as YYYu or YYYx. For example, '198u', '198x' Dates such as '198-' are not supported because they may
 * indicate a decade or a time period with an open end
 */
public class PatternDecadeDateExtractor implements DateExtractor {

  Pattern patUncertainBegining = Pattern.compile("\\s*(?<uncertain>\\?)?(?<year>\\d\\d\\d)[xu]\\s*",
      Pattern.CASE_INSENSITIVE);
  Pattern patUncertainEnding = Pattern.compile(
      "\\s*(?<year>\\d\\d\\d)([ux](?<uncertain>\\?)?|\\?(?<uncertain2>\\?))\\s*", Pattern.CASE_INSENSITIVE);

  public Match extract(String inputValue) {
    Matcher m = patUncertainEnding.matcher(inputValue);
    if (m.matches()) {
      EDTFDatePart d = new EDTFDatePart();
      d.setYearPrecision(YearPrecision.DECADE);
      d.setYear(Integer.parseInt(m.group("year")) * 10);
      if (m.group("uncertain") != null || m.group("uncertain2") != null) {
        d.setUncertain(true);
      }
      return new Match(MatchId.Decade, inputValue, new InstantEDTFDate(d));
    }
    m = patUncertainBegining.matcher(inputValue);
    if (m.matches()) {
      EDTFDatePart d = new EDTFDatePart();
      d.setYearPrecision(YearPrecision.DECADE);
      d.setYear(Integer.parseInt(m.group("year")) * 10);
      if (m.group("uncertain") != null) {
        d.setUncertain(true);
      }
      return new Match(MatchId.Decade, inputValue, new InstantEDTFDate(d));
    }
    return null;
  }

}
