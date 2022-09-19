package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.YearPrecision;
import eu.europeana.normalization.dates.edtf.EdtfDatePart;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
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

  public DateNormalizationResult extract(String inputValue) {
    Matcher m = patUncertainEnding.matcher(inputValue);
    if (m.matches()) {
      EdtfDatePart d = new EdtfDatePart();
      d.setYearPrecision(YearPrecision.DECADE);
      d.setYear(Integer.parseInt(m.group("year")) * 10);
      if (m.group("uncertain") != null || m.group("uncertain2") != null) {
        d.setUncertain(true);
      }
      return new DateNormalizationResult(DateNormalizationExtractorMatchId.DECADE, inputValue, new InstantEdtfDate(d));
    }
    m = patUncertainBegining.matcher(inputValue);
    if (m.matches()) {
      EdtfDatePart d = new EdtfDatePart();
      d.setYearPrecision(YearPrecision.DECADE);
      d.setYear(Integer.parseInt(m.group("year")) * 10);
      if (m.group("uncertain") != null) {
        d.setUncertain(true);
      }
      return new DateNormalizationResult(DateNormalizationExtractorMatchId.DECADE, inputValue, new InstantEdtfDate(d));
    }
    return null;
  }

}
