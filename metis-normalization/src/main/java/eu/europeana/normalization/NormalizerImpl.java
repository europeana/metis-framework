package eu.europeana.normalization;

import eu.europeana.normalization.model.NormalizationBatchResult;
import eu.europeana.normalization.model.NormalizationReport;
import eu.europeana.normalization.model.NormalizationResult;
import eu.europeana.normalization.normalizers.RecordNormalizeAction;
import eu.europeana.normalization.util.NormalizationException;
import eu.europeana.normalization.util.XmlException;
import eu.europeana.normalization.util.XmlUtil;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * This class is the implementation of the {@link Normalizer} interface.
 */
class NormalizerImpl implements Normalizer {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
      final Document recordDom = XmlUtil.parseDom(new StringReader(edmRecord));
      final NormalizationReport report = recordNormalizer.normalize(recordDom);
      final String resultRecord = XmlUtil.writeDomToString(recordDom);
      return NormalizationResult.createInstanceForSuccess(resultRecord, report);
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

  @Override
  public byte[] normalize(InputStream edmRecord) throws NormalizationException {

    // Sanity check.
    if (edmRecord == null) {
      throw new IllegalArgumentException("Input is null.");
    }

    // Perform the normalization.
    try {
      final Document recordDom = XmlUtil.parseDom(new InputStreamReader(edmRecord));
      recordNormalizer.normalize(recordDom);
      return XmlUtil.writeDomToByteArray(recordDom);
    } catch (XmlException e) {
      throw new NormalizationException("Error parsing XML: " + e.getMessage(), e);
    } catch (RuntimeException e) {
      throw new NormalizationException("Unexpected problem occurred: " + e.getMessage(), e);
    }
  }
}
