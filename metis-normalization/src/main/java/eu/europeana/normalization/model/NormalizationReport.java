package eu.europeana.normalization.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * The report on a normalization action.
 */
public class NormalizationReport {

  private final Map<String, NormalizationOperationReport> operations = new HashMap<>();

  /**
   * This method merges the report with another one, also merging the underlying operation reports.
   * This instance will contain the merged information.
   * 
   * @param otherReport The report that is to be merged into this report.
   */
  public void mergeWith(NormalizationReport otherReport) {
    Set<Entry<String, NormalizationOperationReport>> entrySet = otherReport.operations.entrySet();
    for (Entry<String, NormalizationOperationReport> op : entrySet) {
      NormalizationOperationReport myOpRep = operations.get(op.getKey());
      if (myOpRep == null) {
        operations.put(op.getKey(), op.getValue());
      } else {
        myOpRep.mergeWith(op.getValue());
      }
    }
  }

  /**
   * This method increments the counter for the specified operation by one. This method is not
   * public: we don't want to expose this.
   * 
   * @param operation The operation.
   * @param confidence The confidence of this operation.
   */
  protected void increment(String operation, ConfidenceLevel confidence) {
    NormalizationOperationReport normalizationOperationReport =
        operations.computeIfAbsent(operation, key -> new NormalizationOperationReport());
    normalizationOperationReport.increment(confidence);
  }

  /**
   * @return The reports, joined per operation and mapped by operation name.
   */
  public Map<String, NormalizationOperationReport> getReportsPerOperation() {
    return Collections.unmodifiableMap(operations);
  }
}
