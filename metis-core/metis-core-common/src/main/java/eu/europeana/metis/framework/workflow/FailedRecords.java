package eu.europeana.metis.framework.workflow;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import java.util.List;

/**
 * A failed records model
 * Created by ymamakis on 11/17/16.
 */
@Entity
public class FailedRecords {

    /**
     * The id of the object
     */
    @Id
    private ObjectId id;

    /**
     * THe execution id corresponding to this list of failed records
     */
    @Indexed
    private String executionId;

    /**
     * The list of failed records
     */
    private List<String> records;

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

    public List<String> getRecords() {
        return records;
    }

    public void setRecords(List<String> records) {
        this.records = records;
    }
}
