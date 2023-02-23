package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.extraction.DateExtractionException;

/**
 * The interface for all the implementation of date patterns
 */
public interface DateExtractor {

  DateNormalizationResult extract(String inputValue, DateQualification requestedDateQualification,
      boolean allowSwitchesDuringValidation) throws DateExtractionException;

  DateNormalizationResult extractDateProperty(String inputValue);

  DateNormalizationResult extractGenericProperty(String inputValue);

  DateNormalizationResult extractGenericProperty(String inputValue, DateQualification dateQualification);

  DateNormalizationResult extractDateProperty(String inputValue, DateQualification dateQualification);

}

