package eu.europeana.normalization.dates;

import eu.europeana.normalization.dates.edtf.Instant;
import eu.europeana.normalization.dates.edtf.Interval;
import eu.europeana.normalization.dates.edtf.TemporalEntity;

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

	public Match(MatchId matchId, String input, TemporalEntity extracted) {
		super();
		this.matchId = matchId;
		this.input = input;
		this.extracted = extracted == null ? null : new EdmTemporalEntity(extracted);
	}

	public Match(MatchId matchId, CleanId cleanOperation, String input, TemporalEntity extracted) {
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
		if (extracted.getEdtf() instanceof Instant) {
			return ((Instant) extracted.getEdtf()).getDate().getDay() != null;
		} else {
			Interval interval = (Interval) extracted.getEdtf();
			if (interval.getStart() != null && interval.getEnd() != null) {
				if (interval.getStart().getDate().isUnkown() || interval.getStart().getDate().isUnspecified()) {
					return false;
				}
				if (interval.getEnd().getDate().isUnkown() || interval.getEnd().getDate().isUnspecified()) {
					return false;
				}
				if (interval.getStart().getDate().getYearPrecision() != null
						|| interval.getEnd().getDate().getYearPrecision() != null) {
					return false;
				}
				if (interval.getStart().getDate().getDay() != null && interval.getStart().getDate().getDay() != null) {
					return true;
				}
				if (interval.getStart().getDate().getMonth() == null
						&& interval.getStart().getDate().getMonth() == null) {
					return true;
				}
			}
			return false;
		}
	}

}
