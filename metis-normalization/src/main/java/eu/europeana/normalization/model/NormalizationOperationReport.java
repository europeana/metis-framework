package eu.europeana.normalization.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The report on one operation during a normalization action.
 */
class NormalizationOperationReport {

  private final Map<ConfidenceLevel, Integer> counts = new EnumMap<>(ConfidenceLevel.class);

  /**
   * This method merges this report with another report, adding the counters. This report will
   * contain the updated counters.
   * 
   * @param otherReport The report to merge into this report.
   */
  public void mergeWith(NormalizationOperationReport otherReport) {
    for (Entry<ConfidenceLevel, Integer> entry : otherReport.counts.entrySet()) {
      this.counts.merge(entry.getKey(), entry.getValue(), Integer::sum);
    }
  }

  /**
   * TODO JOCHEN hide this from code using this library!
   * 
   * This method increments the counter for the specified confidence level by one.
   * 
   * @param confidence The confidence level for which to increase the counter.
   */
  public void increment(ConfidenceLevel confidence) {
    this.counts.merge(confidence, 1, Integer::sum);
  }

  /**
   * 
   * @return The counts in this report, mapped by confidence level.
   */
  public Map<ConfidenceLevel, Integer> getCounts() {
    return Collections.unmodifiableMap(counts);
  }
}
