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
import java.util.Set;
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
  public AbstractExecutablePluginMetadata getPluginMetadata(ExecutablePluginType pluginType) {
    for (AbstractExecutablePluginMetadata metisPluginMetadata : metisPluginsMetadata) {
      if (metisPluginMetadata.getExecutablePluginType() == pluginType) {
        return metisPluginMetadata;
      }
    }
    return null;
  }

  public ExecutablePluginType getFirstEnabledPluginBeforeLink() {
    ExecutablePluginType previousExecutablePluginType = null;
    for (AbstractExecutablePluginMetadata metisPluginMetadata : metisPluginsMetadata) {
      if (metisPluginMetadata.isEnabled()
          && metisPluginMetadata.getExecutablePluginType() == ExecutablePluginType.LINK_CHECKING) {
        return previousExecutablePluginType;
      }
      previousExecutablePluginType = metisPluginMetadata.getExecutablePluginType();
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
   * <li>The first occurrence of the given plugin type comes <b>after</b> the earliest plugin of
   * one of the possible earlier type.</li>
   * </ol>
   * </p>
   * <p>
   * Note that this method does not assume that each plugin type only occurs once.
   * </p>
   *
   * @param pluginType The plugin type that we are testing for. Cannot be null.
   * @param possibleEarlierPluginTypes The possible plugin types that has to occur before the other
   * given type. Can be null or empty (in which case the result will be false).
   * @return Whether the workflow contains plugins of the two given types in the given order.
   */
  public boolean pluginTypeOccursOnlyAfter(ExecutablePluginType pluginType,
      Set<ExecutablePluginType> possibleEarlierPluginTypes) {
    if (pluginType == null) {
      throw new IllegalArgumentException();
    }
    if (possibleEarlierPluginTypes == null || possibleEarlierPluginTypes.isEmpty()) {
      return false;
    }
    boolean earlierPluginTypeFound = false;
    for (AbstractExecutablePluginMetadata plugin : metisPluginsMetadata) {
      if (plugin.getExecutablePluginType() == pluginType) {
        return earlierPluginTypeFound;
      }
      earlierPluginTypeFound = earlierPluginTypeFound || possibleEarlierPluginTypes
          .contains(plugin.getExecutablePluginType());
    }
    return false;
  }
}
