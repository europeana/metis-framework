package eu.europeana.metis.core.workflow.plugins;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-06-01
 */
public class ExecutionRecordsStatistics {
  private long recordsProcessed;
  private long recordsFailed;
  private long recordsCreated;
  private long recordsUpdated;
  private long recordsDeleted;
  private long progress;

  public long getRecordsProcessed() {
    return recordsProcessed;
  }

  public void setRecordsProcessed(long recordsProcessed) {
    this.recordsProcessed = recordsProcessed;
  }

  public long getRecordsFailed() {
    return recordsFailed;
  }

  public void setRecordsFailed(long recordsFailed) {
    this.recordsFailed = recordsFailed;
  }

  public long getRecordsCreated() {
    return recordsCreated;
  }

  public void setRecordsCreated(long recordsCreated) {
    this.recordsCreated = recordsCreated;
  }

  public long getRecordsUpdated() {
    return recordsUpdated;
  }

  public void setRecordsUpdated(long recordsUpdated) {
    this.recordsUpdated = recordsUpdated;
  }

  public long getRecordsDeleted() {
    return recordsDeleted;
  }

  public void setRecordsDeleted(long recordsDeleted) {
    this.recordsDeleted = recordsDeleted;
  }

  public long getProgress() {
    return progress;
  }

  public void setProgress(long progress) {
    this.progress = progress;
  }
}
