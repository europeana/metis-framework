package eu.europeana.metis.dereference.vocimport;

import eu.europeana.metis.dereference.IncomingRecordToEdmConverter;
import eu.europeana.metis.dereference.RdfRetriever;
import eu.europeana.metis.dereference.vocimport.exception.VocabularyImportException;
import eu.europeana.metis.dereference.vocimport.model.Vocabulary;
import eu.europeana.metis.dereference.vocimport.model.VocabularyLoader;
import eu.europeana.metis.dereference.vocimport.utils.NonCollidingPathVocabularyTrie;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

public class VocabularyCollectionValidatorImpl implements VocabularyCollectionValidator {

  private final VocabularyCollectionImporter importer;
  private final boolean lenientOnLackOfExamples;
  private final boolean lenientOnMappingTestFailures;
  private final boolean lenientOnExampleRetrievalFailures;
  private final DocumentBuilder documentBuilder;

  /**
   * Constructor.
   *
   * @param importer Vocabulary importer.
   * @param lenientOnLackOfExamples Whether the the validator is lenient on vocabulary mappings
   * without examples.
   * @param lenientOnMappingTestFailures Whether the validator is lenient on errors and unmet
   * expectations when applying the mapping to the example and counterexample values.
   * @param lenientOnExampleRetrievalFailures Whether the validator is lenient on example or
   * counterexample retrieval (download) issues.
   */
  public VocabularyCollectionValidatorImpl(VocabularyCollectionImporter importer,
          boolean lenientOnLackOfExamples, boolean lenientOnMappingTestFailures,
          boolean lenientOnExampleRetrievalFailures) {

    // Set parameters
    this.importer = importer;
    this.lenientOnLackOfExamples = lenientOnLackOfExamples;
    this.lenientOnMappingTestFailures = lenientOnMappingTestFailures;
    this.lenientOnExampleRetrievalFailures = lenientOnExampleRetrievalFailures;

    // Create XML builder
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    try {
      documentBuilder = factory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException("Problems setting up XML reader (this should not happen).", e);
    }
  }

  @Override
  public void validate(Consumer<Vocabulary> vocabularyReceiver, Consumer<String> warningReceiver)
          throws VocabularyImportException {
    validateInternal(vocabularyReceiver, warningReceiver, true);
  }

  @Override
  public void validateVocabularyOnly(Consumer<Vocabulary> vocabularyReceiver)
          throws VocabularyImportException {
    validateInternal(vocabularyReceiver, null, false);
  }

  private void validateInternal(Consumer<Vocabulary> vocabularyReceiver,
          Consumer<String> warningReceiver, boolean validateExamples)
          throws VocabularyImportException {
    final DuplicationChecker duplicationChecker = new DuplicationChecker();
    final Iterable<VocabularyLoader> vocabularyLoaders = importer.importVocabularies();
    for (VocabularyLoader loader : vocabularyLoaders) {
      final Vocabulary vocabulary = loader.load();
      final IncomingRecordToEdmConverter converter = validateVocabulary(vocabulary,
              duplicationChecker);
      if (validateExamples) {
        validateExamples(vocabulary, warningReceiver, converter);
      }
      vocabularyReceiver.accept(vocabulary);
    }
  }

  private IncomingRecordToEdmConverter validateVocabulary(Vocabulary vocabulary,
          DuplicationChecker duplicationChecker) throws VocabularyImportException {

    // Check the presence of the required fields.
    if (vocabulary.getName() == null) {
      throw new VocabularyImportException(
              String.format("No vocabulary name given in metadata at [%s].",
                      vocabulary.getReadableMetadataLocation()));
    }
    if (vocabulary.getTypes().isEmpty()) {
      throw new VocabularyImportException(
              String.format("No vocabulary type(s) given in metadata at [%s].",
                      vocabulary.getReadableMetadataLocation()));
    }
    if (vocabulary.getPaths().isEmpty()) {
      throw new VocabularyImportException(
              String.format("No vocabulary path(s) given in metadata at [%s].",
                      vocabulary.getReadableMetadataLocation()));
    }
    if (vocabulary.getTransformation() == null) {
      throw new VocabularyImportException(
              String.format("No transformation given in mapping at [%s].",
                      vocabulary.getReadableMappingLocation()));
    }

    // Check whether name and links are unique.
    duplicationChecker.checkAndRegister(vocabulary);

    // Verifying the xslt - compile it.
    try {
      return new IncomingRecordToEdmConverter(vocabulary.getTransformation());
    } catch (TransformerException e) {
      throw new VocabularyImportException(
              String.format("Error in the transformation given in mapping at [%s].",
                      vocabulary.getReadableMappingLocation()), e);
    }
  }

