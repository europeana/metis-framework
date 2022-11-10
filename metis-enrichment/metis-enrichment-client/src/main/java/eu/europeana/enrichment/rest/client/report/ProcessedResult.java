package eu.europeana.enrichment.rest.client.report;

import java.util.HashSet;
import java.util.Objects;

public class ProcessedResult<T> {

  private final T processedRecord;
  private HashSet<ReportMessage> reportMessages;
  private final RecordStatus recordStatus;

  public ProcessedResult(T processedRecord) {
    this.processedRecord = processedRecord;
    this.reportMessages = new HashSet<>();
    this.recordStatus = getRecordStatus();
  }

  public ProcessedResult(T processedRecord, HashSet<ReportMessage> reportMessages) {
    this.processedRecord = processedRecord;
    this.reportMessages = reportMessages;
    this.recordStatus = getRecordStatus();
  }

  public ProcessedResult(ProcessedResult<T> processedResult) {
    this.processedRecord = processedResult.processedRecord;
    this.reportMessages = processedResult.reportMessages;
    this.recordStatus = processedResult.recordStatus;
  }

  public T getProcessedRecord() {
    return processedRecord;
  }

  public HashSet<ReportMessage> getReport() {
    return reportMessages;
  }

  public RecordStatus getRecordStatus() {
    if (reportMessages.isEmpty()) {
      return RecordStatus.CONTINUE;
    } else if (reportMessages.stream()
                             .anyMatch(reportMessage -> Objects.equals(reportMessage.getMessageType(), Type.ERROR))) {
      return RecordStatus.STOP;
    } else {
      return RecordStatus.CONTINUE;
    }
  }
}
