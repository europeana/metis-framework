package eu.europeana.normalization;

import eu.europeana.normalization.model.NormalizationReport;
import eu.europeana.normalization.model.NormalizedRecordResult;
import org.w3c.dom.Document;

public interface NormalizationService {

  NormalizationReport normalize(Document edm);

  NormalizedRecordResult processNormalize(String record);
}
