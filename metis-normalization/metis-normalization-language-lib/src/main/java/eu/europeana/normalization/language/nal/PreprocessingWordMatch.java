package eu.europeana.normalization.language.nal;

import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

public class PreprocessingWordMatch {
	String unmatchedRemainingValue; 
	List<String> matchedLabels;
	
	public PreprocessingWordMatch() {
		matchedLabels=new ArrayList<>();
	}

	public String getUnmatchedRemainingValue() {
		return unmatchedRemainingValue;
	}

	public void setUnmatchedRemainingValue(String unmatchedRemainingValue) {
		this.unmatchedRemainingValue = unmatchedRemainingValue;
	}

	public List<String> getMatchedLabels() {
		return matchedLabels;
	}

	public void setMatchedLabels(List<String> matchedLabels) {
		this.matchedLabels = matchedLabels;
	}
	
	
}
