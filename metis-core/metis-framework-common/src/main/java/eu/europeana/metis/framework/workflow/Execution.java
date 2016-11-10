package eu.europeana.metis.framework.workflow;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * Created by ymamakis on 11/9/16.
 */
@XmlRootElement
@Entity
public class Execution <T extends AbstractMetisWorkflow>  {
    @Id
    private ObjectId id;

    @Indexed
    private Date startedAt;

    @Indexed
    private Date finishedAt;

    @Indexed
    private Date updatedAt;

    @Embedded
    private T workflow;

    @Indexed
    private String type;

    @Indexed
    private String datasetId;


    @XmlElement
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }
    @XmlElement
    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }
    @XmlElement
    public Date getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Date finishedAt) {
        this.finishedAt = finishedAt;
    }
    @XmlElement
    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    @XmlElement
    public T getWorkflow() {
        return workflow;
    }

    public void setWorkflow(T workflow) {
        this.workflow = workflow;
    }
    @XmlElement
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    @XmlElement
    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }
}
