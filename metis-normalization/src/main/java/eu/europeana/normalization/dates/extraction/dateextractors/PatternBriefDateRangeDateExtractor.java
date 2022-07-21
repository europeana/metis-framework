package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.EdtfDatePart;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A date range where the end year includes only the rightmost two digits. This pattern needs to be executed before the Edtf one.
 * Most values that match this pattern also match the EDTF pattern, but would result in an invalid date. This pattern only matches
 * values that would not be valid EDTF dates.
 */
public class PatternBriefDateRangeDateExtractor implements DateExtractor {

  Pattern briefDateRangePattern = Pattern
      .compile("(?<uncertain>\\?\\s*)?(?<start>\\d\\d\\d\\d?)[\\-/](?<end>\\d\\d)(?<uncertain2>\\s*\\?)?");

  public DateNormalizationResult extract(String inputValue) {
    Matcher m = briefDateRangePattern.matcher(inputValue.trim());

    if (m.matches()) {
      EdtfDatePart dStart = new EdtfDatePart();
      dStart.setYear(Integer.parseInt(m.group("start")));
      int endYear = Integer.parseInt(m.group("end"));
      if (endYear > 12) {
        int startYear = dStart.getYear() % 100;
        if (startYear < endYear) {
          EdtfDatePart dEnd = new EdtfDatePart();
          dEnd.setYear((dStart.getYear() / 100) * 100 + endYear);

          if (m.group("uncertain") != null || m.group("uncertain2") != null) {
            dStart.setUncertain(true);
            dEnd.setUncertain(true);
          }
          return new DateNormalizationResult(DateNormalizationExtractorMatchId.BRIEF_DATE_RANGE, inputValue,
              new IntervalEdtfDate(new InstantEdtfDate(dStart), new InstantEdtfDate(dEnd)));
        }
      }
    }
    return null;
  }

}
