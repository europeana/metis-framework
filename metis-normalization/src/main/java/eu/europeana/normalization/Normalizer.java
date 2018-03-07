package eu.europeana.normalization;

import java.util.List;
import eu.europeana.normalization.common.model.NormalizationBatchResult;
import eu.europeana.normalization.common.model.NormalizationResult;

public interface Normalizer {

  public NormalizationResult normalize(String edmRecord);

  public NormalizationBatchResult normalize(List<String> edmRecords);

}
