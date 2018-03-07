package eu.europeana.normalization;

import java.io.StringReader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import eu.europeana.normalization.common.RecordNormalization;
import eu.europeana.normalization.common.cleaning.DuplicateStatementCleaning;
import eu.europeana.normalization.common.cleaning.MarkupTagsCleaning;
import eu.europeana.normalization.common.cleaning.TrimAndEmptyValueCleaning;
import eu.europeana.normalization.common.language.LanguageNormalizer;
import eu.europeana.normalization.common.language.LanguageNormalizer.SupportedOperations;
import eu.europeana.normalization.common.language.LanguagesVocabulary;
import eu.europeana.normalization.common.model.NormalizationBatchResult;
import eu.europeana.normalization.common.model.NormalizationReport;
import eu.europeana.normalization.common.model.NormalizationResult;
import eu.europeana.normalization.common.normalizers.ChainedNormalization;
import eu.europeana.normalization.util.NormalizationConfigurationException;
import eu.europeana.normalization.util.XmlException;
import eu.europeana.normalization.util.XmlUtil;

class NormalizerImpl implements Normalizer {

  private static final Logger LOGGER = LoggerFactory.getLogger(NormalizerImpl.class);

  private final RecordNormalization recordNormalizer;

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
    final TrimAndEmptyValueCleaning spacesCleaner = new TrimAndEmptyValueCleaning();
    final MarkupTagsCleaning markupStatementsCleaner = new MarkupTagsCleaning();
    final LanguageNormalizer languageNormalizer =
        new LanguageNormalizer(targetLanguageVocabulary, minimumConfidence);
    languageNormalizer.setOperations(SupportedOperations.ALL);
    final DuplicateStatementCleaning dupStatementsCleaner = new DuplicateStatementCleaning();

    // Combine the normalizers into one record normalizer
    this.recordNormalizer = new ChainedNormalization(spacesCleaner.toEdmRecordNormalizer(),
        markupStatementsCleaner.toEdmRecordNormalizer(), languageNormalizer.toEdmRecordNormalizer(),
        dupStatementsCleaner);
  }

  @Override
  public NormalizationBatchResult normalize(List<String> edmRecords) {

    // Sanity check.
    if (edmRecords == null || edmRecords.stream().anyMatch(Objects::isNull)) {
      throw new IllegalArgumentException("Input is null or contains null elements.");
    }

    // Normalize all records.
    final List<NormalizationResult> result =
        edmRecords.stream().map(this::normalize).collect(Collectors.toList());
    return new NormalizationBatchResult(result);
  }

  @Override
  public NormalizationResult normalize(String edmRecord) {

    // Sanity check.
    if (edmRecord == null) {
      throw new IllegalArgumentException("Input is null.");
    }

    // Perform the normalization.
    try {
      return normalizeInternal(edmRecord);
    } catch (XmlException e) {
      return new NormalizationResult("Error parsing XML: " + e.getMessage(), edmRecord);
    } catch (RuntimeException e) {
      LOGGER.error(e.getMessage(), e);
      return new NormalizationResult("Unexpected problem: " + e.getMessage(), edmRecord);
    }
  }

  private NormalizationResult normalizeInternal(String edmRecord) throws XmlException {
    final Document recordDom = XmlUtil.parseDom(new StringReader(edmRecord));
    final NormalizationReport report = recordNormalizer.normalize(recordDom);
    final String resultRecord = XmlUtil.writeDomToString(recordDom);
    return new NormalizationResult(resultRecord, report);
  }
}
