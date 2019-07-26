package eu.europeana.metis.core.workflow.plugins;

/**
 * This interface represents plugin metadata that can be executed by Metis.
 */
public interface ExecutablePluginMetadata extends MetisPluginMetadata {

  ExecutablePluginType getExecutablePluginType();

  boolean isEnabled();

  void setEnabled(boolean enabled);

  /**
   * For the current plugin, setup the source/previous revision information.
   * <p>
   * The source revision information that this plugin will be based on, is coming from the {@code
   * previousPlugin} plugin metadata. The {@code previousPlugin} can be a RevisionLess plugin, in
   * which case the revision information for the current plugin will take the source revision
   * information that the {@code previousPlugin} was based on.
   * </p>
   *
   * @param previousPlugin the source/previous plugin that is used to base the current plugin on
   */
  void setPreviousRevisionInformation(ExecutablePlugin<?> previousPlugin);

}
