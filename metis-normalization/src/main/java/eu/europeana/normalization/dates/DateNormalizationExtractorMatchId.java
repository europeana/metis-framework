package eu.europeana.normalization.dates;

/**
 * Identifies the pattern that was matched, or if none of the patterns matched, or if a date matched a pattern but was invalid
 */
public enum DateNormalizationExtractorMatchId {
  BC_AD("BC/AD date"),
  BRIEF_DATE_RANGE("brief year range"),
  CENTURY_NUMERIC("century (numeric)"),
  CENTURY_RANGE_ROMAN("century range (roman numerals)"),
  CENTURY_ROMAN("century (roman numerals)"),
  DCMI_PERIOD("DCMI period"),
  DECADE("decade"),
  EDTF("already normalised in EDTF"),
  FORMATTED_FULL_DATE("formatted timestamp"),
  LONG_NEGATIVE_YEAR("long negative year"),
  MONTH_NAME("date with month name"),
  NUMERIC_ALL_VARIANTS("numeric date (various separators)"),
  NUMERIC_ALL_VARIANTS_XX("numeric date (various separators and unknown parts)"),
  NUMERIC_RANGE_ALL_VARIANTS("numeric date interval (various separators)"),
  NUMERIC_RANGE_ALL_VARIANTS_XX("numeric date interval (various separators and unknown parts)"),
  YYYY_MM_DD_SPACES("numeric date (whitespace separators)");

  final String label;

  DateNormalizationExtractorMatchId(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
