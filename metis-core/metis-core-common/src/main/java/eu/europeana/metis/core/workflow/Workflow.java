package eu.europeana.metis.core.workflow;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePluginMetadata;
import eu.europeana.metis.mongo.utils.ObjectIdSerializer;
import eu.europeana.metis.mongo.model.HasMongoObjectId;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;

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
}
