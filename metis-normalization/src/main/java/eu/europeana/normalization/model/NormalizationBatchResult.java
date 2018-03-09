package eu.europeana.normalization.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The class containing the reporting on a number of normalization actions.
 */
public class NormalizationBatchResult {

  private final NormalizationReport report;
  private final List<String> normalizedRecordsInEdmXml;
  private final int errorCount;

  /**
   * Constructor.
   * 
   * @param results The normalization result objects to be compiled/merged into this batch result.
   */
  public NormalizationBatchResult(List<NormalizationResult> results) {
    normalizedRecordsInEdmXml = new ArrayList<>(results.size());
    report = new NormalizationReport();
    int errorCounter = 0;
    for (NormalizationResult result : results) {
      normalizedRecordsInEdmXml.add(result.getNormalizedRecordInEdmXml());
      if (result.getErrorMessage() != null) {
        errorCounter++;
      }
      report.mergeWith(result.getReport());
    }
    this.errorCount = errorCounter;
  }

  /**
   * 
   * @return The merged normalization report.
   */
  public NormalizationReport getReport() {
    return report;
  }

  /**
   * 
   * @return The normalized records.
   */
  public List<String> getNormalizedRecordsInEdmXml() {
    return Collections.unmodifiableList(normalizedRecordsInEdmXml);
  }

  /**
   * 
   * @return The number of normalizations that presented with an error.
   */
  public int getErrorCount() {
    return errorCount;
  }
}
