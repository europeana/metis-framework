package eu.europeana.metis.core.workflow.plugins;

import java.util.Date;

/**
 * This interface represents plugin metadata
 */
public interface MetisPluginMetadata {

  PluginType getPluginType();

  String getRevisionNamePreviousPlugin();

  void setRevisionNamePreviousPlugin(String revisionNamePreviousPlugin);

  Date getRevisionTimestampPreviousPlugin();

  void setRevisionTimestampPreviousPlugin(Date revisionTimestampPreviousPlugin);

  /**
   * For the current plugin, setup the source/previous revision information.
   *
   * @param predecessor the predecessor plugin that the current plugin is based on. Is not null.
   */
  void setPreviousRevisionInformation(ExecutablePlugin<?> predecessor);

}
