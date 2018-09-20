package eu.europeana.metis.core.workflow;

import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexes;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.json.ObjectIdSerializer;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-29
 */
@Entity
@Indexes(@Index(fields = {@Field("datasetId")}, options = @IndexOptions(unique = true)))
@JsonPropertyOrder({"id", "datasetId", "metisPluginMetadata"})
public class Workflow implements HasMongoObjectId {

  @Id
  @JsonSerialize(using = ObjectIdSerializer.class)
  private ObjectId id;
  private String datasetId;

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

  public String getDatasetId() {
    return datasetId;
  }

  public void setDatasetId(String datasetId) {
    this.datasetId = datasetId;
  }

  public List<AbstractMetisPluginMetadata> getMetisPluginsMetadata() {
    return metisPluginsMetadata;
  }

  public void setMetisPluginsMetadata(
      List<AbstractMetisPluginMetadata> metisPluginsMetadata) {
    this.metisPluginsMetadata = metisPluginsMetadata;
  }

  /**
   * Get the {@link AbstractMetisPluginMetadata} class based on the {@link PluginType} if that
   * exists in the {@link Workflow#metisPluginsMetadata}.
   *
   * @param pluginType the {@link PluginType} to search for
   * @return {@link AbstractMetisPluginMetadata} corresponding to the concrete class
   */
  public AbstractMetisPluginMetadata getPluginMetadata(PluginType pluginType) {
    for (AbstractMetisPluginMetadata metisPluginMetadata : metisPluginsMetadata) {
      if (metisPluginMetadata.getPluginType() == pluginType) {
        return metisPluginMetadata;
      }
    }
    return null;
  }

  public boolean isPluginTypeAfterBasedPluginType(PluginType basedPluginType,
      PluginType pluginType) {
    int basedPluginTypeIndex = -1;
    int pluginTypeIndex = -1;

    for (int i = 0; i < metisPluginsMetadata.size(); i++) {
      AbstractMetisPluginMetadata abstractMetisPluginMetadata = metisPluginsMetadata.get(i);
      if (abstractMetisPluginMetadata.getPluginType() == basedPluginType) {
        basedPluginTypeIndex = i;
      } else if (abstractMetisPluginMetadata.getPluginType() == pluginType) {
        pluginTypeIndex = i;
      }
    }
    //Do both plugins exist and is the pluginType after the basedPluginType declared
    return pluginTypeIndex > -1 && basedPluginTypeIndex > -1 && pluginTypeIndex > basedPluginTypeIndex;
  }
}
