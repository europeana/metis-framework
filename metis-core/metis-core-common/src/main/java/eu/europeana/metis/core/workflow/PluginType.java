package eu.europeana.metis.core.workflow;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-24
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum PluginType {
  HTTP_HARVEST("HTTP_HARVEST", 1, 1),
  OAIPMH_HARVEST("OAIPMH_HARVEST", 1, 1),
  DEREFERENCE("DEREFERENCE", 2, 1),
  VOID("VOID", 2, 1),
  QA("QA", 3, 1),
  NULL("NULL", -1, -1);

  private String pluginName;
  private int group;
  private int order;

  PluginType(String pluginName, int group, int order) {
    this.pluginName = pluginName;
    this.group = group;
    this.order = order;
  }

  public String getPluginName() {
    return pluginName;
  }

  public void setPluginName(String pluginName) {
    this.pluginName = pluginName;
  }

  public int getGroup() {
    return group;
  }

  public void setGroup(int group) {
    this.group = group;
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  @JsonCreator
  public static PluginType pluginConstructor(@JsonProperty("pluginName") String pluginName, @JsonProperty("group")int group, @JsonProperty("order")int order){
    for (PluginType pluginType:PluginType.values()) {
      if(pluginType.name().equalsIgnoreCase(pluginName)){
        return pluginType;
      }
    }
    return NULL;
  }

  public static PluginType getPluginTypeFromEnumName(String pluginName){
    for (PluginType pluginType:PluginType.values()) {
      if(pluginType.name().equalsIgnoreCase(pluginName)){
        return pluginType;
      }
    }
    return NULL;
  }
}
