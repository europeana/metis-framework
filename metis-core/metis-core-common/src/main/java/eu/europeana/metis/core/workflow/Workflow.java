package eu.europeana.metis.core.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

  /**
   * <p>
   * This method tests whether in this workflow all plugins of the given type occur after at least
   * one plugin of one of the given earlier types. So, more formally, it returns true if and only if
   * all the following conditions are met:
   * <ol>
   * <li>The workflow contains at least one plugin of the given plugin type,</li>
   * <li>The workflow contains at least one plugin of one of the given possible earlier types,</li>
   * <li>The first occurrence of the given plugin type comes <b>after</b> the earliest plugin of one
   * of the possible earlier type.</li>
   * </ol>
   * </p>
   * <p>
   * Note that this method does not assume that each plugin type only occurs once.
   * </p>
   * 
   * @param pluginType The plugin type that we are testing for. Cannot be null.
   * @param possibleEarlierPluginTypes The possible plugin types that has to occur before the other
   *        given type. Can be null (in which case the result will be false).
   * @return Whether the workflow contains plugins of the two given types in the given order.
   */
  public boolean pluginTypeOccursOnlyAfter(PluginType pluginType,
      Set<PluginType> possibleEarlierPluginTypes) {
    if (pluginType == null) {
      throw new IllegalArgumentException();
    }
    if (possibleEarlierPluginTypes == null) {
      return false;
    }
    boolean earlierPluginTypeFound = false;
    for (AbstractMetisPluginMetadata plugin : metisPluginsMetadata) {
      if (plugin.getPluginType() == pluginType) {
        return earlierPluginTypeFound;
      }
      earlierPluginTypeFound =
          earlierPluginTypeFound || possibleEarlierPluginTypes.contains(plugin.getPluginType());
    }
    return false;
  }
}
