package eu.europeana.metis.dereference.vocimport;

import eu.europeana.metis.dereference.vocimport.exception.VocabularyImportException;
import eu.europeana.metis.dereference.vocimport.model.Vocabulary;
import java.util.function.Consumer;

/**
 * Implementations of this interface can perform vocabulary collection validation. They load the
 * vocabularies one by one, validate them and send the result to the caller.
 */
public interface VocabularyCollectionValidator {

  /**
   * Load the vocabularies, validate them and send them to the user via the receiver. Note that this
   * is happening on-demand (in a lazy fashion), meaning that it is possible that the receiver has
   * already received some vocabularies before a problem is detected and reported.
   *
   * @param vocabularyReceiver The destination of the vocabularies.
   * @param warningReceiver The destination for warning messages (that are not errors).
   * @throws VocabularyImportException In case loading or validation failed.
   */
  void validate(Consumer<Vocabulary> vocabularyReceiver, Consumer<String> warningReceiver)
          throws VocabularyImportException;

  /**
   * Load the vocabularies, validate them and send them to the user via the receiver. Note that this
   * is happening on-demand (in a lazy fashion), meaning that it is possible that the receiver has
   * already received some vocabularies before a problem is detected and reported. This validation
   * does not include retrieving and testing the example and counter example records. It is
   * therefore a strict subset of the validation performed by {@link #validate(Consumer,
   * Consumer)}.
   *
   * @param vocabularyReceiver The destination of the vocabularies.
   * @throws VocabularyImportException In case loading or validation failed.
   */
  void validateVocabularyOnly(Consumer<Vocabulary> vocabularyReceiver)
          throws VocabularyImportException;

}
