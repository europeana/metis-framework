package eu.europeana.normalization.dates.edtf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EdtfParseAndSerializeTest {

  // TODO: 14/02/2023 Can be simplified with parameterized tests
  EdtfParser parser = new EdtfParser();

  @Test
  void parseDate() {
    String dateString = "2004-01-01";
    String expectedResultString = "2004-01-01";
    InstantEdtfDate instantEdtfDate = (InstantEdtfDate) parser.parse(dateString, true);
    assertEquals(expectedResultString, instantEdtfDate.toString());

    dateString = "2004-01";
    expectedResultString = "2004-01";
    instantEdtfDate = (InstantEdtfDate) parser.parse(dateString, true);
    assertEquals(expectedResultString, instantEdtfDate.toString());

    dateString = "2004";
    expectedResultString = "2004";
    instantEdtfDate = (InstantEdtfDate) parser.parse(dateString, true);
    assertEquals(expectedResultString, instantEdtfDate.toString());

    dateString = "2004-01?";
    expectedResultString = "2004-01?";
    instantEdtfDate = (InstantEdtfDate) parser.parse(dateString, true);
    assertEquals(expectedResultString, instantEdtfDate.toString());

    dateString = "2004~";
    expectedResultString = "2004~";
    instantEdtfDate = (InstantEdtfDate) parser.parse(dateString, true);
    assertEquals(expectedResultString, instantEdtfDate.toString());

    dateString = "2004-01-01%";
    expectedResultString = "2004-01-01%";
    instantEdtfDate = (InstantEdtfDate) parser.parse(dateString, true);
    assertEquals(expectedResultString, instantEdtfDate.toString());

    dateString = "Y-200000";
    expectedResultString = "Y-200000";
    instantEdtfDate = (InstantEdtfDate) parser.parse(dateString, true);
    assertEquals(expectedResultString, instantEdtfDate.toString());

    dateString = "Y200000";
    expectedResultString = "Y200000";
    instantEdtfDate = (InstantEdtfDate) parser.parse(dateString, true);
    assertEquals(expectedResultString, instantEdtfDate.toString());

    dateString = "..";
    expectedResultString = "..";
    instantEdtfDate = (InstantEdtfDate) parser.parse(dateString, true);
    assertEquals(expectedResultString, instantEdtfDate.toString());
  }

  @Test
  void parseTime() {
    String dateString = "2004-01-01T23:05:02";
    String expectedResultString = "2004-01-01";
    InstantEdtfDate instantEdtfDate = (InstantEdtfDate) parser.parse(dateString, true);
    assertEquals(expectedResultString, instantEdtfDate.toString());

    dateString = "2004-01-01T23:05";
    expectedResultString = "2004-01-01";
    instantEdtfDate = (InstantEdtfDate) parser.parse(dateString, true);
    assertEquals(expectedResultString, instantEdtfDate.toString());

    dateString = "2004-01-01T23";
    expectedResultString = "2004-01-01";
    instantEdtfDate = (InstantEdtfDate) parser.parse(dateString, true);
    assertEquals(expectedResultString, instantEdtfDate.toString());
  }

  @Test
  void parseInterval() {
    String dateString = "2004-01-01/2004-01-02";
    String expectedResultString = "2004-01-01/2004-01-02";
    IntervalEdtfDate intervalEdtfDate = (IntervalEdtfDate) parser.parse(dateString, true);
    assertEquals(expectedResultString, intervalEdtfDate.toString());

    dateString = "2004-01-01/2005";
    expectedResultString = "2004-01-01/2005";
    intervalEdtfDate = (IntervalEdtfDate) parser.parse(dateString, true);
    assertEquals(expectedResultString, intervalEdtfDate.toString());

    dateString = "2004/2005";
    expectedResultString = "2004/2005";
    intervalEdtfDate = (IntervalEdtfDate) parser.parse(dateString, true);
    assertEquals(expectedResultString, intervalEdtfDate.toString());

    dateString = "2004?/2005~";
    expectedResultString = "2004?/2005~";
    intervalEdtfDate = (IntervalEdtfDate) parser.parse(dateString, true);
    assertEquals(expectedResultString, intervalEdtfDate.toString());
  }
}