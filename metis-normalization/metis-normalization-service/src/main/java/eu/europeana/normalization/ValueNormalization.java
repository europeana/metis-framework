package eu.europeana.normalization;

import java.util.List;


public interface ValueNormalization {

  List<String> normalize(String value);
	
  List<NormalizeDetails> normalizeDetailed(String lbl);
}
