package eu.europeana.normalization.dates;

/**
 * Identifies the pattern that was matched, or if none of the patterns matched, or if a date matched a pattern but was invalid
 */
public enum DateNormalizationExtractorMatchId {
  BRIEF_DATE_RANGE("normalisable: brief year range"),
  EDTF("already normalised in EDTF"),
  CENTURY_NUMERIC("normalisable: century (numeric)"),
  CENTURY_ROMAN("normalisable: century (roman numerals)"),
  CENTURY_RANGE_ROMAN("normalisable: century range (roman numerals)"),
  DECADE("normalisable: decade"),
  NUMERIC_RANGE_ALL_VARIANTS("normalisable: numeric date interval (various separators)"),
  NUMERIC_RANGE_ALL_VARIANTS_XX("normalisable: numeric date interval (various separators and unknown parts)"),
  NUMERIC_ALL_VARIANTS("normalisable: numeric date (various separators)"),
  NUMERIC_ALL_VARIANTS_XX("normalisable: numeric date (various separators and unknown parts)"),
  YYYY_MM_DD_SPACES("normalisable: numeric date (whitespace separators)"),
  DCMI_PERIOD("normalisable: DCMI period"),
  MONTH_NAME("normalisable: date with month name"),
  FORMATTED_FULL_DATE("normalisable: formatted timestamp"),
  BC_AD("normalisable: BC/AD date"),
  LONG_YEAR("normalisable: long negative year"),
  INVALID("not normalisable: date apparently invalid"),
  NO_MATCH("not normalisable: no match with existing patterns");

  final String label;

  DateNormalizationExtractorMatchId(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
