package eu.europeana.normalization;

import java.util.ArrayList;
import java.util.List;


public interface ValueNormalization {

	public List<String> normalize(String value);
	
    public List<NormalizeDetails> normalizeDetailed(String lbl);
}
