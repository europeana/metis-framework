package eu.europeana.metis.dereference.vocimport;

import eu.europeana.metis.dereference.IncomingRecordToEdmConverter;
import eu.europeana.metis.dereference.RdfRetriever;
import eu.europeana.metis.dereference.vocimport.exception.VocabularyImportException;
import eu.europeana.metis.dereference.vocimport.model.Vocabulary;
import eu.europeana.metis.dereference.vocimport.model.VocabularyLoader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import javax.xml.transform.TransformerException;
import org.apache.commons.lang3.StringUtils;

public class VocabularyCollectionValidatorImpl implements VocabularyCollectionValidator {

  private final VocabularyCollectionImporter importer;
  private final boolean lenientOnLackOfExamples;
  private final boolean lenientOnMappingTestFailures;
  private final boolean lenientOnExampleRetrievalFailures;

  VocabularyCollectionValidatorImpl(VocabularyCollectionImporter importer,
          boolean lenientOnLackOfExamples, boolean lenientOnMappingTestFailures,
          boolean lenientOnExampleRetrievalFailures) {
    this.importer = importer;
    this.lenientOnLackOfExamples = lenientOnLackOfExamples;
    this.lenientOnMappingTestFailures = lenientOnMappingTestFailures;
    this.lenientOnExampleRetrievalFailures = lenientOnExampleRetrievalFailures;
  }

  @Override
  public void validate(Consumer<Vocabulary> vocabularyReceiver, Consumer<String> warningReceiver)
          throws VocabularyImportException {
    final NameDuplicationChecker nameDuplicationChecker = new NameDuplicationChecker();
    final Iterable<VocabularyLoader> vocabularyLoaders = importer.importVocabularies();
    for (VocabularyLoader loader : vocabularyLoaders) {
      final Vocabulary vocabulary = loader.load();
      validate(vocabulary, warningReceiver, nameDuplicationChecker);
      vocabularyReceiver.accept(vocabulary);
    }
  }

  private void validate(Vocabulary vocabulary, Consumer<String> warningReceiver,
          NameDuplicationChecker nameDuplicationChecker) throws VocabularyImportException {

    // Check the presence of the required fields.
    if (vocabulary.getName() == null) {
      throw new VocabularyImportException(
              String.format("No vocabulary name given in metadata at [%s].",
                      vocabulary.getMetadataSourceLocation()));
    }
    if (vocabulary.getType() == null) {
      throw new VocabularyImportException(
              String.format("No vocabulary type given in metadata at [%s].",
                      vocabulary.getMetadataSourceLocation()));
    }
    if (vocabulary.getPaths().isEmpty()) {
      throw new VocabularyImportException(
              String.format("No vocabulary path(s) given in metadata at [%s].",
                      vocabulary.getMetadataSourceLocation()));
    }
    if (vocabulary.getTransformation() == null) {
      throw new VocabularyImportException(
              String.format("No transformation given in mapping at [%s].",
                      vocabulary.getMappingSourceLocation()));
    }

    // Check whether name is unique.
    nameDuplicationChecker
            .checkAndRegisterName(vocabulary.getName(), vocabulary.getMetadataSourceLocation());

    // Verifying the xslt - compile it.
    final IncomingRecordToEdmConverter converter;
    try {
      converter = new IncomingRecordToEdmConverter(vocabulary.getTransformation());
    } catch (TransformerException e) {
      throw new VocabularyImportException(
              String.format("Error in the transformation given in mapping at [%s].",
                      vocabulary.getMappingSourceLocation()), e);
    }

    // Testing the examples (if there are any - otherwise issue warning).
    if (vocabulary.getExamples().isEmpty()) {
      final String message = String.format("No examples specified for metadata at [%s].",
              vocabulary.getMetadataSourceLocation());
      if (lenientOnLackOfExamples) {
        warningReceiver.accept(message);
      } else {
        throw new VocabularyImportException(message);
      }
    }
    for (String example : vocabulary.getExamples()) {
      testExample(converter, example, vocabulary.getSuffix(), false,
              vocabulary.getMetadataSourceLocation(), warningReceiver);
    }

    // Testing the counter examples (if there are any).
    for (String example : vocabulary.getCounterExamples()) {
      testExample(converter, example, vocabulary.getSuffix(), true,
              vocabulary.getMetadataSourceLocation(), warningReceiver);
    }
  }

  private String getTestErrorMessage(String example, boolean isCounterExample,
          String metadataSourceLocation, String sentenceContinuation, Exception exception) {
    final String sentence = String.format("%s '%s' in metadata at [%s] %s.",
            isCounterExample ? "Counterexample" : "Example", example, metadataSourceLocation,
            sentenceContinuation);
    return sentence + (exception == null ? "" : " Error: " + exception.getMessage());
  }

  private void processTestError(String message, boolean isWarning, Consumer<String> warningReceiver,
          Exception originalException) throws VocabularyImportException {
    if (isWarning) {
      warningReceiver.accept(message);
    } else {
      throw new VocabularyImportException(message, originalException);
    }
  }

  private void testExample(IncomingRecordToEdmConverter converter, String example, String suffix,
          boolean isCounterExample, String metadataSourceLocation, Consumer<String> warningReceiver)
          throws VocabularyImportException {

    // Retrieve the example - is not null.
    final String exampleContent;
    try {
      exampleContent = new RdfRetriever().retrieve(example, suffix);
    } catch (IOException e) {
      final String message = getTestErrorMessage(example, isCounterExample, metadataSourceLocation,
              "could not be retrieved", e);
      processTestError(message, lenientOnExampleRetrievalFailures, warningReceiver, e);
      return;
    }

    // Convert the example
    final String result;
    try {
      result = converter.convert(exampleContent, example);
    } catch (TransformerException e) {
      final String message = getTestErrorMessage(example, isCounterExample, metadataSourceLocation,
              "could not be mapped", e);
      processTestError(message, lenientOnMappingTestFailures, warningReceiver, e);
      return;
    }

    // Check whether the example yielded a mapped entity or not
    if (StringUtils.isNotBlank(result) && isCounterExample) {
      final String message = getTestErrorMessage(example, isCounterExample, metadataSourceLocation,
              "yielded a mapped result, but is expected not to", null);
      processTestError(message, lenientOnMappingTestFailures, warningReceiver, null);
    } else if (StringUtils.isBlank(result) && !isCounterExample) {
      final String message = getTestErrorMessage(example, isCounterExample, metadataSourceLocation,
              "did not yield a mapped result, but is expected to", null);
      processTestError(message, lenientOnMappingTestFailures, warningReceiver, null);
    }
  }

  private static class NameDuplicationChecker {

    private final Map<String, String> knownNames = new HashMap<>();

    void checkAndRegisterName(String name, String metadataLocation) {
      final String nameToCheck = name.trim().replaceAll("\\s", " ").toLowerCase(Locale.ENGLISH);
      if (knownNames.containsKey(nameToCheck)) {
        final String message = String.format("Duplicate name '%s' detected in metadata at [%s]:"
                        + " metadata at [%s] contains a name that is similar.", name,
                metadataLocation, knownNames.get(nameToCheck));
        throw new IllegalStateException(message);
      }
      knownNames.put(nameToCheck, metadataLocation);
    }
  }
}
