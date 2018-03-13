package eu.europeana.normalization;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import eu.europeana.normalization.languages.LanguagesVocabulary;
import eu.europeana.normalization.model.NormalizationBatchResult;
import eu.europeana.normalization.model.NormalizationReport;
import eu.europeana.normalization.model.NormalizationResult;
import eu.europeana.normalization.normalizers.ChainedNormalizer;
import eu.europeana.normalization.normalizers.CleanMarkupTagsNormalizer;
import eu.europeana.normalization.normalizers.CleanSpaceCharactersNormalizer;
import eu.europeana.normalization.normalizers.LanguageReferenceNormalizer;
import eu.europeana.normalization.normalizers.LanguageReferenceNormalizer.SupportedElements;
import eu.europeana.normalization.normalizers.RecordNormalizer;
import eu.europeana.normalization.normalizers.RemoveDuplicateStatementNormalizer;
import eu.europeana.normalization.util.NormalizationConfigurationException;
import eu.europeana.normalization.util.NormalizationException;
import eu.europeana.normalization.util.XmlException;
import eu.europeana.normalization.util.XmlUtil;

class NormalizerImpl implements Normalizer {

  private static final Logger LOGGER = LoggerFactory.getLogger(NormalizerImpl.class);

  private final RecordNormalizer recordNormalizer;

  NormalizerImpl(LanguagesVocabulary targetLanguageVocabulary, float minimumConfidence)
      throws NormalizationConfigurationException {

    // Sanity check.
    if (targetLanguageVocabulary == null) {
      throw new IllegalArgumentException("Target language vocabulary is null");
    }
    if (minimumConfidence < 0 || minimumConfidence > 1) {
      throw new IllegalArgumentException(
          "Minimum confidence must be a number between 0 and 1 (inclusive).");
    }

    // Set up the normalizers
    final CleanSpaceCharactersNormalizer spacesCleaner = new CleanSpaceCharactersNormalizer();
    final CleanMarkupTagsNormalizer markupStatementsCleaner = new CleanMarkupTagsNormalizer();
    final LanguageReferenceNormalizer languageNormalizer = new LanguageReferenceNormalizer(
        targetLanguageVocabulary, minimumConfidence, SupportedElements.ALL);
    final RemoveDuplicateStatementNormalizer dupStatementsCleaner =
        new RemoveDuplicateStatementNormalizer();

    // Combine the normalizers into one record normalizer
    this.recordNormalizer = new ChainedNormalizer(spacesCleaner.getAsRecordNormalizer(),
        markupStatementsCleaner.getAsRecordNormalizer(), languageNormalizer.getAsRecordNormalizer(),
        dupStatementsCleaner);
  }

  @Override
  public NormalizationBatchResult normalize(List<String> edmRecords) throws NormalizationException {

    // Sanity check.
    if (edmRecords == null || edmRecords.stream().anyMatch(Objects::isNull)) {
      throw new IllegalArgumentException("Input is null or contains null elements.");
    }

    // Normalize all records.
    final List<NormalizationResult> result = new ArrayList<>();
    for (String record : edmRecords) {
      result.add(normalize(record));
    }
    return new NormalizationBatchResult(result);
  }

  @Override
  public NormalizationResult normalize(String edmRecord) throws NormalizationException {

    // Sanity check.
    if (edmRecord == null) {
      throw new IllegalArgumentException("Input is null.");
    }

    // Perform the normalization.
    try {
      return normalizeInternal(edmRecord);
    } catch (XmlException e) {
      return NormalizationResult.createInstanceForError("Error parsing XML: " + e.getMessage(),
          edmRecord);
    } catch (RuntimeException e) {
      LOGGER.error(e.getMessage(), e);
      return NormalizationResult.createInstanceForError("Unexpected problem: " + e.getMessage(),
          edmRecord);
    }
  }

  private NormalizationResult normalizeInternal(String edmRecord)
      throws XmlException, NormalizationException {
    final Document recordDom = XmlUtil.parseDom(new StringReader(edmRecord));
    final NormalizationReport report = recordNormalizer.normalize(recordDom);
    final String resultRecord = XmlUtil.writeDomToString(recordDom);
    return NormalizationResult.createInstanceForSuccess(resultRecord, report);
  }
}
