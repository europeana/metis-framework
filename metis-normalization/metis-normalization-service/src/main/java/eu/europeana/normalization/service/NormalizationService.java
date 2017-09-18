package eu.europeana.normalization.service;

import eu.europeana.normalization.common.model.NormalizationReport;
import eu.europeana.normalization.common.model.NormalizedRecordResult;
import org.w3c.dom.Document;

public interface NormalizationService {

  NormalizationReport normalize(Document edm);

  NormalizedRecordResult processNormalize(String record);
}
