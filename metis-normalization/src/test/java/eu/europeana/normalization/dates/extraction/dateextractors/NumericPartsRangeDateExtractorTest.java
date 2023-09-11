package eu.europeana.normalization.dates.extraction.dateextractors;


import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_RANGE_ALL_VARIANTS;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_RANGE_ALL_VARIANTS_XX;
import static eu.europeana.normalization.dates.edtf.DateQualification.NO_QUALIFICATION;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

class NumericPartsRangeDateExtractorTest implements DateExtractorTest {

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
    final DateNormalizationResult dateNormalizationResult = NUMERIC_PARTS_RANGE_DATE_EXTRACTOR.extractDateProperty(input,
        NO_QUALIFICATION);
    assertDateNormalizationResult(dateNormalizationResult, expected, dateNormalizationExtractorMatchId);
  }
}