  private void validateExamples(Vocabulary vocabulary, Consumer<String> warningReceiver,
          IncomingRecordToEdmConverter converter) throws VocabularyImportException {

    // Testing the examples (if there are any - otherwise issue warning).
    if (vocabulary.getExamples().isEmpty()) {
      final String message = String.format("No examples specified for metadata at [%s].",
              vocabulary.getReadableMetadataLocation());
      if (lenientOnLackOfExamples) {
        warningReceiver.accept(message);
      } else {
        throw new VocabularyImportException(message);
      }
    }
    for (String example : vocabulary.getExamples()) {
      testExample(converter, example, vocabulary.getSuffix(), false,
              vocabulary.getReadableMetadataLocation(), warningReceiver);
    }

    // Testing the counter examples (if there are any).
    for (String example : vocabulary.getCounterExamples()) {
      testExample(converter, example, vocabulary.getSuffix(), true,
              vocabulary.getReadableMetadataLocation(), warningReceiver);
    }
  }

  private String getTestErrorMessage(String example, boolean isCounterExample,
          String readableMetadataLocation, String sentenceContinuation, Exception exception) {
    final String sentence = String.format("%s '%s' in metadata at [%s] %s.",
            isCounterExample ? "Counterexample" : "Example", example, readableMetadataLocation,
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
          boolean isCounterExample, String readableMetadataLocation,
          Consumer<String> warningReceiver) throws VocabularyImportException {

    // Retrieve the example - is not null.
    final String exampleContent;
    try {
      exampleContent = new RdfRetriever().retrieve(example, suffix);
    } catch (IOException e) {
      final String message = getTestErrorMessage(example, isCounterExample,
              readableMetadataLocation, "could not be retrieved", e);
      processTestError(message, lenientOnExampleRetrievalFailures, warningReceiver, e);
      return;
    }

    // Convert the example
    final String result;
    try {
      result = converter.convert(exampleContent, example);
    } catch (TransformerException e) {
      final String message = getTestErrorMessage(example, isCounterExample,
              readableMetadataLocation, "could not be mapped", e);
      processTestError(message, lenientOnMappingTestFailures, warningReceiver, e);
      return;
    }

    // Check whether the example yielded a mapped entity or not
    if (StringUtils.isNotBlank(result) && isCounterExample) {
      final String message = getTestErrorMessage(example, isCounterExample,
              readableMetadataLocation, "yielded a mapped result, but is expected not to", null);
      processTestError(message, lenientOnMappingTestFailures, warningReceiver, null);
    } else if (StringUtils.isBlank(result) && !isCounterExample) {
      final String message = getTestErrorMessage(example, isCounterExample,
              readableMetadataLocation, "did not yield a mapped result, but is expected to", null);
      processTestError(message, lenientOnMappingTestFailures, warningReceiver, null);
    }

    // Check whether the example yielded valid XML
    if (StringUtils.isNotBlank(result)) {
      try (InputStream inputStream = new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8))) {
        documentBuilder.parse(inputStream);
      } catch (IOException | SAXException e) {
        final String message = getTestErrorMessage(example, isCounterExample,
                readableMetadataLocation, "did not yield a valid XML", e);
        throw new VocabularyImportException(message, e);
      }
    }
  }

  private static class DuplicationChecker {

    private final NonCollidingPathVocabularyTrie trie = new NonCollidingPathVocabularyTrie();
    private final Map<String, String> knownNames = new HashMap<>();

    void checkAndRegister(Vocabulary vocabulary) throws VocabularyImportException {

      // Handle the link uniqueness
      trie.insert(vocabulary);

      // Handle the name uniqueness
      final String nameToCheck = vocabulary.getName().trim().replaceAll("\\s", " ")
              .toLowerCase(Locale.ENGLISH);
      if (knownNames.containsKey(nameToCheck)) {
        final String message = String.format("Duplicate name '%s' detected in metadata at [%s]:"
                        + " metadata at [%s] contains a name that is similar.", vocabulary.getName(),
                vocabulary.getReadableMetadataLocation(), knownNames.get(nameToCheck));
        throw new VocabularyImportException(message);
      }
      knownNames.put(nameToCheck, vocabulary.getReadableMetadataLocation());
    }
  }
}
