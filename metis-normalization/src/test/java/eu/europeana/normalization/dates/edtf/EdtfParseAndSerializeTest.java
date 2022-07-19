package eu.europeana.normalization.dates.edtf;

import java.text.ParseException;
import org.junit.jupiter.api.Test;

public class EdtfParseAndSerializeTest {

  EdtfParser parser = new EdtfParser();
  EdtfSerializer serializer = new EdtfSerializer();

  @Test
  void parseDate() throws ParseException {
    String dateStr = "2004-01-01";
    Instant parse = (Instant) parser.parse(dateStr);
    assert (parse.getTime() == null);
    assert (parse.getDate().getYear() == 2004);
    assert (parse.getDate().getMonth() == 1);
    assert (parse.getDate().getDay() == 1);
    assert (serializer.serialize(parse).equals(dateStr));

    dateStr = "2004-01";
    parse = (Instant) parser.parse(dateStr);
    assert (parse.getTime() == null);
    assert (parse.getDate().getYear() == 2004);
    assert (parse.getDate().getMonth() == 1);
    assert (parse.getDate().getDay() == null);
    assert (serializer.serialize(parse).equals(dateStr));

    dateStr = "2004";
    parse = (Instant) parser.parse(dateStr);
    assert (parse.getTime() == null);
    assert (parse.getDate().getYear() == 2004);
    assert (parse.getDate().getMonth() == null);
    assert (parse.getDate().getDay() == null);
    assert (serializer.serialize(parse).equals(dateStr));

    dateStr = "2004-01?";
    parse = (Instant) parser.parse(dateStr);
    assert (parse.getDate().isUncertain());
    assert (parse.getDate().getYear() == 2004);
    assert (parse.getDate().getMonth() == 1);
    assert (parse.getDate().getDay() == null);
    assert (serializer.serialize(parse).equals(dateStr));

    dateStr = "2004~";
    parse = (Instant) parser.parse(dateStr);
    assert (parse.getDate().isApproximate());
    assert (parse.getDate().getYear() == 2004);
    assert (parse.getDate().getMonth() == null);
    assert (parse.getDate().getDay() == null);
    assert (serializer.serialize(parse).equals(dateStr));

    dateStr = "2004-01-01%";
    parse = (Instant) parser.parse(dateStr);
    assert (parse.getDate().isUncertain());
    assert (parse.getDate().isApproximate());
    assert (parse.getDate().getYear() == 2004);
    assert (parse.getDate().getMonth() == 1);
    assert (parse.getDate().getDay() == 1);
    assert (serializer.serialize(parse).equals(dateStr));

    dateStr = "Y-200000";
    parse = (Instant) parser.parse(dateStr);
    assert (parse.getDate().getYear() == -200000);
    assert (serializer.serialize(parse).equals(dateStr));

    dateStr = "Y200000";
    parse = (Instant) parser.parse(dateStr);
    assert (parse.getDate().getYear() == 200000);
    assert (serializer.serialize(parse).equals(dateStr));

    dateStr = "..";
    parse = (Instant) parser.parse(dateStr);
    assert (parse.getDate().isUnspecified());
    assert (parse.getDate().getYear() == null);
    assert (parse.getDate().getMonth() == null);
    assert (parse.getDate().getDay() == null);
    assert (serializer.serialize(parse).equals(dateStr));
  }

  @Test
  void parseTime() throws ParseException {
    String dateStr = "2004-01-01T23:05:02";
    Instant parse = (Instant) parser.parse(dateStr);
    assert (parse.getDate().getYear() == 2004);
    assert (parse.getDate().getMonth() == 1);
    assert (parse.getDate().getDay() == 1);
    assert (parse.getTime().getHour() == 23);
    assert (parse.getTime().getMinute() == 5);
    assert (parse.getTime().getSecond() == 2);
    assert (serializer.serialize(parse).equals(dateStr));

    dateStr = "2004-01-01T23:05";
    parse = (Instant) parser.parse(dateStr);
    assert (parse.getTime().getHour() == 23);
    assert (parse.getTime().getMinute() == 5);
    assert (parse.getTime().getSecond() == null);
    assert (serializer.serialize(parse).equals(dateStr));

    dateStr = "2004-01-01T23";
    parse = (Instant) parser.parse(dateStr);
    assert (parse.getTime().getHour() == 23);
    assert (parse.getTime().getMinute() == null);
    assert (serializer.serialize(parse).equals(dateStr));
  }

  @Test
  void parseInterval() throws ParseException {
    String dateStr = "2004-01-01/2004-01-02";
    Interval parse = (Interval) parser.parse(dateStr);
    assert (parse.getStart().getTime() == null);
    assert (parse.getStart().getDate().getYear() == 2004);
    assert (parse.getStart().getDate().getMonth() == 1);
    assert (parse.getStart().getDate().getDay() == 1);
    assert (parse.getEnd().getDate().getYear() == 2004);
    assert (parse.getEnd().getDate().getMonth() == 1);
    assert (parse.getEnd().getDate().getDay() == 2);
    assert (serializer.serialize(parse).equals(dateStr));

    dateStr = "2004-01-01/2005";
    parse = (Interval) parser.parse(dateStr);
    assert (parse.getStart().getTime() == null);
    assert (parse.getStart().getDate().getYear() == 2004);
    assert (parse.getStart().getDate().getMonth() == 1);
    assert (parse.getStart().getDate().getDay() == 1);
    assert (parse.getEnd().getDate().getYear() == 2005);
    assert (parse.getEnd().getDate().getMonth() == null);
    assert (parse.getEnd().getDate().getDay() == null);
    assert (serializer.serialize(parse).equals(dateStr));

    dateStr = "2004/2005";
    parse = (Interval) parser.parse(dateStr);
    assert (parse.getStart().getTime() == null);
    assert (parse.getStart().getDate().getYear() == 2004);
    assert (parse.getStart().getDate().getMonth() == null);
    assert (parse.getStart().getDate().getDay() == null);
    assert (parse.getEnd().getDate().getYear() == 2005);
    assert (parse.getEnd().getDate().getMonth() == null);
    assert (parse.getEnd().getDate().getDay() == null);
    assert (serializer.serialize(parse).equals(dateStr));

    dateStr = "2004?/2005~";
    parse = (Interval) parser.parse(dateStr);
    assert (parse.getStart().getTime() == null);
    assert (parse.getStart().getDate().getYear() == 2004);
    assert (parse.getStart().getDate().getMonth() == null);
    assert (parse.getStart().getDate().getDay() == null);
    assert (parse.getStart().getDate().isUncertain());
    assert (parse.getEnd().getDate().getYear() == 2005);
    assert (parse.getEnd().getDate().getMonth() == null);
    assert (parse.getEnd().getDate().getDay() == null);
    assert (parse.getEnd().getDate().isApproximate());
    assert (serializer.serialize(parse).equals(dateStr));
  }

  @Test
  void parseInvalid() throws ParseException {
    String dateStr = "";
    try {
      Interval parse = (Interval) parser.parse(dateStr);
      assert (false);
    } catch (ParseException e) {
      // OK
    }
  }
}