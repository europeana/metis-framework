package eu.europeana.metis.core.workflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.json.ObjectIdSerializer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Indexes;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-26
 */
@Entity
@Indexes({@Index(fields = {@Field("workflowOwner"), @Field("workflowName")})})
public class WorkflowExecution implements HasMongoObjectId {

  @Id
  @JsonSerialize(using = ObjectIdSerializer.class)
  private ObjectId id;
  private int datasetId;
  @Indexed
  private String workflowOwner;
  @Indexed
  private String workflowName;
  @Indexed
  private WorkflowStatus workflowStatus;
  @Indexed
  private String ecloudDatasetId;
  private int workflowPriority;
  private boolean cancelling;

  @Indexed
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date createdDate;
  @Indexed
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date startedDate;
  @Indexed
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date updatedDate;
  @Indexed
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date finishedDate;

  private List<AbstractMetisPlugin> metisPlugins = new ArrayList<>();

  public WorkflowExecution() {
    //Required for json serialization
  }

  public WorkflowExecution(Dataset dataset, Workflow workflow,
      List<AbstractMetisPlugin> metisPlugins,
      int workflowPriority) {
    this.workflowOwner = workflow.getWorkflowOwner();
    this.workflowName = workflow.getWorkflowName();
    this.datasetId = dataset.getDatasetId();
    this.ecloudDatasetId = dataset.getEcloudDatasetId();
    this.workflowPriority = workflowPriority;
    this.metisPlugins = metisPlugins;
  }

  public void setAllRunningAndInqueuePluginsToCancelled() {
    this.setWorkflowStatus(WorkflowStatus.CANCELLED);
    for (AbstractMetisPlugin metisPlugin :
        this.getMetisPlugins()) {
      if (metisPlugin.getPluginStatus() == PluginStatus.INQUEUE
          || metisPlugin.getPluginStatus() == PluginStatus.RUNNING) {
        metisPlugin.setPluginStatus(PluginStatus.CANCELLED);
      }
    }
    this.setCancelling(false);
  }

  @Override
  public ObjectId getId() {
    return id;
  }

  @Override
  public void setId(ObjectId id) {
    this.id = id;
  }

  public boolean isCancelling() {
    return cancelling;
  }

  public void setCancelling(boolean cancelling) {
    this.cancelling = cancelling;
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

  public WorkflowStatus getWorkflowStatus() {
    return workflowStatus;
  }

  public void setWorkflowStatus(WorkflowStatus workflowStatus) {
    this.workflowStatus = workflowStatus;
  }

  public int getDatasetId() {
    return datasetId;
  }

  public void setDatasetId(int datasetId) {
    this.datasetId = datasetId;
  }

  public String getEcloudDatasetId() {
    return ecloudDatasetId;
  }

  public void setEcloudDatasetId(String ecloudDatasetId) {
    this.ecloudDatasetId = ecloudDatasetId;
  }

  public int getWorkflowPriority() {
    return workflowPriority;
  }

  public void setWorkflowPriority(int workflowPriority) {
    this.workflowPriority = workflowPriority;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public Date getStartedDate() {
    return startedDate;
  }

  public void setStartedDate(Date startedDate) {
    this.startedDate = startedDate;
  }

  public Date getFinishedDate() {
    return finishedDate;
  }

  public void setFinishedDate(Date finishedDate) {
    this.finishedDate = finishedDate;
  }

  public Date getUpdatedDate() {
    return updatedDate;
  }

  public void setUpdatedDate(Date updatedDate) {
    this.updatedDate = updatedDate;
  }

  public List<AbstractMetisPlugin> getMetisPlugins() {
    return metisPlugins;
  }

  public void setMetisPlugins(
      List<AbstractMetisPlugin> metisPlugins) {
    this.metisPlugins = metisPlugins;
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + Integer.hashCode(datasetId);
    result = prime * result + ((workflowOwner == null) ? 0 : workflowOwner.hashCode());
    result = prime * result + ((workflowName == null) ? 0 : workflowName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null || obj.getClass() != this.getClass()) {
      return false;
    }
    WorkflowExecution that = (WorkflowExecution) obj;
    return (id == that.getId() && datasetId == that.datasetId && workflowOwner
        .equals(that.workflowOwner)
        && workflowName.equals(that.workflowName));
  }
}


