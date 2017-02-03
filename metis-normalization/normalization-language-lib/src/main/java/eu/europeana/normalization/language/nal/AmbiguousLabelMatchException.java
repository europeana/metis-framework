package eu.europeana.normalization.language.nal;

import java.util.List;

public class AmbiguousLabelMatchException extends Exception {
	List<String> ambigouosMatches;

	public AmbiguousLabelMatchException(List<String> ambigouosMatches) {
		super();
		this.ambigouosMatches = ambigouosMatches;
	}

	public List<String> getAmbigouosMatches() {
		return ambigouosMatches;
	}
}
