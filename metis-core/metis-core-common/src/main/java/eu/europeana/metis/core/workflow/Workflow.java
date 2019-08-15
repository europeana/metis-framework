package eu.europeana.metis.core.workflow;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.json.ObjectIdSerializer;
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

/**
 * Workflow model class.
 *
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
  private List<AbstractExecutablePluginMetadata> metisPluginsMetadata = new ArrayList<>();

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

  public List<AbstractExecutablePluginMetadata> getMetisPluginsMetadata() {
    return metisPluginsMetadata;
  }

  public void setMetisPluginsMetadata(
      List<AbstractExecutablePluginMetadata> metisPluginsMetadata) {
    this.metisPluginsMetadata = metisPluginsMetadata;
  }

  /**
   * Get the {@link AbstractExecutablePluginMetadata} class based on the {@link PluginType} if that
   * exists in the {@link Workflow#metisPluginsMetadata}.
   *
   * @param pluginType the {@link PluginType} to search for
   * @return {@link AbstractExecutablePluginMetadata} corresponding to the concrete class
   */
  // TODO JOCHEN no longer needed.
  public AbstractExecutablePluginMetadata getPluginMetadata(ExecutablePluginType pluginType) {
    for (AbstractExecutablePluginMetadata metisPluginMetadata : metisPluginsMetadata) {
      if (metisPluginMetadata.getExecutablePluginType() == pluginType) {
        return metisPluginMetadata;
      }
    }
    return null;
  }
}
