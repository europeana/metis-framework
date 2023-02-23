package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.DateQualification;
import java.util.function.Supplier;

/**
 * The interface for all the implementation of date patterns
 */
public interface DateExtractor {

  default DateNormalizationResult extractDateProperty(String inputValue) {
    return extractDateProperty(inputValue, DateQualification.NO_QUALIFICATION);
  }

  default DateNormalizationResult extractGenericProperty(String inputValue) {
    return extractGenericProperty(inputValue, DateQualification.NO_QUALIFICATION);
  }

  DateNormalizationResult extract(String inputValue, DateQualification requestedDateQualification,
      boolean allowSwitchesDuringValidation);

  /**
   * Utility method for calling {@link #extract(String, DateQualification, boolean)} with allowSwitchesDuringValidation as true.
   *
   * @param inputValue the input value
   * @param dateQualification the date qualification requested
   * @return the date normalization result
   */
  default DateNormalizationResult extractDateProperty(String inputValue, DateQualification dateQualification) {
    return extract(inputValue, dateQualification, true);
  }

  /**
   * Utility method for calling {@link #extract(String, DateQualification, boolean)} with allowSwitchesDuringValidation as false.
   *
   * @param inputValue the input value
   * @param dateQualification the date qualification requested
   * @return the date normalization result
   */
  default DateNormalizationResult extractGenericProperty(String inputValue, DateQualification dateQualification) {
    return extract(inputValue, dateQualification, false);
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
  default DateQualification computeDateQualification(DateQualification requestedDateQualification,
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

