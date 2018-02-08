package eu.europeana.metis.core.workflow;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eu.europeana.metis.json.ObjectIdSerializer;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
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
@Indexes(@Index(fields = {@Field("workflowOwner"),
    @Field("workflowName")}, options = @IndexOptions(unique = true)))
@JsonPropertyOrder({"id", "workflowOwner", "workflowName", "harvestPlugin", "transformPlugin",
    "metisPluginMetadata"})
public class Workflow implements HasMongoObjectId {

  @Id
  @JsonSerialize(using = ObjectIdSerializer.class)
  private ObjectId id;
  @Indexed
  private String workflowOwner;
  @Indexed
  private String workflowName;
  private boolean harvestPlugin;

  @JacksonXmlElementWrapper(localName = "metisPluginsMetadatas")
  @JacksonXmlProperty(localName = "metisPluginsMetadata")
  @Embedded
  private List<AbstractMetisPluginMetadata> metisPluginsMetadata = new ArrayList<>();

  @Override
  public ObjectId getId() {
    return id;
  }

  @Override
  public void setId(ObjectId id) {
    this.id = id;
  }

  public boolean isHarvestPlugin() {
    return harvestPlugin;
  }

  public void setHarvestPlugin(boolean harvestPlugin) {
    this.harvestPlugin = harvestPlugin;
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

  public List<AbstractMetisPluginMetadata> getMetisPluginsMetadata() {
    return metisPluginsMetadata;
  }

  public void setMetisPluginsMetadata(
      List<AbstractMetisPluginMetadata> metisPluginsMetadata) {
    this.metisPluginsMetadata = metisPluginsMetadata;
  }

  /**
   * Get the {@link AbstractMetisPluginMetadata} class based on the {@link PluginType} if that exists in the {@link Workflow#metisPluginsMetadata}.
   * @param pluginType the {@link PluginType} to search for
   * @return {@link AbstractMetisPluginMetadata} corresponding to the concrete class
   */
  public AbstractMetisPluginMetadata getPluginMetadata(PluginType pluginType) {
    for (AbstractMetisPluginMetadata metisPluginMetadata : metisPluginsMetadata
        ) {
      if (metisPluginMetadata.getPluginType() == pluginType) {
        return metisPluginMetadata;
      }
    }
    return null;
  }
}
