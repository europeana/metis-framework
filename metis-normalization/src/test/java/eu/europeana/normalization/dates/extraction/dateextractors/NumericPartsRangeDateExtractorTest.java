package eu.europeana.normalization.dates.extraction.dateextractors;


import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_RANGE_ALL_VARIANTS;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_RANGE_ALL_VARIANTS_XX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

class NumericPartsRangeDateExtractorTest {

  private static final NumericPartsRangeDateExtractor NUMERIC_PARTS_RANGE_DATE_EXTRACTOR = new NumericPartsRangeDateExtractor();

  @ParameterizedTest
  @ArgumentsSource(NumericRangeYMDArgumentsProvider.class)
  void extractYMD(String input, String expected) {
    extract(input, expected, NUMERIC_RANGE_ALL_VARIANTS);
  }

  @ParameterizedTest
  @ArgumentsSource(NumericRangeDMYArgumentsProvider.class)
  void extractDMY(String input, String expected) {
    extract(input, expected, NUMERIC_RANGE_ALL_VARIANTS);
  }

  @ParameterizedTest
  @ArgumentsSource(NumericRangeYMDXXArgumentsProvider.class)
  void extractYMD_XX(String input, String expected) {
    extract(input, expected, NUMERIC_RANGE_ALL_VARIANTS_XX);
  }

  @ParameterizedTest
  @ArgumentsSource(NumericRangeDMYXXArgumentsProvider.class)
  void extractDMY_XX(String input, String expected) {
    extract(input, expected, NUMERIC_RANGE_ALL_VARIANTS_XX);
  }

  void extract(String input, String expected, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    final DateNormalizationResult dateNormalizationResult = NUMERIC_PARTS_RANGE_DATE_EXTRACTOR.extract(input);
    if (expected == null) {
      assertNull(dateNormalizationResult);
    } else {
      final String actual = dateNormalizationResult.getEdtfDate().toString();
      assertEquals(expected, actual);
      assertEquals(actual.contains("?"), dateNormalizationResult.getEdtfDate().isUncertain());
      assertEquals(actual.contains(".."), dateNormalizationResult.getEdtfDate().isUnspecified());
      assertEquals(dateNormalizationExtractorMatchId, dateNormalizationResult.getDateNormalizationExtractorMatchId());
    }
  }
}