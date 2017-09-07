package eu.europeana.normalization;

import eu.europeana.normalization.model.NormalizationReport;
import org.w3c.dom.Document;


public interface RecordNormalization {

  NormalizationReport normalize(Document edm);
}
