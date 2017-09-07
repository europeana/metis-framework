package eu.europeana.normalization.common;

import java.util.List;


public interface ValueNormalization {

  List<String> normalize(String value);

  List<NormalizeDetails> normalizeDetailed(String lbl);

  RecordNormalization toEdmRecordNormalizer();
}
