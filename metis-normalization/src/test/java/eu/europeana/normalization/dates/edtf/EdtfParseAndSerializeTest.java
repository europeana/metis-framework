package eu.europeana.normalization.dates.edtf;

import java.text.ParseException;
import org.junit.jupiter.api.Test;

public class EdtfParseAndSerializeTest {

  EdtfParser parser = new EdtfParser();

  @Test
  void parseDate() throws ParseException {
    String dateStr = "2004-01-01";
    InstantEdtfDate parse = (InstantEdtfDate) parser.parse(dateStr);
    assert (parse.getEdtfDatePart().getYear() == 2004);
    assert (parse.getEdtfDatePart().getMonth() == 1);
    assert (parse.getEdtfDatePart().getDay() == 1);
    assert (parse.toString().equals(dateStr));

    dateStr = "2004-01";
    parse = (InstantEdtfDate) parser.parse(dateStr);
    assert (parse.getEdtfDatePart().getYear() == 2004);
    assert (parse.getEdtfDatePart().getMonth() == 1);
    assert (parse.getEdtfDatePart().getDay() == null);
    assert (parse.toString().equals(dateStr));

    dateStr = "2004";
    parse = (InstantEdtfDate) parser.parse(dateStr);
    assert (parse.getEdtfDatePart().getYear() == 2004);
    assert (parse.getEdtfDatePart().getMonth() == null);
    assert (parse.getEdtfDatePart().getDay() == null);
    assert (parse.toString().equals(dateStr));

    dateStr = "2004-01?";
    parse = (InstantEdtfDate) parser.parse(dateStr);
    assert (parse.getEdtfDatePart().isUncertain());
    assert (parse.getEdtfDatePart().getYear() == 2004);
    assert (parse.getEdtfDatePart().getMonth() == 1);
    assert (parse.getEdtfDatePart().getDay() == null);
    assert (parse.toString().equals(dateStr));

    dateStr = "2004~";
    parse = (InstantEdtfDate) parser.parse(dateStr);
    assert (parse.getEdtfDatePart().isApproximate());
    assert (parse.getEdtfDatePart().getYear() == 2004);
    assert (parse.getEdtfDatePart().getMonth() == null);
    assert (parse.getEdtfDatePart().getDay() == null);
    assert (parse.toString().equals(dateStr));

    dateStr = "2004-01-01%";
    parse = (InstantEdtfDate) parser.parse(dateStr);
    assert (parse.getEdtfDatePart().isUncertain());
    assert (parse.getEdtfDatePart().isApproximate());
    assert (parse.getEdtfDatePart().getYear() == 2004);
    assert (parse.getEdtfDatePart().getMonth() == 1);
    assert (parse.getEdtfDatePart().getDay() == 1);
    assert (parse.toString().equals(dateStr));

    dateStr = "Y-200000";
    parse = (InstantEdtfDate) parser.parse(dateStr);
    assert (parse.getEdtfDatePart().getYear() == -200000);
    assert (parse.toString().equals(dateStr));

    dateStr = "Y200000";
    parse = (InstantEdtfDate) parser.parse(dateStr);
    assert (parse.getEdtfDatePart().getYear() == 200000);
    assert (parse.toString().equals(dateStr));

    dateStr = "..";
    parse = (InstantEdtfDate) parser.parse(dateStr);
    assert (parse.getEdtfDatePart().isUnspecified());
    assert (parse.getEdtfDatePart().getYear() == null);
    assert (parse.getEdtfDatePart().getMonth() == null);
    assert (parse.getEdtfDatePart().getDay() == null);
    assert (parse.toString().equals(dateStr));
  }

  @Test
  void parseTime() throws ParseException {
    String dateStr = "2004-01-01T23:05:02";
    String resultString = "2004-01-01";
    InstantEdtfDate parse = (InstantEdtfDate) parser.parse(dateStr);
    assert (parse.getEdtfDatePart().getYear() == 2004);
    assert (parse.getEdtfDatePart().getMonth() == 1);
    assert (parse.getEdtfDatePart().getDay() == 1);
    assert (parse.toString().equals(resultString));

    dateStr = "2004-01-01T23:05";
    resultString = "2004-01-01";
    parse = (InstantEdtfDate) parser.parse(dateStr);
    assert (parse.toString().equals(resultString));

    dateStr = "2004-01-01T23";
    resultString = "2004-01-01";
    parse = (InstantEdtfDate) parser.parse(dateStr);
    assert (parse.toString().equals(resultString));
  }

  @Test
  void parseInterval() throws ParseException {
    String dateStr = "2004-01-01/2004-01-02";
    IntervalEdtfDate parse = (IntervalEdtfDate) parser.parse(dateStr);
    assert (parse.getStart().getEdtfDatePart().getYear() == 2004);
    assert (parse.getStart().getEdtfDatePart().getMonth() == 1);
    assert (parse.getStart().getEdtfDatePart().getDay() == 1);
    assert (parse.getEnd().getEdtfDatePart().getYear() == 2004);
    assert (parse.getEnd().getEdtfDatePart().getMonth() == 1);
    assert (parse.getEnd().getEdtfDatePart().getDay() == 2);
    assert (parse.toString().equals(dateStr));

    dateStr = "2004-01-01/2005";
    parse = (IntervalEdtfDate) parser.parse(dateStr);
    assert (parse.getStart().getEdtfDatePart().getYear() == 2004);
    assert (parse.getStart().getEdtfDatePart().getMonth() == 1);
    assert (parse.getStart().getEdtfDatePart().getDay() == 1);
    assert (parse.getEnd().getEdtfDatePart().getYear() == 2005);
    assert (parse.getEnd().getEdtfDatePart().getMonth() == null);
    assert (parse.getEnd().getEdtfDatePart().getDay() == null);
    assert (parse.toString().equals(dateStr));

    dateStr = "2004/2005";
    parse = (IntervalEdtfDate) parser.parse(dateStr);
    assert (parse.getStart().getEdtfDatePart().getYear() == 2004);
    assert (parse.getStart().getEdtfDatePart().getMonth() == null);
    assert (parse.getStart().getEdtfDatePart().getDay() == null);
    assert (parse.getEnd().getEdtfDatePart().getYear() == 2005);
    assert (parse.getEnd().getEdtfDatePart().getMonth() == null);
    assert (parse.getEnd().getEdtfDatePart().getDay() == null);
    assert (parse.toString().equals(dateStr));

    dateStr = "2004?/2005~";
    parse = (IntervalEdtfDate) parser.parse(dateStr);
    assert (parse.getStart().getEdtfDatePart().getYear() == 2004);
    assert (parse.getStart().getEdtfDatePart().getMonth() == null);
    assert (parse.getStart().getEdtfDatePart().getDay() == null);
    assert (parse.getStart().getEdtfDatePart().isUncertain());
    assert (parse.getEnd().getEdtfDatePart().getYear() == 2005);
    assert (parse.getEnd().getEdtfDatePart().getMonth() == null);
    assert (parse.getEnd().getEdtfDatePart().getDay() == null);
    assert (parse.getEnd().getEdtfDatePart().isApproximate());
    assert (parse.toString().equals(dateStr));
  }
}