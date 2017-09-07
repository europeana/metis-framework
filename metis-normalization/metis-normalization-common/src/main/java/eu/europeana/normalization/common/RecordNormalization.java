package eu.europeana.normalization.common;

import eu.europeana.normalization.common.model.NormalizationReport;
import org.w3c.dom.Document;


public interface RecordNormalization {

  NormalizationReport normalize(Document edm);
}
