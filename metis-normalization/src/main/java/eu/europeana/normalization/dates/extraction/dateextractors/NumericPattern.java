package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import java.util.regex.Pattern;

public interface NumericPattern {

  Pattern getPattern();

  DateNormalizationExtractorMatchId getDateNormalizationExtractorMatchId();

  int getYearIndex();

  int getMonthIndex();

  int getDayIndex();

}
