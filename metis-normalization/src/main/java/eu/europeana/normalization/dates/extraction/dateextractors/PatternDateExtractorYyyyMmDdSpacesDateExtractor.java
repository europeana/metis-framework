package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.EdtfDatePart;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A complete date using whitespace to separate the date components.
 */
public class PatternDateExtractorYyyyMmDdSpacesDateExtractor implements DateExtractor {

  Pattern patYyyyMmDd = Pattern.compile("\\s*(\\d{4}) (\\d{1,2}) (\\d{1,2})\\s*");
  Pattern patDdMmYyyy = Pattern.compile("\\s*(\\d{1,2}) (\\d{1,2}) (\\d{4})\\s*");

  public DateNormalizationResult extract(String inputValue) {
    Matcher m = patYyyyMmDd.matcher(inputValue);
    if (m.matches()) {
      EdtfDatePart d = new EdtfDatePart();
      d.setYear(Integer.parseInt(m.group(1)));
      d.setMonth(Integer.parseInt(m.group(2)));
      d.setDay(Integer.parseInt(m.group(3)));
      return new DateNormalizationResult(DateNormalizationExtractorMatchId.YYYY_MM_DD_SPACES, inputValue, new InstantEdtfDate(d));
    }
    m = patDdMmYyyy.matcher(inputValue);
    if (m.matches()) {
      EdtfDatePart d = new EdtfDatePart();
      d.setYear(Integer.parseInt(m.group(3)));
      d.setMonth(Integer.parseInt(m.group(2)));
      d.setDay(Integer.parseInt(m.group(1)));
      return new DateNormalizationResult(DateNormalizationExtractorMatchId.YYYY_MM_DD_SPACES, inputValue, new InstantEdtfDate(d));
    }
    return null;
  }

}
