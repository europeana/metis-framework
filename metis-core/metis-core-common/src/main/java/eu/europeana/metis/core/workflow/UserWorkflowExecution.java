package eu.europeana.metis.core.workflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.HarvestingMetadata;
import eu.europeana.metis.core.dataset.HttpHarvestingMetadata;
import eu.europeana.metis.core.dataset.OaipmhHarvestingMetadata;
import eu.europeana.metis.core.organization.ObjectIdSerializer;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.core.workflow.plugins.VoidDereferencePlugin;
import eu.europeana.metis.core.workflow.plugins.VoidHTTPHarvestPlugin;
import eu.europeana.metis.core.workflow.plugins.VoidHTTPHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.VoidMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.VoidOaipmhHarvestPlugin;
import eu.europeana.metis.core.workflow.plugins.VoidOaipmhHarvestPluginMetadata;
import java.util.ArrayList;
import java.util.Comparator;
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
@JsonPropertyOrder({"id", "workflowOnwer", "workflowName", "workflowStatus", "datasetName",
    "workflowPriority", "harvest", "createdDate", "startedDate", "updatedDate", "finishedDate",
    "voidHTTPHarvestPlugin", "voidOaipmhHarvestPlugin", "voidDereferencePlugin", "voidMetisPlugin"})
public class UserWorkflowExecution implements HasMongoObjectId {

  @Id
  @JsonSerialize(using = ObjectIdSerializer.class)
  private ObjectId id;
  @Indexed
  private String workflowOwner;
  @Indexed
  private String workflowName;
  @Indexed
  private WorkflowStatus workflowStatus;
  @Indexed
  private String datasetName;
  private int workflowPriority;
  private boolean harvest;
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

  public UserWorkflowExecution(Dataset dataset, UserWorkflow userWorkflow, int workflowPriority) {
    this.workflowOwner = userWorkflow.getWorkflowOwner();
    this.workflowName = userWorkflow.getWorkflowName();
    this.datasetName = dataset.getDatasetName();
    this.workflowPriority = workflowPriority;

    addHarvestingPlugin(dataset, userWorkflow);

    // TODO: 31-5-17 Add transformation plugin retrieved probably from the dataset, and generated from the mapping tool.

    addProcessPlugins(userWorkflow);
  }

  private void addHarvestingPlugin(Dataset dataset, UserWorkflow userWorkflow) {
    HarvestingMetadata harvestingMetadata = dataset.getHarvestingMetadata();
    if (userWorkflow.isHarvestPlugin()) {
      switch (harvestingMetadata.getHarvestType()) {
        case FTP_HARVEST:
          break;
        case HTTP_HARVEST:
          VoidHTTPHarvestPlugin voidHTTPHarvestPlugin = new VoidHTTPHarvestPlugin(
              new VoidHTTPHarvestPluginMetadata((HttpHarvestingMetadata) harvestingMetadata, null));
          voidHTTPHarvestPlugin
              .setId(
                  new ObjectId().toString() + "-" + voidHTTPHarvestPlugin.getPluginType().name());
          metisPlugins.add(voidHTTPHarvestPlugin);
          break;
        case OAIPMH_HARVEST:
          VoidOaipmhHarvestPlugin voidOaipmhHarvestPlugin = new VoidOaipmhHarvestPlugin(
              new VoidOaipmhHarvestPluginMetadata(
                  (OaipmhHarvestingMetadata) harvestingMetadata, null));
          voidOaipmhHarvestPlugin
              .setId(
                  new ObjectId().toString() + "-" + voidOaipmhHarvestPlugin.getPluginType().name());
          metisPlugins.add(voidOaipmhHarvestPlugin);
          break;
        case FOLDER_HARVEST:
          break;
        case NULL:
          break;
      }
    }
  }

  private void addProcessPlugins(UserWorkflow userWorkflow) {
    AbstractMetisPluginMetadata voidDereferencePluginMetadata = userWorkflow
        .getVoidDereferencePluginMetadata();
    if (voidDereferencePluginMetadata != null) {
      VoidDereferencePlugin voidDereferencePlugin = new VoidDereferencePlugin(voidDereferencePluginMetadata);
      voidDereferencePlugin
          .setId(new ObjectId().toString() + "-" + voidDereferencePlugin.getPluginType().name());
      metisPlugins.add(voidDereferencePlugin);
    }
    AbstractMetisPluginMetadata voidMetisPluginMetadata = userWorkflow
        .getVoidMetisPluginMetadata();
    if (voidMetisPluginMetadata != null) {
      VoidMetisPlugin voidMetisPlugin = new VoidMetisPlugin(voidMetisPluginMetadata);
      voidMetisPlugin
          .setId(new ObjectId().toString() + "-" + voidMetisPlugin.getPluginType().name());
      metisPlugins.add(voidMetisPlugin);
    }
  }

  public void setAllRunningAndInqueuePluginsToCancelled()
  {
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

  public boolean isHarvest() {
    return harvest;
  }

  public void setHarvest(boolean harvest) {
    this.harvest = harvest;
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

  public String getDatasetName() {
    return datasetName;
  }

  public void setDatasetName(String datasetName) {
    this.datasetName = datasetName;
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
    result = prime * result + ((datasetName == null) ? 0 : datasetName.hashCode());
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
    UserWorkflowExecution that = (UserWorkflowExecution) obj;
    return (id == that.getId() && datasetName.equals(that.datasetName) && workflowOwner
        .equals(that.workflowOwner)
        && workflowName.equals(that.workflowName));
  }

  public static class UserWorkflowExecutionPriorityComparator implements
      Comparator<UserWorkflowExecution> {

    @Override
    public int compare(UserWorkflowExecution o1, UserWorkflowExecution o2) {
      if (o1.workflowPriority > o2.workflowPriority) {
        return -1;
      }
      if (o1.workflowPriority == o2.workflowPriority) {
        return o1.getCreatedDate().compareTo(o2.getCreatedDate());
      } else {
        return 1;
      }
    }
  }
}


