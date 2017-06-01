package eu.europeana.metis.core.workflow;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eu.europeana.metis.core.organization.ObjectIdSerializer;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
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
@Indexes(@Index(fields = {@Field("owner"),
    @Field("workflowName")}, options = @IndexOptions(unique = true)))
@JsonPropertyOrder({"id", "onwer", "workflowName", "harvest", "voidDereferencePluginMetadata",
    "voidMetisPluginMetadata"})
public class UserWorkflow {

  @Id
  @JsonSerialize(using = ObjectIdSerializer.class)
  @ApiModelProperty(position = 1)
  private ObjectId id;
  @Indexed
  @ApiModelProperty(position = 2)
  private String owner;
  @Indexed
  @ApiModelProperty(position = 3)
  private String workflowName;
  @ApiModelProperty(position = 4)
  private boolean harvestPlugin;
  @ApiModelProperty(position = 5)
  private boolean transformPlugin;

  //Plugins information
  @ApiModelProperty(position = 6)
  @JacksonXmlElementWrapper(localName = "metisPluginsMetadatas")
  @JacksonXmlProperty(localName = "metisPluginsMetadata")
  private List<AbstractMetisPluginMetadata> metisPluginsMetadata = new ArrayList<>();

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

  public List<AbstractMetisPluginMetadata> getMetisPluginsMetadata() {
    return metisPluginsMetadata;
  }

  public void setMetisPluginsMetadata(
      List<AbstractMetisPluginMetadata> metisPluginsMetadata) {
    this.metisPluginsMetadata = metisPluginsMetadata;
  }

  public AbstractMetisPluginMetadata getVoidDereferencePluginMetadata() {
    for (AbstractMetisPluginMetadata metisPluginMetadata : metisPluginsMetadata
        ) {
      if (metisPluginMetadata.getPluginType() == PluginType.DEREFERENCE) {
        return metisPluginMetadata;
      }
    }
    return null;
  }


  public AbstractMetisPluginMetadata getVoidMetisPluginMetadata() {
    for (AbstractMetisPluginMetadata metisPluginMetadata : metisPluginsMetadata
        ) {
      if (metisPluginMetadata.getPluginType() == PluginType.VOID) {
        return metisPluginMetadata;
      }
    }
    return null;
  }
}
