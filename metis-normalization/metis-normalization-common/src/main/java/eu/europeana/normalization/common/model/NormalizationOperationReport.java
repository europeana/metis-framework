package eu.europeana.normalization.common.model;

import eu.europeana.normalization.util.MapOfInts;
import java.util.Map.Entry;
import java.util.Set;

class NormalizationOperationReport {

  private String operation;
  private final MapOfInts<ConfidenceLevel> counts = new MapOfInts<>();


  public NormalizationOperationReport(String operation) {
    super();
    this.operation = operation;
  }

  public NormalizationOperationReport() {
  }

  public void mergeWith(NormalizationOperationReport other) {
    Set<Entry<ConfidenceLevel, Integer>> entries = other.getCounts().entrySet();
    for (Entry<ConfidenceLevel, Integer> entry : entries) {
      counts.addTo(entry.getKey(), entry.getValue());
    }
  }


  public MapOfInts<ConfidenceLevel> getCounts() {
    return counts;
  }


  public void increment(ConfidenceLevel confidence) {
    counts.incrementTo(confidence);
  }


}