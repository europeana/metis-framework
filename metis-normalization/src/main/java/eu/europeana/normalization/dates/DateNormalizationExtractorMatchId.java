package eu.europeana.normalization.dates;

/**
 * Identifies the pattern that was matched, or if none of the patterns matched, or if a date matched a pattern but was invalid
 */
public enum DateNormalizationExtractorMatchId {
  BC_AD("normalisable: BC/AD date"),
  BRIEF_DATE_RANGE("normalisable: brief year range"),
  CENTURY_NUMERIC("normalisable: century (numeric)"),
  CENTURY_RANGE_ROMAN("normalisable: century range (roman numerals)"),
  CENTURY_ROMAN("normalisable: century (roman numerals)"),
  DCMI_PERIOD("normalisable: DCMI period"),
  DECADE("normalisable: decade"),
  EDTF("already normalised in EDTF"),
  EDTF_CLEANED("normalisable: EDTF with data cleaning"),
  FORMATTED_FULL_DATE("normalisable: formatted timestamp"),
  INVALID("not normalisable: date apparently invalid"),
  LONG_YEAR("normalisable: long negative year"),
  MONTH_NAME("normalisable: date with month name"),
  NO_MATCH("not normalisable: no match with existing patterns"),
  NUMERIC_ALL_VARIANTS("normalisable: numeric date (various separators)"),
  NUMERIC_ALL_VARIANTS_XX("normalisable: numeric date (various separators and unknown parts)"),
  NUMERIC_RANGE_ALL_VARIANTS("normalisable: numeric date interval (various separators)"),
  NUMERIC_RANGE_ALL_VARIANTS_XX("normalisable: numeric date interval (various separators and unknown parts)"),
  YYYY_MM_DD_SPACES("normalisable: numeric date (whitespace separators)");

  final String label;

  DateNormalizationExtractorMatchId(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
