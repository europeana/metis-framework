package eu.europeana.normalization.common.model;

import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class NormalizationReport {

  private Map<String, NormalizationOperationReport> operations = new Hashtable<>();

  public void mergeWith(NormalizationReport other) {
    Set<Entry<String, NormalizationOperationReport>> entrySet = other.getOperations().entrySet();
    for (Entry<String, NormalizationOperationReport> op : entrySet) {
      NormalizationOperationReport myOpRep = operations.get(op.getKey());
      if (myOpRep == null) {
        operations.put(op.getKey(), op.getValue());
      } else {
        myOpRep.mergeWith(op.getValue());
      }
    }

  }

  public Map<String, NormalizationOperationReport> getOperations() {
    return operations;
  }

  public void increment(String op, ConfidenceLevel confidence) {
    NormalizationOperationReport normalizationOperationReport = operations
        .computeIfAbsent(op, NormalizationOperationReport::new);
    normalizationOperationReport.increment(confidence);
  }


}
