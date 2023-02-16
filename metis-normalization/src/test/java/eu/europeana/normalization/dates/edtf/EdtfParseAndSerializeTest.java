package eu.europeana.normalization.dates.edtf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.europeana.normalization.dates.extraction.dateextractors.PatternEdtfDateExtractor;
import org.junit.jupiter.api.Test;

class EdtfParseAndSerializeTest {

  // TODO: 14/02/2023 Can be simplified with parameterized tests
  private final PatternEdtfDateExtractor patternEdtfDateExtractor = new PatternEdtfDateExtractor();

  @Test
  void extractDate() {
    String dateString = "2004-01-01";
    String expectedResultString = "2004-01-01";
    InstantEdtfDate instantEdtfDate = (InstantEdtfDate) patternEdtfDateExtractor.extractDateProperty(dateString).getEdtfDate();
    assertEquals(expectedResultString, instantEdtfDate.toString());

    dateString = "2004-01";
    expectedResultString = "2004-01";
    instantEdtfDate = (InstantEdtfDate) patternEdtfDateExtractor.extractDateProperty(dateString).getEdtfDate();
    assertEquals(expectedResultString, instantEdtfDate.toString());

    dateString = "2004";
    expectedResultString = "2004";
    instantEdtfDate = (InstantEdtfDate) patternEdtfDateExtractor.extractDateProperty(dateString).getEdtfDate();
    assertEquals(expectedResultString, instantEdtfDate.toString());

    dateString = "2004-01?";
    expectedResultString = "2004-01?";
    instantEdtfDate = (InstantEdtfDate) patternEdtfDateExtractor.extractDateProperty(dateString).getEdtfDate();
    assertEquals(expectedResultString, instantEdtfDate.toString());

    dateString = "2004~";
    expectedResultString = "2004~";
    instantEdtfDate = (InstantEdtfDate) patternEdtfDateExtractor.extractDateProperty(dateString).getEdtfDate();
    assertEquals(expectedResultString, instantEdtfDate.toString());

    dateString = "2004-01-01%";
    expectedResultString = "2004-01-01%";
    instantEdtfDate = (InstantEdtfDate) patternEdtfDateExtractor.extractDateProperty(dateString).getEdtfDate();
    assertEquals(expectedResultString, instantEdtfDate.toString());

    dateString = "Y-200000";
    expectedResultString = "Y-200000";
    instantEdtfDate = (InstantEdtfDate) patternEdtfDateExtractor.extractDateProperty(dateString).getEdtfDate();
    assertEquals(expectedResultString, instantEdtfDate.toString());

    dateString = "Y200000";
    expectedResultString = "Y200000";
    instantEdtfDate = (InstantEdtfDate) patternEdtfDateExtractor.extractDateProperty(dateString).getEdtfDate();
    assertEquals(expectedResultString, instantEdtfDate.toString());

    dateString = "..";
    expectedResultString = "..";
    instantEdtfDate = (InstantEdtfDate) patternEdtfDateExtractor.extractDateProperty(dateString).getEdtfDate();
    assertEquals(expectedResultString, instantEdtfDate.toString());
  }

  @Test
  void extractDatePropertyTime() {
    String dateString = "2004-01-01T23:05:02";
    String expectedResultString = "2004-01-01";
    InstantEdtfDate instantEdtfDate = (InstantEdtfDate) patternEdtfDateExtractor.extractDateProperty(dateString).getEdtfDate();
    assertEquals(expectedResultString, instantEdtfDate.toString());

    dateString = "2004-01-01T23:05";
    expectedResultString = "2004-01-01";
    instantEdtfDate = (InstantEdtfDate) patternEdtfDateExtractor.extractDateProperty(dateString).getEdtfDate();
    assertEquals(expectedResultString, instantEdtfDate.toString());

    dateString = "2004-01-01T23";
    expectedResultString = "2004-01-01";
    instantEdtfDate = (InstantEdtfDate) patternEdtfDateExtractor.extractDateProperty(dateString).getEdtfDate();
    assertEquals(expectedResultString, instantEdtfDate.toString());
  }

  @Test
  void extractDatePropertyInterval() {
    String dateString = "2004-01-01/2004-01-02";
    String expectedResultString = "2004-01-01/2004-01-02";
    IntervalEdtfDate intervalEdtfDate = (IntervalEdtfDate) patternEdtfDateExtractor.extractDateProperty(dateString).getEdtfDate();
    assertEquals(expectedResultString, intervalEdtfDate.toString());

    dateString = "2004-01-01/2005";
    expectedResultString = "2004-01-01/2005";
    intervalEdtfDate = (IntervalEdtfDate) patternEdtfDateExtractor.extractDateProperty(dateString).getEdtfDate();
    assertEquals(expectedResultString, intervalEdtfDate.toString());

    dateString = "2004/2005";
    expectedResultString = "2004/2005";
    intervalEdtfDate = (IntervalEdtfDate) patternEdtfDateExtractor.extractDateProperty(dateString).getEdtfDate();
    assertEquals(expectedResultString, intervalEdtfDate.toString());

    dateString = "2004?/2005~";
    expectedResultString = "2004?/2005~";
    intervalEdtfDate = (IntervalEdtfDate) patternEdtfDateExtractor.extractDateProperty(dateString).getEdtfDate();
    assertEquals(expectedResultString, intervalEdtfDate.toString());
  }
}