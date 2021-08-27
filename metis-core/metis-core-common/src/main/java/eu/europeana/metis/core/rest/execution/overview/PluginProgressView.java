package eu.europeana.metis.core.rest.execution.overview;

import eu.europeana.metis.core.workflow.plugins.ExecutionProgress;

/**
 * This class contains executionProgress information on a plugin's execution.
 */
public class PluginProgressView {

  private int expectedRecords;
  private int processedRecords;
  private int ignoredRecords;
  private int deletedRecords;
  private int errors;
  private int progressPercentage;

  PluginProgressView() {
  }

  PluginProgressView(ExecutionProgress progress) {
    if (progress != null) {
      this.expectedRecords = progress.getExpectedRecords();
      this.processedRecords = progress.getProcessedRecords();
      this.ignoredRecords = progress.getIgnoredRecords();
      this.deletedRecords = progress.getDeletedRecords();
      this.errors = progress.getErrors();
      this.progressPercentage = progress.getProgressPercentage();
    }
  }

  public int getExpectedRecords() {
    return expectedRecords;
  }

  public int getProcessedRecords() {
    return processedRecords;
  }

  public int getIgnoredRecords() {
    return ignoredRecords;
  }

  public int getDeletedRecords() {
    return deletedRecords;
  }

  public int getErrors() {
    return errors;
  }

  public int getProgressPercentage() {
    return progressPercentage;
  }
}
