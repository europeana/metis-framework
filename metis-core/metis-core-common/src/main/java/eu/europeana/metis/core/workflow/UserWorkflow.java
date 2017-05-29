package eu.europeana.metis.core.workflow;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.europeana.metis.core.organization.ObjectIdSerializer;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Indexes;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-29
 */
@Entity
@Indexes(@Index(fields = { @Field("owner"), @Field("workflowName")}, options = @IndexOptions(unique = true)))
public class UserWorkflow {

  @Id
  @JsonSerialize(using = ObjectIdSerializer.class)
  @ApiModelProperty(position = 1)
  private ObjectId id;
  @ApiModelProperty(position = 2)
  boolean harvest;
  @ApiModelProperty(position = 3)
  boolean incremental;
  @Indexed
  @ApiModelProperty(position = 4)
  String owner;
  @Indexed
  @ApiModelProperty(position = 5)
  String workflowName;

  //Plugins
  @ApiModelProperty(position = 6)
  VoidMetisPluginInfo voidMetisPluginInfo;
  @ApiModelProperty(position = 7)
  VoidDereferencePluginInfo voidDereferencePluginInfo;

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

  public VoidMetisPluginInfo getVoidMetisPluginInfo() {
    return voidMetisPluginInfo;
  }

  public void setVoidMetisPluginInfo(
      VoidMetisPluginInfo voidMetisPluginInfo) {
    this.voidMetisPluginInfo = voidMetisPluginInfo;
  }

  public VoidDereferencePluginInfo getVoidDereferencePluginInfo() {
    return voidDereferencePluginInfo;
  }

  public void setVoidDereferencePluginInfo(
      VoidDereferencePluginInfo voidDereferencePluginInfo) {
    this.voidDereferencePluginInfo = voidDereferencePluginInfo;
  }
}
