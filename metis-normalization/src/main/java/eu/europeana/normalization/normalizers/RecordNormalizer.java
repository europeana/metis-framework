package eu.europeana.normalization.normalizers;

import org.w3c.dom.Document;
import eu.europeana.normalization.model.NormalizationReport;
import eu.europeana.normalization.util.NormalizationException;

/**
 * Instances of this class perform normalizations on an EDM document (represented as a DOM tree) and
 * provide feedback of their actions in a report.
 */
public interface RecordNormalizer {

  /**
   * This method performs the normalization.
   * 
   * @param edm The EDM document to normalize. Changes will be made directly in this document.
   * @return A report on the actions of this normalizer.
   * @throws NormalizationException In case something goes wrong during normalization.
   */
  NormalizationReport normalize(Document edm) throws NormalizationException;

}
