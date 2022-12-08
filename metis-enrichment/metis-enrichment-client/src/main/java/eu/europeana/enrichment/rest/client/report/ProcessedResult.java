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
   * RecordStatus
   * This is use to know if a processing needs to STOP or CONTINUE for a record.
   */
  public enum RecordStatus {
    CONTINUE,
    STOP
  }

  /**
   * Constructor with record of type T
   * @param processedRecord record that has been processed as result.
   */
  public ProcessedResult(T processedRecord) {
    this.processedRecord = processedRecord;
    this.reportMessages = new HashSet<>();
    this.recordStatus = RecordStatus.CONTINUE;
  }

  /**
   * Constructor with record of type T and report messages
   * @param processedRecord record that has been processed as result.
   * @param reportMessages report of the processing of the record as result.
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
   * @param processedResult contains a record, a report messages and record status encapsulated within.
   */
  public ProcessedResult(ProcessedResult<T> processedResult) {
    this.processedRecord = processedResult.processedRecord;
    this.reportMessages = processedResult.reportMessages;
    this.recordStatus = processedResult.recordStatus;
  }

  /**
   * Obtain the record
   * @return record that has been set as a result of processing.
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
   * @return status of overall the processed result if there is an error in the report messages
   * returns STOP, otherwise it returns CONTINUE.
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
