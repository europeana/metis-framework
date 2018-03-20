package eu.europeana.normalization.normalizers;

import org.w3c.dom.Document;
import eu.europeana.normalization.model.NormalizationReport;
import eu.europeana.normalization.util.NormalizationException;

/**
 * An instance of this class performs a normalize action on an EDM document (represented as a DOM
 * tree) and provide feedback of their actions in a report.
 */
public interface RecordNormalizeAction extends NormalizeAction {

  /**
   * This method performs the normalize action.
   * 
   * @param edm The EDM document to normalize. Changes will be made directly in this document.
   * @return A report on the actions of this normalizer.
   * @throws NormalizationException In case something goes wrong during normalization.
   */
  NormalizationReport normalize(Document edm) throws NormalizationException;

  /**
   * Default behavior: return the current instance.
   */
  @Override
  default RecordNormalizeAction getAsRecordNormalizer() {
    return this;
  }
}
