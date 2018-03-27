package eu.europeana.metis.core.workflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.europeana.metis.CommonStringValues;
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
 * Class to represent a scheduled workflow.
 * The {@link ScheduleFrequence} {@link #scheduleFrequence} will be used in conjunction with the {@link #pointerDate} to determine when a scheduled execution is ready to be ran.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-09-25
 */
@Entity
@Indexes({@Index(fields = {@Field("datasetId"), @Field("workflowOwner")})})
public class ScheduledWorkflow implements HasMongoObjectId {

  @Id
  @JsonSerialize(using = ObjectIdSerializer.class)
  private ObjectId id;
  private int datasetId;
  @Indexed
  @JsonFormat(pattern = CommonStringValues.DATE_FORMAT_FOR_SCHEDULING)
  private Date pointerDate;
  @Indexed
  private String workflowOwner;
  private ScheduleFrequence scheduleFrequence;
  private int workflowPriority;

  public ScheduledWorkflow() {
    //Required for json serialization
  }

  /**
   * Constructor for creating a scheduled workflow
   *
   * @param pointerDate the {@link Date} that will be used as a pointer Date
   * @param datasetId identifier of the dataset for the scheduled workflow
   * @param workflowOwner the owner of the workflow
   * @param scheduleFrequence the {@link ScheduleFrequence} for the workflow
   * @param workflowPriority the priority of the workflow when it is run
   */
  public ScheduledWorkflow(Date pointerDate, int datasetId, String workflowOwner, ScheduleFrequence scheduleFrequence, int workflowPriority) {
    this.pointerDate = pointerDate == null?null:new Date(pointerDate.getTime());
    this.datasetId = datasetId;
    this.workflowOwner = workflowOwner;
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

  public int getDatasetId() {
    return datasetId;
  }

  public void setDatasetId(int datasetId) {
    this.datasetId = datasetId;
  }

  public String getWorkflowOwner() {
    return workflowOwner;
  }

  public void setWorkflowOwner(String workflowOwner) {
    this.workflowOwner = workflowOwner;
  }

  public Date getPointerDate() {
    return pointerDate == null?null:new Date(pointerDate.getTime());
  }

  public void setPointerDate(Date pointerDate) {
    this.pointerDate = pointerDate == null?null:new Date(pointerDate.getTime());
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
