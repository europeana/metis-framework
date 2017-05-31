package eu.europeana.metis.core.workflow;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.europeana.metis.core.common.HarvestingMetadata;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.organization.ObjectIdSerializer;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.core.workflow.plugins.VoidDereferencePlugin;
import eu.europeana.metis.core.workflow.plugins.VoidHTTPHarvestPlugin;
import eu.europeana.metis.core.workflow.plugins.VoidMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.VoidOaipmhHarvestPlugin;
import java.util.Comparator;
import java.util.Date;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
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
@Indexes(@Index(fields = {@Field("owner"), @Field("workflowName")}))
@JsonPropertyOrder({"id", "onwer", "workflowName", "workflowStatus", "datasetName",
    "workflowPriority", "harvest", "createdDate", "startedDate", "updatedDate", "finishedDate",
    "voidHTTPHarvestPlugin", "voidOaipmhHarvestPlugin", "voidDereferencePlugin", "voidMetisPlugin"})
public class UserWorkflowExecution {

  @Id
  @JsonSerialize(using = ObjectIdSerializer.class)
  private ObjectId id;
  @Indexed
  String owner;
  @Indexed
  String workflowName;
  @Indexed
  WorkflowStatus workflowStatus;
  @Indexed
  String datasetName;
  int workflowPriority;
  boolean harvest;

  @Indexed
  private Date createdDate;
  @Indexed
  private Date startedDate;
  @Indexed
  private Date updatedDate;
  @Indexed
  private Date finishedDate;

  //Plugins
  @Embedded
  VoidHTTPHarvestPlugin voidHTTPHarvestPlugin;
  @Embedded
  VoidOaipmhHarvestPlugin voidOaipmhHarvestPlugin;
  @Embedded
  VoidDereferencePlugin voidDereferencePlugin;
  @Embedded
  VoidMetisPlugin voidMetisPlugin;

  public UserWorkflowExecution() {
  }

  public UserWorkflowExecution(Dataset dataset, UserWorkflow userWorkflow, int workflowPriority) {
    HarvestingMetadata harvestingMetadata = dataset.getHarvestingMetadata();
    switch (harvestingMetadata.getHarvestType()) {
      case UNSPECIFIED:
        break;
      case FTP:
        break;
      case HTTP:
        this.voidHTTPHarvestPlugin = new VoidHTTPHarvestPlugin();
        this.voidHTTPHarvestPlugin
            .setId(new ObjectId().toString() + "-" + PluginType.HTTP_HARVEST.name());
        break;
      case OAIPMH:
        this.voidOaipmhHarvestPlugin = new VoidOaipmhHarvestPlugin(
            harvestingMetadata.getMetadataSchema());
        this.voidOaipmhHarvestPlugin
            .setId(new ObjectId().toString() + "-" + PluginType.OAIPMH_HARVEST.name());
        break;
      case FOLDER:
        break;
    }

    this.owner = userWorkflow.getOwner();
    this.workflowName = userWorkflow.getWorkflowName();
    this.datasetName = dataset.getDatasetName();
    this.workflowPriority = workflowPriority;
    this.voidMetisPlugin = new VoidMetisPlugin(userWorkflow.getVoidMetisPluginInfo());
    this.voidMetisPlugin.setId(new ObjectId().toString() + "-" + PluginType.VOID.name());
    this.voidDereferencePlugin = new VoidDereferencePlugin(
        userWorkflow.getVoidDereferencePluginInfo());
    this.voidDereferencePlugin
        .setId(new ObjectId().toString() + "-" + PluginType.DEREFERENCE.name());
  }

  public UserWorkflowExecution(Dataset dataset, UserWorkflow userWorkflow) {
    this(dataset, userWorkflow, 0);
  }

  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }

  public boolean isHarvest() {
    return harvest;
  }

  public void setHarvest(boolean harvest) {
    this.harvest = harvest;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
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

  public VoidMetisPlugin getVoidMetisPlugin() {
    return voidMetisPlugin;
  }

  public void setVoidMetisPlugin(VoidMetisPlugin voidMetisPlugin) {
    this.voidMetisPlugin = voidMetisPlugin;
  }

  public VoidHTTPHarvestPlugin getVoidHTTPHarvestPlugin() {
    return voidHTTPHarvestPlugin;
  }

  public void setVoidHTTPHarvestPlugin(
      VoidHTTPHarvestPlugin voidHTTPHarvestPlugin) {
    this.voidHTTPHarvestPlugin = voidHTTPHarvestPlugin;
  }

  public VoidOaipmhHarvestPlugin getVoidOaipmhHarvestPlugin() {
    return voidOaipmhHarvestPlugin;
  }

  public void setVoidOaipmhHarvestPlugin(
      VoidOaipmhHarvestPlugin voidOaipmhHarvestPlugin) {
    this.voidOaipmhHarvestPlugin = voidOaipmhHarvestPlugin;
  }

  public VoidDereferencePlugin getVoidDereferencePlugin() {
    return voidDereferencePlugin;
  }

  public void setVoidDereferencePlugin(
      VoidDereferencePlugin voidDereferencePlugin) {
    this.voidDereferencePlugin = voidDereferencePlugin;
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


