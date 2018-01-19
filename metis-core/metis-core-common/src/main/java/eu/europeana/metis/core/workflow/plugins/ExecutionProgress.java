package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.common.model.dps.TaskInfo;
import eu.europeana.cloud.common.model.dps.TaskState;

/**
 * Contains execution progress information of a task.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-06-01
 */
public class ExecutionProgress {

  private int expectedRecords;
  private int processedRecords;
  private int progressPercentage;
  private int errors;
  private TaskState status;

  /**
   * Copy information from {@link TaskInfo} to {@link ExecutionProgress}
   *
   * @param taskInfo {@link TaskInfo}
   * @return the current object
   */
  public ExecutionProgress copyExternalTaskInformation(TaskInfo taskInfo) {
    expectedRecords = taskInfo.getExpectedSize();
    processedRecords = taskInfo.getProcessedElementCount();
    progressPercentage = taskInfo.getProcessedPercentage();
    errors = taskInfo.getErrors();
    status = taskInfo.getState();
    return this;
  }

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
}
