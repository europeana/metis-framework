package eu.europeana.normalization.normalizers;

import eu.europeana.normalization.model.ConfidenceLevel;
import eu.europeana.normalization.model.NormalizationReport;

/**
 * This is a subclass of {@link NormalizationReport} for internal use within the normalizers to
 * provide access to the {@link #increment(String, ConfidenceLevel)} method.
 * 
 * @author jochen
 *
 */
class InternalNormalizationReport extends NormalizationReport {

  @Override
  public void increment(String operation, ConfidenceLevel confidence) {
    super.increment(operation, confidence);
  }
}

