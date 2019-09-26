package eu.europeana.metis.core.workflow.plugins;

/**
 * This interface represents plugin metadata that can be executed by Metis.
 */
public interface ExecutablePluginMetadata extends MetisPluginMetadata {

  ExecutablePluginType getExecutablePluginType();

  boolean isEnabled();

  void setEnabled(boolean enabled);

}
