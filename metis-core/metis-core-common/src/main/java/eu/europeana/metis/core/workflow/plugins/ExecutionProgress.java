package eu.europeana.metis.core.workflow.plugins;

import dev.morphia.annotations.Entity;
import eu.europeana.cloud.common.model.dps.TaskState;

/**
 * Contains execution progress information of a task.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-06-01
 */
@Entity
public class ExecutionProgress {

  // The total number of expected records excluding deleted records.
  private int expectedRecords;

  // The total number of records processed so far excluding deleted records and including ignored records if applicable.
  private int processedRecords;

  // The percentage: the division of the actual and expected number of processed records.
  private int progressPercentage;

  // The number of processed records so far that are to be ignored for follow-up tasks.
  private int ignoredRecords = 0;

  // The number of deleted records processed so far.
  private int deletedRecords = 0;

  // The number of errors encountered so far.
  private int errors;

  // The current state of the task.
  private TaskState status;

  public int getExpectedRecords() {
    return expectedRecords;
  }

  public void setExpectedRecords(int expectedRecords) {
    this.expectedRecords = expectedRecords;
  }

  public int getProcessedRecords() {
    return processedRecords;
  }

  public void setProcessedRecords(int processedRecords) {
    this.processedRecords = processedRecords;
  }

  public int getProgressPercentage() {
    return progressPercentage;
  }

  public void setProgressPercentage(int progressPercentage) {
    this.progressPercentage = progressPercentage;
  }

  public int getIgnoredRecords() {
    return ignoredRecords;
  }

  public void setIgnoredRecords(int ignoredRecords) {
    this.ignoredRecords = ignoredRecords;
  }

  public int getDeletedRecords() {
    return deletedRecords;
  }

  public void setDeletedRecords(int deletedRecords) {
    this.deletedRecords = deletedRecords;
  }

  public int getErrors() {
    return errors;
  }

  public void setErrors(int errors) {
    this.errors = errors;
  }

  public TaskState getStatus() {
    return status;
  }

  public void setStatus(TaskState status) {
    this.status = status;
  }

  public void recalculateProgressPercentage() {
    this.progressPercentage = this.expectedRecords == 0 ? 0
        : (int) Math.round(100.0 * (this.processedRecords + this.deletedRecords)/ (this.expectedRecords + this.deletedRecords));
  }
}
