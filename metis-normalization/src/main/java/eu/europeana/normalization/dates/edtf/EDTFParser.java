package eu.europeana.normalization.dates.edtf;

import static java.lang.String.format;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * This class implements the deserialization of EDTF strings into the EDTF structure.
 */
public class EDTFParser {

  // TODO: 19/07/2022 Simplify regex by potentially splitting it
  private static final Pattern DATE_PATTERN = Pattern
      .compile("((?<year1>-?\\d{4})-(?<month1>\\d{2})-(?<day1>\\d{2})|"
          + "(?<year2>-?\\d{4})-(?<month2>\\d{2})|" + "(?<year3>-?\\d{4})" + ")(?<modifier>[?%~]?)");
  private static final Pattern TIME_PATTERN = Pattern
      .compile("((?<hour1>\\d{2}):(?<minute1>\\d{2}):(?<second1>\\d{2})|" + "(?<hour2>\\d{2}):(?<minute2>\\d{2})|"
          + "(?<hour3>\\d{2})|"
          + "(?<hour4>\\d{2}):(?<minute4>\\d{2}):(?<second4>\\d{2})(\\.(?<millis>\\d{1,3}))?(Z|[+\\-]\\d{2}:?\\d{0,2})?)");


  public AbstractEDTFDate parse(String edtfString) throws ParseException {
    if (StringUtils.isEmpty(edtfString)) {
      throw new ParseException("Empty argument", 0);
    }
    if (edtfString.contains("/")) {
      return parseInterval(edtfString);
    }
    return parseInstant(edtfString);
  }

  protected InstantEDTFDate parseInstant(String edtfString) throws ParseException {
    if (edtfString.contains("T")) {
      String datePart = edtfString.substring(0, edtfString.indexOf('T'));
      String timePart = edtfString.substring(edtfString.indexOf('T') + 1);
      if (datePart.isEmpty()) {
        return new InstantEDTFDate(parseTime(timePart));
      }
      return new InstantEDTFDate(parseDate(datePart), parseTime(timePart));
    } else if (edtfString.contains(":")) {
      return new InstantEDTFDate(parseTime(edtfString));
    } else {
      return new InstantEDTFDate(parseDate(edtfString));
    }
  }

  protected IntervalEDTFDate parseInterval(String edtfString) throws ParseException {
    String startPart = edtfString.substring(0, edtfString.indexOf('/'));
    String endPart = edtfString.substring(edtfString.indexOf('/') + 1);
    InstantEDTFDate start = parseInstant(startPart);
    InstantEDTFDate end = parseInstant(endPart);
    if ((end.getEdtfDatePart().isUnknown() || end.getEdtfDatePart().isUnspecified()) && (start.getEdtfDatePart().isUnknown()
        || start.getEdtfDatePart().isUnspecified())) {
      throw new ParseException(edtfString, 0);
    }
    return new IntervalEDTFDate(start, end);
  }

  protected EDTFTimePart parseTime(String edtfString) throws ParseException {
    Matcher matcher = TIME_PATTERN.matcher(edtfString);
    final EDTFTimePart edtfTimePart;
    if (matcher.matches()) {
      edtfTimePart = new EDTFTimePart();
      if (!StringUtils.isEmpty(matcher.group("hour3"))) {
        edtfTimePart.setHour(Integer.parseInt(matcher.group("hour3")));
      } else if (!StringUtils.isEmpty(matcher.group("hour2"))) {
        edtfTimePart.setHour(Integer.parseInt(matcher.group("hour2")));
        edtfTimePart.setMinute(Integer.parseInt(matcher.group("minute2")));
      } else if (!StringUtils.isEmpty(matcher.group("hour1"))) {
        edtfTimePart.setHour(Integer.parseInt(matcher.group("hour1")));
        edtfTimePart.setMinute(Integer.parseInt(matcher.group("minute1")));
        edtfTimePart.setSecond(Integer.parseInt(matcher.group("second1")));
      } else {
        edtfTimePart.setHour(Integer.parseInt(matcher.group("hour4")));
        edtfTimePart.setMinute(Integer.parseInt(matcher.group("minute4")));
        edtfTimePart.setSecond(Integer.parseInt(matcher.group("second4")));
      }
    } else {
      throw new ParseException(format("Time parsing does not match for input: %s", edtfString), 0);
    }
    return edtfTimePart;
  }

  protected EDTFDatePart parseDate(String edtfString) throws ParseException {
    final EDTFDatePart edtfDatePart;
    if (edtfString.isEmpty()) {
      edtfDatePart = EDTFDatePart.getUnknownInstance();
    } else if ("..".equals(edtfString)) {
      edtfDatePart = EDTFDatePart.getUnspecifiedInstance();
    } else if (edtfString.startsWith("Y")) {
      edtfDatePart = new EDTFDatePart();
      edtfDatePart.setYear(Integer.parseInt(edtfString.substring(1)));
    } else {
      edtfDatePart = getRegexParsedEDTFDatePart(edtfString);
    }
    return edtfDatePart;
  }

  private EDTFDatePart getRegexParsedEDTFDatePart(String edtfString) throws ParseException {
    final EDTFDatePart edtfDatePart = new EDTFDatePart();
    Matcher matcher = DATE_PATTERN.matcher(edtfString);
    if (matcher.matches()) {
      //Select data based on the name of the regex group matching
      if (StringUtils.isNotEmpty(matcher.group("year3"))) {
        edtfDatePart.setYear(Integer.parseInt(matcher.group("year3")));
      } else if (StringUtils.isNotEmpty(matcher.group("year2"))) {
        edtfDatePart.setYear(Integer.parseInt(matcher.group("year2")));
        edtfDatePart.setMonth(Integer.parseInt(matcher.group("month2")));
      } else {
        edtfDatePart.setYear(Integer.parseInt(matcher.group("year1")));
        edtfDatePart.setMonth(Integer.parseInt(matcher.group("month1")));
        edtfDatePart.setDay(Integer.parseInt(matcher.group("day1")));
      }
      //Check modifier value
      if (StringUtils.isNotEmpty(matcher.group("modifier"))) {
        String modifier = matcher.group("modifier");
        if ("?".equals(modifier)) {
          edtfDatePart.setUncertain(true);
        } else if ("~".equals(modifier)) {
          edtfDatePart.setApproximate(true);
        } else if ("%".equals(modifier)) {
          edtfDatePart.setApproximate(true);
          edtfDatePart.setUncertain(true);
        }
      }
    } else {
      throw new ParseException(format("Date parsing does not match for input: %s", edtfString), 0);
    }
    return edtfDatePart;
  }

}
