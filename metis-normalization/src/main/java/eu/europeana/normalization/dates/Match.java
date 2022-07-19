package eu.europeana.normalization.dates;

import eu.europeana.normalization.dates.edtf.AbstractEDTFDate;
import eu.europeana.normalization.dates.edtf.InstantEDTFDate;
import eu.europeana.normalization.dates.edtf.IntervalEDTFDate;

/**
 * a data class that contains the result of a normalisation. It contains the pattern that was matched, if some cleaning was done,
 * and the normalised value (if successfully normalised).
 */
public class Match {

	MatchId matchId;
	CleanId cleanOperation;
	String input;
	EdmTemporalEntity extracted;

	public Match(MatchId matchId, String input, EdmTemporalEntity extracted) {
		super();
		this.matchId = matchId;
		this.input = input;
		this.extracted = extracted;
	}

	public Match(MatchId matchId, CleanId cleanOperation, String input, EdmTemporalEntity extracted) {
		super();
		this.matchId = matchId;
		this.cleanOperation = cleanOperation;
		this.input = input;
		this.extracted = extracted;
	}

	public Match(MatchId matchId, String input, AbstractEDTFDate extracted) {
		super();
		this.matchId = matchId;
		this.input = input;
		this.extracted = extracted == null ? null : new EdmTemporalEntity(extracted);
	}

	public Match(MatchId matchId, CleanId cleanOperation, String input, AbstractEDTFDate extracted) {
		super();
		this.matchId = matchId;
		this.cleanOperation = cleanOperation;
		this.input = input;
		this.extracted = extracted == null ? null : new EdmTemporalEntity(extracted);
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

	public EdmTemporalEntity getExtracted() {
		return extracted;
	}

	public void setMatchId(MatchId matchId) {
		this.matchId = matchId;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public void setExtracted(EdmTemporalEntity extracted) {
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
		if (extracted.getEdtf() instanceof InstantEDTFDate) {
			return ((InstantEDTFDate) extracted.getEdtf()).getEdtfDatePart().getDay() != null;
		} else {
			IntervalEDTFDate intervalEDTFDate = (IntervalEDTFDate) extracted.getEdtf();
			if (intervalEDTFDate.getStart() != null && intervalEDTFDate.getEnd() != null) {
				if (intervalEDTFDate.getStart().getEdtfDatePart().isUnknown() || intervalEDTFDate.getStart().getEdtfDatePart()
																																												 .isUnspecified()) {
					return false;
				}
				if (intervalEDTFDate.getEnd().getEdtfDatePart().isUnknown() || intervalEDTFDate.getEnd().getEdtfDatePart()
																																											 .isUnspecified()) {
					return false;
				}
				if (intervalEDTFDate.getStart().getEdtfDatePart().getYearPrecision() != null
						|| intervalEDTFDate.getEnd().getEdtfDatePart().getYearPrecision() != null) {
					return false;
				}
				if (intervalEDTFDate.getStart().getEdtfDatePart().getDay() != null
						&& intervalEDTFDate.getStart().getEdtfDatePart().getDay() != null) {
					return true;
				}
				if (intervalEDTFDate.getStart().getEdtfDatePart().getMonth() == null
						&& intervalEDTFDate.getStart().getEdtfDatePart().getMonth() == null) {
					return true;
				}
			}
			return false;
		}
	}

}
