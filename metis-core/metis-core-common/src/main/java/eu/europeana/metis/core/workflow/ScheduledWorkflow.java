package eu.europeana.metis.core.workflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.europeana.metis.json.ObjectIdSerializer;
import java.util.Date;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Indexes;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-09-25
 */
@Entity
@Indexes({@Index(fields = {@Field("datasetId"), @Field("workflowOwner"), @Field("workflowName")})})
public class ScheduledWorkflow implements HasMongoObjectId {
  @Id
  @JsonSerialize(using = ObjectIdSerializer.class)
  private ObjectId id;
  @Indexed
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  private Date pointerDate;
  @Indexed
  private String datasetId;
  @Indexed
  private String workflowOwner;
  @Indexed
  private String workflowName;
  private ScheduleFrequence scheduleFrequence;
  private int workflowPriority;

  public ScheduledWorkflow() {
  }

  public ScheduledWorkflow(Date pointerDate, String datasetId, String workflowOwner,
      String workflowName, ScheduleFrequence scheduleFrequence, int workflowPriority) {
    this.pointerDate = pointerDate;
    this.datasetId = datasetId;
    this.workflowOwner = workflowOwner;
    this.workflowName = workflowName;
    this.scheduleFrequence = scheduleFrequence;
    this.workflowPriority = workflowPriority;
  }

  @Override
  public ObjectId getId() {
    return id;
  }

  @Override
  public void setId(ObjectId id) {
    this.id = id;
  }

  public String getDatasetId() {
    return datasetId;
  }

  public void setDatasetId(String datasetId) {
    this.datasetId = datasetId;
  }

  public String getWorkflowOwner() {
    return workflowOwner;
  }

  public void setWorkflowOwner(String workflowOwner) {
    this.workflowOwner = workflowOwner;
  }

  public String getWorkflowName() {
    return workflowName;
  }

  public void setWorkflowName(String workflowName) {
    this.workflowName = workflowName;
  }

  public Date getPointerDate() {
    return pointerDate;
  }

  public void setPointerDate(Date pointerDate) {
    this.pointerDate = pointerDate;
  }

  public ScheduleFrequence getScheduleFrequence() {
    return scheduleFrequence;
  }

  public void setScheduleFrequence(ScheduleFrequence scheduleFrequence) {
    this.scheduleFrequence = scheduleFrequence;
  }

  public int getWorkflowPriority() {
    return workflowPriority;
  }

  public void setWorkflowPriority(int workflowPriority) {
    this.workflowPriority = workflowPriority;
  }
}
