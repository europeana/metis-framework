package eu.europeana.normalization.dates.extraction.dateextractors;

import static java.lang.String.format;

import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import java.lang.invoke.MethodHandles;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class implementing interface {@link DateExtractor} with default functionality for all extractors
 */
public abstract class AbstractDateExtractor implements DateExtractor {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().getClass());

  @Override
  public DateNormalizationResult extractDateProperty(String inputValue) {
    return extractDateProperty(inputValue, DateQualification.NO_QUALIFICATION);
  }

  @Override
  public DateNormalizationResult extractGenericProperty(String inputValue) {
    return extractGenericProperty(inputValue, DateQualification.NO_QUALIFICATION);
  }

  /**
   * Utility method for calling {@link #extract(String, DateQualification, boolean)} with allowSwitchesDuringValidation as true.
   * <p>It also captures relevant exceptions so that return is performed</p>
   *
   * @param inputValue the input value
   * @param dateQualification the date qualification requested
   * @return the date normalization result
   */
  @Override
  public DateNormalizationResult extractDateProperty(String inputValue, DateQualification dateQualification) {
    DateNormalizationResult dateNormalizationResult = DateNormalizationResult.getNoMatchResult(inputValue);
    try {
      dateNormalizationResult = extract(inputValue, dateQualification, true);
    } catch (DateExtractionException e) {
      LOGGER.debug(format("Date extraction failed %s: ", inputValue), e);
    }
    if (dateNormalizationResult == null) {
      dateNormalizationResult = DateNormalizationResult.getNoMatchResult(inputValue);
    }
    return dateNormalizationResult;
  }

  /**
   * Utility method for calling {@link #extract(String, DateQualification, boolean)} with allowSwitchesDuringValidation as false.
   * <p>It also captures relevant exceptions so that return is performed</p>
   *
   * @param inputValue the input value
   * @param dateQualification the date qualification requested
   * @return the date normalization result
   */
  @Override
  public DateNormalizationResult extractGenericProperty(String inputValue, DateQualification dateQualification) {
    DateNormalizationResult dateNormalizationResult = DateNormalizationResult.getNoMatchResult(inputValue);
    try {
      dateNormalizationResult = extract(inputValue, dateQualification, false);
    } catch (DateExtractionException e) {
      LOGGER.debug(format("Date extraction failed %s: ", inputValue), e);
    }
    if (dateNormalizationResult == null) {
      dateNormalizationResult = DateNormalizationResult.getNoMatchResult(inputValue);
    }
    return dateNormalizationResult;
  }

  /**
   * Default method to get the correct date qualification.
   * <p>If a requested date qualification is requested we then set that, overwriting any other that would otherwise be computed.
   * The date qualification will be overwritten if the requested date qualification in non-null and
   * non-{@link DateQualification#NO_QUALIFICATION}. Otherwise we compute it with the supplier provided.</p>
   *
   * @param requestedDateQualification the requested date qualification
   * @param dateQualificationSupplier the date qualification supplier
   * @return the computed date qualification
   */
  DateQualification computeDateQualification(DateQualification requestedDateQualification,
      Supplier<DateQualification> dateQualificationSupplier) {
    final DateQualification dateQualification;
    if (requestedDateQualification != null && requestedDateQualification != DateQualification.NO_QUALIFICATION) {
      dateQualification = requestedDateQualification;
    } else {
      dateQualification = dateQualificationSupplier.get();
    }
    return dateQualification;
  }
}
