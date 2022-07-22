package eu.europeana.normalization.dates;

import static java.util.function.Predicate.not;

import eu.europeana.normalization.dates.cleaning.CleanOperation;
import eu.europeana.normalization.dates.cleaning.CleanResult;
import eu.europeana.normalization.dates.cleaning.Cleaner;
import eu.europeana.normalization.dates.edtf.AbstractEdtfDate;
import eu.europeana.normalization.dates.edtf.EdtfValidator;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import eu.europeana.normalization.dates.extraction.dateextractors.DateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.DcmiPeriodDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternBcAdDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternBriefDateRangeDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternCenturyDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternDateExtractorYyyyMmDdSpacesDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternDecadeDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternEdtfDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternFormatedFullDateDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternLongNegativeYearDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternMonthNameDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternNumericDateExtractorWithMissingPartsAndXxDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternNumericDateExtractorWithMissingPartsDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternNumericDateRangeExtractorWithMissingPartsAndXxDateExtractor;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternNumericDateRangeExtractorWithMissingPartsDateExtractor;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * The main class that implements the normalisation procedure.
 * <p>
 * It provides procedures for normalising values of properties that:
 *   <ul>
 *     <li>should contain date values.</li>
 *     <li>may contain dates as well as other kinds of entities (i.e., dc:subject and dc:coverage).</li>
 *   </ul>
 * </p>
 */
public class DatesNormalizer {

  private final Cleaner cleaner = new Cleaner();
  private final List<DateExtractor> extractorsInOrderForDateProperties;
  private final List<DateExtractor> extractorsInOrderForGenericProperties;
  private final List<Function<String, DateNormalizationResult>> normalizationOperationsInOrderDateProperty;
  private final List<Function<String, DateNormalizationResult>> normalizationOperationsInOrderGenericProperty;

  /**
   * Default constructor.
   */
  public DatesNormalizer() {
    // The pattern PatternBriefDateRangeDateExtractor needs to be executed before the EDTF pattern.
    // Most values that match this pattern also match the EDTF pattern, but would result in an invalid date.
    // This pattern only matches values that would not be valid EDTF dates.
    extractorsInOrderForDateProperties = List.of(
        new PatternBriefDateRangeDateExtractor(),
        new PatternEdtfDateExtractor(),
        new PatternCenturyDateExtractor(),
        new PatternDecadeDateExtractor(),
        new PatternNumericDateRangeExtractorWithMissingPartsDateExtractor(),
        new PatternNumericDateRangeExtractorWithMissingPartsAndXxDateExtractor(),
        new PatternNumericDateExtractorWithMissingPartsDateExtractor(),
        new PatternNumericDateExtractorWithMissingPartsAndXxDateExtractor(),
        new PatternDateExtractorYyyyMmDdSpacesDateExtractor(),
        new DcmiPeriodDateExtractor(),
        new PatternMonthNameDateExtractor(),
        new PatternFormatedFullDateDateExtractor(),
        new PatternBcAdDateExtractor(),
        new PatternLongNegativeYearDateExtractor());

    extractorsInOrderForGenericProperties = extractorsInOrderForDateProperties.stream().filter(
        not(PatternBriefDateRangeDateExtractor.class::isInstance)).collect(
        Collectors.toList());

    normalizationOperationsInOrderDateProperty = List.of(
        input -> normalizeInput(extractorsInOrderForDateProperties, input),
        input -> normalizeInput(extractorsInOrderForDateProperties, input, cleaner::clean1stTime),
        input -> normalizeInput(extractorsInOrderForDateProperties, input, cleaner::clean2ndTime));

    normalizationOperationsInOrderGenericProperty = List.of(
        input -> normalizeInput(extractorsInOrderForGenericProperties, input),
        input -> normalizeInput(extractorsInOrderForGenericProperties, input, cleaner::cleanGenericProperty));
  }

  public DateNormalizationResult normalizeDateProperty(String input) {
    return normalizeProperty(input, normalizationOperationsInOrderDateProperty,
        dateNormalizationResult -> false,
        CleanOperation::isApproximateCleanOperationIdForDateProperty,
        this::validateAndFix,
        this::noMatchIfValidAndTimeOnly);
  }

  // TODO: 21/07/2022 To check. This method does not do dates switching like the other method
  // TODO: 21/07/2022 Not used? Assuming this was not unit tested?
  public DateNormalizationResult normalizeGenericProperty(String input) {
    return normalizeProperty(input, normalizationOperationsInOrderGenericProperty,
        dateNormalizationResult -> !dateNormalizationResult.isCompleteDate(),
        CleanOperation::isApproximateCleanOperationIdForGenericProperty,
        this::validate,
        dateNormalizationResult -> {//NOOP
        });
  }

