package eu.europeana.metis.core.workflow;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.europeana.metis.core.organization.ObjectIdSerializer;
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
@Indexes(@Index(fields = { @Field("owner"), @Field("workflowName")}))
public class UserWorkflowExecution {

  @Id
  @JsonSerialize(using = ObjectIdSerializer.class)
  private ObjectId id;
  boolean harvest;
  boolean incremental;
  @Indexed
  String owner;
  @Indexed
  String workflowName;
  @Indexed
  WorkflowStatus workflowStatus;
  @Indexed
  String datasetName;

  //Plugins
  VoidMetisPlugin voidMetisPlugin;
  VoidHTTPHarvestPlugin voidHTTPHarvestPlugin;
  VoidOaipmhHarvestPlugin voidOaipmhHarvestPlugin;
  VoidDereferencePlugin voidDereferencePlugin;

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

  public boolean isIncremental() {
    return incremental;
  }

  public void setIncremental(boolean incremental) {
    this.incremental = incremental;
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
