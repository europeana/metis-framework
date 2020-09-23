package eu.europeana.metis.core.rest.execution.details;

import eu.europeana.cloud.common.model.dps.TaskState;
import eu.europeana.metis.core.workflow.plugins.ExecutionProgress;

/**
 * This class contains executionProgress information on a plugin's execution.
 */
public class PluginProgressView {

  private final int expectedRecords;
  private final int processedRecords;
  private final int progressPercentage;
  private final int errors;
  private final TaskState status;

  PluginProgressView(ExecutionProgress progress) {
    this.expectedRecords = progress.getExpectedRecords();
    this.processedRecords = progress.getProcessedRecords();
    this.errors = progress.getErrors();
    this.progressPercentage = progress.getProgressPercentage();
    this.status = progress.getStatus();
  }

  public int getExpectedRecords() {
    return expectedRecords;
  }

  public int getProcessedRecords() {
    return processedRecords;
  }

  public int getProgressPercentage() {
    return progressPercentage;
  }

  public int getErrors() {
    return errors;
  }

  public TaskState getStatus() {
    return status;
  }
}
