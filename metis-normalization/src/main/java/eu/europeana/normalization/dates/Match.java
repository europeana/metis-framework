package eu.europeana.normalization.dates;

import eu.europeana.normalization.dates.edtf.AbstractEdtfDate;
import eu.europeana.normalization.dates.edtf.EdtfDateWithLabel;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;

/**
 * Contains the result of a date normalisation.
 * <p>
 * It contains the pattern that was matched, if some cleaning was done, and the normalised value (if successfully normalised).
 * </p>
 */
public class Match {

  MatchId matchId;
  CleanId cleanOperation;
  String input;
  EdtfDateWithLabel extracted;

  public Match(MatchId matchId, String input, EdtfDateWithLabel extracted) {
    super();
    this.matchId = matchId;
    this.input = input;
    this.extracted = extracted;
  }

  public Match(MatchId matchId, CleanId cleanOperation, String input, EdtfDateWithLabel extracted) {
    super();
    this.matchId = matchId;
    this.cleanOperation = cleanOperation;
    this.input = input;
    this.extracted = extracted;
  }

  public Match(MatchId matchId, String input, AbstractEdtfDate extracted) {
    super();
    this.matchId = matchId;
    this.input = input;
    this.extracted = extracted == null ? null : new EdtfDateWithLabel(extracted);
  }

  public Match(MatchId matchId, CleanId cleanOperation, String input, AbstractEdtfDate extracted) {
    super();
    this.matchId = matchId;
    this.cleanOperation = cleanOperation;
    this.input = input;
    this.extracted = extracted == null ? null : new EdtfDateWithLabel(extracted);
  }

  public Match(String input) {
    this.input = input;
  }

  public MatchId getMatchId() {
    return matchId;
  }

  public String getInput() {
    return input;
  }

  public EdtfDateWithLabel getExtracted() {
    return extracted;
  }

  public void setMatchId(MatchId matchId) {
    this.matchId = matchId;
  }

  public void setInput(String input) {
    this.input = input;
  }

  public void setExtracted(EdtfDateWithLabel extracted) {
    this.extracted = extracted;
  }

  public void setResult(Match result) {
    matchId = result.getMatchId();
    this.cleanOperation = result.getCleanOperation();
    this.extracted = result.getExtracted();
  }

  public CleanId getCleanOperation() {
    return cleanOperation;
  }

  public void setCleanOperation(CleanId cleanOperation) {
    this.cleanOperation = cleanOperation;
  }

  @Override
  public String toString() {
    return "Match [matchId=" + matchId + ", cleanOperation=" + cleanOperation + ", input=" + input + ", extracted="
        + extracted + "]";
  }

  public boolean isCompleteDate() {
    if (extracted == null || extracted.getEdtf().isTimeOnly()) {
      return false;
    }
    if (extracted.getEdtf() instanceof InstantEdtfDate) {
      return ((InstantEdtfDate) extracted.getEdtf()).getEdtfDatePart().getDay() != null;
    } else {
      IntervalEdtfDate intervalEdtfDate = (IntervalEdtfDate) extracted.getEdtf();
      if (intervalEdtfDate.getStart() != null && intervalEdtfDate.getEnd() != null) {
        if (intervalEdtfDate.getStart().getEdtfDatePart().isUnknown() || intervalEdtfDate.getStart().getEdtfDatePart()
                                                                                         .isUnspecified()) {
          return false;
        }
        if (intervalEdtfDate.getEnd().getEdtfDatePart().isUnknown() || intervalEdtfDate.getEnd().getEdtfDatePart()
                                                                                       .isUnspecified()) {
          return false;
        }
        if (intervalEdtfDate.getStart().getEdtfDatePart().getYearPrecision() != null
            || intervalEdtfDate.getEnd().getEdtfDatePart().getYearPrecision() != null) {
          return false;
        }
        if (intervalEdtfDate.getStart().getEdtfDatePart().getDay() != null
            && intervalEdtfDate.getStart().getEdtfDatePart().getDay() != null) {
          return true;
        }
        if (intervalEdtfDate.getStart().getEdtfDatePart().getMonth() == null
            && intervalEdtfDate.getStart().getEdtfDatePart().getMonth() == null) {
          return true;
        }
      }
      return false;
    }
  }

}
