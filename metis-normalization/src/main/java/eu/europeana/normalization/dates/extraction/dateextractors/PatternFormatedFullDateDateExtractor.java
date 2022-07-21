package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.Match;
import eu.europeana.normalization.dates.MatchId;
import eu.europeana.normalization.dates.edtf.EDTFDatePart;
import eu.europeana.normalization.dates.edtf.InstantEDTFDate;
import eu.europeana.normalization.dates.extraction.MonthMultilingual;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Patterns for date formats that are well-structured but do not follow a particular standard
 */
public class PatternFormatedFullDateDateExtractor implements DateExtractor {

  MonthMultilingual monthNames = new MonthMultilingual();

  // "Thu Dec 31 01:00:00 CET 1863","31 Dec 1863"
  // month day hour minute second year
  Pattern patFormatedDate = Pattern
      .compile("\\w{3} (\\w{3}) (\\d{2}) (\\d{2}):(\\d{2}):(\\d{2}) \\w{3,4} (\\d{1,4})");

  // 2020-06-21 13:43:26 UTC
  // year month day hour minute second
  Pattern patFormatedDate2 = Pattern
      .compile("(\\d{4})-(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2}) \\w{3,4}\\s?(\\d{0,4})");

  // 2018-03-27 09:08:34
  // year month day hour minute second
  Pattern patFormatedDate3 = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2})(\\.\\d{1,3})?");

  public Match extract(String inputValue) {
    Matcher m = patFormatedDate2.matcher(inputValue);
    if (m.matches()) {
      EDTFDatePart d = new EDTFDatePart();
      d.setYear(Integer.parseInt(m.group(1)));
      d.setMonth(Integer.parseInt(m.group(2)));
      d.setDay(Integer.parseInt(m.group(3)));
      // discard the time
      //			Time t=new Time();
      //			t.setHour(Integer.parseInt(m.group(4)));
      //			t.setMinute(Integer.parseInt(m.group(5)));
      //			t.setSecond(Integer.parseInt(m.group(6)));
      return new Match(MatchId.FormatedFullDate, inputValue, new InstantEDTFDate(d));
    }
    m = patFormatedDate.matcher(inputValue);
    if (m.matches()) {
      EDTFDatePart d = new EDTFDatePart();
      d.setYear(Integer.parseInt(m.group(6)));
      d.setMonth(monthNames.parse(m.group(1)));
      d.setDay(Integer.parseInt(m.group(2)));
      // discard the time
      //			Time t=new Time();
      //			t.setHour(Integer.parseInt(m.group(3)));
      //			t.setMinute(Integer.parseInt(m.group(4)));
      //			t.setSecond(Integer.parseInt(m.group(5)));
      return new Match(MatchId.FormatedFullDate, inputValue, new InstantEDTFDate(d));
    }
    m = patFormatedDate3.matcher(inputValue);
    if (m.matches()) {
      EDTFDatePart d = new EDTFDatePart();
      d.setYear(Integer.parseInt(m.group(1)));
      d.setMonth(Integer.parseInt(m.group(2)));
      d.setDay(Integer.parseInt(m.group(3)));
      // discard the time
      //			Time t=new Time();
      //			t.setHour(Integer.parseInt(m.group(4)));
      //			t.setMinute(Integer.parseInt(m.group(5)));
      //			t.setSecond(Integer.parseInt(m.group(6)));
      return new Match(MatchId.FormatedFullDate, inputValue, new InstantEDTFDate(d));
    }
    return null;
  }

}
