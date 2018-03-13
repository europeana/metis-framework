package eu.europeana.normalization;

import java.util.List;
import eu.europeana.normalization.model.NormalizationBatchResult;
import eu.europeana.normalization.model.NormalizationResult;
import eu.europeana.normalization.util.NormalizationException;

public interface Normalizer {

  public NormalizationResult normalize(String edmRecord) throws NormalizationException;

  public NormalizationBatchResult normalize(List<String> edmRecords) throws NormalizationException;

}
