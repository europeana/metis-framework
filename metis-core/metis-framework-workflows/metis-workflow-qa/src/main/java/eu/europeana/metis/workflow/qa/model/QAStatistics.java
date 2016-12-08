package eu.europeana.metis.workflow.qa.model;

import eu.europeana.metis.framework.workflow.CloudStatistics;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import java.util.List;

/**
 * Created by ymamakis on 11/22/16.
 */
@Entity
public class QAStatistics implements CloudStatistics {
    @Id
    private ObjectId id;


    @Indexed
    private String executionId;

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

    private String sessionId;

    private List<String> failedRecords;

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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setFailedRecords(List<String> records){
        this.failedRecords = records;
    }

    @Override
    public List<String> getFailedRecords() {
        return failedRecords;
    }


    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }
}
