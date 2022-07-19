package eu.europeana.normalization.dates;

/**
 * Identifies the pattern that was matched, or if none of the patterns matched, or if a date matched a pattern but was invalid
 */
public enum MatchId {
  NO_MATCH, INVALID, YYYY, xYYYYx, YYYY_Range_Open, YYYY_Range, DCMIPeriod, YYYY_MM_DD, xYYYY_MM_DDx,
  YYYYMMDD_Range_Open, YYYYMMDD_Range, MONTH_NAME, FormatedFullDate, YYYY_MM, xYYYY_MMx, DD_MM_YYYY, xDD_MM_YYYYx,
  Century_Numeric, Century_Roman, Century_Range_Roman, Numeric_Range_AllVariants, Numeric_Range_AllVariants_Xx,
  Numeric_AllVariants, Decade, Numeric_AllVariants_Xx, Iso8601Date, Iso8601BriefDate, Iso8601BriefDateRange,
  Iso8601DateRange, YYYY_MM_DD_Spaces, BcAd, Edtf, Edtf_Cleaned, Brief_Date_Range, LongYear;

  public String getLabel() {
    switch (this) {
      case NO_MATCH:
        return "not normalisable: no match with existing patterns";
      case INVALID:
        return "not normalisable: date apparently invalid";
      case BcAd:
        return "normalisable: BC/AD date";
      case Century_Numeric:
        return "normalisable: century (numeric)";
      case Century_Range_Roman:
        return "normalisable: century range (roman numerals)";
      case Century_Roman:
        return "normalisable: century (roman numerals)";
      case DCMIPeriod:
        return "normalisable: DCMI period";
      case DD_MM_YYYY:
        return "normalisable: dd-mm-yyyy";
      case Decade:
        return "normalisable: decade";
      case Edtf:
        return "already normalised in EDTF";
      case Edtf_Cleaned:
        return "normalisable: EDTF with data cleaning";
      case FormatedFullDate:
        return "normalisable: formated timestamp";
      case MONTH_NAME:
        return "normalisable: date with month name";
      case Numeric_AllVariants:
        return "normalisable: numeric date (various separators)";
      case Numeric_AllVariants_Xx:
        return "normalisable: numeric date (various separators and unknown parts)";
      case Numeric_Range_AllVariants:
        return "normalisable: numeric date interval (various separators)";
      case Numeric_Range_AllVariants_Xx:
        return "normalisable: numeric date interval (various separators and unknown parts)";
      case YYYY_MM_DD_Spaces:
        return "normalisable: numeric date (whitespace separators)";
      case Brief_Date_Range:
        return "normalisable: brief year range";
      case LongYear:
        return "normalisable: long negative year";
      default:
        return name();
    }
  }
}
