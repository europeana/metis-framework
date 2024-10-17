package eu.europeana.metis.debias.detect.service;

import eu.europeana.metis.debias.detect.model.DeBiasResult;
import eu.europeana.metis.debias.detect.model.request.BiasInputLiterals;

/**
 * Implementations of this interface are able to detect biased terms given the languages
 * and the terms to search, and it returns a report indicating the result of the terms.
 */
public interface BiasDetectService {

  /**
   * Method to detect biased terms according to the input values provided
   *
   * @param biasInputLiterals language and values
   * @return DeBiasResult containing metadata and values of the detection
   */
  DeBiasResult detect(BiasInputLiterals biasInputLiterals);
}
