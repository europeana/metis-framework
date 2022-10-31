package eu.europeana.enrichment.rest.client.report;

import java.util.HashSet;

public class ProcessedResult<T> {
  private final T processedRecord;
  private HashSet<ReportMessage> reportMessages;

  public ProcessedResult(T processedRecord) {
    this.processedRecord = processedRecord;
  }

  public ProcessedResult(T processedRecord, HashSet<ReportMessage> reportMessages) {
    this.processedRecord = processedRecord;
    this.reportMessages = reportMessages;
  }

  public ProcessedResult(ProcessedResult<T> processedResult) {
    this.processedRecord = processedResult.processedRecord;
    this.reportMessages = processedResult.reportMessages;
  }

  public T getProcessedRecord() {
    return processedRecord;
  }

  public HashSet<ReportMessage> getReport() {
    return reportMessages;
  }
}
