package eu.europeana.normalization;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import eu.europeana.normalization.model.NormalizationBatchResult;
import eu.europeana.normalization.model.NormalizationReport;
import eu.europeana.normalization.model.NormalizationResult;
import eu.europeana.normalization.normalizers.RecordNormalizeAction;
import eu.europeana.normalization.util.NormalizationException;
import eu.europeana.normalization.util.XmlException;
import eu.europeana.normalization.util.XmlUtil;

/**
 * This class is the implementation of the {@link Normalizer} interface.
 */
class NormalizerImpl implements Normalizer {

  private static final Logger LOGGER = LoggerFactory.getLogger(NormalizerImpl.class);

  private final RecordNormalizeAction recordNormalizer;

  /**
   * Constructor.
   * 
   * @param recordNormalizer The record normalizer that is executed by this normalizer.
   */
  NormalizerImpl(RecordNormalizeAction recordNormalizer) {
    this.recordNormalizer = recordNormalizer;
  }

  @Override
  public NormalizationBatchResult normalize(List<String> edmRecords) throws NormalizationException {

    // Sanity check.
    if (edmRecords == null || edmRecords.stream().anyMatch(Objects::isNull)) {
      throw new IllegalArgumentException("Input is null or contains null elements.");
    }

    // Normalize all records.
    final List<NormalizationResult> result = new ArrayList<>(edmRecords.size());
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
      LOGGER.warn("Parsing of xml exception", e);
      return NormalizationResult.createInstanceForError("Error parsing XML: " + e.getMessage(),
          edmRecord);
    } catch (RuntimeException e) {
      LOGGER.error("Unexpected runtime exception", e);
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
