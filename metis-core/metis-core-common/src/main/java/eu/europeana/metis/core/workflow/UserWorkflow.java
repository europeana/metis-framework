package eu.europeana.metis.core.workflow;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.europeana.metis.core.organization.ObjectIdSerializer;
import eu.europeana.metis.core.workflow.plugins.VoidDereferencePluginMetadata;
import eu.europeana.metis.core.workflow.plugins.VoidMetisPluginMetadata;
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
@JsonPropertyOrder({"id", "onwer", "workflowName", "harvest", "voidDereferencePluginMetadata", "voidMetisPluginMetadata"})
public class UserWorkflow {

  @Id
  @JsonSerialize(using = ObjectIdSerializer.class)
  @ApiModelProperty(position = 1)
  private ObjectId id;
  @Indexed
  @ApiModelProperty(position = 2)
  String owner;
  @Indexed
  @ApiModelProperty(position = 3)
  String workflowName;
  @ApiModelProperty(position = 4)
  boolean harvestPlugin;
  @ApiModelProperty(position = 5)
  boolean transformPlugin;

  //Plugins information
  @ApiModelProperty(position = 6)
  VoidDereferencePluginMetadata voidDereferencePluginMetadata;
  @ApiModelProperty(position = 7)
  VoidMetisPluginMetadata voidMetisPluginMetadata;

  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }

  public boolean isHarvestPlugin() {
    return harvestPlugin;
  }

  public void setHarvestPlugin(boolean harvestPlugin) {
    this.harvestPlugin = harvestPlugin;
  }

  public boolean isTransformPlugin() {
    return transformPlugin;
  }

  public void setTransformPlugin(boolean transformPlugin) {
    this.transformPlugin = transformPlugin;
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

  public VoidMetisPluginMetadata getVoidMetisPluginMetadata() {
    return voidMetisPluginMetadata;
  }

  public void setVoidMetisPluginMetadata(
      VoidMetisPluginMetadata voidMetisPluginMetadata) {
    this.voidMetisPluginMetadata = voidMetisPluginMetadata;
  }

  public VoidDereferencePluginMetadata getVoidDereferencePluginMetadata() {
    return voidDereferencePluginMetadata;
  }

  public void setVoidDereferencePluginMetadata(
      VoidDereferencePluginMetadata voidDereferencePluginMetadata) {
    this.voidDereferencePluginMetadata = voidDereferencePluginMetadata;
  }
}