  private DateNormalizationResult normalizeProperty(
      String input, final List<Function<String, DateNormalizationResult>> normalizationOperationsInOrder,
      Predicate<DateNormalizationResult> extraCheckForNoMatch,
      Predicate<CleanOperation> checkIfApproximateCleanOperationId,
      Consumer<DateNormalizationResult> validationOperation,
      Consumer<DateNormalizationResult> postProcessingMatchId) {

    DateNormalizationResult dateNormalizationResult;
    String sanitizedInput = sanitizeCharacters(input);

    //Normalize trying operations in order
    dateNormalizationResult = normalizationOperationsInOrder
        .stream()
        .map(operation -> operation.apply(sanitizedInput))
        .filter(Objects::nonNull).findFirst().orElse(null);

    //Check if we have a match
    if (Objects.isNull(dateNormalizationResult) || extraCheckForNoMatch.test(dateNormalizationResult)) {
      return DateNormalizationResult.getNoMatchResult(input);
    } else {
      //Check if we did a clean operation and update approximate
      if (dateNormalizationResult.getCleanOperationMatchId() != null) {
        dateNormalizationResult.getNormalizedEdtfDateWithLabel().getEdtfDate().setApproximate(
            checkIfApproximateCleanOperationId.test(dateNormalizationResult.getCleanOperationMatchId()));
      }
      validationOperation.accept(dateNormalizationResult);
      postProcessingMatchId.accept(dateNormalizationResult);
      return dateNormalizationResult;
    }
  }

  private void noMatchIfValidAndTimeOnly(DateNormalizationResult dateNormalizationResult) {
    if (dateNormalizationResult.getMatchId() != DateNormalizationExtractorMatchId.INVALID
        && dateNormalizationResult.getNormalizedEdtfDateWithLabel().getEdtfDate().isTimeOnly()) {
      // TODO: 21/07/2022 In the result only the match id is declared NO_MATCH but the contents are still present in the object. Is that okay?
      dateNormalizationResult.setDateNormalizationExtractorMatchId(DateNormalizationExtractorMatchId.NO_MATCH);
    }
  }

  private DateNormalizationResult normalizeInput(List<DateExtractor> dateExtractors, String input) {
    return dateExtractors.stream().map(dateExtractor -> dateExtractor.extract(input))
                         .filter(Objects::nonNull).findFirst().orElse(null);
  }

  private DateNormalizationResult normalizeInput(List<DateExtractor> dateExtractors, String input,
      Function<String, CleanResult> cleanFunction) {
    final CleanResult cleanedInput = cleanFunction.apply(input);
    DateNormalizationResult dateNormalizationResult = null;
    if (cleanedInput != null && StringUtils.isNotEmpty(cleanedInput.getCleanedValue())) {
      dateNormalizationResult = normalizeInput(dateExtractors, cleanedInput.getCleanedValue());
      if (dateNormalizationResult != null) {
        dateNormalizationResult.setCleanOperationMatchId(cleanedInput.getCleanOperation());
        // TODO: 21/07/2022 Perhaps this should be done differently, because it pollutes the map of the extractor and its id
        //Update the extractor match id.
        if (dateNormalizationResult.getMatchId() == DateNormalizationExtractorMatchId.EDTF) {
          dateNormalizationResult.setDateNormalizationExtractorMatchId(DateNormalizationExtractorMatchId.EDTF_CLEANED);
        }
      }
    }
    return dateNormalizationResult;
  }

  private void validateAndFix(DateNormalizationResult dateNormalizationResult) {
    final AbstractEdtfDate edtfDate = dateNormalizationResult.getNormalizedEdtfDateWithLabel().getEdtfDate();
    if (!EdtfValidator.validate(edtfDate, false)) {
      switchAndValidate(dateNormalizationResult, edtfDate);
    }
  }

  private void validate(DateNormalizationResult dateNormalizationResult) {
    final AbstractEdtfDate edtfDate = dateNormalizationResult.getNormalizedEdtfDateWithLabel().getEdtfDate();
    if (!EdtfValidator.validate(edtfDate, false)) {
      dateNormalizationResult.setDateNormalizationExtractorMatchId(DateNormalizationExtractorMatchId.INVALID);
    }
  }

  private void switchAndValidate(DateNormalizationResult dateNormalizationResult, AbstractEdtfDate edtfDate) {
    if (edtfDate instanceof IntervalEdtfDate) {
      ((IntervalEdtfDate) edtfDate).switchStartWithEnd();
      if (!EdtfValidator.validate(edtfDate, false)) {
        //Revert the start/end
        ((IntervalEdtfDate) edtfDate).switchStartWithEnd();
        switchDayWithMonthAndValidate(dateNormalizationResult, edtfDate);
      }
    } else {
      switchDayWithMonthAndValidate(dateNormalizationResult, edtfDate);
    }
  }

  private void switchDayWithMonthAndValidate(DateNormalizationResult dateNormalizationResult, AbstractEdtfDate edtfDate) {
    edtfDate.switchDayAndMonth();
    if (EdtfValidator.validate(edtfDate, false)) {
      dateNormalizationResult.getNormalizedEdtfDateWithLabel().setEdtfDate(edtfDate);
    } else {
      edtfDate.switchDayAndMonth();
      dateNormalizationResult.setDateNormalizationExtractorMatchId(DateNormalizationExtractorMatchId.INVALID);
    }
  }

  /**
   * Cleans and normalizes specific characters.
   * <p>
   * Specifically it will in order:
   *   <ul>
   *     <li>Trim the input</li>
   *     <li>Replace non-breaking spaces with normal spaces</li>
   *     <li>Replace en dash by a normal dash</li>
   *   </ul>
   * </p>
   *
   * @param input the string input
   * @return the normalized string
   */
  private static String sanitizeCharacters(String input) {
    String valTrim = input.trim();
    valTrim = valTrim.replace('\u00a0', ' '); // replace non-breaking spaces by normal spaces
    valTrim = valTrim.replace('\u2013', '-'); // replace en dash by normal dash
    return valTrim;
  }

}
