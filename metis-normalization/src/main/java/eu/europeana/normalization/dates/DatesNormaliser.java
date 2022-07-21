package eu.europeana.normalization.dates;

import eu.europeana.normalization.dates.Cleaner.CleanResult;
import eu.europeana.normalization.dates.edtf.AbstractEDTFDate;
import eu.europeana.normalization.dates.edtf.EDTFValidator;
import eu.europeana.normalization.dates.edtf.InstantEDTFDate;
import eu.europeana.normalization.dates.edtf.IntervalEDTFDate;
import eu.europeana.normalization.dates.extraction.dateextractors.DateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.DcmiPeriodDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternBcAdDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternBriefDateRangeDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternCenturyDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternDateExtractorYyyyMmDdSpacesDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternDecadeDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternEdtfDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternFormatedFullDateDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternLongNegativeYearDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternMonthNameDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternNumericDateExtractorWithMissingPartsAndXxDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternNumericDateExtractorWithMissingPartsDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternNumericDateRangeExtractorWithMissingPartsAndXxDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternNumericDateRangeExtractorWithMissingPartsDateExtractor;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;

/**
 * The main class that implements the normalisation procedure. It provides two procedures for normalising values: one for
 * normalising values of properties that should contain date values, and another for normalising values of properties that may
 * contain dates as well as other kinds of entities (i.e., dc:subject and dc:coverage).
 */
public class DatesNormaliser {

  private static Cleaner cleaner = new Cleaner();

  private static final ArrayList<DateExtractor> extractors = new ArrayList<>() {
    {
      // this pattern needs to be executed before the Edtf one. Most values that match
      // this pattern also match the EDTF pattern, but would result in an invalid
      // date. This pattern only matches values that would not be valid EDTF dates
      add(new PatternBriefDateRangeDateExtractor());
      add(new PatternEdtfDateExtractor());
      add(new PatternCenturyDateExtractor());
      add(new PatternDecadeDateExtractor());
      add(new PatternNumericDateRangeExtractorWithMissingPartsDateExtractor());
      add(new PatternNumericDateRangeExtractorWithMissingPartsAndXxDateExtractor());
      add(new PatternNumericDateExtractorWithMissingPartsDateExtractor());
      add(new PatternNumericDateExtractorWithMissingPartsAndXxDateExtractor());
      add(new PatternDateExtractorYyyyMmDdSpacesDateExtractor());
      add(new DcmiPeriodDateExtractor());
      add(new PatternMonthNameDateExtractor());
      add(new PatternFormatedFullDateDateExtractor());
      add(new PatternBcAdDateExtractor());
      add(new PatternLongNegativeYearDateExtractor());
    }
  };
  private static final ArrayList<Class> extractorsExcludedForGenericProperties = new ArrayList<Class>() {
    {
      add(PatternBriefDateRangeDateExtractor.class);
    }
  };

  /**
   * if true, the normaliser attempts to fix dates that are invalid because the end date is earlier than the start date
   */
  boolean validateAndFix = true;
  /**
   * if true, the normaliser attempts to fix dates that are invalid because the end date is earlier than the start date
   */
  boolean trySwitchingDayMonth = true;

  public DatesNormaliser() {
  }

  /**
   * @param validateAndFix if true, the normaliser attempts to fix dates that are invalid because the end date is earlier than the
   * start date
   * @param trySwitchingDayMonth if true, the normaliser attempts to fix dates that are invalid because the end date is earlier
   * than the start date
   */
  public void configure(boolean validateAndFix, boolean trySwitchingDayMonth) {
    this.validateAndFix = validateAndFix;
    this.trySwitchingDayMonth = trySwitchingDayMonth;
  }

  public Match normaliseDateProperty(String input) throws Exception {
    try {
      Match extracted = null;
      String valTrim = normalizeCharacters(input);
      for (DateExtractor extractor : extractors) {
        extracted = extractor.extract(valTrim);
        if (extracted != null) {
          break;
        }
      }
      if (extracted == null) {
        // Trying patterns after cleaning
        CleanResult cleanResult = cleaner.clean1stTime(valTrim);
        if (cleanResult != null && !StringUtils.isEmpty(cleanResult.getCleanedValue())) {
          for (DateExtractor extractor : extractors) {
            extracted = extractor.extract(cleanResult.getCleanedValue());
            if (extracted != null) {
              extracted.setCleanOperation(cleanResult.getCleanOperation());
              break;
            }
          }
        }
      }
      if (extracted == null) {
        // Trying patterns after cleaning
        CleanResult cleanResult = cleaner.clean2ndTime(valTrim);
        if (cleanResult != null && !StringUtils.isEmpty(cleanResult.getCleanedValue())) {
          for (DateExtractor extractor : extractors) {
            extracted = extractor.extract(cleanResult.getCleanedValue());
            if (extracted != null) {
              extracted.setCleanOperation(cleanResult.getCleanOperation());
              break;
            }
          }
        }
      }
      if (extracted == null) {
        return new Match(MatchId.NO_MATCH, input, (AbstractEDTFDate) null);
      } else {
        extracted.setInput(input);
      }

      if (extracted.getCleanOperation() != null) {
        if (extracted.getCleanOperation() == CleanId.CIRCA) {
          extracted.getExtracted().getEdtf().setApproximate(true);
        } else if (extracted.getCleanOperation() == CleanId.SQUARE_BRACKETS_AND_CIRCA) {
          extracted.getExtracted().getEdtf().setApproximate(true);
        } else if (extracted.getCleanOperation() == CleanId.PARENTHESES_FULL_VALUE_AND_CIRCA) {
          extracted.getExtracted().getEdtf().setApproximate(true);
        }
      }
      if (extracted.getMatchId() != MatchId.NO_MATCH) {
        if (validateAndFix) {
          validateAndFix(extracted);
        } else {
          if (!EDTFValidator.validate(extracted.getExtracted().getEdtf(), false)) {
            extracted.setMatchId(MatchId.INVALID);
          }
        }
      }

      if (extracted.getMatchId() == MatchId.EDTF && extracted.getCleanOperation() != null) {
        extracted.setMatchId(MatchId.EDTF_CLEANED);
      }
      if (extracted.getMatchId() != MatchId.NO_MATCH) {
        if (extracted.getMatchId() != MatchId.NO_MATCH && extracted.getMatchId() != MatchId.INVALID) {
          if (extracted.getExtracted().getEdtf().isTimeOnly()) {
            extracted.setMatchId(MatchId.NO_MATCH);
          }
        }
      }
      return extracted;
    } catch (Exception e) {
      throw new Exception("Error in value: " + input, e);
    }
  }

