package eu.europeana.metis.debias.detect.service;

import eu.europeana.metis.debias.detect.model.DeBiasResult;
import eu.europeana.metis.debias.detect.model.request.DetectionParameter;

/**
 * Implementations of this interface are able to detect biased terms given the languages
 * and the terms to search, and it returns a report indicating the result of the terms.
 */
public interface DetectService {

  /**
   * Method to detect biased terms according to the input values provided
   *
   * @param detectionParameter language and values
   * @return DeBiasResult containing metadata and values of the detection
   */
  DeBiasResult detect(DetectionParameter detectionParameter);
}
