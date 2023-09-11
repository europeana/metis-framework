package eu.europeana.normalization.dates.extraction.extractors;

import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.extraction.DateExtractionException;

/**
 * The interface for date extractors.
 */
public interface DateExtractor {

  /**
   * Extractor of a date normalization operation.
   *
   * @param inputValue the value containing the date
   * @param requestedDateQualification the overwriting value of date qualification, if any
   * @param flexibleDateBuild the flag indicating if during creating of the dates we are flexible with validation
   * @return the date normalization result
   * @throws DateExtractionException if anything happened during the extraction of the date
   */
  DateNormalizationResult extract(String inputValue, DateQualification requestedDateQualification, boolean flexibleDateBuild)
      throws DateExtractionException;

  /**
   * Extractor of a date normalization operation for <strong>date</strong> properties
   *
   * @param inputValue the value containing the date
   * @param requestedDateQualification the overwriting value of date qualification, if any
   * @return the date normalization result
   */
  DateNormalizationResult extractDateProperty(String inputValue, DateQualification requestedDateQualification);

  /**
   * Extractor of a date normalization operation for <strong>generic</strong> properties
   *
   * @param inputValue the value containing the date
   * @param requestedDateQualification the overwriting value of date qualification, if any
   * @return the date normalization result
   */
  DateNormalizationResult extractGenericProperty(String inputValue, DateQualification requestedDateQualification);

}
