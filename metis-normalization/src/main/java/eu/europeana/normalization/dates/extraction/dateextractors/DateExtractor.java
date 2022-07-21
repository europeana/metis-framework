package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationResult;

/**
 * The interface for all the implementation of date patterns
 */
public interface DateExtractor {

  DateNormalizationResult extract(String inputValue);

}
