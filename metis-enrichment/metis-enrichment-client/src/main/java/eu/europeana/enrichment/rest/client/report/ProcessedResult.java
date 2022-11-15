package eu.europeana.enrichment.rest.client.report;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Processed result class
 * @param <T> type of object to process and return
 */
public class ProcessedResult<T> {

  private final T processedRecord;
  private HashSet<ReportMessage> reportMessages;
  private final RecordStatus recordStatus;

  /**
   * Constructor with record
   * @param processedRecord
   */
  public ProcessedResult(T processedRecord) {
    this.processedRecord = processedRecord;
    this.reportMessages = new HashSet<>();
    this.recordStatus = RecordStatus.CONTINUE;
  }

  /**
   * Constructor with record and report
   * @param processedRecord
   * @param reportMessages
   */
  public ProcessedResult(T processedRecord, Set<ReportMessage> reportMessages) {
    this.processedRecord = processedRecord;
    this.reportMessages = (HashSet<ReportMessage>) reportMessages;
    if (reportMessages.isEmpty()) {
      this.recordStatus =  RecordStatus.CONTINUE;
    } else if (reportMessages.stream()
                             .anyMatch(reportMessage -> Objects.equals(reportMessage.getMessageType(), Type.ERROR))) {
      this.recordStatus =  RecordStatus.STOP;
    } else {
      this.recordStatus =  RecordStatus.CONTINUE;
    }
  }

  /**
   * Constructor with a Processed Result
   * @param processedResult
   */
  public ProcessedResult(ProcessedResult<T> processedResult) {
    this.processedRecord = processedResult.processedRecord;
    this.reportMessages = processedResult.reportMessages;
    this.recordStatus = processedResult.recordStatus;
  }

  /**
   * Obtain the record
   * @return record
   */
  public T getProcessedRecord() {
    return processedRecord;
  }

  /**
   * Obtain the report
   * @return report messages after processing is completed
   */
  public Set<ReportMessage> getReport() {
    return reportMessages;
  }

  /**
   * Obtain record status
   * @return status of overall result
   */
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