  public Match normaliseGenericProperty(String input) throws Exception {
    try {
      String valTrim = normalizeCharacters(input);
      Match match = null;
      for (DateExtractor extractor : extractors) {
        if (extractorsExcludedForGenericProperties.contains(extractor.getClass())) {
          continue;
        }
        match = extractor.extract(valTrim);
        if (match != null) {
          break;
        }
      }
      if (match == null) {
        // Trying patterns after cleaning
        CleanResult cleanResult = cleaner.cleanGenericProperty(valTrim);
        if (cleanResult != null && !StringUtils.isEmpty(cleanResult.getCleanedValue())) {
          for (DateExtractor extractor : extractors) {
            if (extractorsExcludedForGenericProperties.contains(extractor.getClass())) {
              continue;
            }
            match = extractor.extract(cleanResult.getCleanedValue());
            if (match != null) {
              match.setCleanOperation(cleanResult.getCleanOperation());
              break;
            }
          }
        }
      }
      if (match == null || !match.isCompleteDate()) {
        return new Match(MatchId.NO_MATCH, input, (AbstractEDTFDate) null);
      } else {
        match.setInput(input);
      }

      if (match.getCleanOperation() != null) {
        if (match.getCleanOperation() == CleanId.CIRCA) {
          match.getExtracted().getEdtf().setApproximate(true);
        } else if (match.getCleanOperation() == CleanId.SQUARE_BRACKETS_AND_CIRCA) {
          match.getExtracted().getEdtf().setApproximate(true);
        }
      }

      if (match.getMatchId() == MatchId.EDTF && match.getCleanOperation() != null) {
        match.setMatchId(MatchId.EDTF_CLEANED);
      }

      if (match.getMatchId() != MatchId.NO_MATCH) {
        if (!EDTFValidator.validate(match.getExtracted().getEdtf(), false)) {
          match.setMatchId(MatchId.INVALID);
        }
      }
      return match;
    } catch (Exception e) {
      throw new Exception("Error in value: " + input, e);
    }
  }

  private void validateAndFix(Match extracted) {
    if (!EDTFValidator.validate(extracted.getExtracted().getEdtf(), false)) {
      if (extracted.getExtracted().getEdtf() instanceof IntervalEDTFDate) {
        // lets try to invert the start and end dates and see if it validates
        IntervalEDTFDate i = (IntervalEDTFDate) extracted.getExtracted().getEdtf();
        InstantEDTFDate start = i.getStart();
        i.setStart(i.getEnd());
        i.setEnd(start);
        if (!EDTFValidator.validate(extracted.getExtracted().getEdtf(), false)) {
          i.setEnd(i.getStart());
          i.setStart(start);

          if (trySwitchingDayMonth) {
            EdmTemporalEntity copy = extracted.getExtracted().copy();
            copy.getEdtf().switchDayAndMonth();
            if (!EDTFValidator.validate(copy.getEdtf(), false)) {
              extracted.setMatchId(MatchId.INVALID);
            } else {
              extracted.setExtracted(copy);
            }
          } else {
            extracted.setMatchId(MatchId.INVALID);
          }
        }
      } else {
        if (trySwitchingDayMonth) {
          EdmTemporalEntity copy = extracted.getExtracted().copy();
          copy.getEdtf().switchDayAndMonth();
          if (!EDTFValidator.validate(copy.getEdtf(), false)) {
            extracted.setMatchId(MatchId.INVALID);
          } else {
            extracted.setExtracted(copy);
          }
        } else {
          extracted.setMatchId(MatchId.INVALID);
        }
      }
    }
    if (extracted.getMatchId() != MatchId.NO_MATCH && extracted.getMatchId() != MatchId.INVALID) {
      if (extracted.getExtracted().getEdtf().isTimeOnly()) {
        extracted.setMatchId(MatchId.NO_MATCH);
      }
    }
  }

  private static String normalizeCharacters(String input) {
    String valTrim = input.trim();
    valTrim = valTrim.replace('\u00a0', ' '); // replace non-breaking spaces by normal spaces
    valTrim = valTrim.replace('\u2013', '-'); // replace en dash by normal dash
    return valTrim;
  }

}
