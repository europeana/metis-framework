package eu.europeana.metis.framework.workflow;

/**
 * The statistics for a given execution
 * Created by ymamakis on 11/9/16.
 */
public class ExecutionStatistics {

    /**
     * number of deleted records in this execution
     */
    private Long deleted;
    /**
     * number of processed records in this execution
     */
    private Long processed;

    /**
     * number of updated records in this execution
     */
    private Long updated;
    /**
     * number of created records in this execution
     */
    private Long created;
    /**
     * number of failed records in this execution
     */
    private Long failed;

    private String status;

    public Long getDeleted() {
        return deleted;
    }

    public void setDeleted(Long deleted) {
        this.deleted = deleted;
    }

    public Long getProcessed() {
        return processed;
    }

    public void setProcessed(Long processed) {
        this.processed = processed;
    }

    public Long getUpdated() {
        return updated;
    }

    public void setUpdated(Long updated) {
        this.updated = updated;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public Long getFailed() {
        return failed;
    }

    public void setFailed(Long failed) {
        this.failed = failed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
