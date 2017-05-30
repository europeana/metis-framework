package eu.europeana.metis.core.workflow;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.europeana.metis.core.common.HarvestingMetadata;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.organization.ObjectIdSerializer;
import eu.europeana.metis.core.workflow.plugins.VoidDereferencePlugin;
import eu.europeana.metis.core.workflow.plugins.VoidHTTPHarvestPlugin;
import eu.europeana.metis.core.workflow.plugins.VoidMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.VoidOaipmhHarvestPlugin;
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
public class UserWorkflowExecution {

  @Id
  @JsonSerialize(using = ObjectIdSerializer.class)
  private ObjectId id;
  boolean harvest;
  @Indexed
  String owner;
  @Indexed
  String workflowName;
  @Indexed
  WorkflowStatus workflowStatus;
  @Indexed
  String datasetName;

  @Indexed
  private Date startedDate;
  @Indexed
  private Date finishedDate;
  @Indexed
  private Date updatedDate;

  //Plugins
  @Embedded
  VoidHTTPHarvestPlugin voidHTTPHarvestPlugin;
  @Embedded
  VoidOaipmhHarvestPlugin voidOaipmhHarvestPlugin;
  @Embedded
  VoidMetisPlugin voidMetisPlugin;
  @Embedded
  VoidDereferencePlugin voidDereferencePlugin;

  public UserWorkflowExecution() {
  }

  public UserWorkflowExecution(Dataset dataset, UserWorkflow userWorkflow) {
    HarvestingMetadata harvestingMetadata = dataset.getHarvestingMetadata();
    switch (harvestingMetadata.getHarvestType()) {
      case UNSPECIFIED:
        break;
      case FTP:
        break;
      case HTTP:
        this.voidHTTPHarvestPlugin = new VoidHTTPHarvestPlugin();
        this.voidHTTPHarvestPlugin.setId(new ObjectId());
        break;
      case OAIPMH:
        this.voidOaipmhHarvestPlugin = new VoidOaipmhHarvestPlugin(
            harvestingMetadata.getMetadataSchema());
        this.voidOaipmhHarvestPlugin.setId(new ObjectId());
        break;
      case FOLDER:
        break;
    }

    this.owner = userWorkflow.getOwner();
    this.workflowName = userWorkflow.getWorkflowName();
    this.datasetName = dataset.getDatasetName();
    this.voidMetisPlugin = new VoidMetisPlugin(userWorkflow.getVoidMetisPluginInfo());
    this.voidMetisPlugin.setId(new ObjectId());
    this.voidDereferencePlugin = new VoidDereferencePlugin(
        userWorkflow.getVoidDereferencePluginInfo());
    this.voidDereferencePlugin.setId(new ObjectId());
    this.workflowStatus = WorkflowStatus.INQUEUE;
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
}
