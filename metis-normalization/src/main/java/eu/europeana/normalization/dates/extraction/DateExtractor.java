package eu.europeana.normalization.dates.extraction;

import eu.europeana.normalization.dates.Match;

/**
 * The interface for all the implementation of date patterns
 */
public interface DateExtractor {

  Match extract(String inputValue);

}
