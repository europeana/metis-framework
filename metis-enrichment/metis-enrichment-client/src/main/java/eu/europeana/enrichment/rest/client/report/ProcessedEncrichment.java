package eu.europeana.enrichment.rest.client.report;

import java.util.HashSet;

public class ProcessedEncrichment<T> {
  private final T enrichedRecord;
  private HashSet<ReportMessage> reportMessages;

  public ProcessedEncrichment(T enrichedRecord) {
    this.enrichedRecord = enrichedRecord;
  }

  public ProcessedEncrichment(T enrichedRecord, HashSet<ReportMessage> reportMessages) {
    this.enrichedRecord = enrichedRecord;
    this.reportMessages = reportMessages;
  }

  public ProcessedEncrichment(ProcessedEncrichment<T> processedEncrichment) {
    this.enrichedRecord = processedEncrichment.enrichedRecord;
    this.reportMessages = processedEncrichment.reportMessages;
  }

  public T getEnrichedRecord() {
    return enrichedRecord;
  }

  public HashSet<ReportMessage> getReport() {
    return reportMessages;
  }
}
